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

package org.squonk.execution.variable.impl;

import org.squonk.api.VariableHandler;
import org.squonk.client.VariableClient;
import org.squonk.core.client.NotebookRestClient;
import org.squonk.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by timbo on 13/03/16.
 */
public class VariableReadContext implements VariableHandler.ReadContext{

    private final VariableClient client;
    private final Long notebookId;
    private final Long sourceId;
    private final Long cellId;
    private final String variableName;

    public VariableReadContext(VariableClient client, Long notebookId, Long sourceId, Long cellId, String variableName) {
        this.client = client;
        this.notebookId = notebookId;
        this.sourceId = sourceId;
        this.cellId = cellId;
        this.variableName = variableName;
    }

    @Override
    public String readTextValue(String key) throws Exception {
        return client.readTextValue(notebookId, sourceId, cellId, variableName, key);
    }

    @Override
    public String readSingleTextValue(String key) throws Exception {
        return readTextValue(null);
    }

    @Override
    public InputStream readStreamValue(String key) throws Exception {
        InputStream is = client.readStreamValue(notebookId, sourceId, cellId, variableName, key);
        return (is == null ? null : IOUtils.getGunzippedInputStream(is));
    }

    @Override
    public InputStream readSingleStreamValue(String key) throws Exception {
        return readStreamValue(null);
    }
}
