<!-- 
This is the standard dialog that initiates the delete action.
-->
<div id="DeleteModal" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="DeleteModalLabel"
     aria-hidden="true" onshow="POSTLOADED.setDeleteConfirmationInformations('${controllerLink}');">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" ria-hidden="true">×</button>

        <h3 id="DeleteModalLabel"><g:message code="default.button.delete.confirm.title" default="Delete Item"/></h3>
    </div>

    <div class="modal-body">
        <p><g:message code="default.button.delete.confirm.messageWithArgument" args="[entityName]"
                      default="Do you really want to delete this item?"/></p>
        <g:if test="${additionalInformationDelete}">
            ${raw(additionalInformationDelete)}
        </g:if>

        <div id="spinner-position"></div>
    </div>

    <div class="modal-footer">
        <g:form>
            <button class="btn" data-dismiss="modal" aria-hidden="true"><g:message code="default.button.cancel.label"
                                                                                   default="Cancel"/></button>
            <g:hiddenField name="id" value="${item ? item.id : params.id}"/>
            <g:hiddenField name="_method" value="DELETE"/>
            <span class="button">
                <g:actionSubmit class="btn btn-danger" action="delete"
                                value="${message(code: 'default.button.delete.label', default: 'Delete')}"/>
            </span>
        </g:form>

    </div>
</div>