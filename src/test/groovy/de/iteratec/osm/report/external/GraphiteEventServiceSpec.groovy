/*
* OpenSpeedMonitor (OSM)
* Copyright 2014 iteratec GmbH
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* 	http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package de.iteratec.osm.report.external

import de.iteratec.osm.batch.Activity
import de.iteratec.osm.batch.BatchActivity
import de.iteratec.osm.batch.BatchActivityService
import de.iteratec.osm.batch.BatchActivityUpdaterDummy
import de.iteratec.osm.measurement.environment.wptserverproxy.HttpRequestService
import de.iteratec.osm.measurement.environment.wptserverproxy.Protocol
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.chart.CsiAggregationUtilService
import de.iteratec.osm.report.chart.Event
import de.iteratec.osm.report.chart.EventDaoService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import groovyx.net.http.RESTClient
import org.apache.http.HttpHost
import org.joda.time.DateTime
import org.junit.Rule
import software.betamax.Configuration
import software.betamax.ProxyConfiguration
import software.betamax.junit.Betamax
import software.betamax.junit.RecorderRule
import spock.lang.Specification

import static org.apache.http.conn.params.ConnRoutePNames.DEFAULT_PROXY

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(GraphiteEventService)
@Mock([GraphiteServer, BatchActivity, Event, JobGroup, GraphiteEventSourcePath])
class GraphiteEventServiceSpec extends Specification {

    public static final DateTime untilDateTime = new DateTime(2015, 5, 29, 5, 0, 0)
    public static final int minutesInPast = 1
    GraphiteEventService serviceUnderTest

    Configuration configuration = ProxyConfiguration.builder().tapeRoot(new File("src/test/resources/betamax_tapes")).ignoreLocalhost(false).build();
    @Rule
    public RecorderRule recorder = new RecorderRule(configuration)
    public static final String jobGroupName = 'associated JobGroup'
    public static String metricName = 'alias(drawAsInfinite(server.monitor02.*.load.load_fifteen),"my-graph")'

    def doWithSpring = {
        eventDaoService(EventDaoService)
        csiAggregationUtilService(CsiAggregationUtilService)
        httpRequestService(HttpRequestService)
    }

    def setup() {

        serviceUnderTest = service

        //mocks common for all tests/////////////////////////////////////////////////////////////////////////////////////////////
        mockBatchActivityService()
        serviceUnderTest.eventDaoService = grailsApplication.mainContext.getBean('eventDaoService')
        mockCsiAggregationUtilService()
        mockHttpBuilderToUseBetamax()

    }

    @Betamax(tape = 'GraphiteEventServiceSpec_retrieve_events')
    def "retrieve events from test graphite server test"() {
        given:
        createGraphiteServerWithSourcePaths()

        when: "we fetch graphite events and create some filtered lists"
        serviceUnderTest.fetchGraphiteEvents(false, minutesInPast)

        List<Event> allEvents = Event.list()
        List<Event> eventsWithExpectedNames = allEvents.findAll { it.shortName == 'from graphite|my-graph' }
        List<Event> eventsWithExpectedDescriptions = allEvents.findAll{it.description == "Read from Graphite: my-graph [$metricName]" }
        List<Event> eventsWhichAreNotGloballyVisible = allEvents.findAll{ !it.globallyVisible }
        List<Event> eventsWithExpectedJobGroup = allEvents.findAll{it.jobGroups*.name.contains(jobGroupName)}

        then: "the filtered events should match contain the expected events"
        allEvents.size() == 6
        eventsWithExpectedNames.size() == allEvents.size()
        eventsWithExpectedDescriptions.size() == allEvents.size()
        eventsWhichAreNotGloballyVisible.size() == allEvents.size()
        eventsWithExpectedJobGroup.size() == allEvents.size()
    }

    private void createGraphiteServerWithSourcePaths() {
        GraphiteServer server = new GraphiteServer(
                serverAdress: 'url.to.carbon',
                port: 2003,
                webappUrl: 'monitoring.hh.iteratec.de/',
                webappProtocol: Protocol.HTTP,
                webappPathToRenderingEngine: 'render'
        )
        JobGroup jobGroup = new JobGroup(
                name: jobGroupName,
                graphiteServers: [server],
        )
        GraphiteEventSourcePath eventSourcePath = new GraphiteEventSourcePath(
                staticPrefix: 'from graphite|',
                targetMetricName: metricName,
                jobGroups: [jobGroup]
        )
        server.addToGraphiteEventSourcePaths(eventSourcePath)
        server.save(failOnError: true)

    }

    private void mockHttpBuilderToUseBetamax() {
        Properties properties = new Properties()
        new File('grails-app/conf/betamax.properties').withInputStream {
            properties.load(it)
        }
        String host = properties.'betamax.proxyHost'
        int port = properties.'betamax.proxyPort' as int
        HttpRequestService httpRequestService = grailsApplication.mainContext.getBean('httpRequestService')
        httpRequestService.metaClass.getRestClient = { String url ->
            RESTClient restClient = new RESTClient(url)
            restClient.client.params.setParameter(DEFAULT_PROXY, new HttpHost(host, port, 'http'))
            return restClient
        }
        serviceUnderTest.httpRequestService = httpRequestService
    }

    private void mockBatchActivityService(){
        serviceUnderTest.batchActivityService = Stub(BatchActivityService){
            getActiveBatchActivity(_ as Class, _ as Activity,_ as String, _ as int,_ as Boolean) >> { Class c, Activity activity, String name, int maxStages, boolean observe ->
                return new BatchActivityUpdaterDummy(name,c.name,activity, maxStages, 5000)
            }
        }
    }

    private void mockCsiAggregationUtilService(){
        CsiAggregationUtilService mockedCsiAggregationUtilService = Spy(CsiAggregationUtilService)
        mockedCsiAggregationUtilService.getNowInUtc() >> untilDateTime
        serviceUnderTest.csiAggregationUtilService = mockedCsiAggregationUtilService
    }

}
