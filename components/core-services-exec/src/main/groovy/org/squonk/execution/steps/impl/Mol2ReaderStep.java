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

package org.squonk.execution.steps.impl;

import org.apache.camel.CamelContext;
import org.squonk.execution.steps.AbstractStandardStep;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.VariableManager;

import java.io.InputStream;
import java.util.logging.Logger;

/**
 * Reads a Tripos Mol2 format file.
 * The structure is passed as an {@link InputStream} (can be gzipped).
 *
 * @author timbo
 */
public class Mol2ReaderStep extends AbstractStandardStep {

    private static final Logger LOG = Logger.getLogger(Mol2ReaderStep.class.getName());

    /**
     * Expected variable name for the input
     */
    private static final String VAR_FILE_INPUT = StepDefinitionConstants.VARIABLE_FILE_INPUT;

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {
        LOG.info("execute Mol2ReaderStep");
        statusMessage = "Reading file";
        String filename = fetchMappedInput(VAR_FILE_INPUT, String.class, varman);
        statusMessage = "Read Mol2 file " + filename;
    }

}
