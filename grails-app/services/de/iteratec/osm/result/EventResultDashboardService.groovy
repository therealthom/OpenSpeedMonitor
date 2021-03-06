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

package de.iteratec.osm.result

import de.iteratec.osm.csi.Page
import de.iteratec.osm.dao.CriteriaSorting
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.BrowserService
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.ConnectivityProfileDaoService
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.dao.JobGroupDaoService
import de.iteratec.osm.report.chart.*
import de.iteratec.osm.result.dao.EventResultDaoService
import de.iteratec.osm.util.I18nService
import de.iteratec.osm.util.PerformanceLoggingService
import de.iteratec.osm.util.PerformanceLoggingService.LogLevel
import grails.transaction.Transactional
import grails.web.mapping.LinkGenerator
import org.joda.time.DateTime

import static de.iteratec.osm.util.Constants.HIGHCHART_LEGEND_DELIMITTER
import static de.iteratec.osm.util.Constants.UNIQUE_STRING_DELIMITTER

/**
 * <p>
 * A utility service for the event result dashboard and related operations.
 * </p>
 */
@Transactional
public class EventResultDashboardService {

    BrowserService browserService
    JobGroupDaoService jobGroupDaoService
    ResultCsiAggregationService resultCsiAggregationService
    I18nService i18nService
    EventResultDaoService eventResultDaoService
    CsiAggregationUtilService csiAggregationUtilService
    PerformanceLoggingService performanceLoggingService
    ConnectivityProfileDaoService connectivityProfileDaoService
    OsmChartProcessingService osmChartProcessingService

    /**
     * The Grails engine to generate links.
     *
     * @see http://mrhaki.blogspot.ca/2012/01/grails-goodness-generate-links-outside.html
     */
    LinkGenerator grailsLinkGenerator

    /**
     * Fetches all {@link MeasuredEvent}s from Database.
     *
     * <p>
     * 	Proxy for {@link MeasuredEvent}
     * </p>
     *
     * @return all {@link MeasuredEvent} ordered by their name.
     */
    public List<MeasuredEvent> getAllMeasuredEvents() {
        return MeasuredEvent.findAll().sort(false, { it.name.toLowerCase() })
    }

    /**
     * Fetches all {@link Location}s from Database.
     *
     * @return all {@link Location} ordered by their label.
     */
    public List<Location> getAllLocations() {
        return Location.list().sort(false, { it.label.toLowerCase() })
    }

    /**
     * Fetches all {@link ConnectivityProfile}s from Database.
     *
     * <p>
     * 	Proxy for {@link ConnectivityDaoService}
     * </p>
     *
     * @return all {@link ConnectivityProfile} ordered by their toString() representation.
     */
    public List<ConnectivityProfile> getAllConnectivityProfiles() {
        return ConnectivityProfile.findAllByActive(true).sort(false, { it.name.toLowerCase() })
    }

    /**
     * Collects all available connectivities.
     * This includes all ConnectivityProfiles, all CustomConnectivityNames from EventResult and "native" if EventResults with native measurement exist
     * @param includeNative if set to true and eventResults exists with noTrafficShapingAtAll "native" is added to the list
     * @return
     */
    List<Map<String, String>> getAllConnectivities(boolean includeNative = true) {
        List<Map<String, String>> result = [].withDefault { [:] }
        result.addAll(getAllConnectivityProfiles().collect { ["id": it.id, "name": it.toString()] })

        if (includeNative) {
            result.add(["id": ResultSelectionController.MetaConnectivityProfileId.Native.value, "name": ResultSelectionController.MetaConnectivityProfileId.Native.value])
        }

        return result
    }

    /**
     * Fetches all {@link Browser}s from Database.
     *
     * <p>
     * 	Proxy for {@link BrowserService}
     * </p>
     *
     * @return all {@link Browser} ordered by their name.
     */
    public List<Browser> getAllBrowser() {

        return browserService.findAll().sort(false, { it.name.toLowerCase() })
    }

    /**
     * Fetches all {@link Page}s from Database.
     *
     * @return all {@link Page} ordered by their name.
     */
    public List<Page> getAllPages() {
        return Page.list().sort(false, { it.name.toLowerCase() })
    }

