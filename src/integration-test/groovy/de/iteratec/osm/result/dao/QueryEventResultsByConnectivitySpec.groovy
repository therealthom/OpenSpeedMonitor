package de.iteratec.osm.result.dao

import de.iteratec.osm.csi.NonTransactionalIntegrationSpec
import de.iteratec.osm.csi.Page
import de.iteratec.osm.csi.TestDataUtil
import de.iteratec.osm.dao.CriteriaSorting
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.result.*
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

/**
 *
 */
@Integration
@Rollback
class QueryEventResultsByConnectivitySpec extends NonTransactionalIntegrationSpec {

    EventResultDaoService eventResultDaoService

    public static final String CUSTOM_CONN_NAME_1 = 'Custom (6000/512 Kbps, 100ms, 0% PLR)'
    public static final String CUSTOM_CONN_NAME_2 = 'Custom (50000/6000 Kbps, 100ms, 0% PLR)'

    DateTime runDate
    private Job jobWithPredefinedConnectivity
    private Job jobWithNativeConnectivity
    private Job jobWithCustomConnectivity
    private MeasuredEvent measuredEvent

    private EventResult withPredefinedProfile1
    private EventResult withPredefinedProfile2
    private EventResult withNativeConnectivity
    private EventResult withCustomConnectivityMatchingRegex
    private EventResult withCustomConnectivityNotMatchingRegex
    private ConnectivityProfile predefinedProfile1
    private ConnectivityProfile predefinedProfile2

    def setupTest() {
        EventResult.withNewSession { session ->
            createTestDataCommonToAllTests();
            session.flush()
        }
    }

    // selection by one type of selector: predefined profile(s), custom conn or native conn ///////////////////////////////////////////
    void "select by single predefined profile"() {
        setup:
        setupTest()
        Collection<EventResult> results

        when:
        EventResult.withNewSession {

            MvQueryParams queryParams = new ErQueryParams()
            queryParams.browserIds.add(jobWithPredefinedConnectivity.location.browser.id)
            queryParams.jobGroupIds.add(jobWithPredefinedConnectivity.jobGroup.id)
            queryParams.measuredEventIds.add(measuredEvent.id)
            queryParams.pageIds.add(measuredEvent.testedPage.id)
            queryParams.locationIds.add(jobWithPredefinedConnectivity.location.id)
            queryParams.connectivityProfileIds.add(predefinedProfile1.ident())

            results = eventResultDaoService.getLimitedMedianEventResultsBy(
                    runDate.toDate(),
                    runDate.plusHours(1).toDate(),
                    [
                            CachedView.CACHED,
                            CachedView.UNCACHED
                    ] as Set,
                    queryParams,
                    [:],
                    new CriteriaSorting(sortingActive: false)
            )
        }

        then:
        results.size() == 1
        results[0].connectivityProfile.ident() == predefinedProfile1.ident()
        results[0].customConnectivityName == null
    }

    void "select by a list of predefined profiles"() {
        setup:
        setupTest()
        Collection<EventResult> results

        when:
        EventResult.withNewSession {
            MvQueryParams queryParams = new ErQueryParams()
            queryParams.browserIds.add(jobWithPredefinedConnectivity.location.browser.id)
            queryParams.jobGroupIds.add(jobWithPredefinedConnectivity.jobGroup.id)
            queryParams.measuredEventIds.add(measuredEvent.id)
            queryParams.pageIds.add(measuredEvent.testedPage.id)
            queryParams.locationIds.add(jobWithPredefinedConnectivity.location.id)
            queryParams.connectivityProfileIds.addAll([predefinedProfile1.ident(), predefinedProfile2.ident()])

            results = eventResultDaoService.getLimitedMedianEventResultsBy(
                    runDate.toDate(),
                    runDate.plusHours(1).toDate(),
                    [
                            CachedView.CACHED,
                            CachedView.UNCACHED
                    ] as Set,
                    queryParams,
                    [:],
                    new CriteriaSorting(sortingActive: false)
            )
        }

        then:
        results.size() == 2
        List idsOfConnProfilesAssociatedToResults = results*.connectivityProfile*.ident()
        idsOfConnProfilesAssociatedToResults.contains(predefinedProfile1.ident())
        idsOfConnProfilesAssociatedToResults.contains(predefinedProfile2.ident())
        results[0].customConnectivityName == null
        results[1].customConnectivityName == null
    }

