<div class="card">
    <h2><g:message
            code="de.iteratec.isr.wptrd.labels.filterRepeatedView"
            default="Repeated View"/></h2>
    <iteratec:optGroupedSelect id="selectAggregatorCachedHtmlId"
                               dataMap="${aggrGroupValuesCached}"
                               multiple="true" id="selectAggregatorCachedHtmlId"
                               class="form-control long-select"
                               name="selectedAggrGroupValuesCached" optionKey="value"
                               optionValue="value"
                               value="${selectedAggrGroupValuesCached}"/>
</div>