    /**
     * Fetches all {@link JobGroup}s from Database.
     *
     * <p>
     * 	Proxy for {@link JobGroupDaoService}
     * </p>
     *
     * @return all {@link JobGroup} ordered by their name.
     *
     */
    public List<JobGroup> getAllJobGroups() {
        return jobGroupDaoService.findAll().sort(false, { it.name.toLowerCase() })
    }

    /**
     * Returns a list of {@linkHighchartGraph}s for the highchart lib.
     *
     * @param startDate selected start date
     * @param endDate selected end date
     * @param interval selected interval
     * @param aggregators selected AggregatorTypes
     * @param queryParams
     * 		Query params for querying {@link EventResult}s.
     * @return List of {@linkHighchartGraph}s
     *
     * @throws IllegalArgumentException if an argument is not found or supported.
     *
     * @todo TODO mze-2013-09-12: Suggest to move to a generic HighchartFactoryService.
     */
    public OsmRickshawChart getEventResultDashboardHighchartGraphs(
            Date startDate, Date endDate, Integer interval, List<AggregatorType> aggregators, ErQueryParams queryParams) {

        Map<String, Number> gtValues = [:]
        Map<String, Number> ltValues = [:]
        aggregators.each { AggregatorType aggregator ->
            String associatedEventResultAttributeName = resultCsiAggregationService.getEventResultAttributeNameFromMeasurand(aggregator)
            if (aggregator.measurandGroup == MeasurandGroup.LOAD_TIMES) {
                if (queryParams.minLoadTimeInMillisecs) {
                    gtValues[associatedEventResultAttributeName] = queryParams.minLoadTimeInMillisecs
                }
                if (queryParams.maxLoadTimeInMillisecs) {
                    ltValues[associatedEventResultAttributeName] = queryParams.maxLoadTimeInMillisecs
                }
            } else if (aggregator.measurandGroup == MeasurandGroup.REQUEST_COUNTS) {
                if (queryParams.minRequestCount) {
                    gtValues[associatedEventResultAttributeName] = queryParams.minRequestCount
                }
                if (queryParams.maxRequestCount) {
                    ltValues[associatedEventResultAttributeName] = queryParams.maxRequestCount
                }
            } else if (aggregator.measurandGroup == MeasurandGroup.REQUEST_SIZES) {
                if (queryParams.minRequestSizeInBytes) {
                    gtValues[associatedEventResultAttributeName] = queryParams.minRequestSizeInBytes
                }
                if (queryParams.maxRequestSizeInBytes) {
                    ltValues[associatedEventResultAttributeName] = queryParams.maxRequestSizeInBytes
                }
            }
        }

        Collection<EventResult> eventResults
        performanceLoggingService.logExecutionTime(LogLevel.DEBUG, 'getting event-results - getEventResultDashboardHighchartGraphs - getLimitedMedianEventResultsBy', 1) {
            eventResults = eventResultDaoService.getLimitedMedianEventResultsBy(
                    startDate,
                    endDate,
                    [CachedView.CACHED, CachedView.UNCACHED] as Set,
                    queryParams,
                    [:],
                    new CriteriaSorting(sortingActive: false)
            )
        }
        return calculateResultMap(eventResults, aggregators, interval, gtValues, ltValues)
    }

    /**
     * <p>
     * Transforms given eventResults to a list of {@link de.iteratec.osm.report.chart.OsmChartGraph}s respective given measurands (aggregators) and interval.
     * If interval is not {@link CsiAggregationInterval#RAW} the values for the measurands will be aggregated respective interval.
     * </p>
     *
     * @param eventResults
     * @param aggregators
     * @param interval
     * @return
     */
    private OsmRickshawChart calculateResultMap(Collection<EventResult> eventResults, List<AggregatorType> aggregators, Integer interval, Map<String, Number> gtBoundary, Map<String, Number> ltBoundary) {
        Map<String, List<OsmChartPoint>> calculatedResultMap
        performanceLoggingService.logExecutionTime(LogLevel.DEBUG, 'getting result-map', 1) {
            if (interval == CsiAggregationInterval.RAW) {
                calculatedResultMap = calculateResultMapForRawData(aggregators, eventResults, gtBoundary, ltBoundary)
            } else {
                calculatedResultMap = calculateResultMapForAggregatedData(aggregators, eventResults, interval, gtBoundary, ltBoundary)
            }
        }
        List<OsmChartGraph> graphs = []
        OsmRickshawChart chart
        performanceLoggingService.logExecutionTime(LogLevel.DEBUG, 'set speaking graph labels and sorting', 1) {
            performanceLoggingService.logExecutionTime(LogLevel.DEBUG, 'set speaking graph labels', 2) {
                graphs = setSpeakingGraphLabelsAndSort(calculatedResultMap)
            }
            performanceLoggingService.logExecutionTime(LogLevel.DEBUG, 'sorting', 2) {
                chart = osmChartProcessingService.summarizeEventResultGraphs(graphs)
            }
        }
        return chart
    }

