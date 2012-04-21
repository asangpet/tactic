$(document).ready(function() {
var w = 960,
    h = 500,
    fill = d3.scale.category20();

var vis = d3.select("#gchart")
  .append("svg:svg")
    .attr("width", w)
    .attr("height", h);

d3.json("http://"+window.location.hostname+"/tactic/graph", function(json) {
/*
  var force = d3.layout.force()
      .charge(-200)
      .linkDistance(100)
      .friction(0.5)
      .nodes(json.nodes)
      .links(json.links)
      .size([w, h])
      .start();
*/

  customlayout = function() {
	var layout = {};
	var drag;
	var nodes = json.nodes;
	var links = json.links;
	var event = d3.dispatch("tick");
	var dragnode;
	layout.start = function() {
		var i,j,n,l;
		n = nodes.length;
		l = links.length;

		var finish = 0;
		for (i=0;i < n;i++) {
			nodes[i].depth = -1;
		}

		// find node level using BFS
		var q = [ 0 ];
		nodes[0].depth = 0;
		var maxdepth = 1;
		while (q.length > 0) {
			var head = q[0];
			q = q.slice(1);
			var depth = nodes[head].depth;
			if (depth == maxdepth) maxdepth++;
			for (j=0;j<l;j++) {
				if (links[j].source == head && nodes[links[j].target].depth < 0) {
					nodes[links[j].target].depth = depth+1;
					q.push(links[j].target);
				}	
			}
		}

		for (i=0;i<maxdepth;i++) {
			var height = 1;
			for (j=0;j<n;j++) {
				if (nodes[j].depth == i) { nodes[j].vdepth = height++; }
			}
			for (j=0;j<n;j++) {
				if (nodes[j].depth == i) { nodes[j].maxvdepth = height; }
			}
		}

		var wportion = w/maxdepth;
		for (i=0;i<n;i++) {
			nodes[i].x = (nodes[i].depth)*w/(maxdepth)+wportion/2;
			nodes[i].y = (nodes[i].vdepth)*h/(nodes[i].maxvdepth);
		}
		for (i=0;i < l;i++) {
			links[i].source = nodes[links[i].source];
			links[i].target = nodes[links[i].target];
		}

		return layout;
	}

	layout.drag = function() {
		if (!drag) drag = d3.behavior.drag()
			.on("dragstart", function(d) { dragnode = d; } )
			.on("drag", function() { 
				if (dragnode) {
					dragnode.x += d3.event.dx; dragnode.y += d3.event.dy; 
					event.tick.dispatch({type:"tick"});
				}
			})
			.on("dragend", function() { dragnode = null; });

		this.call(drag);
		return layout;
	}

	layout.on = function(type, listener) {
		event[type].add(listener);
		return layout;
	}

	return layout;
  };
  var mylayout = customlayout().start();

  var link = vis.selectAll("line.link")
      .data(json.links)
    .enter().append("svg:line")
      .attr("class", "link")
      .style("stroke-width", function(d) { return Math.sqrt(d.value); })
      .attr("x1", function(d) { return d.source.x; })
      .attr("y1", function(d) { return d.source.y; })
      .attr("x2", function(d) { return d.target.x; })
      .attr("y2", function(d) { return d.target.y; });
/*
  var node = vis.selectAll("circle.node")
      .data(json.nodes)
    .enter().append("svg:circle")
      .attr("class", "node")
      .attr("cx", function(d) { return d.x; })
      .attr("cy", function(d) { return d.y; })
      .attr("r", 10)
      .style("fill", function(d) { return fill(d.group); })
      .call(mylayout.drag);
*/
/*
  var node = vis.selectAll("rect.node")
      .data(json.nodes)
    .enter().append("svg:rect")
      .attr("class", "node")
      .attr("width", 50)
      .attr("height", 50)
      .style("fill", function(d) { return fill(d.group); })
      .attr("x", function(d) { return d.x; })
      .attr("y", function(d) { return d.y; })      
      .call(mylayout.drag);
      */

  var node = vis.selectAll("svg.node")
      .data(json.nodes)
    .enter().append("svg:svg")
    .attr("class", "node")
    .call(mylayout.drag);

  node.append("svg:text")
      .style("fill", function(d) { return fill(d.group); })
      .style("text-anchor", "middle")
      .attr("x", function(d) { return d.x; })
      .attr("y", function(d) { return d.y; })      
      .text(function(d) { return d.name;})

  node.append("svg:title")
      .text(function(d) { return d.name; });

  node.append("svg:image")
      .attr("x", function(d) { return d.x-32; })
      .attr("y", function(d) { return d.y; })
      .attr("width", 64)
      .attr("height", 64)
      .attr("xlink:href", "/images/osa_server.svg");

  vis.style("opacity", 1e-6)
    .transition()
      .duration(1000)
      .style("opacity", 1);

  mylayout.on("tick", function() {
    link.attr("x1", function(d) { return d.source.x; })
        .attr("y1", function(d) { return d.source.y; })
        .attr("x2", function(d) { return d.target.x; })
        .attr("y2", function(d) { return d.target.y; });

    node.select("text")
    	.attr("x", function(d) { return d.x; })
        .attr("y", function(d) { return d.y; });
    node.select("image")
    	.attr("x", function(d) { return d.x-32; })
        .attr("y", function(d) { return d.y; });

  });
});

});
