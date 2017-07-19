function graphItem(node, itemData, unit){
	let past = itemData.pastItems;
	let selectPastItem = findItem(node.name, past);
	let future = itemData.futureItems;
	let selectFutureItem = findItem(node.name, future);

	let pData = selectPastItem !== null ? selectPastItem.numSoldPerUnitMap : [];
	let fData = selectFutureItem !== null ? selectFutureItem.numSoldPerUnitMap : [];
	setupItemGraph(pData, fData, unit, node.name);
}

function findItem(name, itemList){
	for(let i in itemList){
		if(itemList[i].name == name){
			return itemList[i];
		}
	}
	return null;

}

function expandBubble(currentTarget, currentNode, colors, data){
	//let nodeColor = d3.event.currentTarget.childNodes[0].style.fill;
	const scaleColor = getColorRange(colors, data);
	let nodeColor = scaleColor(currentNode.value);
	$("#overlay").remove();
	var overlay = $('<div id="overlay"> </div>');
	const container = $('#bubble-container');
    overlay.appendTo(container);
    overlay.css('background-color',nodeColor);
    overlay.css('height', container.height());
	$('#overlay').hide().fadeIn(900);
    $("#overlay").click(function(x){
    	$('#overlay').fadeOut(900);
    });

}


function bubbleChart(data, itemData, unit){

	let svg = d3.select('#teck-stack-svg');
	let width = svg.property('clientWidth');
	let height = +svg.attr('height');
	let centerX = width * 0.5;
	let centerY = height * 0.5;
	let strength = 0.5;
	let focusedNode;

	const colors = ["#49c1ff","#7fcdbb","#41b6c4","#1d91c0","#225ea8","#253494","#081d58"];


	let format = d3.format(',d');

	const scaleColor = getColorRange(colors, data);

	// use pack to calculate radius of the circle
	let pack = d3.pack()
		.size([width, height ])
		.padding(1.5);

	let forceCollide = d3.forceCollide(d => d.r + 2);

	// use the force
	let simulation = d3.forceSimulation()
		// .force('link', d3.forceLink().id(d => d.id))
		.force('charge', d3.forceManyBody())
		.force('collide', forceCollide)
		// .force('center', d3.forceCenter(centerX, centerY))
		.force('x', d3.forceX(centerX ).strength(strength))
		.force('y', d3.forceY(centerY ).strength(strength));

	// reduce number of circles on mobile screen due to slow computation
	if ('matchMedia' in window && window.matchMedia('(max-device-width: 767px)').matches) {
		data = data.filter(el => {
			return el.value >= 50;
		});
	}

	let root = d3.hierarchy({ children: data })
		.sum(d => d.value);

	// we use pack() to automatically calculate radius conveniently only
	// and get only the leaves
	let nodes = pack(root).leaves().map(node => {
		// console.log('node:', node.x, (node.x - centerX) * 2);
		const data = node.data;
		return {
			x: centerX + (node.x - centerX) * 3, // magnify start position to have transition to center movement
			y: centerY + (node.y - centerY) * 3,
			r: 1, // for tweening
			radius: node.r + 10, //original radius
			id: data.value,
			name: data.name,
			value: data.value,
		};
	});

	simulation.nodes(nodes).on('tick', ticked);

	let node = svg.selectAll('.node')
		.data(nodes)
		.enter()
		.filter(function(n){
  			return n.id > 0;
		})
		.append('g')
		.attr('class', 'node')
		.call(d3.drag()
			.on('start', (d) => {
				if (!d3.event.active) { simulation.alphaTarget(0.2).restart(); }
				d.fx = d.x;
				d.fy = d.y;
			})
			.on('drag', (d) => {
				d.fx = d3.event.x;
				d.fy = d3.event.y;
			})
			.on('end', (d) => {
				if (!d3.event.active) { simulation.alphaTarget(0); }
				d.fx = null;
				d.fy = null;
			}));

	node.append('circle')
		.attr('id', d => d.value)
		.attr('r', 0)
		.style('fill', d => scaleColor(d.value))
		.on("mouseover", function () {
		    d3.select(this).style("fill", "rgb(255, 136, 84)")
		})
		.on("mouseout", function () {
		    d3.select(this).style("fill", d => scaleColor(d.value))
		})
		.transition().duration(2000).ease(d3.easeElasticOut)
			.tween('circleIn', (d) => {
				let i = d3.interpolateNumber(0, d.radius);
				return (t) => {
					d.r = i(t);
					simulation.force('collide', forceCollide);
				};
	});

	// display text as circle icon
	node.append('text')
		.attr('clip-path', d => `url(#clip-${d.id})`)
		.selectAll('tspan')
		.data(d => d.name)
		.enter()
		.append('tspan')
		.text(name => name)
		.style('font-size', '24px')
		.style("font-family", "Source Sans Pro, Helvetica Neue")
		.style("font-weight", "100")
		.style("fill", "white");


	node.append("text")
        .attr("dy", function(d){return 30})
        .text(function(d){return d.value})
        .style('font-size', '28px')
        .style("font-family", "Source Sans Pro, Helvetica Neue")
        .style("font-weight", "400")
        .style("fill", "white");


	let legendOrdinal = d3.legendColor()
		.scale(scaleColor)
		.shape('circle');

	// legend 1
	svg.append('g')
		.classed('legend-color', true)
		.attr('text-anchor', 'start')
		.attr('transform', 'translate(20,30)')
		.style('font-size', '20px')
		.style("font-family", "Source Sans Pro, Helvetica Neue")
		.style("fill", "white")
		.call(legendOrdinal);

	let sizeScale = d3.scaleOrdinal()
		.domain(['less use', 'more use'])
		.range([5, 10] );

	let legendSize = d3.legendSize()
		.scale(sizeScale)
		.shape('circle')
		.shapePadding(10)
		.labelAlign('end');

	node.on('click', (currentNode) => {
		d3.event.stopPropagation();
		let currentTarget = d3.event.currentTarget; // the <g> el

		expandBubble(currentTarget, currentNode, colors, data)
		graphItem(currentNode, itemData, unit);

		if (currentNode === focusedNode) {
			// no focusedNode or same focused node is clicked
			return;
		}
		let lastNode = focusedNode;
		focusedNode = currentNode;

		simulation.alphaTarget(0.2).restart();
		// hide all circle-overlay
		d3.selectAll('.circle-overlay').classed('hidden', true);
		d3.selectAll('.node-icon').classed('node-icon--faded', false);

		// don't fix last node to center anymore
		if (lastNode) {
			lastNode.fx = null;
			lastNode.fy = null;
			node.filter((d, i) => i === lastNode.index)
				.transition().duration(2000).ease(d3.easePolyOut)
				.tween('circleOut', () => {
					let irl = d3.interpolateNumber(lastNode.r, lastNode.radius);
					return (t) => {
						lastNode.r = irl(t);
					};
				})
				.on('interrupt', () => {
					lastNode.r = lastNode.radius;
				});
		}

		// if (!d3.event.active) simulation.alphaTarget(0.5).restart();

		d3.transition().duration(2000).ease(d3.easePolyOut)
			.tween('moveIn', () => {
				let ix = d3.interpolateNumber(currentNode.x, centerX);
				let iy = d3.interpolateNumber(currentNode.y, centerY);
				let ir = d3.interpolateNumber(currentNode.r, centerY * 0.5);
				return function (t) {
					// console.log('i', ix(t), iy(t));
					currentNode.fx = ix(t);
					currentNode.fy = iy(t);
					currentNode.r = ir(t);
					simulation.force('collide', forceCollide);
				};
			})
			.on('end', () => {
				simulation.alphaTarget(0);
				let $currentGroup = d3.select(currentTarget);
				$currentGroup.select('.circle-overlay')
					.classed('hidden', false);
				$currentGroup.select('.node-icon')
					.classed('node-icon--faded', true);

			})
			.on('interrupt', () => {
				currentNode.fx = null;
				currentNode.fy = null;
				simulation.alphaTarget(0);
			});

	});

	// blur
	d3.select(document).on('click', () => {
		let target = d3.event.target;

		// check if click on document but not on the circle overlay
		if (!target.closest('#circle-overlay') && focusedNode) {
			focusedNode.fx = null;
			focusedNode.fy = null;
			simulation.alphaTarget(0.2).restart();
			d3.transition().duration(2000).ease(d3.easePolyOut)
				.tween('moveOut', function () {
					let ir = d3.interpolateNumber(focusedNode.r, focusedNode.radius);
					return function (t) {
						focusedNode.r = ir(t);
						simulation.force('collide', forceCollide);
					};
				})
				.on('end', () => {
					focusedNode = null;
					simulation.alphaTarget(0);
				})
				.on('interrupt', () => {
					simulation.alphaTarget(0);
				});

			// hide all circle-overlay
			d3.selectAll('.circle-overlay').classed('hidden', true);
			d3.selectAll('.node-icon').classed('node-icon--faded', false);
		}
	});

	function ticked() {
		node
			.attr('transform', d => `translate(${d.x},${d.y})`)
			.select('circle')
			.attr('r', d => d.r);
	}
}

function getColorRange(colors, data) {
	return d3.scaleQuantile()
            .domain([0, 7, d3.max(data, (d) => d.value)])
            .range(colors);
}