    private Map<String, List<OsmChartPoint>> calculateResultMapForRawData(List<AggregatorType> aggregators, Collection<EventResult> eventResults, Map<String, Number> gtBoundary, Map<String, Number> ltBoundary) {

        Map<String, List<OsmChartPoint>> highchartPointsForEachGraph = [:].withDefault { [] }

        aggregators.each { AggregatorType aggregator ->

            CachedView aggregatorTypeCachedView = resultCsiAggregationService.getAggregatorTypeCachedViewType(aggregator)

            eventResults.each { EventResult eventResult ->

                String connectivity = eventResult.connectivityProfile != null ? eventResult.connectivityProfile.name : eventResult.customConnectivityName

                URL testsDetailsURL = eventResult.testDetailsWaterfallURL ?: this.buildTestsDetailsURL(eventResult)

                // Get WPT event result info to build the WPT url dynamically
                String serverBaseUrl = eventResult.jobResult.wptServerBaseurl
                String testId = eventResult.jobResult.testId
                Integer numberOfWptRun = eventResult.numberOfWptRun
                CachedView cachedView = eventResult.cachedView
                Integer oneBaseStepIndexInJourney = eventResult.oneBasedStepIndexInJourney
                WptEventResultInfo chartPointWptInfo = new WptEventResultInfo(
                        serverBaseUrl: serverBaseUrl,
                        testId: testId,
                        numberOfWptRun: numberOfWptRun,
                        cachedView: cachedView,
                        oneBaseStepIndexInJourney: oneBaseStepIndexInJourney
                )

                if (isCachedViewEqualToAggregatorTypesView(eventResult, aggregatorTypeCachedView)) {
                    Double value = resultCsiAggregationService.getEventResultPropertyForCalculation(aggregator, eventResult)
                    if (value != null && isInBounds(eventResult, aggregator, gtBoundary, ltBoundary)) {
                        String tag = "${eventResult.jobGroupId};${eventResult.measuredEventId};${eventResult.pageId};${eventResult.browserId};${eventResult.locationId}"
                        String graphLabel = "${aggregator.name}${UNIQUE_STRING_DELIMITTER}${tag}${UNIQUE_STRING_DELIMITTER}${connectivity}"
                        OsmChartPoint chartPoint = new OsmChartPoint(
                                time: eventResult.getJobResultDate().getTime(),
                                csiAggregation: value,
                                countOfAggregatedResults: 1,
                                sourceURL: testsDetailsURL,
                                testingAgent: eventResult.testAgent,
                                chartPointWptInfo: chartPointWptInfo
                        )
                        // customer satisfaction can be 0.
                        if (chartPoint.isValid() || (aggregator.measurandGroup == MeasurandGroup.PERCENTAGES && chartPoint.time >= 0 && chartPoint.csiAggregation != null))
                            highchartPointsForEachGraph[graphLabel].add(chartPoint)
                    }
                }
            }
        }
        return highchartPointsForEachGraph
    }

    private boolean isInBounds(EventResult eventResult, AggregatorType aggregatorType, Map<String, Number> gtBoundary, Map<String, Number> ltBoundary) {
        String name = aggregatorType.getName().replace("Uncached", "").replace("Cached", "") //TODO make this pretty
        Number lt = gtBoundary[name]
        Number gt = ltBoundary[name]

        boolean inBound = true
        if (lt) inBound &= eventResult."$name" > lt
        if (gt) inBound &= eventResult."$name" < gt

        return inBound
    }

