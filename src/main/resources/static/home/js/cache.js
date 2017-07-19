let graphHistory;
let bubblesHistory;

function storepreviousBubbles(data, results, units){
	bubblesHistory = {
    "data" : data,
    "results" : results,
    "units" : units
  } 
}

function pullPreviousBubbles(){
	let previousBubbleData = bubblesHistory;
  $('#teck-stack-svg').empty();

  bubbleChart(previousBubbleData.data, previousBubbleData.results, previousBubbleData.units);
   
}

function storepreviousGraph(past_sales, predicted_sales, temperature, precipitation, unit){
	let previousGraphData = {
		"past" : past_sales,
		"future" : predicted_sales,
		"temp" : temperature,
		"precip" : precipitation,
		"unit" : unit,
	}
	graphHistory = previousGraphData;
}

function pullPreviousGraph(){
	let previousGraphData = graphHistory;
  salesGraph(previousGraphData.past, previousGraphData.future, 
  previousGraphData.temp, previousGraphData.precip, previousGraphData.unit, "day");
}