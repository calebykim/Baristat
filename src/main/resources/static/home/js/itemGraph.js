/** Sets up Highchart **/
function setupItemGraph(past_sales, predicted_sales, unit, name) {
    let pastData = [];
    let futureData = [];
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

    itemGraph(pastData, futureData, dates, unit, name);

}

function itemGraph(pastData, futureData, dates, unit, name) {

    let end;
    let start;


    end = new Date(Math.max.apply(null, dates));
    start = new Date(Math.min.apply(null, dates));



    let startString = start.format("mmmm dS");
    let endString = end.format("mmmm dS");

    let intervalUnits = 24 * 3600 * 1000;

    if (unit == "hour") {
        intervalUnits = 3600 * 1000;
    }

    let graphTitle = startString + " - " + endString
    if(unit === "hour") graphTitle = startString;

    Highcharts.chart('overlay', {
        chart: {
            type: 'line',
            backgroundColor: "transparent"
        },

        title: {
            text:  name,
            style:  { "color": "white", "fill": "white", "fontSize" : "25px" }
        },

        subtitle: {
            text: graphTitle,
            style:  { "color": "white", "fill": "white", "fontSize" : "20px" }
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
                style: { "color": "#FFFFFF", "fill": "#FFFFFF" }
            }
        },

        yAxis: {
            title: {
                text: 'Number of Items',
                style: { "color": "#FFFFFF", "fill": "#FFFFFF" }

            },
            gridLineColor: 'transparent',
            labels: {
                style: { "color": "#FFFFFF", "fill": "#FFFFFF" }
            }

        },

        legend: {
            layout: 'vertical',
            align: 'right',
            verticalAlign: 'middle',
            itemStyle: {
                color: '#FFFFFF',
            }
        },

        plotOptions: {
            // series: {
            //     pointStart: 2010,
            //     cursor: 'pointer',
            //     point: {
            //         events: {
            //             click: function () {
            //                 // if(unit === "day"){
            //                 //     $('#prev').show();
            //                 //     let clickedDate = Highcharts.dateFormat('%Y-%m-%d',this.x);
            //                 //     let d = new Date(clickedDate);
            //                 //     d.setDate(d.getDate()+1);
            //                 //     d = d.toLocaleDateString();
            //                 //     requestSalesTempPrecip(d, d, "hour");
            //                 //     requestItem(d, d, "day");
            //                 // }

            //             }
            //         }
            //     }
            // }
        },

        series: [
         {
            name: 'past',
            data: pastData,
            pointStart: Date.UTC(start.getFullYear(), start.getMonth(), start.getDate()),
            pointInterval: intervalUnits,
            color: '#FFFFFF'
        }, {
            name: 'future',
            data: futureData,
            pointStart: Date.UTC(start.getFullYear(), start.getMonth(), start.getDate()) + intervalUnits*Math.max(0,pastData.length-1),
            pointInterval: intervalUnits,
            dashStyle: 'dot',
            color: '#FFFFFF'
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
}
