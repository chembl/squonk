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

package org.squonk.core.client

import org.squonk.io.DepictionParameters
import spock.lang.Shared
import spock.lang.Specification

/**
 * Created by timbo on 04/09/16.
 */
class StructureIOClientSpec extends Specification {

    @Shared
    StructureIOClient client = new StructureIOClient.CDK()

    void "one line svg message"() {



        when:
        String svg =client.renderErrorSVG(new DepictionParameters(100, 75), "I am an error")
        println svg

        then:
        svg != null
    }

    void "two line svg message"() {



        when:
        String svg =client.renderErrorSVG(new DepictionParameters(100, 75), "I am an error", "So am I")
        println svg

        then:
        svg != null
    }
}
