$(document).ready( function() {
  var chart;
  var interval = 10000;
  var queryTime = (new Date()).getTime();
  Highcharts.setOptions({
	global:{useUTC:false},
	credits:{enabled:false},
  });

  var options = {
      chart: {
         renderTo: 'chartarea',
	 defaultSeriesType:'scatter',
         zoomType:'xy',
      },
      title: {
         text: 'Response time pdf',
      },
      plotOptions: {
	 scatter: {
		marker:{ radius:4, states: { hover:{enabled:true,lineColor:'rgb(100,100,100)'} } },
		states:{hover:{marker:{enabled:false}}}
	 },
       	 line: { 
		step:true,
		lineWidth:1,
		shadow:false,
		marker:{enabled:false, states:{hover:{enabled:true}}},
		states:{hover:{lineWidth:1}}
	 }
      },
      yAxis: {min:0},
      xAxis: {min:0},
      series: []
   };
   
  function requestData() {
	$.ajax({
		url: 'http://'+window.location.hostname+'/tactic/scatterResponseTime',
		success: function(data) {
			colorwheel = [ 'rgba(80,80,220,.2)', 'rgba(80,220,80,.2)', 'rgba(220,80,80,.2)', 
				       'rgba(220,80,220,.2)', 'rgba(220,220,80,.2)', 'rgba(220,80,220,.2)' 
					];
			colorid = 0;
			$.each(data, function(name,val) {
				options.series.push({name:name, color:colorwheel[colorid++], data:val});
				if (colorid == colorwheel.length) colorid = 0;
			});

			var chart = new Highcharts.Chart(options);
		},
		cache: false	
	});
  }
  
  requestData();   
});
