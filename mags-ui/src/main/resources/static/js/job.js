'use strict';

// /**
//  * Synchronize zooming through the setExtremes event handler.
//  */
// function syncExtremes(e) {
//     var thisChart = this.chart;
//
//     if (e.trigger !== 'syncExtremes') { // Prevent feedback loop
//         window.charts.forEach( function (chart) {
//             if (chart !== thisChart) {
//                 if (chart.xAxis[0].setExtremes) { // It is null while updating
//                     chart.xAxis[0].setExtremes(e.min, e.max, undefined, false, { trigger: 'syncExtremes' });
//                 }
//             }
//         });
//     }
// }

/**
 * Custom Axis extension to allow emulation of negative values on a logarithmic
 * Y axis. Note that the scale is not mathematically correct, as a true
 * logarithmic axis never reaches or crosses zero.
 */
(function (H) {
    // Pass error messages
    H.Axis.prototype.allowNegativeLog = true;

    // Override conversions
    H.Axis.prototype.log2lin = function (num) {
        var isNegative = num < 0,
            adjustedNum = Math.abs(num),
            result;
        if (adjustedNum < 10) {
            adjustedNum += (10 - adjustedNum) / 10;
        }
        result = Math.log(adjustedNum) / Math.LN10;
        return isNegative ? -result : result;
    };
    H.Axis.prototype.lin2log = function (num) {
        var isNegative = num < 0,
            absNum = Math.abs(num),
            result = Math.pow(10, absNum);
        if (result < 10) {
            result = (10 * (result - 1)) / (10 - 1);
        }
        return isNegative ? -result : result;
    };
}(Highcharts));


$(document).ready(function () {
    if (job.complete && !job.failed) {
        initializeGraphs();
    } else {
        pollUntilDone(5000, 0).then(function(bases) {
            job.result = {bases: bases};
            $.get(window.location.pathname + "/content", function(fragment) {
                $("#job-view-content").replaceWith(fragment); // update snippet of page
                initializeGraphs();
            });
        }).catch(function(err) {
            console.error(err);
        });
    }
});

// create a promise that resolves after a short delay
function delay(t) {
    return new Promise(function(resolve) {
        setTimeout(resolve, t);
    });
}

function pollUntilDone(interval, timeout) {
    let start = Date.now();
    function run() {
        return $.get(window.location.pathname + "/bases").then(function(bases) {
            if ( bases.length > 0 ) {
                // done
                return bases;
            } else {
                if (timeout !== 0 && Date.now() - start > timeout) {
                    throw new Error("timeout error on pollUntilDone");
                } else {
                    // run again with a short delay
                    return delay(interval).then(run);
                }
            }
        });
    }
    return run();
}

function updateBinaryCutoff() {
    window.heatmapChart.colorAxis[0].update({
        stops: [[0, '#a1dab4'],
            [window.heatmapChart.heatmapCutoff, '#a1dab4'],
            [window.heatmapChart.heatmapCutoff, '#253494'],
            [1, '#253494']],
        tickPositions: [0, window.heatmapChart.heatmapCutoff, 1]
    });
}

function resetCutoff() {
    window.heatmapChart.heatmapCutoff = 0.88;
    $('#cutoff').val(window.heatmapChart.heatmapCutoff);
    $('#cutoff-value').html(parseFloat(window.heatmapChart.heatmapCutoff).toFixed(2));
    if ($('#binary-radio').prop("checked", true)) {
        updateBinaryCutoff();
    }
}

