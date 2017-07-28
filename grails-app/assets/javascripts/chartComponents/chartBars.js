//= require /bower_components/d3/d3.min.js
//= require_self

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartComponents = OpenSpeedMonitor.ChartComponents || {};

OpenSpeedMonitor.ChartComponents.ChartBars = (function () {
    var data = [];
    var minValue = 0;
    var maxValue = 1000;
    var height = 500;
    var width = 1000;
    var barBand = OpenSpeedMonitor.ChartComponents.ChartBars.BarBand;
    var barColor = "#1660A7";
    var transitionDuration = 500;

    var setData = function (componentData) {
        data = componentData.values || data;
        minValue = componentData.min || minValue;
        maxValue = componentData.max || maxValue;
        height = componentData.height || height;
        width = componentData.width || width;
        barColor = componentData.color || barColor;
    };

    var render = function (selector) {
        var xScale = d3.scale.linear().range([0, width]);
        var yScale = d3.scale.ordinal().rangeBands([0, height]);

        xScale.domain([minValue, maxValue]);
        yScale.domain(data.map(function(d) { return d.page; }));

        var bars = d3.select(selector).selectAll(".bar").data(data, function (d) {
            return d.page;
        });
        renderExit(bars.exit());
        renderEnter(bars.enter(), yScale);
        renderUpdate(bars, xScale, yScale);
    };

    var renderEnter = function (enterSelection) {
        var bars = enterSelection.append("g")
            .attr("class", "bar");
        bars.append("rect")
            .attr("class", "bar-rect")
            .attr("x", 0)
            .attr("width", 0)
            .attr("height", barBand)
            .attr("fill", barColor);
        bars.append("text")
            .attr("class", "bar-value")
            .attr("x", 0)
            .text(function (d) { return Math.round(d.value) + " " + d.unit})
            .attr("text-anchor", "end")
            .attr("dominant-baseline", "middle")
            .style("fill", "white")
            .style("font-weight", "bold");
    };

    var renderUpdate = function (updateSelection, xScale, yScale) {
        var valueLabelOffset = 10;
        var transition = updateSelection
            .transition()
            .duration(transitionDuration);

        transition.select(".bar-rect")
            .style("fill", barColor)
            .attr("y", function (d) {
                return yScale(d.page)
            })
            .attr("width", function (d) {
                return xScale(d.value)
            });

        transition.select(".bar-value")
            .attr("y", function (d) { return yScale(d.page) + barBand / 2 })
            .attr("x", function (d) { return xScale(d.value) - valueLabelOffset });
    };

    var renderExit = function (exitSelection) {
        var exitTransition = exitSelection
            .transition()
            .duration(transitionDuration);
        exitTransition
            .attr("width", 0)
            .remove();
    };

    return {
        render: render,
        setData: setData
    };
});

OpenSpeedMonitor.ChartComponents.ChartBars.BarBand = 40;
OpenSpeedMonitor.ChartComponents.ChartBars.BarGap = 5;
