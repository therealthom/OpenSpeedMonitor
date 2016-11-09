package de.iteratec.osm.measurement.environment

import de.iteratec.osm.csi.BrowserConnectivityWeight
import de.iteratec.osm.report.chart.CsiAggregation
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.util.I18nService
import grails.converters.JSON
import grails.gorm.DetachedCriteria
import org.joda.time.DateTime
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus

import javax.servlet.http.HttpServletResponse

import static org.springframework.http.HttpStatus.*
//TODO: This controller was generated due to a scaffolding bug (https://github.com/grails3-plugins/scaffolding/issues/24). The dynamically scaffolded controllers cannot handle database exceptions

class BrowserController {
    I18nService i18nService
    static scaffold = Browser
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index() {
        
    }

    def show(Browser browser) {
        int browserAliaseCount = BrowserAlias.countByBrowser(browser)
        int browserConnectivityWeightCount = BrowserConnectivityWeight.countByBrowser(browser)
        int csiAggregationCount = CsiAggregation.countByBrowser(browser)
        int eventResultCount = EventResult.countByBrowser(browser)

        int jobResultCount = EventResult.createCriteria().get{
            eq('browser', browser)
            projections {
                countDistinct('jobResult')
            }
        }
        String additionalInfoWarning = i18nService.msg("de.iteratec.osm.browser.delete.additionalinformations",
                "The following Elements will also be deleted: <br> BrowserAliases: ${browserAliaseCount} <br>BrowserConnectivityWeights: ${browserConnectivityWeightCount}<br>CsiAggregations: ${csiAggregationCount}<br>EventResults: ${eventResultCount}<br>JobResults: ${jobResultCount}",
                [browserAliaseCount,browserConnectivityWeightCount,csiAggregationCount,eventResultCount,jobResultCount])
        ["browser":browser, "additionalInformationDelete":additionalInfoWarning]
    }

    def create() {
        respond new Browser(params)
    }

    def save(Browser browser) {
        if (browser == null) {
            
            notFound()
            return
        }

        if (browser.hasErrors()) {

            respond browser.errors, view:'create'
            return
        }

        browser.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'browser.label', default: 'Browser'), browser.id])
                redirect browser
            }
            '*' { respond browser, [status: CREATED] }
        }
    }

    def edit(Browser browser) {
        respond browser
    }

    def update(Browser browser) {
        if (browser == null) {

            notFound()
            return
        }

        if (browser.hasErrors()) {

            respond browser.errors, view:'edit'
            return
        }

        browser.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'browser.label', default: 'Browser'), browser.id])
                redirect browser
            }
            '*'{ respond browser, [status: OK] }
        }
    }

    def delete(Browser browser) {
        if (browser == null) {
            notFound()
            return
        }
        try {
            def criteria = new DetachedCriteria(CsiAggregation).build {
                eq 'browser', browser
            }
            println new DateTime()
            int total = criteria.deleteAll()
            println "${total} csiAggregations deleted"
            println new DateTime()
            criteria = new DetachedCriteria(EventResult).build {
                eq 'browser', browser
            }
            println new DateTime()
            total = criteria.deleteAll()
            println "${total} eventResults deleted"
            println new DateTime()
            browser.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'browser.label', default: 'Browser'), params.id])
            redirect(action: "index")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'browser.label', default: 'Browser'), params.id])
            redirect(action: "show", id: params.id)
        }
    }
    def updateTable(){
        params.order = params.order ? params.order : "desc"
        params.sort = params.sort ? params.sort : "name"
        params.sort = params.sort == "browserAliases"? "name" : params.sort
        def paramsForCount = Boolean.valueOf(params.limitResults) ? [max:1000]:[:]
        params.max = params.max as Integer
        params.offset = params.offset as Integer
        List<Browser> result
        int count
        result = Browser.createCriteria().list(params) {
            if(params.filter)
                or{
                    ilike("name","%"+params.filter+"%")
                    if(params.filter.isNumber())eq("weight",Double.valueOf(params.filter))
                }
        }
        count = Browser.createCriteria().list(paramsForCount) {
            if(params.filter)
                or{
                    ilike("name","%"+params.filter+"%")
                    if(params.filter.isNumber())eq("weight",Double.valueOf(params.filter))
                }
        }.size()
        String templateAsPlainText = g.render(
                template: 'browserTable',
                model: [browsers: result]
        )
        def jsonResult = [table:templateAsPlainText, count:count]as JSON
        sendSimpleResponseAsStream(response, HttpStatus.OK, jsonResult.toString(false))
    }


    private void sendSimpleResponseAsStream(HttpServletResponse response, HttpStatus httpStatus, String message) {

        response.setContentType('text/plain;charset=UTF-8')
        response.status=httpStatus.value()

        Writer textOut = new OutputStreamWriter(response.getOutputStream())
        textOut.write(message)
        textOut.flush()
        response.getOutputStream().flush()

    }
    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'browser.label', default: 'Browser'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}
