package geb.de.iteratec.osm.measurement.script

import de.iteratec.osm.csi.TestDataUtil
import de.iteratec.osm.security.User
import geb.CustomUrlGebReportingSpec
import geb.pages.de.iteratec.osm.measurement.environment.script.ScriptCreatePage
import geb.pages.de.iteratec.osm.measurement.environment.script.ScriptListPage
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import spock.lang.Shared
import spock.lang.Stepwise

/**
 * @author nkuhn
 */
@Integration
@Rollback
@Stepwise
class ScriptGebSpec extends CustomUrlGebReportingSpec{

    @Shared
    String nameOfCreatedScript

    void "test user gets to script list when logged in"() {
        given: "User is logged in"
        User.withNewTransaction {
            TestDataUtil.createOsmConfig()
            TestDataUtil.createAdminUser()
        }
        doLogin()

        when: "user tries to navigate to script list"
        go "/script/list?lang=en"

        then: "user gets to script list page"
        waitFor {
            at ScriptListPage
        }
    }
    void "test user gets to script create page when click create new script button"() {
        when: "user clicks create new script button"
        createButton.click()

        then: "user gets to script create page"
        waitFor {
            at ScriptCreatePage
        }
    }
    void "no script creation if label and scriptCode are missing"(){
        when: "create button clicked without name and scriptCode"
        createButton.click()
        then: "we are on create page again and at least 2 danger alerts are shown"
        waitFor {
            at ScriptCreatePage
        }
        dangerAlerts.size() > 1
    }
    void "no script creation if scriptCode is missing"(){
        when: "a script name but no scriptCode is added"
        nameOfCreatedScript = "myScript"
        nameInput << nameOfCreatedScript
        then: "we are on create page again and at least 1 danger alerts are shown"
        waitFor {
            at ScriptCreatePage
        }
        dangerAlerts.size() > 0
    }
    void "a script can be created with label and scriptCode"(){
        when: "a script code is added"
        String scriptCode = "navigate  https://www.amazon.de"
        js.exec("OpenSpeedMonitor.script.codemirrorEditor.setNewContent('${scriptCode}')")
        createButton.click()
        then: "script was created and we are on script list page"
        waitFor {
            at ScriptListPage
        }
        allScripts.find { it.text().contains(nameOfCreatedScript) }
    }

    def cleanupSpec() {
        doLogout()
    }

}