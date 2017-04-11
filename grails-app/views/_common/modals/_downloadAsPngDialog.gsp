<!-- Modal dialog -->
<div id="downloadAsPngModal" class="modal" tabindex="-1" role="dialog" aria-labelledby="ModalLabel"
     aria-hidden="true">
    <div id="downloadAsPngDialog" class="modal-dialog modal-xl">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                <h4 id="ModalLabel" class="modal-title"><g:message code="de.iteratec.osm.pngDownloader.modal.title"
                                                                   default="Download as PNG"/></h4>
            </div>
            <div id="download-chart-container" style="height: 400px"></div>
            <div class="modal-footer">
                <button href="#" class="btn btn-primary pull-right" id="downloadConfirm"
                        onclick="downloadPNG()">
                    <g:message code="de.iteratec.osm.pngDownloader.modal.confirmButton" default="download"/>
                </button>
            </div>
        </div>
    </div>
</div>

<asset:script>
    $(window).load(function () {
        OpenSpeedMonitor.postLoader.loadJavascript('<g:assetPath src="/pngDownloader.js" />', true, 'pngDownloader')
    });

    function downloadPNG(chartContainerID) {
        downloadAsPNG($("#download-chart-container svg"), "download.png");
        $('#downloadAsPngModal').modal('hide');
    }
</asset:script>
