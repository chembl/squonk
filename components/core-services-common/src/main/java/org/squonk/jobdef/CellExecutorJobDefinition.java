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

package org.squonk.jobdef;

import org.squonk.io.IODescriptor;

/**
 * Created by timbo on 31/12/15.
 */
public interface CellExecutorJobDefinition extends JobDefinition {

    Long getNotebookId();
    Long getEditableId();
    Long getCellId();

    /** The inputs the cell accepts.
     *
     * @return
     */
    IODescriptor[] getInputs();

    /** The outputs the cell produces.
     *
     * @return
     */
    IODescriptor[] getOutputs();
}
