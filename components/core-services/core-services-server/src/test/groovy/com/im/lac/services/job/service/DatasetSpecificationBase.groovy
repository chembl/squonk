package com.im.lac.services.job.service

import com.im.lac.services.*
import com.im.lac.services.camel.CamelLifeCycle
import com.im.lac.services.util.*
import com.im.lac.services.dataset.service.*
import org.apache.camel.CamelContext
import org.apache.camel.ProducerTemplate
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.impl.SimpleRegistry
import spock.lang.Shared
import spock.lang.Specification

/**
 *
 * @author timbo
 */
abstract class DatasetSpecificationBase extends Specification {
    
    @Shared SimpleRegistry registry
    @Shared CamelContext camelContext
    @Shared CamelLifeCycle lifeCycle
    @Shared ProducerTemplate producerTemplate
    
    void setupSpec() {
        doSetup(DatasetServiceImpl.DEFAULT_TABLE_NAME + "_test_JobServiceRouteBuilderSpec")
    }
    
    void doSetup(String datasetsTableName) {
        lifeCycle = new CamelLifeCycle(Utils.createDataSource())
        lifeCycle.datasetsTableName = datasetsTableName
        lifeCycle.createTables = true
        lifeCycle.dropTables = true
        registry = new SimpleRegistry()
        camelContext = new DefaultCamelContext(registry)
        doProcessCamelContext()
        producerTemplate = camelContext.createProducerTemplate()
        
        lifeCycle.beforeAddRoutes(camelContext, registry)
        doAddRoutes()
        lifeCycle.afterAddRoutes(camelContext, registry)

        lifeCycle.beforeStart(camelContext, registry)
        camelContext.start()
        lifeCycle.afterStart(camelContext, registry)
    }
    
    abstract void doAddRoutes()
    
    void doProcessCamelContext() {}
    
    
    def cleanupSpec() {
        lifeCycle.beforeStop(camelContext, registry)
        camelContext.stop()
        lifeCycle.afterStop(camelContext, registry)
    }  
    
    DatasetHandler getDatasetHandler() {
        return registry.lookupByNameAndType(ServerConstants.DATASET_HANDLER, DatasetHandler.class)
    }
    
   
}