function initializeGraphs() {
    $('#job-graphs').bind('mouseleave', function(e) {
        window.charts.forEach(function (chart) {
            chart.tooltip.hide();
            chart.xAxis[0].removePlotLine('plot-line-sync');
        });
    });

    const $cutoffContainer = $('#cutoff-container');

    const $gradRad = $('#grad-radio');
    $gradRad.bind('change', function () {
        $cutoffContainer.hide();
        window.heatmapChart.colorAxis[0].update(window.heatmapChart.gradientColorAxis);
    });

    const $binRad = $('#binary-radio');
    $binRad.bind('change', function () {
        $cutoffContainer.show();
        updateBinaryCutoff();
    });


    const $cutoff = $('#cutoff');
    const $cutoffVal = $('#cutoff-value');
    $cutoff.bind('input', function () {
        window.heatmapChart.heatmapCutoff = this.value;
        $cutoffVal.html(parseFloat(this.value).toFixed(2));
    });

    $cutoff.bind('mouseup touchend', function () {
        if ($binRad.prop("checked", true)) {
            updateBinaryCutoff();
        }
    });

    let data = [];
    let categories = [];
    let depth = [];
    let iupData = [];
    let espritzData = [];

    let conservation = [];
    job.result.bases.forEach(function (base, x) {
        // if (x < 100) {
        categories.push(base.reference);
        depth.push(base.depth);
        iupData.push(base.iupred);
        espritzData.push(base.espritz);
        conservation.push(base.conservation);
        if (base.list.length === 0) {
            base.list = new Array(20).fill(0);
        }
        base.list.forEach(function (val, y) {
            // if (y < 5) {
            data.push([x+1, y, val]);
            // }
        });

        // }
    });

    // let predictionData = [{name: "IUPred -l", type: "area", data: iupData},{name: "ESpritz -D", type: "line", color: "red", data: espritzData}];
    let conservationData = [{name: "Conservation", type: "area", data: conservation}];
    let depthData = [{name:"Depth",  type: "area", data:depth}];


    window.charts = [];
    if (data.length !== 0) {
        window.heatmapChart = new Highcharts.Chart(
            document.getElementById('heatmap-container'),
            createHeatMap( "Position Conservation Matrix", data, categories)
        );
        window.heatmapChart.heatmapCutoff = 0.88;
        window.heatmapChart.gradientColorAxis = {
            stops: window.heatmapChart.colorAxis[0].stops,
            tickPositions: window.heatmapChart.colorAxis[0].tickPositions
        };
        $("#heatmap-axis-toggle").show();
        charts.push( window.heatmapChart );
    }

    if (conservationData.every(function(v) {return v.data.length !== 0})) {
        window.conservationChart = new Highcharts.Chart(
            document.getElementById('conservation-container'),
            createChart( "Conservation", conservationData, true, false, "linear")
        );
        charts.push( window.conservationChart );
    }

    if (depthData.every(function(v) {return v.data.length !== 0})) {
        window.depthChart = new Highcharts.Chart(
            document.getElementById('depth-container'),
            createChart( "Alignment Depth", depthData, true, true, "linear")
        );
        charts.push( window.depthChart );
    }

    // if (predictionData.every(function(v) {return v.data.length !== 0})) {
    // window.predictionChart = new Highcharts.Chart(
    //     document.getElementById('prediction-container'),
    //     createHeatMap( "Depth", predictionData, true, true, "linear")
    // );
    // charts.push( window.predictionChart );
    // }
}

