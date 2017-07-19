let hourView = false;
let nextWeek = new Date();
nextWeek.setDate(nextWeek.getDate()+7);

$(document).ready(() => {

    let now = new Date();
    let next = new Date();
    next.setDate(now.getDate()+7);
    // requestSalesTempPrecip("05/08/2017", "05/07/2017", "day", true);
    // requestItem("05/01/2017", "05/07/2017", "day");
    requestSalesTempPrecip(now.toLocaleDateString(), next.toLocaleDateString(), "day", true);
    requestItem(now.toLocaleDateString(), next.toLocaleDateString(), "day");
    trafficMap();
    setupPage();
    listenForScroll();
    listenForDates();
    listenForPrev();
    setupSettings();
});

function setupPage() {
    $("#start").datepicker({"endDate" : nextWeek});
    $("#end").hide();

    setupTrends();
    setupOverview();

    $('#logout').click(function () {
        $.post("/logout", {}, function () {
            window.location.replace("http://localhost:4567/login");
        })
    });

}

function setupOverview() {
    getNextWeekOverview(function (overview, error) {

        if (!error) {
            $('#top-item-value').html("<span class='highlight-small'>" + overview.items.mostPopularItem.name + "</span><br> will be the top selling item");

             $('#predicted-visitors-value').text(overview.quartiles[0] + " - " + overview.quartiles[1]);

            $('#predicted-total-sales-value').text("$ " + Math.round(overview.predictedTotalSales));

            $('#delta-sales-value').text(Math.round(overview.percentChange * 100) + " %");

            $('#trending-item-value').html("<span class='highlight-small'>" + overview.items.itemOnRise.name + "</span><br> is trending");

            $('#busiest-day-value').html("<span class='highlight-small'>" + overview.busiestDay + "</span><br> will be your best day");

            $('.shaded').height($('.shaded').width())
        } else {
            $('#overview').hide();
        }

    });
}


function setupSettings() {
   $.post("/location", function(responseJSON) {
        const responseObject = JSON.parse(responseJSON);
        const locationDropdown = $('#list-locations');
        for (let i = 0; i < responseObject.locations.length; i++) {
            const item = $('<li class="block-li"></li>');

            const emptyAnchor = $('<a href="#"></a>');
            emptyAnchor.data("location_id", responseObject.locations[i].locationId);


            emptyAnchor.text(responseObject.locations[i].businessName + " - "
                + responseObject.locations[i].city + ", " + responseObject.locations[i].state);

            item.append(emptyAnchor);
             if(responseObject.currLoc === responseObject.locations[i].locationId){
                emptyAnchor.css('color', 'black');
                locationDropdown.prepend(item);

            }else{
                locationDropdown.append(item);

            }

        }
        listenForSettingChange();
    });
}

function setupTrends() {
    $("#by-week").on("click", function(e) {
        $("#by-week").addClass("underline");
        $("#by-month").removeClass("underline");
        $("#by-year").removeClass("underline");
        trafficMap();
    });
    $("#by-month").on("click", function(e) {
        $("#by-month").addClass("underline");
        $("#by-week").removeClass("underline");
        $("#by-year").removeClass("underline");
        trafficMap();

    });
}

function listenForSettingChange() {
    const options = $(".dropdown-menu li.block-li a");
    for (let i = 0; i < options.length; i++) {
        $(options[i]).click(function() {
            $("#labelMenu").text(options[i].text);
            const locationId = $(this).data("location_id");
            const postParameters = {"loc" : locationId};
            $("#loc").click(function(){
                $.post("/change", postParameters, responseJSON =>{
                    window.location.reload();
                })
            })
        });
    }
}


function requestSalesTempPrecip(start, end, unit, waitForLoad) {
    if(waitForLoad) {
      loading();
    }
    if (start == null || end == null || unit == null) {
        if(waitForLoad) {
          loadDone();
        }
        return;
    }

    getTotalSalesByUnit(start, end, unit, function (sales, sError) {
        if (!sError) {
            getTemperatureByUnit(start, end, unit, function (temps, tError) {

                if (!tError) {

                    getPrecipitationByUnit(start, end, unit, function (precips, pError) {

                        if (!pError) {

                            setupChartComplete(sales.past_sales, sales.predicted_sales,
                                temps.temperature, precips.precipitation, unit);


                        }
                    })
                }
            })
        }
    });

}

function requestItem(start, end, unit) {
    if (start == null || end == null || unit == null) {
        return;
    }
    getItemsByUnit(start, end, unit, function (results, error) {
        d3.select("#bc").selectAll("svg").remove();
        if (!error && (results.futureItems.length > 0 || results.pastItems.length > 0)) {
            $('#teck-stack-svg').empty();
            setUpBubbles(results, unit);
        }
    });
}

/** Listens for end dates */
function listenForDates() {
    $('#start').on('pick.datepicker', function (e) {
        $("#end").show();
        $("#end").remove();
        const end = $("<input data-toggle='datepicker' id='end' placeholder='End'>")[0];
        let first = $("ul.toggles input")[0];
        first.after(end);
        $("#end").datepicker({
            "startDate" :  $('#start').datepicker('getDate'),
            "endDate" : nextWeek
        });

        // Create new listener on end
        $('#end').on('pick.datepicker', function (e) {
            const formattedStart = $('#start').datepicker('getDate').format("mm/dd/yyyy");
            const formattedEnd = e.date.format("mm/dd/yyyy");
            requestSalesTempPrecip(formattedStart, formattedEnd, "day");
            requestItem(formattedStart, formattedEnd, "day");
        });

    });
}

function getDatesFromDatePicker() {
    const start = $('#start').datepicker('getDate').format("mm/dd/yyyy");
    const end = $('#end').datepicker('getDate').format("mm/dd/yyyy");
    return {start: start, end: end};
}

function listenForPrev() {
    $('#prev').on('click', function () {
        pullPreviousGraph();
        pullPreviousBubbles();
        $('#prev').hide();
        hourView = false;
    });

}

function listenForScroll() {
    let scroll_start = 0;
    let o = $('#sales-row').position().top;

    $(document).scroll(function() {
        scroll_start = $(this).scrollTop();
        if(scroll_start > $('#sales-row').position().top - 200) {
            $('#start').show();
            $('#end').show();
            $('#dashboard').hide();
            if(hourView){
                $("#prev").show();
            }
        } else {
            $('#dashboard').show();
            $('#start').hide();
            $('#end').hide();
            $("#prev").hide();


        }
   });
}


function setupGrid() {
    $('.grid').packery({
        itemSelector: '.grid-item',
        gutter: 10
    });
}


function loading(){
    $("#load").show();
    $(".row").hide();

}

function loadDone(){
    $("#load").hide();
    $(".row").show();
}
