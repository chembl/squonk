/*
 * This Spock specification was auto generated by running 'gradle init --type groovy-library'
 * by 'timbo' at '23/02/15 15:20' with Gradle 2.2.1
 *
 * @author timbo, @date 23/02/15 15:20
 */

import spock.lang.Specification

class LibraryTest extends Specification{
    def "someLibraryMethod returns true"() {
        setup:
        Library lib = new Library()
        when:
        def result = lib.someLibraryMethod()
        then:
        result == true
    }
}
