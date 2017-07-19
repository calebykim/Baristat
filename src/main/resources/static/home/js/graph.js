
/** Sets up Highchart **/
function setupChartComplete(past_sales, predicted_sales, temperature, precipitation, unit, previous) {
    let pastData = [];
    let futureData = [];
    let temperatureData = [];
    let precipitationData = [];
    let dates = [];

    Object.entries(past_sales).forEach(
        ([key, value]) => {
            dates.push(new Date(key));
            pastData.push(value);
    });

    Object.entries(predicted_sales).forEach(
        ([key, value]) => {
            dates.push(new Date(key));
            futureData.push(Math.round(value*100) / 100);
    });

    if(unit !== "hour"){
        Object.entries(temperature).forEach(
            ([key, value]) => {
            dates.push(new Date(key));
            temperatureData.push(Math.round(value*100) / 100);
        });
    }

    if(unit !== "hour"){
        Object.entries(precipitation).forEach(
        ([key, value]) => {
            dates.push(new Date(key));
            precipitationData.push(Math.round(value*100000) / 100000);
        });
    }

    salesGraph(pastData, futureData, temperatureData, precipitationData, dates, unit);

}

function salesGraph(pastData, futureData, temperatureData, precipitationData, dates, unit) {

    let end;
    let start;

    if(dates.length == 0){
        end = new Date($('#end').datepicker('getDate').format("mm/dd/yyyy"));
        start = new Date($('#start').datepicker('getDate').format("mm/dd/yyyy"));
    }else{
        end = new Date(Math.max.apply(null, dates));
        start = new Date(Math.min.apply(null, dates));
    }


    let startString = start.format("mmmm dS");
    let endString = end.format("mmmm dS");

    let intervalUnits = 24 * 3600 * 1000;

    if (unit == "hour") {
        intervalUnits = 3600 * 1000;
    }

    let graphTitle =  startString + " - " + endString
    if(unit === "hour") graphTitle = startString;

    Highcharts.chart('container2', {
        chart: {
            type: 'area',
            backgroundColor: "transparent",
        },

        colors: ['rgb(255, 136, 84)', 'rgb(114, 208, 255)', 'rgba(255,255,255, .75)', '#6cf7d7' ],

        title: {
            text: graphTitle,
            style:  { "color": "white", "fill": "white" }
        },

        xAxis: {
            type: 'datetime',
            dateTimeLabelFormats: {
                hour: '%H:%M'
            },
            gridLineColor: 'transparent',
            lineWidth: 0,
            minorGridLineWidth: 0,
            lineColor: 'transparent',
            minorTickLength: 0,
            tickLength: 0,
            gridLineColor: 'transparent',
            labels: {
                style: { "color": "rgb(166, 184, 215)", "fill": "rgb(166, 184, 215)" }
            }
        },

        yAxis: [{
            title: {
                text: 'Precipitation'
            },

            gridLineColor: 'transparent',
            opposite: true,
            labels: {
                style: { "color": "rgb(166, 184, 215)", "fill": "rgb(166, 184, 215)" },
                format: '{value} in.'
            }

        }, {
            title: {
                text: 'Temperature'
            },
            gridLineColor: 'transparent',
            opposite: true,
            labels: {
                style: { "color": "rgb(166, 184, 215)", "fill": "rgb(166, 184, 215)" },
                format: '{value} °F'
            }
        } , {
            title: {
                text: 'Sales',
            },

            gridLineColor: 'transparent',

            labels: {
                style: { "color": "rgb(166, 184, 215)", "fill": "rgb(166, 184, 215)" },
                format: '${value}'
            }
        }],

        legend: {
            layout: 'vertical',
            align: 'right',
            verticalAlign: 'middle',
            itemStyle: { "color": "#c7c7c7", "fill": "#c7c7c7" }
        },

        plotOptions: {
            series: {
                pointStart: 2010,
                cursor: 'pointer',
                point: {
                    events: {
                        click: function () {
                            let clickedDate = Highcharts.dateFormat('%Y-%m-%d',this.x);
                            let d = new Date(clickedDate);
                            //hubway now
                            let now = new Date("February 27, 2017 00:00:00");
                            //actual now
                            //let now = new Date();
                            if(unit === "day" && now >= d){
                                $('#prev').show();
                                d.setDate(d.getDate()+1);
                                d = d.toLocaleDateString();
                                storepreviousGraph(pastData, futureData, temperatureData, precipitationData, dates, unit);
                                requestSalesTempPrecip(d, d, "hour", false);
                                requestItem(d, d, "hour");
                                hourView = true;

                            }
                        }
                    }
                }
            },
            area: {
                fillOpacity: 0.9
            }
        },

        series: [{
            name: 'temperature',
            data: temperatureData,
            yAxis: 1,
            pointStart: Date.UTC(start.getFullYear(), start.getMonth(), start.getDate()),
            pointInterval: intervalUnits,
        }, {
            name: 'precipitation',
            data: precipitationData,
            pointStart: Date.UTC(start.getFullYear(), start.getMonth(), start.getDate()),
            pointInterval: intervalUnits,

        }, {
            name: 'past sales',
            data: pastData,
            yAxis: 2,
            pointStart: Date.UTC(start.getFullYear(), start.getMonth(), start.getDate()),
            pointInterval: intervalUnits
        }, {
            name: 'future sales',
            data: futureData,
            yAxis: 2,
            pointStart: Date.UTC(start.getFullYear(), start.getMonth(), start.getDate()) + intervalUnits*Math.max(0,pastData.length-1),
            pointInterval: intervalUnits,
        }],

        exporting: {
            enabled: true,
            buttons: {
                contextButton: {
                    theme: {
                        'stroke-width': 1,
                        stroke: 'silver',
                        r: 3,
                        states: {
                            hover: {
                                fill: 'transparent'
                            },
                            select: {
                                stroke: '#039',
                                fill: '#transparent'
                            }
                        }
                    }
                }
            }
        }

    });
    loadDone();
}
