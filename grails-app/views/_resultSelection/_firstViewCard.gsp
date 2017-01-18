<div class="card">
    <h2 for="selectAggregatorUncachedHtmlId"><g:message
            code="de.iteratec.isr.wptrd.labels.filterFirstView"
            default="First View"/></h2>
    <g:if test="${selectedAggrGroupValuesUnCached.size() == 0}"><g:set
            var="selectedAggrGroupValuesUnCached"
            value="${['docCompleteTimeInMillisecsUncached']}"/></g:if>
    <iteratec:optGroupedSelect dataMap="${aggrGroupValuesUnCached}"
                               id="selectAggregatorUncachedHtmlId"
                               class="form-control long-select"
                               name="selectedAggrGroupValuesUnCached"
                               optionKey="value" optionValue="value"
                               multiple="true"
                               value="${selectedAggrGroupValuesUnCached}"/>
</div>
