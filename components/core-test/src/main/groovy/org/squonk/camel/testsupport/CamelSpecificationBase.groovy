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

package org.squonk.camel.testsupport

import org.apache.camel.impl.DefaultCamelContext
import spock.lang.Specification
import org.apache.camel.*
import org.apache.camel.builder.*


/**
 * Created by timbo on 13/04/2014.
 */
abstract class CamelSpecificationBase extends Specification {

    def camelContext
    def template

    def setup() {
        camelContext = createCamelContext()
        RouteBuilder rb = createRouteBuilder()
        println "ctx=$camelContext rb=$rb"
        camelContext.addRoutes(rb)
        camelContext.start()
        template = camelContext.createProducerTemplate()
    }

    def cleanup() {
        template?.stop()
        camelContext?.stop()
    }

    CamelContext createCamelContext() {
        new DefaultCamelContext()
    }

    abstract RouteBuilder createRouteBuilder()


}
