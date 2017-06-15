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

package org.squonk.types;

import java.io.IOException;
import java.io.InputStream;

/**
 * TODO - make Dataset, SDFile and CSVFile implement this.
 *
 * Created by timbo on 30/01/17.
 */
public interface SquonkDataType {


    String getMediaType();

    InputStream getInputStream(boolean gzip) throws IOException;
}
