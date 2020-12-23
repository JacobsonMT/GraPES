'use strict';

$(document).ready(function () {
    initializeGraphs();
});

function initializeGraphs() {
    // $('#graphs').bind('mouseleave', function(e) {
    //     window.charts.forEach(function (chart) {
    //         chart.tooltip.hide();
    //         chart.xAxis[0].removePlotLine('plot-line-sync');
    //     });
    // });

    window.charts = [];

    for (let graph of graphs) {
        if (graph.score) {
            window[graph.label] = new Highcharts.Chart(
                document.getElementById(graph.label + '-graph'),
                createHistogram(graph.title, graph.title,
                    graph.distribution.background, graph.score,
                    graph.distribution.markers)
            );
            charts.push(window[graph.label]);
        }
    }

}

function createHistogram(title, xAxis, data, score, markers) {

    const filteredData = filterOutliers(data);
    const min = Math.min(...filteredData, score);
    const max = Math.max(...filteredData, score);
    const mid = (max-min)/2;

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
            min: min,
            max: max,
            plotLines: [{
                value: score,
                width: 2,
                color: 'green',
                dashStyle: 'solid',
                zIndex: 99,
                label: {
                    text: label,
                    // verticalAlign: 'top',
                    // textAlign: 'left',
                    align: score > (max - (max-min)/10) ? 'right' : 'left',
                    rotation: 0,
                    x: score > (max - (max-min)/10) ? -1 : 1,
                    y: 15,
                    style: {
                        fontWeight: 'bold'
                    }
                }
            }]
        }],

        yAxis: [{
            title: { text: 'Count' }
        }],

        series: [{
            name: 'Histogram',
            type: 'histogram',
            xAxis: 0,
            yAxis: 0,
            baseSeries: 's1',
            showInLegend: false
        }, {
            name: 'Data',
            type: 'scatter',
            data: filteredData,
            visible: false,
            showInLegend: false,
            id: 's1',
            marker: {
                radius: 1.5
            }
        }],
    };

    var y = 50;
    for (let accession in markers) {
        var markerScore = markers[accession];
        options.xAxis[0].plotLines.push({
                value: markerScore,
                width: 1,
                color: 'red',
                dashStyle: 'dash',
                zIndex: 98,
                label: {
                    text: accession,
                    // verticalAlign: 'top',
                    // textAlign: 'left',
                    align: markerScore > (max - (max-min)/10) ? 'right' : 'left',
                    rotation: 0,
                    x: markerScore > (max - (max-min)/10) ? -1 : 1,
                    y: y,
                    style: {
                        fontWeight: 'bold'
                    }
                }
            })
        y += 35;
    }

    return options;
}

function filterOutliers(someArray) {

    if(someArray.length < 4)
        return someArray;

    let values, q1, q3, iqr, maxValue, minValue;

    values = someArray.slice().sort( (a, b) => a - b);//copy array fast and sort

    if((values.length / 4) % 1 === 0){//find quartiles
        q1 = 1/2 * (values[(values.length / 4)] + values[(values.length / 4) + 1]);
        q3 = 1/2 * (values[(values.length * (3 / 4))] + values[(values.length * (3 / 4)) + 1]);
    } else {
        q1 = values[Math.floor(values.length / 4 + 1)];
        q3 = values[Math.ceil(values.length * (3 / 4) + 1)];
    }

    iqr = q3 - q1;
    maxValue = q3 + iqr * 1.5;
    minValue = q1 - iqr * 1.5;

    return values.filter((x) => (x >= minValue) && (x <= maxValue));
}
