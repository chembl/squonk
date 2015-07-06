package com.im.lac.jobs.impl;

import com.im.lac.jobs.CancellableJob;
import com.im.lac.service.Environment;
import com.im.lac.jobs.JobStatus;
import com.im.lac.jobs.UpdatableJob;
import com.im.lac.model.DatasetJobDefinition;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.Exchange;
import org.apache.camel.PollingConsumer;
import org.apache.camel.component.jms.JmsQueueEndpoint;
import org.apache.camel.spi.UnitOfWork;

/**
 * Queue based executor for slow jobs that can be split into parts and submitted to a queue for
 * execution.
 * <p>
 * Each queue would have a corresponding consumer that takes from the queue, processes the item and
 * sends the response (if any) to a second queue that is dedicated for the results of this job. The
 * service consuming the request queue can be auto-scaled as needed to increase throughput
 * (auto-scaling on the number of unprocessed messages).</p>
 *
 * <p>
 * Assumptions:
 * <ol>
 * <li>the submitted messages have fields for Job ID, response queue name</li>
 * <li>the submitted message bodies are JSON and the consuming service knows what class to
 * instantiate from that JSON</li>
 * <li>the response bodies are JSON and need no further processing other than being written as a new
 * dataset as an Array JSON objects (note: potentially we could add a post-processing step if this
 * is too restrictive?)
 * </ol>
 * </p>
 * <p>
 * The process for execution is:
 * <ol>
 * <li>retrieve dataset</li>
 * <li>split dataset - assume it's a Collection, Stream or similar - something that we know how to
 * split</li>
 * <li>create a new queue for the response (dedicated to this job)</li>
 * <li>submit each item to request queue, specifying the queue name for the responses</li>
 * <li>monitor items from response queue</li>
 * <li>create/update dataset when requested</li>
 * <li>when response queue has expected totalCount then update dataset with final results</li>
 * <li>delete the response queue</li>
 * </ol>
 * </p>
 *
 * @author timbo
 */
public class QueueJob<T extends DatasetJobDefinition> extends AbstractDatasetJob implements UpdatableJob, CancellableJob {

    private static final Logger LOG = Logger.getLogger(QueueJob.class.getName());

    private final String queueName;
    private transient PollingConsumer consumer;
    private transient JmsQueueEndpoint jms;
    private final transient AtomicBoolean cancelled = new AtomicBoolean(false);

    public QueueJob(T jobdef, String queueName) {
        super(jobdef);
        this.queueName = queueName;
    }

    /**
     * Constructor for re-hydrating a "parked" job
     *
     * @param jobId
     * @param queueName
     * @param totalCount
     * @param processedCount
     * @param started
     */
    public QueueJob(String jobId, T jobdef, String queueName, int totalCount, int processedCount, Date started) {
        super(jobId, jobdef, totalCount, processedCount);
        this.queueName = queueName;
    }

    protected String getResponseQueueName() {
        return "QueueJob_" + getJobId();
    }

    protected String getResponseQueueUri() {
        return CamelExecutor.JMS_BROKER_NAME + ":queue:" + getResponseQueueName();
    }

    @Override
    protected JobStatus doExecute(Environment env) throws CamelExecutionException {
        LOG.log(Level.FINE, "QueueJob.execute() Submitting job {0} to queue {1}", new Object[]{jobdef, queueName});
//        Object dataset = null; ////env.getDatasetService().get(inputDatasetId);
//        Map<String, Object> headers = new HashMap<>();
//
//        headers.put("CamelJmsDestinationName", queueName);
//        headers.put("JMSReplyTo", getResponseQueueName());
//        headers.put("JobId", getJobId());
//        totalCount = env.getExecutorService().getProducerTemplate().requestBodyAndHeaders(CamelExecutor.ENDPOINT_SPLIT_AND_SUBMIT, dataset, headers, Integer.class);
        return null;
    }

    @Override
    protected void updateStatus(Environment env) {
        doUpdateStatus(env);
    }

    private void doUpdateStatus(Environment env) {
        long size = getQueueEndpoint(env).queueSize();
        pendingCount = (int) Math.min((long) Integer.MAX_VALUE, size);
        if (pendingCount + processedCount == totalCount) {
            status = JobStatus.Status.RESULTS_READY;
            completed = new Date();
        } else {
            status = JobStatus.Status.RUNNING;
        }
    }

    /**
     * Process the current results and write to the result set.
     *
     * @param env
     * @return The current status
     */
    @Override
    public JobStatus processResults(Environment env) {
        if (status != JobStatus.Status.COMPLETED) {
            try {
                // we have new results
                handleResults(env);
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, "Failed to update results", ex);
                status = JobStatus.Status.ERROR;
                this.exception = ex;
            }
        }
        return buildStatus();
    }

    protected JobStatus handleResults(Environment env) throws Exception {
        List currentResults = null;
//        if (outputDatasetId == null) {
//            currentResults = new ArrayList();
//        } else {
//            currentResults = null; ////(List) env.getDatasetService().get(outputDatasetId);
//        }
        LOG.log(Level.FINE, "Found {0} previous results", currentResults.size());
        consumeResults(env, currentResults);
        processedCount = currentResults.size();
        LOG.log(Level.FINE, "Found {0} results", processedCount);
        if (totalCount == processedCount) {
            // job complete
            status = JobStatus.Status.COMPLETED;
            if (completed != null) {
                completed = new Date();
            }
        }
//        if (processedCount > 0) {
//            if (outputDatasetId == null) {
//                outputDatasetId = null; ////env.getDatasetService().put(currentResults);
//            } else {
//                ////env.getDatasetService().update(outputDatasetId, currentResults);
//            }
//        }
        return buildStatus();
    }

    private void consumeResults(Environment env, List currentResults) throws Exception {
        if (cancelled.get()) {
            LOG.info("Already cancelled");
            return;
        }
        PollingConsumer pollingConsumer = getResponseConsumer(env);
        Exchange exch;
        LOG.fine("Checking for results");
        while (!cancelled.get() && (exch = pollingConsumer.receive(10)) != null) {
            Object body = exch.getIn().getBody();
            //LOG.log(Level.FINER, "Polled: {0}", body);
            currentResults.add(body);
            UnitOfWork uow = exch.getUnitOfWork();
            if (uow != null) {
                uow.done(exch);
            }
        }
    }

    private PollingConsumer getResponseConsumer(Environment env) throws Exception {
        if (consumer == null) {
            LOG.log(Level.INFO, "Creating polling consumer for {0}", getResponseQueueName());
            consumer = getQueueEndpoint(env).createPollingConsumer();
        }
        return consumer;
    }

    private JmsQueueEndpoint getQueueEndpoint(Environment env) {
        if (jms == null) {
            LOG.log(Level.INFO, "Creating endpoint for {0}", getResponseQueueUri());
            jms = env.getExecutorService().getProducerTemplate().getCamelContext().getEndpoint(
                    getResponseQueueUri(), JmsQueueEndpoint.class);
        }
        return jms;
    }

//    private void deleteQueue() {
//        JmsQueueEndpoint ep = getQueueEndpoint();
//        if (ep instanceof JmsTemporaryQueueEndpoint) {
//            JmsTemporaryQueueEndpoint tep = (JmsTemporaryQueueEndpoint) ep;
//            tep.get
//        }
//
//    }
    @Override
    public boolean cancel(Environment env) {
        if (!cancelled.get()) {
            // TODO - how to stop the job? 
            // Consume from the request queue all items with the job ID? 
            this.status = JobStatus.Status.CANCELLED;
            this.completed = new Date();
            return cancelled.getAndSet(true);
        }
        return true;
    }
}