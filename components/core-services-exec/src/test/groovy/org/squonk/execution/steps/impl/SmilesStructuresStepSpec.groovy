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

import org.squonk.dataset.Dataset
import org.squonk.execution.variable.VariableManager
import org.squonk.io.IODescriptor
import org.squonk.io.IODescriptors
import org.squonk.io.IORoute
import org.squonk.notebook.api.VariableKey
import spock.lang.Specification

/**
 * Created by timbo on 07/10/16.
 */
class SmilesStructuresStepSpec extends Specification {


    Long producer = 1

    void "read smiles"() {

        String text = "CCCC\nCCCCC\nCCCCCC"
        VariableManager varman = new VariableManager(null, 1, 1);
        SmilesStructuresStep step = new SmilesStructuresStep()

        step.configure(producer, "job1",
                [(SmilesStructuresStep.OPTION_SMILES): text],
                [IODescriptors.createMoleculeObjectDataset("input")] as IODescriptor[],
                [IODescriptors.createMoleculeObjectDataset("output")] as IODescriptor[],
                [:], [:])

        when:
        step.execute(varman, null)
        Dataset dataset = varman.getValue(new VariableKey(producer, "output"), Dataset.class)

        then:
        dataset != null
        def items = dataset.items
        items.size() == 3
        items[0].values.size() == 0

    }

    void "read smiles with names"() {

        String text = "CCCC one\nCCCCC two\nCCCCCC three"
        VariableManager varman = new VariableManager(null, 1, 1);
        SmilesStructuresStep step = new SmilesStructuresStep()

        step.configure(producer, "job1",
                [(SmilesStructuresStep.OPTION_SMILES): text],
                [IODescriptors.createMoleculeObjectDataset("input")] as IODescriptor[],
                [IODescriptors.createMoleculeObjectDataset("output")] as IODescriptor[],
                [:], [:])

        when:
        step.execute(varman, null)
        Dataset dataset = varman.getValue(new VariableKey(producer, "output"), Dataset.class)

        then:
        dataset != null
        def items = dataset.items
        items.size() == 3
        items[0].values.size() == 1
        items[0].values.Name == 'one'

    }
}
