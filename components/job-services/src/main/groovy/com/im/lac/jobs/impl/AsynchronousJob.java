package com.im.lac.jobs.impl;

import com.im.lac.service.Environment;
import com.im.lac.jobs.JobStatus;
import com.im.lac.model.ProcessDatasetJobDefinition;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Asynchronous executor for jobs that typically take a few seconds or minutes to complete.
 * <p>
 * The job remains active during execution and automatically processes the results in the
 * background. The caller should use the {@link getStatus()} method to get the current status, and
 * when it changes to JobStatus.Status.COMPLETED the job is finished.</p>
 *
 * <p>
 * The job is executed by the {@link doExecute()} method along these lines:
 * <ol>
 * <li>Retrieving the dataset from the MockDatasetService</li>
 * <li>Sending the dataset to the specified JMS queue in an asynchronous request-response
 * manner</li>
 * <li>Returning to the caller as soon as the message is sent</li>
 * <li>Waiting for the result on the temporary queue generated for the response</li>
 * <li>Saving the result to the dataset service according to the specified result mode.</li>
 * <li>Updating status to COMPLETE</li>
 * </ol>
 * </p>
 * <p>
 * The job is processed by some consumer that is listening to the JMS queue and posting the result
 * to the temporary queue specified by the JmsReplyTo header. Except for the name of the queue to
 * post the data to this is all transparent to the job definition.</p>
 *
 * @author timbo
 */
public class AsynchronousJob extends AbstractDatasetJob<ProcessDatasetJobDefinition> {

    private static final Logger LOG = Logger.getLogger(AsynchronousJob.class.getName());


    /**
     *
     * @param jobdef The job definition
     */
    public AsynchronousJob(ProcessDatasetJobDefinition jobdef) {
        super(jobdef);
    }


    @Override
    public JobStatus doExecute(Environment env) throws Exception {
        LOG.log(Level.FINE, "QueueJob.execute() {0}", jobdef);
        return env.getExecutorService().submitJob(this, AsyncJobRouteBuilder.ROUTE_SUBMIT);
    }

}
