package de.iteratec.osm.result

import de.iteratec.osm.batch.Activity
import de.iteratec.osm.batch.BatchActivity
import de.iteratec.osm.batch.BatchActivityService
import de.iteratec.osm.batch.BatchActivityUpdater
import grails.transaction.Transactional
import org.hibernate.type.StandardBasicTypes
import org.joda.time.DateTime
import org.joda.time.Days

@Transactional
class ResultSelectionInformationService {
    BatchActivityService batchActivityService

    def createLatestResultSelectionInformation(boolean createBatchActivity = true) {
        String jobName = "Nightly aggregation of result selection information"
        log.info "begin with $jobName"
        if(!batchActivityService.runningBatch(BatchActivity.class, jobName, Activity.CREATE)) {
            def lastProcessedDate = ResultSelectionInformation.createCriteria().get {
                projections {
                    max "jobResultDate"
                }
            } as Date
            lastProcessedDate = lastProcessedDate ? lastProcessedDate.getTime() : 0
            def lastProcessedDateStartDay = new DateTime(lastProcessedDate).withTimeAtStartOfDay()
            def daysToProcess = Days.daysBetween(lastProcessedDateStartDay, DateTime.now()).days
            BatchActivityUpdater batchActivityUpdater = batchActivityService.getActiveBatchActivity(ResultSelectionInformation.class, Activity.CREATE, jobName, 1, createBatchActivity)
            batchActivityUpdater.beginNewStage("Create new ResultSelectionInformation from EventResults", daysToProcess)
            // delete last day and recompute
            ResultSelectionInformation.findAllByJobResultDateGreaterThanEquals(lastProcessedDateStartDay.toDate()).each{ it.delete() }
            for (startDayOffset in 0..daysToProcess) {
                def startDay = lastProcessedDateStartDay.plusDays(startDayOffset)
                def endDay = startDay.plusDays(1)
                batchActivityUpdater.update()
                def groupedResults = EventResult.createCriteria().list {
                    between('jobResultDate', startDay.toDate(), endDay.toDate())
                    projections {
                        sqlGroupProjection('CAST(job_result_date as DATE) as jr_date', 'CAST(job_result_date as DATE)', ["jr_date"], [StandardBasicTypes.DATE])
                        groupProperty('page')
                        groupProperty('measuredEvent')
                        groupProperty('jobGroup')
                        groupProperty('location')
                        groupProperty('browser')
                        groupProperty('connectivityProfile')
                        groupProperty('customConnectivityName')
                        groupProperty('noTrafficShapingAtAll')
                    }
                }
                groupedResults.each { entry ->
                    def map = [
                        jobResultDate: entry[0],
                        page: entry[1],
                        measuredEvent: entry[2],
                        jobGroup: entry[3],
                        location: entry[4],
                        browser: entry[5],
                        connectivityProfile: entry[6],
                        customConnectivityName: entry[7],
                        noTrafficShapingAtAll: entry[8]
                    ]
                    def created = new ResultSelectionInformation(map).save()
                    if (!created) {
                        batchActivityUpdater.addFailures("Couldn't create ResultSelectionInformation ${created.errors.allErrors.join(", ")}")
                    }
                }
            }
            batchActivityUpdater.done()
        }
    }
}