function createHeatMap(title,
                       data,
                       categories) {

    return {

        chart: {
            type: 'heatmap',
            zoomType: 'x',
            resetZoomButton: {
                position: {
                    // align: 'right', // by default
                    // verticalAlign: 'top', // by default
                    x: 0,
                    y: -40
                }
            },
            height: 300,
            marginLeft: 60,
            marginRight: 75,
            marginTop: 40,
            // marginBottom: 80,
            plotBorderWidth: 1,
            events : {
                load: function() {
                    console.log( "loading chart", this );
                },
                selection: function (event) {

                    if (event.resetSelection) {
                        window.charts.forEach(function (chart) {
                            // Chosen instead of zoomOut as is doesn't trigger selection
                            chart.zoom();
                        });
                        // window.heatmapChart.zoom();
                        return false;
                    }


                    var extremesObject = event.xAxis[0],
                        min = Math.round(extremesObject.min),
                        max = Math.round(extremesObject.max);

                    // Smooth hacks
                    window.charts.forEach(function (chart) {
                        chart.xAxis[0].setExtremes(min - 0.5, max + 0.5);
                    });

                    window.heatmapChart.xAxis[0].setExtremes(min, max);

                    if (!heatmapChart.resetZoomButton) {
                        window.heatmapChart.showResetZoom();
                    }

                    return false;
                }
            }
        },

        boost: {
            useGPUTranslations: true
        },

        credits: false,


        title: {
            text: title,
            align: 'left',
            margin: 0,
            x: 70,
            style: {
                fontSize: '1.8em',
            },
        },

        plotOptions: {
            series: {
                point: {
                    events: {
                        mouseOver: function (e) {
                            var p = this;
                            window.charts.forEach(function (chart) {
                                try {
                                    chart.xAxis[0].removePlotLine('plot-line-sync');
                                    chart.xAxis[0].addPlotLine({
                                        value: p.x,
                                        color: "#525252",
                                        width: 1,
                                        zIndex: 5,
                                        id: 'plot-line-sync'
                                    });

                                    // Synchronized Labels
                                    if (p.series.chart !== chart && chart.series[0].type !== "heatmap" ) {
                                        let pps = [];
                                        chart.series.forEach(function (s) {
                                            let pp = {};
                                            if (chart.isBoosting) {
                                                pp = s.getPoint({i: p.x - 1});
                                                pp.plotX = s.xAxis.toPixels(pp.x) - chart.plotLeft;
                                                pp.plotY = s.yAxis.toPixels(pp.y) - chart.plotTop;
                                            } else {
                                                pp = s.data[p.x - 1];
                                            }
                                            pps.push(pp);
                                        });

                                        chart.tooltip.refresh(pps); // Show the tooltip
                                    }


                                } catch (e) {
                                    console.log(e);
                                }

                            });
                        }
                    }
                }
            }
        },

        xAxis: [{
            minPadding: 0,
            maxPadding: 0,
            startOnTick: false,
            endOnTick: false,
            allowDecimals: false,
            tickWidth: 0,
            labels: {
                style: {
                    fontSize: '1.5em',
                    color: '#000000',
                },
            }
        }, {
            // visible: this.props.data.length <= 100,
            linkedTo: 0,
            allowDecimals: false,
            minPadding: 0,
            maxPadding: 0,
            startOnTick: false,
            endOnTick: false,
            tickInterval: 1,
            tickWidth: 0,
            tickLength: 0,
            opposite: false,
            lineWidth: 0,
            offset:-9,
            labels: {
                step: 1,
                formatter: function (e) {
                    if (this.axis.max - this.axis.min < 100) {
                        return categories[this.value - 1];
                    } else {
                        return "";
                    }

                },
                style: {
                    fontSize: '0.95em',
                    color: '#000000',
                },
            }
        }],

        yAxis: {
            categories: ['A', 'R', 'N', 'D', 'C', 'Q', 'E', 'G', 'H', 'I', 'L', 'K', 'M', 'F', 'P', 'S', 'T', 'W', 'Y', 'V'],
            title: null,
            tickInterval: 1,
            startOnTick: true,
            endOnTick: false,
            min: 0,
            max: 19,
            padding: 1,
            step: 1,
            tickWidth: 0,
            labels: {
                align: 'center',
                step: 1,
                style: {
                    fontSize: '1em',
                    color: '#000000',
                },
            },
        },

        colorAxis: {
            min: 0,
            max: 1,
            stops: [[0, '#ffffff'],
                [0.1, '#ffffcc'],
                [0.6, '#a1dab4'],
                [0.8, '#41b6c4'],
                [0.9, '#2c7fb8'],
                [0.95, '#253494']],
            labels: {
                x: 5,
                style: {
                    fontSize: '1.5em',
                    color: '#000000',
                }
            },
            tickPositions: [0, 0.5, 1],
            // minColor: '#FFFFFF',
            // maxColor: '#000099' //Highcharts.getOptions().colors[0]
        },

        legend: {
            align: 'right',
            layout: 'vertical',
            margin: 0,
            verticalAlign: 'middle',
            // y: 0,
            // symbolHeight: 217,
            navigation: {
                enabled: false,
                arrowSize: 0,
            }
        },

        tooltip: {
            formatter: function () {
                return 'Mutation: <b>' + categories[this.point.x - 1] + ' ' + this.point.x + ' ' +
                    this.series.yAxis.categories[this.point.y] + '<br></b>Effect: <b>' + this.point.value + '</b>';
            },
            shared: false
        },

        series: [{
            boostThreshold: 100,
            name: 'LIST',
            borderWidth: 0,
            data: data,
            dataLabels: {
                enabled: false,
                color: '#000000'
            },
            turboThreshold: 1000,
        }],

        exporting: {
            filename: job.label,
            sourceWidth: 1200,
            sourceHeight: 500,

            chartOptions: {
                chart : {
                    marginTop: 60,
                },
                title: {
                    style: {
                        fontSize: '3em'
                    }
                },
                xAxis: {
                    tickPixelInterval: 150,
                    labels: {
                        style: {
                            fontSize: '3em'
                        }
                    },
                    tickWidth: null,

                },
                yAxis: {
                    labels: {
                        style: {
                            fontSize: '1.5em'
                        }
                    }

                },
                legend: {
                    symbolHeight: 350,
                },
                colorAxis: {
                    labels: {
                        style: {
                            fontSize: '1.5em'
                        }
                    }
                }
            }
        }

    };

}

