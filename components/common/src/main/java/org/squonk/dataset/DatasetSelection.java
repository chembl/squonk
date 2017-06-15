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

package org.squonk.dataset;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.*;

/**
 * Created by timbo on 03/10/16.
 */
public class DatasetSelection implements Serializable {

    private final Set<UUID> uuids = new HashSet<>();

    public DatasetSelection(@JsonProperty("uuids") Collection<UUID> uuids) {
        if (uuids != null) {
            this.uuids.addAll(uuids);
        }
    }

    public Set<UUID> getUuids() {
        return uuids;
    }
}
