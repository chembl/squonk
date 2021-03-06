/*
 * Copyright (c) 2017 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.squonk.core.client

import org.squonk.core.ServiceConfig
import org.squonk.data.Molecules
import org.squonk.dataset.Dataset
import org.squonk.dataset.DatasetMetadata
import org.squonk.execution.variable.impl.VariableWriteContext
import org.squonk.jobdef.JobStatus
import org.squonk.notebook.api.NotebookDTO
import org.squonk.notebook.api.NotebookEditableDTO
import org.squonk.types.DatasetHandler
import org.squonk.types.MoleculeObject
import org.squonk.types.io.JsonHandler
import org.squonk.util.IOUtils
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

/**
 * Created by timbo on 09/01/17.
 */
abstract class ClientSpecBase extends Specification {

    static String username = 'squonkuser'
    static JsonHandler JSON = JsonHandler.instance

    //static String coreservicesHost = System.getenv("CORESERVICES_HOST")
    //static String coreservicesPort = System.getenv("CORESERVICES_TCP_8080")

    static String coreservicesServer = "http://" + IOUtils.getDockerGateway() + ":8091"

    @Shared
    NotebookEditableDTO editable
    @Shared
    Long notebookId
    @Shared
    Long editableId
    @Shared
    Long cellId = 1

    static NotebookRestClient createNotebookRestClient() {
        //new NotebookRestClient("$coreservicesHost:$coreservicesPort")
        new NotebookRestClient(coreservicesServer);
    }

    static JobStatusRestClient createJobStatusRestClient() {
        new JobStatusRestClient(coreservicesServer);
    }

    static ServicesRestClient createServicesClient() {
        new ServicesRestClient(coreservicesServer);
    }

    static UserRestClient createUserRestClient() {
        new UserRestClient(coreservicesServer);
    }


    @Shared
    JobStatusRestClient jobClient = createJobStatusRestClient()
    @Shared
    NotebookRestClient notebookClient = createNotebookRestClient()
    @Shared
    ServicesRestClient servicesClient = createServicesClient()

    private Future<Map<String, ServiceConfig>> serviceConfigsFuture


    void doSetupSpec(String name) {
        getServiceConfigs() // need to wait for everything to start
        NotebookDTO notebookDTO = notebookClient.createNotebook(username, name+" name", name+" description")
        editable = notebookClient.listEditables(notebookDTO.getId(), username)[0]
        DatasetHandler dh = new DatasetHandler(MoleculeObject.class)
        Dataset dataset = Molecules.datasetFromSDF(Molecules.KINASE_INHIBS_SDF)
        VariableWriteContext vhcontext = new VariableWriteContext(notebookClient, editable.notebookId, editable.id, 1, "input")
        dh.writeVariable(dataset, vhcontext)
        notebookId = editable.notebookId
        editableId = editable.id
    }

    void cleanupSpec() {
        notebookClient.deleteNotebook(editable.getNotebookId())
    }

    Map<String, ServiceConfig> getServiceConfigs() {
        if (serviceConfigsFuture == null) {

            ExecutorService executor = Executors.newFixedThreadPool(1);

            Callable<Map<String, ServiceConfig>> task = {
                Map<String, ServiceConfig> results
                for (int i = 0; i < 60; i++) {
                    println "Trying for services attempt ${i + 1}"
                    def configs = servicesClient.getServiceConfigs(username)
                    println "Discovered ${configs.size()} services"
                    if (configs.size() > 20) {
                        results = [:]
                        configs.each { results[it.id] = it }
                        println "loaded ${configs.size()} service configs"
                        break
                    }
                    TimeUnit.SECONDS.sleep(2);
                }
                if (!results) {
                    // never retrieved the services
                    throw new RuntimeException("Failed to load services")
                }
                println "task generated ${results.size()} service configs"
                return results
            } as Callable

            serviceConfigsFuture = executor.submit(task as Callable);
            executor.shutdown()

        }
        Map<String, ServiceConfig> configs = serviceConfigsFuture.get()
        return configs
    }


    int findResultSize(notebookId, editableId, cellId, varname) {
        def metaJson = notebookClient.readTextValue(notebookId, editableId, cellId, varname)
        return metaJson ? JSON.objectFromJson(metaJson, DatasetMetadata.class).size : -1
    }

    DatasetMetadata readMetadata(notebookId, editableId, cellId, varname) {
        def metaJson = notebookClient.readTextValue(notebookId, editableId, cellId, varname)
        return metaJson ? JSON.objectFromJson(metaJson, DatasetMetadata.class) : null
    }

    InputStream readData(notebookId, editableId, cellId, varname) {
        return notebookClient.readStreamValue(notebookId, editableId, cellId, varname)
    }

    JobStatus waitForJob(def jobId, int numSecsToWait) {
        JobStatus status
        for (int i = 0; i < numSecsToWait; i++) {
            sleep(1000)
            status = jobClient.get(jobId)
            if (status.status == JobStatus.Status.COMPLETED || status.status == JobStatus.Status.ERROR) {
                //println "job completed"
                break
            }
            //println "trying again ..."
        }
        return status
    }

    JobStatus waitForJob(def jobId) {
        return waitForJob(jobId, 60)
    }


}