    void "select by custom conn name regex: not matching all"() {
        setup:
        setupTest()
        Collection<EventResult> results

        when:
        MvQueryParams queryParams = new ErQueryParams();
        queryParams.customConnectivityNames.add(CUSTOM_CONN_NAME_1)
        queryParams.browserIds.add(jobWithPredefinedConnectivity.location.browser.id)
        queryParams.jobGroupIds.add(jobWithPredefinedConnectivity.jobGroup.id)
        queryParams.measuredEventIds.add(measuredEvent.id)
        queryParams.pageIds.add(measuredEvent.testedPage.id)
        queryParams.locationIds.add(jobWithPredefinedConnectivity.location.id)

        results = eventResultDaoService.getLimitedMedianEventResultsBy(
                runDate.toDate(),
                runDate.plusHours(1).toDate(),
                [
                        CachedView.CACHED,
                        CachedView.UNCACHED
                ] as Set,
                queryParams,
                [:],
                new CriteriaSorting(sortingActive: false)
        )

        then:
        results.size() == 1
        results[0].connectivityProfile == null
        results[0].customConnectivityName == CUSTOM_CONN_NAME_1
    }

    void "select by custom conn name regex: matching all"() {
        setup:
        setupTest()
        Collection<EventResult> results

        when:
        EventResult.withNewSession {
            MvQueryParams queryParams = new ErQueryParams();
            queryParams.browserIds.add(jobWithPredefinedConnectivity.location.browser.id);
            queryParams.jobGroupIds.add(jobWithPredefinedConnectivity.jobGroup.id);
            queryParams.measuredEventIds.add(measuredEvent.id);
            queryParams.pageIds.add(measuredEvent.testedPage.id);
            queryParams.locationIds.add(jobWithPredefinedConnectivity.location.id);
            queryParams.customConnectivityNames.add(CUSTOM_CONN_NAME_1)
            queryParams.customConnectivityNames.add(CUSTOM_CONN_NAME_2)

            results = eventResultDaoService.getLimitedMedianEventResultsBy(
                    runDate.toDate(),
                    runDate.plusHours(1).toDate(),
                    [
                            CachedView.CACHED,
                            CachedView.UNCACHED
                    ] as Set,
                    queryParams,
                    [:],
                    new CriteriaSorting(sortingActive: false)
            )
        }

        then:
        results.size() == 2
        List associatedPredefinedProfiles = results*.connectivityProfile
        associatedPredefinedProfiles[0] == null
        associatedPredefinedProfiles[1] == null
        results*.customConnectivityName.contains(withCustomConnectivityMatchingRegex.customConnectivityName)
        results*.customConnectivityName.contains(withCustomConnectivityNotMatchingRegex.customConnectivityName)
    }

    void "select only native conn"() {
        setup:
        setupTest()
        Collection<EventResult> results

        when:
        EventResult.withNewSession {
            MvQueryParams queryParams = new ErQueryParams();
            queryParams.browserIds.add(jobWithPredefinedConnectivity.location.browser.id);
            queryParams.jobGroupIds.add(jobWithPredefinedConnectivity.jobGroup.id);
            queryParams.measuredEventIds.add(measuredEvent.id);
            queryParams.pageIds.add(measuredEvent.testedPage.id);
            queryParams.locationIds.add(jobWithPredefinedConnectivity.location.id);
            queryParams.includeNativeConnectivity = true

            results = eventResultDaoService.getLimitedMedianEventResultsBy(
                    runDate.toDate(),
                    runDate.plusHours(1).toDate(),
                    [
                            CachedView.CACHED,
                            CachedView.UNCACHED
                    ] as Set,
                    queryParams,
                    [:],
                    new CriteriaSorting(sortingActive: false)
            )
        }

        then:
        results.size() == 1
        results[0].connectivityProfile == null
    }

    // selection by combinations of multiple types of selectors: predefined profile(s)/custom conn/native conn ///////////////////////////////////////////
    void "select by custom conn name regex AND native conn"() {
        setup:
        setupTest()
        Collection<EventResult> results

        when:
        EventResult.withNewSession {
            MvQueryParams queryParams = new ErQueryParams();
            queryParams.browserIds.add(jobWithPredefinedConnectivity.location.browser.id);
            queryParams.jobGroupIds.add(jobWithPredefinedConnectivity.jobGroup.id);
            queryParams.measuredEventIds.add(measuredEvent.id);
            queryParams.pageIds.add(measuredEvent.testedPage.id);
            queryParams.locationIds.add(jobWithPredefinedConnectivity.location.id);
            queryParams.customConnectivityNames.add(CUSTOM_CONN_NAME_1)
            queryParams.includeNativeConnectivity = true

            results = eventResultDaoService.getLimitedMedianEventResultsBy(
                    runDate.toDate(),
                    runDate.plusHours(1).toDate(),
                    [
                            CachedView.CACHED,
                            CachedView.UNCACHED
                    ] as Set,
                    queryParams,
                    [:],
                    new CriteriaSorting(sortingActive: false)
            )
        }

        then:
        results.size() == 2
        results.findAll { it.connectivityProfile }.size() == 0
        results.findAll { it.customConnectivityName.equals(CUSTOM_CONN_NAME_1) }.size() == 1
    }