    private Map<String, List<OsmChartPoint>> calculateResultMapForAggregatedData(List<AggregatorType> aggregators, Collection<EventResult> eventResults, Integer interval, Map<String, Number> gtBoundary, Map<String, Number> ltBoundary) {

        Map<String, List<OsmChartPoint>> highchartPointsForEachGraph = [:].withDefault { [] }
        Map<String, List<Double>> eventResultsToAggregate = [:].withDefault { [] }

        performanceLoggingService.logExecutionTime(LogLevel.DEBUG, 'put results to map for aggregation', 2) {
            eventResults.each { EventResult eventResult ->
                aggregators.each { AggregatorType aggregator ->
                    if (isCachedViewEqualToAggregatorTypesView(eventResult, resultCsiAggregationService.getAggregatorTypeCachedViewType(aggregator))) {
                        Double value = resultCsiAggregationService.getEventResultPropertyForCalculation(aggregator, eventResult)
                        if (value != null && isInBounds(eventResult, aggregator, gtBoundary, ltBoundary)) {
                            String connectivity = eventResult.connectivityProfile != null ? eventResult.connectivityProfile.name : eventResult.customConnectivityName
                            Long millisStartOfInterval = csiAggregationUtilService.resetToStartOfActualInterval(new DateTime(eventResult.jobResultDate), interval).getMillis()
                            String tag = "${eventResult.jobGroupId};${eventResult.measuredEventId};${eventResult.pageId};${eventResult.browserId};${eventResult.locationId}"
                            eventResultsToAggregate["${aggregator.name}${UNIQUE_STRING_DELIMITTER}${tag}${UNIQUE_STRING_DELIMITTER}${millisStartOfInterval}${UNIQUE_STRING_DELIMITTER}${connectivity}"] << value
                        }
                    }
                }
            }
        }

        Map<String, AggregatorType> aggregatorTypeMap
        performanceLoggingService.logExecutionTime(LogLevel.DEBUG, 'get aggr-type-lookup-map', 2) {
            aggregatorTypeMap = AggregatorType.list().collectEntries { AggregatorType eachAggregatorType ->
                [
                        eachAggregatorType.name,
                        eachAggregatorType
                ]
            }
        }

        performanceLoggingService.logExecutionTime(LogLevel.DEBUG, 'iterate over aggregation-map', 2) {
            URL testsDetailsURL
            Double sum = 0
            Integer countValues = 0
            eventResultsToAggregate.each { key, value ->

                List tokenized
                Long millisStartOfInterval
                AggregatorType aggregator

                performanceLoggingService.logExecutionTime(LogLevel.TRACE, 'tokenize', 3) {
                    performanceLoggingService.logExecutionTime(LogLevel.TRACE, 'inner tokenize', 4) {
                        tokenized = key.split(UNIQUE_STRING_DELIMITTER)
                    }
                    performanceLoggingService.logExecutionTime(LogLevel.TRACE, 'Long.valueOf()', 4) {
                        millisStartOfInterval = Long.valueOf(tokenized[2])
                    }
                    performanceLoggingService.logExecutionTime(LogLevel.TRACE, 'getting Aggregator from db', 4) {
                        aggregator = aggregatorTypeMap[tokenized[0]]
                    }
                }
                performanceLoggingService.logExecutionTime(LogLevel.TRACE, 'buildTestsDetailsURL', 3) {
                    String[] tagSegments = ((String) tokenized[1]).split(";")
                    testsDetailsURL = buildTestsDetailsURL(tagSegments[0], tagSegments[1], tagSegments[2], tagSegments[3], tagSegments[4], aggregator, millisStartOfInterval, interval, value.size())
                }

                performanceLoggingService.logExecutionTime(LogLevel.TRACE, 'calculate value and create OsmChartPoint', 3) {

                    String graphLabel = "${tokenized[0]}${UNIQUE_STRING_DELIMITTER}${tokenized[1]}${UNIQUE_STRING_DELIMITTER}${tokenized[3]}"
                    countValues = value.size()
                    if (countValues > 0) {
                        sum = 0
                        value.each { singleValue -> sum += singleValue }
                        OsmChartPoint chartPoint = new OsmChartPoint(time: millisStartOfInterval, csiAggregation: sum / countValues, countOfAggregatedResults: countValues, sourceURL: testsDetailsURL, testingAgent: null)
                        if (chartPoint.isValid())
                            highchartPointsForEachGraph[graphLabel] << chartPoint
                    }
                }
            }
        }
        return highchartPointsForEachGraph.sort()
    }

