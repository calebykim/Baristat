
function fillTopBubbles(x, past, future) {

    let bubbleList = [];

    let totals = {};

    for (let i = 0; i < x; i++) {
        if (i < past.length) {
            totals[past[i].name]= past[i].totalNumSold
        }
    }

    for (let i = 0; i < x; i++) {
        if (i < future.length) {
            if(totals.hasOwnProperty(future[i].name)) {
              totals[future[i].name] += future[i].totalNumSold;
            } else {
              totals[future[i].name] = future[i].totalNumSold;
            }
        }
    }

    let count = 0;
    for(let i in totals) {
      count++;
      let data = {
        name: i,
        value: totals[i]
      };
      bubbleList.push(data);
      if(x < count) {
        break;
      }
    }


    return bubbleList;

}

/*
function fillTopBubbles(x, past, future) {

    let bubbleList = [];

    let pastTotals = {};



    for (let i = 0; i < x; i++) {
        if (i < past.length) {
            let data = {
                name: past[i].name,
                value: past[i].totalNumSold,
            }
            bubbleList.push(data);
        }
    }
    for (let i = 0; i < x; i++) {
        if (i < future.length) {
            let data = {
                name: future[i].name,
                value: future[i].totalNumSold,
            }
            bubbleList.push(data);
        }
    }

    return bubbleList;

}
*/

function setUpBubbles(results, unit) {
    const x = 8;
    let bubbleData = fillTopBubbles(x, results.pastItems, results.futureItems);
    $('bc g').remove();
    $('#bc path').remove();
    if(unit === "day"){
      storepreviousBubbles(bubbleData, results, unit);

    }
    bubbleChart(bubbleData, results, unit);
}