    void "select by custom conn name regex AND predefined conn"() {
        setup:
        setupTest()
        Collection<EventResult> results

        when:
        EventResult.withNewSession {
            MvQueryParams queryParams = new ErQueryParams();
            queryParams.browserIds.add(jobWithPredefinedConnectivity.location.browser.id);
            queryParams.jobGroupIds.add(jobWithPredefinedConnectivity.jobGroup.id);
            queryParams.measuredEventIds.add(measuredEvent.id);
            queryParams.pageIds.add(measuredEvent.testedPage.id);
            queryParams.locationIds.add(jobWithPredefinedConnectivity.location.id);
            queryParams.customConnectivityNames.add(CUSTOM_CONN_NAME_1)
            queryParams.customConnectivityNames.add(CUSTOM_CONN_NAME_2)
            queryParams.connectivityProfileIds.addAll([predefinedProfile1.ident()])

            results = eventResultDaoService.getLimitedMedianEventResultsBy(
                    runDate.toDate(),
                    runDate.plusHours(1).toDate(),
                    [
                            CachedView.CACHED,
                            CachedView.UNCACHED
                    ] as Set,
                    queryParams,
                    [:],
                    new CriteriaSorting(sortingActive: false)
            )
        }

        then:
        results.size() == 3
        results.findAll { it.connectivityProfile }.size() == 1
        results.findAll { it.customConnectivityName.equals(CUSTOM_CONN_NAME_1) }.size() == 1
        results.findAll { it.customConnectivityName.equals(CUSTOM_CONN_NAME_2) }.size() == 1
    }

    void "select by native conn AND predefined conn"() {
        setup:
        setupTest()
        Collection<EventResult> results

        when:
        EventResult.withNewSession {
            MvQueryParams queryParams = new ErQueryParams();
            queryParams.browserIds.add(jobWithPredefinedConnectivity.location.browser.id);
            queryParams.jobGroupIds.add(jobWithPredefinedConnectivity.jobGroup.id);
            queryParams.measuredEventIds.add(measuredEvent.id);
            queryParams.pageIds.add(measuredEvent.testedPage.id);
            queryParams.locationIds.add(jobWithPredefinedConnectivity.location.id);
            queryParams.connectivityProfileIds.addAll([predefinedProfile2.ident()])
            queryParams.includeNativeConnectivity = true

            results = eventResultDaoService.getLimitedMedianEventResultsBy(
                    runDate.toDate(),
                    runDate.plusHours(1).toDate(),
                    [
                            CachedView.CACHED,
                            CachedView.UNCACHED
                    ] as Set,
                    queryParams,
                    [:],
                    new CriteriaSorting(sortingActive: false)
            )
        }

        then:
        results.size() == 2
        results.findAll { it.connectivityProfile }.size() == 1
    }

    void "select by custom conn name regex AND native conn AND predefined conn"() {
        setup:
        setupTest()
        MvQueryParams queryParams = new ErQueryParams();
        queryParams.browserIds.add(jobWithPredefinedConnectivity.location.browser.id);
        queryParams.jobGroupIds.add(jobWithPredefinedConnectivity.jobGroup.id);
        queryParams.measuredEventIds.add(measuredEvent.id);
        queryParams.pageIds.add(measuredEvent.testedPage.id);
        queryParams.locationIds.add(jobWithPredefinedConnectivity.location.id);
        queryParams.connectivityProfileIds.addAll([predefinedProfile2.ident()])
        queryParams.includeNativeConnectivity = true
        queryParams.customConnectivityNames.add(CUSTOM_CONN_NAME_1)
        Collection<EventResult> results

        when:
        EventResult.withNewSession {
            results = eventResultDaoService.getLimitedMedianEventResultsBy(
                    runDate.toDate(),
                    runDate.plusHours(1).toDate(),
                    [
                            CachedView.CACHED,
                            CachedView.UNCACHED
                    ] as Set,
                    queryParams,
                    [:],
                    new CriteriaSorting(sortingActive: false)
            )
        }

        then:
        results.size() == 3
        ArrayList<EventResult> resultsWithPredefinedProfiles = results.findAll { it.connectivityProfile }
        resultsWithPredefinedProfiles.size() == 1
        resultsWithPredefinedProfiles[0].connectivityProfile.ident() == predefinedProfile2.ident()
        results.findAll { it.customConnectivityName.equals(CUSTOM_CONN_NAME_1) }.size() == 1
    }