    private List<OsmChartGraph> setSpeakingGraphLabelsAndSort(Map<String, List<OsmChartPoint>> highchartPointsForEachGraphOrigin) {

        String firstViewEnding = i18nService.msg("de.iteratec.isr.measurand.endingCached", "Cached", null)
        String repeatedViewEnding = i18nService.msg("de.iteratec.isr.measurand.endingUncached", "Uncached", null)

        List<OsmChartGraph> graphs = []

        Map<Serializable, JobGroup> jobGroupMap = [:]
        Map<Serializable, MeasuredEvent> measuredEventMap = [:]
        Map<Serializable, Location> locationMap = [:]
        def aggregatorTypes = [:]
        highchartPointsForEachGraphOrigin.each { graphLabel, highChartPoints ->
            performanceLoggingService.logExecutionTime(LogLevel.DEBUG, 'TEST', 1) {
                List<String> tokenizedGraphLabel = graphLabel.split(UNIQUE_STRING_DELIMITTER)
                if (tokenizedGraphLabel.size() != 3) {
                    throw new IllegalArgumentException("The graph-label should consist of three parts: AggregatorType and tag. This is no correct graph-label: ${graphLabel}")
                }
                def aggregatorName = tokenizedGraphLabel[0]
                performanceLoggingService.logExecutionTime(LogLevel.DEBUG, 'Aggregator', 1) {
                    if (!aggregatorTypes[aggregatorName]) {
                        aggregatorTypes[aggregatorName] = AggregatorType.findByName(aggregatorName)
                    }
                }
                AggregatorType aggregator = aggregatorTypes[aggregatorName]
                if (!aggregator) {
                    throw new IllegalArgumentException("First part of graph-label should be the name of AggregatorType. This is no correct aggregator-name: ${tokenizedGraphLabel[0]}")
                }
                String connectivity = tokenizedGraphLabel[2]
                if (!connectivity) {
                    throw new IllegalArgumentException("Thrid part of graph-label should be the the connectivity. This is no correct connectivity: ${tokenizedGraphLabel[2]}")
                }

                String measurand = i18nService.msg("de.iteratec.isr.measurand.${tokenizedGraphLabel[0].replace('Uncached', '').replace('Cached', '')}", tokenizedGraphLabel[0], null)

                if (tokenizedGraphLabel[0].endsWith("Uncached")) {
                    if (!repeatedViewEnding.isEmpty()) {
                        measurand = repeatedViewEnding + " " + measurand
                    }
                } else {
                    if (!firstViewEnding.isEmpty()) {
                        measurand = firstViewEnding + " " + measurand
                    }
                }

                String[] tagSegments = tokenizedGraphLabel[1].split(";")
                if (tagSegments) {
                    Long jobGroupId = Long.valueOf(tagSegments[0])
                    JobGroup group = jobGroupMap[jobGroupId] ?: JobGroup.get(jobGroupId)
                    Long eventId = Long.valueOf(tagSegments[1])
                    MeasuredEvent measuredEvent = measuredEventMap[eventId] ?: MeasuredEvent.get(eventId)
                    Long locationId = Long.valueOf(tagSegments[4])
                    Location location = locationMap[locationId] ?: Location.get(locationId)

                    if (group && measuredEvent && location) {
                        String newGraphLabel = "${measurand}${HIGHCHART_LEGEND_DELIMITTER}${group.name}${HIGHCHART_LEGEND_DELIMITTER}" +
                                "${measuredEvent.name}${HIGHCHART_LEGEND_DELIMITTER}${location.uniqueIdentifierForServer == null ? location.location : location.uniqueIdentifierForServer}" +
                                "${HIGHCHART_LEGEND_DELIMITTER}${connectivity}"
                        graphs.add(new OsmChartGraph(
                                label: newGraphLabel,
                                measurandGroup: aggregator.measurandGroup,
                                points: highChartPoints))
                    } else {
                        graphs.add(new OsmChartGraph(
                                label: "${measurand}${HIGHCHART_LEGEND_DELIMITTER}${tokenizedGraphLabel[1]}",
                                measurandGroup: aggregator.measurandGroup,
                                points: highChartPoints))
                    }
                } else {
                    graphs.add(new OsmChartGraph(
                            label: "${measurand}${HIGHCHART_LEGEND_DELIMITTER}${tokenizedGraphLabel[1]}",
                            measurandGroup: aggregator.measurandGroup,
                            points: highChartPoints))
                }
            }
        }
        graphs.each { graph ->
            graph.points.sort { it.time }
        }
        return graphs.sort()
    }

