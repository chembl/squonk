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

package org.squonk.options;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/** Defines a type of option for a Dataset field (value) allowing to filter based on criteria such as the value type (class).
 *
 * Created by timbo on 03/02/16.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DatasetFieldTypeDescriptor extends SimpleTypeDescriptor<String> implements Serializable {

    private final String inputName;
    private final Class[] typeFilters;

    public DatasetFieldTypeDescriptor(
            @JsonProperty("inputName") String inputName,
            @JsonProperty("typeFilters") Class[] typeFilters
           ) {
        super(String.class);
        this.inputName = inputName == null ? "input" : inputName;
        this.typeFilters = typeFilters;
    }

    /** Creates a descriptor using the default input name of "input"
     *
     * @param typeFilters
     */
    public DatasetFieldTypeDescriptor(Class[] typeFilters) {
        this("input", typeFilters);
    }

    /** Creates a descriptor using the default input name of "input" and no class filtering
     *
     */
    public DatasetFieldTypeDescriptor() {
        this("input", new Class[0]);
    }

    public String getInputName() {
        return inputName;
    }

    public Class[] getTypeFilters() {
        return typeFilters;
    }


    public boolean filter(String name, Class type) {
        if (typeFilters == null || typeFilters.length == 0) {
            return true;
        }
        for (Class cls : typeFilters) {
            if (cls.isAssignableFrom(type)) {
                return true;
            }
        }
        return false;
    }


}
