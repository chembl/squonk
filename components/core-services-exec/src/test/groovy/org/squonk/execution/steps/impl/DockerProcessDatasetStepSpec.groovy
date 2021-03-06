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

package org.squonk.execution.steps.impl

import org.apache.camel.impl.DefaultCamelContext
import org.squonk.dataset.Dataset
import org.squonk.execution.steps.StepDefinitionConstants
import org.squonk.execution.variable.VariableManager
import org.squonk.io.IODescriptor
import org.squonk.io.IODescriptors
import org.squonk.io.IORoute
import org.squonk.notebook.api.VariableKey
import org.squonk.types.BasicObject
import org.squonk.types.MoleculeObject
import org.squonk.util.CommonMimeTypes
import spock.lang.Specification

/**
 * Created by timbo on 13/09/16.
 */
class DockerProcessDatasetStepSpec extends Specification {


    Long producer = 1

    def createDataset() {
        def mols = [
                new BasicObject([idx: 0, a: 11, b: 'red',    c: 7, d: 5]),
                new BasicObject([idx: 1, a: 23, b: 'blue',   c: 5]),
                new BasicObject([idx: 2, a: 7,  b: 'green',  c: 5, d: 7]),
                new BasicObject([idx: 3, a: 17, b: 'orange', c: 1, d: 3])
        ]

        Dataset ds = new Dataset(MoleculeObject.class, mols)
        return ds
    }

    def createVariableManager() {
        VariableManager varman = new VariableManager(null, 1, 1);
        varman.putValue(
                new VariableKey(producer, "input"),
                Dataset.class,
                createDataset())
        return varman
    }

    def createStep(args) {
        DockerProcessDatasetStep step = new DockerProcessDatasetStep()
        step.configure(producer, "job1",
                args,
                [IODescriptors.createMoleculeObjectDataset("input")] as IODescriptor[],
                [IODescriptors.createMoleculeObjectDataset("output")] as IODescriptor[],
                [(DatasetSorterStep.VAR_INPUT_DATASET): new VariableKey(producer, "input")],
                [:])
        return step
    }


    void "simple execute json"() {

        DefaultCamelContext context = new DefaultCamelContext()
        VariableManager varman = createVariableManager()
        Map args = [dockerCommand: '#!/bin/sh\ncp input.data.gz output.data.gz\n', dockerImage:'busybox']
        DockerProcessDatasetStep step = createStep(args)

        when:
        step.execute(varman, context)
        Dataset dataset = varman.getValue(new VariableKey(producer, ValueTransformerStep.VAR_OUTPUT_DATASET), Dataset.class)

        then:
        dataset != null
        dataset.generateMetadata()
        List results = dataset.getItems()
        results.size() == 4

    }

//    void "simple execute sdf"() {
//
//        DefaultCamelContext context = new DefaultCamelContext()
//        VariableManager varman = createVariableManager()#
//        Map args = [dockerCommand: '#!/bin/sh\ncp input.sdf.gz output.sdf.gz\n', dockerImage:'busybox',
//                    (StepDefinitionConstants.OPTION_MEDIA_TYPE_INPUT): CommonMimeTypes.MIME_TYPE_MDL_SDF,
//                    (StepDefinitionConstants.OPTION_MEDIA_TYPE_OUTPUT): CommonMimeTypes.MIME_TYPE_MDL_SDF]
//        DockerProcessDatasetStep step = createStep(args)
//
//        when:
//        step.execute(varman, context)
//        Dataset dataset = varman.getValue(new VariableKey(producer, ValueTransformerStep.VAR_OUTPUT_DATASET), Dataset.class)
//
//        then:
//        dataset != null
//    }
}