    /**
     * <p>
     * Builds up an URL where details to the specified {@link CsiAggregation}
     * are available if possible.
     * </p>
     *
     * @param mv
     *         The measured value for which an URL should be build
     *         not <code>null</code>.
     * @return The created URL or <code>null</code> if not possible to
     *         build up an URL.
     */
    public URL tryToBuildTestsDetailsURL(CsiAggregation mv) {
        URL result = null
        List<Long> eventResultIds = mv.underlyingEventResultsByWptDocCompleteAsList

        if (!eventResultIds.isEmpty()) {
            String testsDetailsURLAsString = grailsLinkGenerator.link([
                    'controller': 'highchartPointDetails',
                    'action'    : 'listAggregatedResults',
                    'absolute'  : true,
                    'params'    : [
                            'csiAggregationId'                       : String.valueOf(mv.id),
                            'lastKnownCountOfAggregatedResultsOrNull': String.valueOf(eventResultIds.size())
                    ]
            ])
            result = testsDetailsURLAsString ? new URL(testsDetailsURLAsString) : null
        }

        return result
    }

    /**
     * <p>
     * Builds up an URL to display details of the given {@link EventResult}s.
     * </p>
     *
     * @param results
     *         The {@link EventResult}s which should be displayed via the returned URL.
     *         not <code>null</code>.
     * @return The created URL or <code>null</code> if not possible to
     *         build up an URL.
     */
    public URL buildTestsDetailsURL(EventResult result) {
        URL resultUrl = null

        if (result) {
            String testsDetailsURLAsString = grailsLinkGenerator.link([
                    'controller': 'highchartPointDetails',
                    'action'    : 'redirectToWptServerDetailPage',
                    'absolute'  : true,
                    'params'    : [
                            'eventResultId': String.valueOf(result.id)
                    ]
            ])
            resultUrl = testsDetailsURLAsString ? new URL(testsDetailsURLAsString) : null
        }

        return resultUrl
    }

    public URL buildTestsDetailsURL(String jobGroupId, String measuredEventId, String pageId, String browserId, String locationId, AggregatorType aggregatorType, Long millisFrom, Integer intervalInMinutes, Integer lastKnownCountOfAggregatedResults) {
        URL result = null


        if (jobGroupId && measuredEventId && pageId && browserId && locationId && aggregatorType) {

            String testsDetailsURLAsString = grailsLinkGenerator.link([
                    'controller': 'highchartPointDetails',
                    'action'    : 'listAggregatedResultsByQueryParams',
                    'absolute'  : true,
                    'params'    : [
                            'from'                                   : String.valueOf(millisFrom),
                            'to'                                     : String.valueOf(millisFrom + intervalInMinutes * 60 * 1000),
                            'jobGroupId'                             : jobGroupId,
                            'measuredEventId'                        : measuredEventId,
                            'pageId'                                 : pageId,
                            'browserId'                              : browserId,
                            'locationId'                             : locationId,
                            'aggregatorTypeNameOrNull'               : aggregatorType.isCachedCriteriaApplicable() ? aggregatorType.getName() : '',
                            'lastKnownCountOfAggregatedResultsOrNull': String.valueOf(lastKnownCountOfAggregatedResults)
                    ]
            ])
            result = testsDetailsURLAsString ? new URL(testsDetailsURLAsString) : null

        }

        return result
    }

    private TreeMap addToPointMap(Map resultMap, String resultName, Long timeStamp, Integer value) {
        Map pointMap = resultMap.get(resultName)
        if (pointMap == null) {
            pointMap = new TreeMap<Long, Double>()
        }
        pointMap.put(timeStamp, value)
        return pointMap
    }

    private boolean isEventResultMachingQueryParams(EventResult eventResult, MvQueryParams queryParams) {
        boolean eins = (queryParams.getMeasuredEventIds().contains(eventResult.measuredEvent.id) || queryParams.getMeasuredEventIds().isEmpty())
        boolean zwei = (queryParams.getPageIds().contains(eventResult.measuredEvent.testedPage.id) || queryParams.getPageIds().isEmpty())
        return eins && zwei
    }

    private boolean isCachedViewEqualToAggregatorTypesView(EventResult eventResult, CachedView aggregatorTypeCachedView) {
        return eventResult.cachedView.equals(aggregatorTypeCachedView)
    }
}
