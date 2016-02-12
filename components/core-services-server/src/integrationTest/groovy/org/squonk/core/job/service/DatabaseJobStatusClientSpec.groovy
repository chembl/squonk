package org.squonk.core.job.service

import com.im.lac.job.jobdef.DoNothingJobDefinition
import com.im.lac.job.jobdef.JobDefinition
import com.im.lac.job.jobdef.JobQuery
import com.im.lac.job.jobdef.JobStatus
import org.squonk.core.util.TestUtils
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

/**
 * Created by timbo on 08/02/16.
 */
@Stepwise
class DatabaseJobStatusClientSpec extends Specification {

    @Shared long inTheFuture = System.currentTimeMillis() + 1000000
    @Shared  DatabaseJobStatusClient client = new DatabaseJobStatusClient()

    void "create job"() {

        JobDefinition jobdef = new DoNothingJobDefinition()

        when:
        int before = client.db.firstRow("SELECT count(*) from users.jobstatus")[0]
        JobStatus status = client.submit(jobdef, TestUtils.TEST_USERNAME, 100)
        int after = client.db.firstRow("SELECT count(*) from users.jobstatus")[0]


        then:
        status.status == JobStatus.Status.PENDING
        status.started != null
        after == before + 1
    }

    void "retrieve job"() {

        String uuid = client.db.firstRow("SELECT uuid from users.jobstatus LIMIT 1")[0]

        when:
        JobStatus status = client.get(uuid)

        then:
        status != null
        status.username == TestUtils.TEST_USERNAME
        status.jobDefinition instanceof DoNothingJobDefinition
    }

    void "update counts"() {

        String uuid = client.db.firstRow("SELECT uuid from users.jobstatus LIMIT 1")[0]

        when:
        JobStatus status1 = client.incrementCounts(uuid, 10, 5)
        JobStatus status2 = client.incrementCounts(uuid, 10, 5)

        then:
        status1.username == TestUtils.TEST_USERNAME
        status1.status == JobStatus.Status.PENDING
        status1.processedCount == 10
        status1.errorCount == 5

        status2.username == TestUtils.TEST_USERNAME
        status2.status == JobStatus.Status.PENDING
        status2.processedCount == 20
        status2.errorCount == 10
    }

    void "update status"() {

        String uuid = client.db.firstRow("SELECT uuid from users.jobstatus LIMIT 1")[0]

        when:
        JobStatus status = client.updateStatus(uuid, JobStatus.Status.ERROR)

        then:
        status != null
        status.username == TestUtils.TEST_USERNAME
        status.jobDefinition instanceof DoNothingJobDefinition
        status.status == JobStatus.Status.ERROR
        status.completed != null
    }

    void "list all statuses"() {

        when:
        List<JobStatus> statuses = client.list(null)

        then:
        statuses != null
        statuses.size() > 0
    }

    void "query statuses"() {

        expect:
        client.list(q).size() == c

        where:
        c|q
        1|new JobQuery(null, 100, [JobStatus.Status.ERROR, JobStatus.Status.COMPLETED], new java.sql.Date(0l), null, null, null)
        1|new JobQuery(null, null, [JobStatus.Status.ERROR, JobStatus.Status.COMPLETED], new java.sql.Date(0l), null, null, null)
        0|new JobQuery(null, 100, [JobStatus.Status.SUBMITTING], new java.sql.Date(0l), null, null, null)
        1|new JobQuery(TestUtils.TEST_USERNAME, 100, [JobStatus.Status.ERROR], new java.sql.Date(0l), null, null, null)
        0|new JobQuery('sombodyelse', 100, [JobStatus.Status.ERROR], new java.sql.Date(0l), null, null, null)
        1|new JobQuery(TestUtils.TEST_USERNAME, 100, [JobStatus.Status.ERROR], null, new java.sql.Date(inTheFuture), null, null)
        0|new JobQuery(TestUtils.TEST_USERNAME, 100, [JobStatus.Status.ERROR], new java.sql.Date(inTheFuture), null, null, null)
        0|new JobQuery(TestUtils.TEST_USERNAME, 100, [JobStatus.Status.ERROR], null, new java.sql.Date(0l), null, null)
        1|new JobQuery(TestUtils.TEST_USERNAME, 100, [JobStatus.Status.ERROR], new java.sql.Date(0l), new java.sql.Date(inTheFuture), null, null)
        1|new JobQuery(TestUtils.TEST_USERNAME, 100, [JobStatus.Status.ERROR], null, null, new java.sql.Date(0l), new java.sql.Date(inTheFuture))
    }

}