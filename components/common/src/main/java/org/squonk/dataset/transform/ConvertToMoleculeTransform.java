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

package org.squonk.dataset.transform;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Convert the Dataset to a Dataset<MoleculeObject> using the specified field and format
 *
 * @author timbo
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ConvertToMoleculeTransform extends AbstractTransform {

    private final String structureFieldName;
    private final String structureFormat;

    protected ConvertToMoleculeTransform(
            @JsonProperty("structureFieldName")String structureFieldName,
            @JsonProperty("structureFormat")String structureFormat) {
        this.structureFieldName = structureFieldName;
        this.structureFormat = structureFormat;
    }

    public String getStructureFieldName() {
        return structureFieldName;
    }

    public String getStructureFormat() {
        return structureFormat;
    }

}
