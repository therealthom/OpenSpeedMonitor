//= require /urlHandling/urlHelper.js

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartModules = OpenSpeedMonitor.ChartModules || {};
OpenSpeedMonitor.ChartModules.UrlHandling = OpenSpeedMonitor.ChartModules.UrlHandling || {};

OpenSpeedMonitor.ChartModules.UrlHandling.JobGroupAggregation = (function () {

    var getTimeFrame = function (map) {
            map["setFromHour"] = ($('#setFromHour:checked').length>0) ? "on" :"";
            map["setToHour"] =  ($('#setToHour:checked').length>0) ? "on" :"";
            map["from"] = $("#fromDatepicker").val();
            map["fromHour"] = $("#startDateTimePicker").find(".input-group.bootstrap-timepicker.time-control").find(".form-control").val();
            map["to"] = $("#toDatepicker").val();
            map["toHour"] = $("#endDateTimePicker").find(".input-group.bootstrap-timepicker.time-control").find(".form-control").val()
          };

    var getJobGroup = function (map) {
        map["selectedFolder"] = $("#folderSelectHtmlId").val();
    };

    var getMeasurands = function (map) {
        var measurands = [];
        var measurandObjects = $('.measurandSeries');
        $.each(measurandObjects, function (_, currentSeries) {
            var currentMeasurands = [$(currentSeries).find(".firstMeasurandSelect").val()];
            $(currentSeries).find(".additionalMeasurand").each(function (_, additionalMeasurand) {
                currentMeasurands.push($(additionalMeasurand).val());
            });
            var json = JSON.stringify({
                "stacked": $(currentSeries).find(".stackedSelect").val(),
                "values": currentMeasurands
            });
            measurands.push(json);
        });
        map['measurand'] = measurands
    };
    var addHandler = function () {
        $('#graphButtonHtmlId').on('click', updateUrl);
    };

    var updateUrl = function () {
        var map = {};
        getTimeFrame(map);
        getJobGroup(map);
        getMeasurands(map);
        var path = "show?" + $.param(map, true);
        window.history.pushState("object or string", "Title", path);
    };

    var setSelections = function () {
        var params = OpenSpeedMonitor.ChartModules.UrlHandling.UrlHelper.getUrlParameter();
        // setTrim(params);
        if (params && params.length > 0) {
            setJobGroups(params);
            setMeasurands(params);
            if(params.selectedFolder != null){
                clickShowButton();
            }
        }

    };
    var setMultiSelect = function (id, values) {
        $("#" + id).val(values);
        $("#" + id).trigger("change")
    };
    var setJobGroups = function (params) {
        var selectedFolderParam = params['selectedFolder'];
        if (selectedFolderParam !== undefined && selectedFolderParam != null) {
            setMultiSelect("folderSelectHtmlId", selectedFolderParam);
        }
    };

    var clickShowButton = function () {
        $("#graphButtonHtmlId").click()
    };

    var setMeasurands = function (params) {
        var measurandGroups = params['measurand'];
        if (measurandGroups == undefined || measurandGroups == null) {
            return;
        }
        var currentGroup;
        if (measurandGroups.constructor === Array) {
            currentGroup = JSON.parse(decodeURIComponent(measurandGroups.shift()));
            addMeasurands(currentGroup, 0);
            var addButton = $("#addMeasurandSeriesButton");
            var length = measurandGroups.length;
            for (var i = 0; i < length; i++) {
                addButton.click();
                addMeasurands(JSON.parse(decodeURIComponent(measurandGroups.shift())), i + 1);
            }
        } else {
            currentGroup = JSON.parse(decodeURIComponent(measurandGroups));
            addMeasurands(currentGroup, 0);
        }
    };

    var addMeasurands = function (measurands, index) {
        var firstSelect = $(".firstMeasurandSelect").eq(index);
        firstSelect.val(measurands['values'].shift());
        var length = measurands['values'].length;
        var currentPanel = firstSelect.closest(".panel");
        var currentAddButton = currentPanel.find(".addMeasurandButton");
        for (var i = 0; i < length; i++) {
            currentAddButton.click();
            currentPanel.find(".additionalMeasurand").eq(i).val(measurands['values'].shift());
            currentAddButton = currentPanel.find(".addMeasurandButton").eq(i + 1);
        }
        currentPanel.find(".stackedSelect").val(measurands['stacked'])

    };

    var timecardResolved = false;
    var barChartResolved = false;
    var markTimeCardAsResolved = function () {
        timecardResolved = true;
        if (allLoaded()) {
           init()
        }
    };
    var markBarChartAsResolved = function () {
        barChartResolved = true;
        if (allLoaded()) {
            init()
        }
    };
    var allLoaded = function () {
        return timecardResolved && barChartResolved;
    };

    var init = function () {
        setSelections();
        addHandler();
    };

    var initWaitForPostload = function () {
        $(window).on("selectIntervalTimeframeCardLoaded", function () {
            markTimeCardAsResolved();
        });
        $(window).on("barchartHorizontalLoaded", function () {
            markBarChartAsResolved();
        });
    };

    return {
        setSelections: setSelections,
        init: initWaitForPostload
    };
});