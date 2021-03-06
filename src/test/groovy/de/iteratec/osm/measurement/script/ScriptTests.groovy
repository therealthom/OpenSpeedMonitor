package de.iteratec.osm.measurement.script

import spock.lang.Specification

class ScriptTests extends Specification{

    def "Test that scripts without parameter doesnt change on parse"(){
        given: "a script without parameters"
        String scriptContent = """setEventName aTest
                                  navigate http://testsite.de
                                  //a comment
                                  setEventName anotherTest
                                  navigate https://anothertestsite.de"""
        Script script = new Script(navigationScript: scriptContent)

        when: "the script is parsed"
        String parsedNavigationScript = script.getParsedNavigationScript([:])

        then: "the content doesn't change"
        parsedNavigationScript == scriptContent
    }

    def "Test that scripts with parameter are parsed correclty"(){
        given: "a script with parameters"
        String scriptContent = """setEventName aTest
                                  navigate http://testsite.de/\${parameter}
                                  //a comment
                                  setEventName anotherTest
                                  navigate https://anothertestsite.de/\${anotherParameter"""
        Script script = new Script(navigationScript: scriptContent)

        when: "the script is parsed"
        String parsedNavigationScript = script.getParsedNavigationScript([parameter:"path","anotherParameter":"anotherPath"])

        then: "the parameters are parsed correctly"
        parsedNavigationScript == scriptContent.replace("\${parameter}","path").replace("\${anotherParameter}","anotherPath")
    }
}