    private void createTestDataCommonToAllTests() {

        predefinedProfile1 = TestDataUtil.createConnectivityProfile('connProfile 1: name')
        predefinedProfile2 = TestDataUtil.createConnectivityProfile('connProfile 2: name')

        WebPageTestServer server =
                TestDataUtil.createWebPageTestServer('server 1 - wpt server', 'server 1 - wpt server', true, 'http://server1.wpt.server.de')

        JobGroup jobGroup = TestDataUtil.createJobGroup("TestGroup")

        Browser fireFoxBrowser = TestDataUtil.createBrowser('FF')

        Location ffAgent1 = TestDataUtil.createLocation(server, 'physNetLabAgent01-FF', fireFoxBrowser, true)

        Page homepage = TestDataUtil.createPage('homepage')

        Script script = Script.createDefaultScript('Unnamed').save(failOnError: true)
        jobWithPredefinedConnectivity = TestDataUtil.createJob('job with predefined connectivity', script, ffAgent1, jobGroup, 'irrelevantDescription', 1, false, 60)
        jobWithNativeConnectivity = new Job(label: 'job with native connectivity', script: script, location: ffAgent1, jobGroup: jobGroup, description: 'irrelevantDescription', runs: 1, active: false, maxDownloadTimeInMinutes: 60, noTrafficShapingAtAll: true, customConnectivityProfile: false, connectivityProfile: null, executionSchedule: '0 0 */2 * * ? *').save(failOnError: true)
        jobWithCustomConnectivity = TestDataUtil.createJob('job with custom connectivity', script, ffAgent1, jobGroup, 'irrelevantDescription', 1, false, 60, predefinedProfile1)

        measuredEvent = TestDataUtil.createMeasuredEvent('Test event', homepage)

        /* Create TestData */
        runDate = new DateTime(2013, 5, 29, 0, 0, 0, DateTimeZone.UTC)

        JobResult jobRunWithPredefinedConnectivity = TestDataUtil.createJobResult('1', runDate.toDate(), jobWithPredefinedConnectivity, jobWithPredefinedConnectivity.location)
        JobResult jobRunWithNativeConnectivity = TestDataUtil.createJobResult('2', runDate.toDate(), jobWithNativeConnectivity, jobWithNativeConnectivity.location)
        JobResult jobRunWithCustomConnectivity = TestDataUtil.createJobResult('3', runDate.toDate(), jobWithCustomConnectivity, jobWithCustomConnectivity.location)

        withPredefinedProfile1 = TestDataUtil.createEventResult(jobWithPredefinedConnectivity, jobRunWithPredefinedConnectivity, 123I, 456.5D, measuredEvent, fireFoxBrowser, predefinedProfile1)

        withPredefinedProfile2 = TestDataUtil.createEventResult(jobWithPredefinedConnectivity, jobRunWithPredefinedConnectivity, 123I, 456.5D, measuredEvent, fireFoxBrowser, predefinedProfile2)

        withNativeConnectivity = TestDataUtil.createEventResult(jobWithNativeConnectivity, jobRunWithNativeConnectivity, 123I, 456.5D, measuredEvent, fireFoxBrowser)

        withCustomConnectivityMatchingRegex = TestDataUtil.createEventResult(jobWithCustomConnectivity, jobRunWithCustomConnectivity, 123I, 456.5D, measuredEvent, fireFoxBrowser)
        withCustomConnectivityMatchingRegex.connectivityProfile = null
        withCustomConnectivityMatchingRegex.noTrafficShapingAtAll = false
        withCustomConnectivityMatchingRegex.customConnectivityName = CUSTOM_CONN_NAME_1
        withCustomConnectivityMatchingRegex.save(failOnError: true)

        withCustomConnectivityNotMatchingRegex = TestDataUtil.createEventResult(jobWithCustomConnectivity, jobRunWithCustomConnectivity, 123I, 456.5D, measuredEvent, fireFoxBrowser)
        withCustomConnectivityNotMatchingRegex.connectivityProfile = null
        withCustomConnectivityNotMatchingRegex.noTrafficShapingAtAll = false
        withCustomConnectivityNotMatchingRegex.customConnectivityName = CUSTOM_CONN_NAME_2
        withCustomConnectivityNotMatchingRegex.save(failOnError: true)

    }

}
