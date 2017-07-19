const colors = ["white","rgb(186, 232, 255)","#72d0ff","#7fcdbb","#41b6c4","#1d91c0","#225ea8","#253494","#081d58"]; // alternatively colorbrewer.YlGnBu[9]

function trafficMap() {
    $('#traffic').empty();

    const selected = $('#traffic-menu .underline');
    const unit = selected.attr('id');
    let timeSpan = "year";

    // Check which toggle has been selected
    if(unit == "by-week") {
      timespan = "week";

    } else if (unit == "by-month") {
      timespan = "month";
    }

    const trafficData = [];

    getTrendsByTimeSpan(timespan, function(response, err) {
        if (!err) {
          $('#num-visitors').animateNumber({ number: response.avgTraffic }, 2000);
          $('#worst-day-value').text(response.worstDay);
          $('#best-day-value').text(response.bestDay);

          Object.entries(response.traffic).forEach(
          ([day, hourMap]) => {
              Object.entries(hourMap).forEach(
                  ([hour, value]) => {
                      const cell = {day: parseInt(day), hour: parseInt(hour) + 1, value: value};
                      trafficData.push(cell);
              });
          });

          heatmapChart(trafficData);
        } else {
            $('#traffic-row').hide();
        }
    });

    var margin = { top: 50, right: 0, bottom: 100, left: 30 },
          width = 960 - margin.left - margin.right,
          height = 430 - margin.top - margin.bottom,
          gridSize = Math.floor(width / 24),
          legendElementWidth = gridSize*2,
          buckets = 9,
          // if you like yellow: #ffffe0
          days = ["Mo", "Tu", "We", "Th", "Fr", "Sa", "Su"],
          times = ["1a", "2a", "3a", "4a", "5a", "6a", "7a", "8a", "9a", "10a", "11a", "12a", "1p", "2p", "3p", "4p", "5p", "6p", "7p", "8p", "9p", "10p", "11p", "12p"];

      var svg = d3.select("#traffic").append("svg")
          .attr("width", width + margin.left + margin.right)
          .attr("height", height + margin.top + margin.bottom)
          .append("g")
          .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

      var dayLabels = svg.selectAll(".dayLabel")
            .data(days)
            .enter().append("text")
            .text(function (d) { return d; })
            .attr("x", 0)
            .attr("y", function (d, i) { return i * gridSize; })
            .style("text-anchor", "end")
            .style("fill", "rgb(166, 184, 215)")
            .attr("transform", "translate(-6," + gridSize / 1.5 + ")")
            .attr("class", function (d, i) { return ((i >= 0 && i <= 4) ? "dayLabel mono axis axis-workweek" : "dayLabel mono axis"); });

      var timeLabels = svg.selectAll(".timeLabel")
            .data(times)
            .enter().append("text")
            .text(function(d) { return d; })
            .attr("x", function(d, i) { return i * gridSize; })
            .attr("y", 0)
            .style("text-anchor", "middle")
            .style("fill", "rgb(166, 184, 215)")
            .attr("transform", "translate(" + gridSize / 2 + ", -6)")
            .attr("class", function(d, i) { return ((i >= 7 && i <= 16) ? "timeLabel mono axis axis-worktime" : "timeLabel mono axis"); });


      var heatmapChart = function(data) {
          const colorScale = d3.scaleQuantile()
            .domain([0, buckets - 1, d3.max(data, (d) => d.value)])
            .range(colors);

          const cards = svg.selectAll(".hour")
              .data(data, (d) => d.day+':'+d.hour);

          cards.append("title");

          cards.enter().append("rect")
              .attr("x", (d) => (d.hour - 1) * gridSize)
              .attr("y", (d) => (d.day - 1) * gridSize)
              .attr("rx", 0)
              .attr("ry", 0)
              .attr("class", "hour bordered")
              .attr("width", gridSize)
              .attr("height", gridSize)
              .style("fill", colors[0])
            .merge(cards)
              .transition()
              .duration(1000)
              .style("fill", (d) => colorScale(d.value));

          cards.select("title").text((d) => d.value);

          cards.exit().remove();

          const legend = svg.selectAll(".legend")
              .data([0].concat(colorScale.quantiles()), (d) => d);

          const legend_g = legend.enter().append("g")
              .attr("class", "legend");

          legend_g.append("rect")
            .attr("x", (d, i) => legendElementWidth * i)
            .attr("y", height)
            .attr("width", legendElementWidth)
            .attr("height", gridSize / 2)
            .style("fill", (d, i) => colors[i]);

          legend_g.append("text")
            .attr("class", "mono")
            .text((d) => "≥ " + Math.round(d))
            .style("fill", "rgb(166, 184, 215)")
            .attr("x", (d, i) => legendElementWidth * i)
            .attr("y", height + gridSize);

          legend.exit().remove();

      };
}