function createChart(title,
                     data,
                     xAxisVisible,
                     enableCredit,
                     yAxisType) {

    const options = {

        title: {
            text: title,
            align: 'left',
            margin: 0,
            x: 70,
            style: {
                fontSize: '1.8em',
            },
        },

        credits: enableCredit,

        chart: {
            height: 200,
            marginLeft: 60,
            marginRight: 75,
            spacingTop: 20,
            spacingBottom: 20,
            zoomType: 'x',
            resetZoomButton: {
                position: {
                    // align: 'right', // by default
                    // verticalAlign: 'top', // by default
                    x: 0,
                    y: -40
                }
            },
            events : {
                load: function() {
                    console.log( "loading chart", this );
                },
                selection: function (event) {

                    if (event.resetSelection) {
                        window.charts.forEach(function (chart) {
                            // Chosen instead of zoomOut as is doesn't trigger selection
                            chart.zoom();
                        });
                        // window.heatmapChart.zoom();
                        return false;
                    }

                    var extremesObject = event.xAxis[0],
                        min = Math.round(extremesObject.min),
                        max = Math.round(extremesObject.max);

                    // Smooth hacks
                    window.charts.forEach(function (chart) {
                        chart.xAxis[0].setExtremes(min - 0.5, max + 0.5);
                    });

                    window.heatmapChart.xAxis[0].setExtremes(min, max);

                    if (!heatmapChart.resetZoomButton) {
                        window.heatmapChart.showResetZoom();
                    }

                    return false;
                }
            }
        },

        boost: {
            usePreallocated: false,
            useGPUTranslations: true,
        },

        plotOptions: {
            series: {
                pointStart: 1,
                point: {
                    events: {
                        mouseOver: function (e) {
                            var p = this;
                            window.charts.forEach(function (chart) {
                                try {
                                    chart.xAxis[0].removePlotLine('plot-line-sync');
                                    chart.xAxis[0].addPlotLine({
                                        value: p.x,
                                        color: "#525252",
                                        width: 1,
                                        zIndex: 5,
                                        id: 'plot-line-sync'
                                    });

                                    // Synchronized Labels
                                    if (p.series.chart !== chart && chart.series[0].type !== "heatmap") {
                                        let pps = [];
                                        chart.series.forEach(function(s) {
                                            let pp = {};
                                            if (chart.isBoosting) {
                                                pp = s.getPoint({i: p.x - 1});
                                                pp.plotX = s.xAxis.toPixels(pp.x) - chart.plotLeft;
                                                pp.plotY = s.yAxis.toPixels(pp.y) - chart.plotTop;
                                            } else {
                                                pp = s.data[p.x - 1];
                                            }
                                            pps.push(pp);
                                        });

                                        chart.tooltip.refresh(pps); // Show the tooltip

                                    }

                                } catch (e) {
                                    console.log(e);
                                }
                            });
                        }
                    }
                }
            }
        },

        xAxis: {
            min: 0.5,
            max: data[0].data.length + 0.5,
            minTickInterval: 1,
            allowDecimals: false,
            crosshair: true,
            minPadding: 0,
            maxPadding: 0,
            startOnTick: false,
            endOnTick: false,
            visible: xAxisVisible,
            labels: {
                style: {
                    fontSize: '1.5em',
                    color: '#000000',
                },
            }
        },

        yAxis: {
            title: null,
            type: yAxisType,
            maxPadding: 0,
            minPadding:0,
            labels: {
                style: {
                    fontSize: '1.5em',
                    color: '#000000',
                },
            }
        },

        legend: {
            enabled: data.length > 1,
            align: 'right',
            verticalAlign: 'top',
            x: -60,
            y: -5,
            floating: true,
            itemStyle: {
                fontSize: '1.2em',
            },
            itemDistance: 50,
        },

        tooltip: {
            shared: true,
        },

        series: [],

        exporting: {
            filename: job.label + "-" + title,
            sourceWidth: 1200,
            sourceHeight: 500,
            chartOptions: {
                chart : {
                    marginTop: 60,
                    marginRight: 20,
                    marginLeft: 80,
                    events: {
                        load: function() {
                            this.xAxis[0].removePlotLine('plot-line-sync');
                        }
                    }
                },
                title: {
                    style: {
                        fontSize: '3em',
                    },
                },
                legend: {
                    itemStyle: {
                        fontSize: '2.5em',
                    },
                    symbolPadding: 10,
                },
                xAxis: {
                    tickPixelInterval: 150,
                    labels: {
                        style: {
                            fontSize: '3em'
                        }
                    }

                },
                yAxis: {
                    labels: {
                        style: {
                            fontSize: '3em'
                        }
                    }

                },
            }
        }
    };

    data.forEach(function(series) {
        options.series.push({
            boostThreshold: 1000,
            name: series.name,
            type: series.type,
            data: series.data,
            color: series.color,
            marker: {
                enabled: false,
                states: {
                    hover: {
                        enabled: false,
                    }
                }
            }
        })
    });

    return options;

}
