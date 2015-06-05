<div id="CreateUserspecifiedDashboardModal" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="CreateUserspecifiedDashboardModalLabel">
<div class="modal-header">
    <button type="button" class="close" data-dismiss="modal" ria-hidden="true">×</button>
    <h3 id="CreateUserspecifiedDashboardModalLabel"><g:message code="de.iteratec.isocsi.dashBoardControllers.custom.title" default="To save as custom dashboard, please enter additional information!"/></h3>
  </div>
  <div class="modal-body">
    <div class="alert alert-error renderInvisible" id="validateDashboardNameErrorDiv"></div>
    <div class="control-group">
      <label class="control-label" for="dashboardNameFromModal">${message(code: 'de.iteratec.isocsi.dashBoardControllers.custom.dashboardName.label', default: 'Dashboard name')}</label>
      <div class="controls">
      <div id="spinner-position"></div>
        <input type="text" class="span3" name="dashboardNameFromModal" id="dashboardNameFromModal" placeholder="${message(code: 'de.iteratec.isocsi.dashBoardControllers.custom.dashboardName.label', default: 'Dashboard name')}">
      </div>
    </div>
    <div class="control-group">
      <div class="controls">
        <label class="checkbox" for="publiclyVisibleFromModal">
          <input type="checkbox" name="publiclyVisibleFromModal" id="publiclyVisibleFromModal" >
          ${message(code: 'de.iteratec.isocsi.dashBoardControllers.custom.publiclyVisible.label', default: 'Everybody is entitled to view this custom dashboard.')}
        </label>
      </div>
    </div>
  </div>
  <div class="modal-footer">
    <g:form>
      <button class="btn" data-dismiss="modal"><g:message code="default.button.cancel.label" default="Cancel"/></button>
      <g:hiddenField name="id" value="${item ? item.id : params.id}" />
      <g:hiddenField name="_method" value="POST" />
      <span class="button">
        <g:actionSubmit class="btn btn-primary" action="delete" value="${message(code: 'de.iteratec.ism.ui.labels.save', default: 'Save')}" onClick="saveCustomDashboard();return false;"/>
      </span>
    </g:form>
    
  </div>
</div>
<script>
    function saveCustomDashboard(){
      $( "#saveDashboardSuccessDiv" ).hide();
      $( "#saveDashboardErrorDiv" ).hide();
      $( "#validateDashboardNameErrorDiv" ).hide();
      //feldeingaben validieren
      if (document.getElementById("dashboardNameFromModal").value.trim() !== "") {
        //feldeingaben validieren gegen ajaxvar
		    var opts = {
		        lines: 15, // The number of lines to draw
		        length: 20, // The length of each line
		        width: 10, // The line thickness
		        radius: 30, // The radius of the inner circle
		        corners: 1, // Corner roundness (0..1)
		        rotate: 0, // The rotation offset
		        direction: 1, // 1: clockwise, -1: counterclockwise
		        color: '#000', // #rgb or #rrggbb or array of colors
		        speed: 1, // Rounds per second
		        trail: 60, // Afterglow percentage
		        shadow: true, // Whether to render a shadow
		        hwaccel: false, // Whether to use hardware acceleration
		        className: 'spinner', // The CSS class to assign to the spinner
		        zIndex: 2e9, // The z-index (defaults to 2000000000)
		        top: '50%', // Top position relative to parent in px
		        left: '50%' // Left position relative to parent in px
		    };
        var spinner = new Spinner(opts).spin(document.getElementById('spinner-position'));
		    jQuery.ajax({
		      type: 'POST', 
		      url: '${createLink(action: 'validateDashboardName', absolute: true)}',
		      data: { proposedDashboardName: document.getElementById("dashboardNameFromModal").value.trim(), noprepend: true },
		      statusCode: {
			        200: function (response) {
				        //formulardaten sammeln
				        dashBoardParamsFormValues = {};
								jQuery('#dashBoardParamsForm').serializeArray().map(function(item) {
								    if ( dashBoardParamsFormValues[item.name] ) {
								        if ( typeof(dashBoardParamsFormValues[item.name]) === "string" ) {
								          dashBoardParamsFormValues[item.name] = [dashBoardParamsFormValues[item.name]];
								        }
								        dashBoardParamsFormValues[item.name].push(item.value);
								    } else {
								      dashBoardParamsFormValues[item.name] = item.value;
								    }
								});

								var params = $('#dashBoardParamsForm').serializeArray();
						    var dashBoardParamsFormValues = '{';
						    for(var ix in params)
						    {
						        var row = params[ix];
						        dashBoardParamsFormValues += '"' + row.name + '":"' + row.value + '",';
						    }
						    var end =dashBoardParamsFormValues.length - 1;
						    dashBoardParamsFormValues = dashBoardParamsFormValues.substr(0, end);
						    dashBoardParamsFormValues += '}';
                //ergebnis annehmen und erfolg oder fehler melden
				        jQuery.ajax({
				          type: 'POST', 
				          url: '${createLink(action: 'validateAndSaveDashboardValues', absolute: true)}',
				          data: { values: dashBoardParamsFormValues, dashboardName: document.getElementById("dashboardNameFromModal").value, publiclyVisible: document.getElementById("publiclyVisibleFromModal").checked },
				          statusCode: {
				              200: function (response) {
					              window.scrollTo(0, 0);
					              $( "#saveDashboardSuccessDiv" ).show();
				                return false;
				              },
                      400: function (response) {
                        window.scrollTo(0, 0);                       
                        $( "#saveDashboardErrorDiv" ).show();
                        var re = /(.*rkrkrk\[)(.*)(\]rkrkrk.*)/;
                        var newtext = response.responseText.replace(re, "$2");
                        newtext = newtext.replace(",", "<br />"); 
                        document.all.saveDashboardErrorDiv.innerHTML = newtext;
                        return false;
                      },
                      500: function (response) {
                        window.scrollTo(0, 0);
                        $( "#saveDashboardErrorDiv" ).show();
                        document.all.saveDashboardErrorDiv.innerHTML = "${message(code: 'de.iteratec.isocsi.dashBoardControllers.custom.dashboardName.error.save', default: 'An error occured while saving - please try again!')}";
                        return false;
                      }
				          },
				          success : function(data) {
                    $('#CreateUserspecifiedDashboardModal').modal('hide');
				            spinner.stop();
                    return false;
				          },
				          error: function(result) {
                    $('#CreateUserspecifiedDashboardModal').modal('hide');
				            spinner.stop();
				            return false;
				          }
				        });
			        },
			        302: function (response) {
			          $( "#validateDashboardNameErrorDiv" ).show();
			          document.all.validateDashboardNameErrorDiv.innerHTML = "${message(code: 'de.iteratec.isocsi.dashBoardControllers.custom.dashboardName.error.uniqueness', default: 'Please enter a unique name.')}";
			          return false;
			        }
			    },
		      success : function(data) {
		      },
		      error: function(result) {
		        spinner.stop();
		        return false;
		      }
		    });
      } else {
        $( "#validateDashboardNameErrorDiv" ).show();
        document.all.validateDashboardNameErrorDiv.innerHTML = "${message(code: 'de.iteratec.isocsi.dashBoardControllers.custom.dashboardName.error.missing', default: 'Please enter a non-empty name.')}";
        return false;
      }
    }
</script>