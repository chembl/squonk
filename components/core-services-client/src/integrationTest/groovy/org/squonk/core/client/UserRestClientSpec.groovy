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

import org.squonk.core.user.User
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class UserRestClientSpec extends Specification {
    
    String username = 'squonkuser'
   
    void "test get user object"() {
        setup:
        def client = ClientSpecBase.createUserRestClient()
        
        when:
        User user = client.getUser(username)
        println "received user $user"
        
        
        then: 
        user != null
        user.username == username
    }
    
    void "test 404"() {
        setup:
        def client = new UserRestClient("http://localhost/bananas/")
        
        when:
        User user = client.getUser(username)
        

        then: 
        thrown(IOException)
    }
    
}

