//= require /bower_components/file-saver/FileSaver.min.js

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartModules = OpenSpeedMonitor.ChartModules || {};
OpenSpeedMonitor.ChartModules.PngDownloader = (function () {
    var modal = $("#downloadAsPngModal");
    var downloadContainer = $("#download-chart-container");
    var originalContainer = null;
    var svgPlaceholder = null;
    var chartModule = null;
    var svgElement = null;

    var init = function() {
        downloadContainer.resizable({
            resize: resize,
            disabled: true
        });
        modal.on('hide.bs.modal', passBackSvg);
    };

    var resize = function () {
        chartModule.refresh();
    };

    var initFor = function (svgContainerId, usedChartModule) {
        originalContainer = $(svgContainerId);
        chartModule = usedChartModule;
        svgElement = originalContainer ? originalContainer.find("svg") : null;
        takeSvg();
    };

    var createPlaceholder = function (original) {
        return $('<div/>', {
            width: original.width(),
            height: original.height()
        });
    };

    var takeSvg = function () {
        if (svgElement && chartModule) {
            svgPlaceholder = createPlaceholder(svgElement);
            svgElement.detach().appendTo(downloadContainer);
            originalContainer.append(svgPlaceholder);
            chartModule.refresh();
            downloadContainer.resizable("enable");
        } else {
            console.error("Unable to find referenced SVG oder used chart module");
        }
    };

    var passBackSvg = function () {
        if (svgElement && originalContainer && chartModule) {
            downloadContainer.resizable("disable");
            svgPlaceholder.remove();
            svgElement.detach().appendTo(originalContainer);
            chartModule.refresh();
        }
    };

    init();
    return {
        initFor: initFor
    }
})();


/**
 * Converts a svg node to a png file
 * @param svgNode the node to convert ( e.g. d3.select('svg').node() )
 * @param fileName the file name
 * @param width the width of the png in px
 * @param height the height of the png in px
 */
function downloadAsPNG(svgNode, fileName) {
    var svgString = getSVGString(svgNode[0]);
    function save(dataBlob) {
        saveAs(dataBlob, fileName); // FileSaver.js function
    }

    svgString2Image(svgString, svgNode.width(), svgNode.height(), 'png', save); // passes Blob and filesize String to the callback



}


// Below are the function that handle actual exporting:
// getSVGString (svgNode ) and svgString2Image( svgString, width, height, format, callback )
function getSVGString(svgNode) {
    svgNode.setAttribute('xlink', 'http://www.w3.org/1999/xlink');
    var cssStyleText = getCSSStyles(svgNode);
    appendCSS(cssStyleText, svgNode);

    var serializer = new XMLSerializer();
    var svgString = serializer.serializeToString(svgNode);
    svgString = svgString.replace(/(\w+)?:?xlink=/g, 'xmlns:xlink='); // Fix root xlink without namespace
    svgString = svgString.replace(/NS\d+:href/g, 'xlink:href'); // Safari NS namespace fix

    return svgString;

    function getCSSStyles(parentElement) {
        var selectorTextArr = [];

        // Add Parent element Id and Classes to the list
        selectorTextArr.push('#' + parentElement.id);
        for (var c = 0; c < parentElement.classList.length; c++)
            if (!contains('.' + parentElement.classList[c], selectorTextArr))
                selectorTextArr.push('.' + parentElement.classList[c]);

        // Add Children element Ids and Classes to the list
        var nodes = parentElement.getElementsByTagName("*");
        for (var i = 0; i < nodes.length; i++) {
            var id = nodes[i].id;
            if (!contains('#' + id, selectorTextArr))
                selectorTextArr.push('#' + id);

            var classes = nodes[i].classList;
            for (var c = 0; c < classes.length; c++)
                if (!contains('.' + classes[c], selectorTextArr))
                    selectorTextArr.push('.' + classes[c]);
        }

        // Extract CSS Rules
        var extractedCSSText = "";
        for (var i = 0; i < document.styleSheets.length; i++) {
            var s = document.styleSheets[i];

            try {
                if (!s.cssRules) continue;
            } catch (e) {
                if (e.name !== 'SecurityError') throw e; // for Firefox
                continue;
            }

            var cssRules = s.cssRules;
            for (var r = 0; r < cssRules.length; r++) {
                if (contains(cssRules[r].selectorText, selectorTextArr))
                    extractedCSSText += cssRules[r].cssText;
            }
        }


        return extractedCSSText;

        function contains(str, arr) {
            return arr.indexOf(str) === -1 ? false : true;
        }

    }

    function appendCSS(cssText, element) {
        var styleElement = document.createElement("style");
        styleElement.setAttribute("type", "text/css");
        styleElement.innerHTML = cssText;
        var refNode = element.hasChildNodes() ? element.children[0] : null;
        element.insertBefore(styleElement, refNode);
    }
}


function svgString2Image(svgString, width, height, format, callback) {
    var format = format ? format : 'png';

    var imgsrc = 'data:image/svg+xml;base64,' + btoa(unescape(encodeURIComponent(svgString))); // Convert SVG string to dataurl

    var canvas = document.createElement("canvas");
    var context = canvas.getContext("2d");

    canvas.width = width;
    canvas.height = height;

    var image = new Image;
    image.onload = function () {
        context.clearRect(0, 0, width, height);
        context.drawImage(image, 0, 0, width, height);

        canvas.toBlob(function (blob) {
            var filesize = Math.round(blob.length / 1024) + ' KB';
            if (callback) callback(blob, filesize);
        });


    };

    image.src = imgsrc;
}
