'use strict';

$(document).ready(function () {
    initializeGraphs();
});

function initializeGraphs() {
    window.charts = [];

    for (let graph of graphs) {
        if (graph.score != null) {
            window[graph.label] = new Highcharts.Chart(
                document.getElementById(graph.label + '-graph'),
                createKernelDensityEstimate(graph.title, graph.title,
                    graph.distribution.kde, graph.score,
                    graph.distribution.markers)
            );
            charts.push(window[graph.label]);
        }
    }

}

function createKernelDensityEstimate(title, xAxis, kde, score, markers) {

    // find Max y-value for kde so we can determine where to stop drawing vertical marker lines
    let yMax = 0;
    for (const xy of kde) {
        if (xy[1] > yMax) {
            yMax = xy[1];
        }
    }

    const options = {
        title: {
            text: title
        },
        chart: {
            height: 300,
            // marginLeft: 60,
            // marginRight: 75,
            // spacingTop: 20,
            // spacingBottom: 20,
            zoomType: 'x',
            resetZoomButton: {
                position: {
                    // align: 'right', // by default
                    // verticalAlign: 'top', // by default
                    x: 0,
                    y: -40
                }
            },
        },

        credits: false,

        // boost: {
        //     usePreallocated: false,
        //     useGPUTranslations: true,
        // },

        xAxis: [{
            title: { text: xAxis },
        }],

        yAxis: [{
            title: { text: 'Density' }
        }],

        series: [{
            type: 'areaspline',
            name: "KDE",
            dashStyle: "solid",
            lineWidth: 1,
            color: "#a6a3ac",
            data: kde,
            marker: {
                enabled: false
            },
        },
        {
            type: 'line',
            name: label,
            dashStyle: "solid",
            lineWidth: 2,
            // color: "#a6a3ac",
            data: [[score, 0],[score, yMax]],
            marker: {
                enabled: false
            },
            tooltip: {
                valueDecimals: 2,
                headerFormat: "{series.name}:",
                pointFormat: "<b>{point.x:.2f}</b>"
            },
        }],
    };

    for (let label in markers) {
        var markerScore = markers[label];
        options.series.push({
            type: 'line',
            name: label,
            dashStyle: "solid",
            lineWidth: 2,
            // color: "#a6a3ac",
            data: [[markerScore, 0],[markerScore, yMax]],
            marker: {
                enabled: false
            },
            tooltip: {
                valueDecimals: 2,
                headerFormat: "{series.name}:",
                pointFormat: "<b>{point.x:.2f}</b>"
            },
        });
    }

    return options;
}