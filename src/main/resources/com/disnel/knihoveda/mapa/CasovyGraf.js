
var CasovyGraf =
{

	paddingX: 32,
	timelineStrokeStyle: "#c0c0c0",
	timelineFillStyle: "#909090",
	timelineFont: "13px Arial",
	cursorLineWidth: 1,
	cursorStrokeStyle: "#ff0000",
	cursorFillStyle: "#ff0000",
	cursorYearRadius: 4,
	cursorTextHeight: 14,
	cursorTextFont: "12px Arial black",
		
	container: null,
	rokOd: null, rokDo: null,
	actRokOd: null, actRokDo: null,
	maxCount: null,
	
	chartData: {},
	
	init: function(id, rokOd, rokDo)
	{
		CasovyGraf.container = $('#' + id);
		CasovyGraf.rokOd = rokOd;
		CasovyGraf.rokDo = rokDo;
		CasovyGraf.actRokOd = rokOd;
		CasovyGraf.actRokDo = rokDo;
		
		CasovyGraf.container.mousemove(CasovyGraf.mousemove);
		CasovyGraf.container.mouseleave(CasovyGraf.mouseleave);
	},
		
	setMaxCount: function(maxCount)
	{
		CasovyGraf.maxCount = maxCount;
	},
	
	addChart: function(chartId, chartData)
	{
		CasovyGraf.chartData[chartId] = chartData;
	},
	
	redraw: function()
	{
		CasovyGraf.contW = CasovyGraf.container.width();
		CasovyGraf.contH = CasovyGraf.container.height();
		CasovyGraf.x1 = CasovyGraf.paddingX;
		CasovyGraf.x2 = CasovyGraf.contW - CasovyGraf.paddingX;
		CasovyGraf.y1 = 0;
		CasovyGraf.y2 = CasovyGraf.contH;
		
		CasovyGraf.container.find("canvas").each(function() {
			this.width = CasovyGraf.contW;
			this.height = CasovyGraf.contH;
		});
		
		CasovyGraf.drawCasovaOsa();
		
		for ( var chartId in CasovyGraf.chartData )
			CasovyGraf.drawChart(CasovyGraf.chartData[chartId], 2, "#000000");
	},
	
	mousemove: function(ev)
	{
		var x = Math.min(Math.max(ev.clientX, CasovyGraf.paddingX), CasovyGraf.contW - CasovyGraf.paddingX) - 0.5;
		var year = CasovyGraf.yearFromX(ev.clientX);
		
		var ctxOver = CasovyGraf.container.find("canvas.overlay")[0].getContext("2d");
		ctxOver.clearRect(0, 0, CasovyGraf.contW, CasovyGraf.contH);

		ctxOver.lineWidth = CasovyGraf.cursorLineWidth;
		ctxOver.strokeStyle = CasovyGraf.cursorStrokeStyle;
		ctxOver.fillStyle = CasovyGraf.cursorFillStyle;
		
		// Svisla cata
		ctxOver.beginPath();
		ctxOver.moveTo(x, CasovyGraf.y1);
		ctxOver.lineTo(x, CasovyGraf.y2);
		ctxOver.stroke();
		
		// Body oznacuji ohraniceni intervalu a informani text
		var yearLess, yeareMore;
		for ( var chartId in CasovyGraf.chartData )
		{
			var data = CasovyGraf.chartData[chartId];
			
			for ( var actYear in data )
			{
				if ( actYear <= year)
					yearLess = actYear;
				
				if ( actYear >= year)
				{
					yearMore = actYear;
					break;
				}
			}
			
			if ( typeof yearLess !== 'undefined' )
			{
				ctxOver.beginPath();
				ctxOver.arc(CasovyGraf.xFromYear(yearLess), CasovyGraf.yFromCount(data[yearLess]),
						CasovyGraf.cursorYearRadius, 0, 2*Math.PI, false);
				ctxOver.fill();
				ctxOver.arc(CasovyGraf.xFromYear(yearMore), CasovyGraf.yFromCount(data[yearMore]),
						CasovyGraf.cursorYearRadius, 0, 2*Math.PI, false);
				ctxOver.fill();
				
				var textY = (CasovyGraf.y1 + CasovyGraf.y2) / 2;
				ctxOver.font = CasovyGraf.cursorTextFont;
				ctxOver.textAlign = ( x > CasovyGraf.contW / 2 ) ? "right" : "left";
				ctxOver.fillText(" rok " + yearLess.toString() + ": " + data[yearLess].toString() + " ",
						x, textY - 0.5*CasovyGraf.cursorTextHeight*(yearMore > yearLess));
				if ( yearMore > yearLess )
					ctxOver.fillText(" rok " + yearMore.toString() + ": " + data[yearMore].toString() + " ",
						x, textY + 0.5*CasovyGraf.cursorTextHeight);
			}
		}
	},
	
	mouseleave: function(ev)
	{
		var ctxOver = CasovyGraf.container.find("canvas.overlay")[0].getContext("2d");
		ctxOver.clearRect(0, 0, CasovyGraf.contW, CasovyGraf.contH);
	},
	
	drawCasovaOsa: function()
	{
		var timeLen = CasovyGraf.actRokDo - CasovyGraf.actRokOd;
		var timeStep = (timeLen > 100) ? 50 : ( (timeLen > 10) ? 5: 1 );
		var time1 = timeStep * Math.floor(CasovyGraf.actRokOd / timeStep);
		var time2 = timeStep * Math.floor(CasovyGraf.actRokDo / timeStep + 1);
		
		var textY = CasovyGraf.y1 + 14;
		
		var ctx = CasovyGraf.container.find("canvas.charts")[0].getContext("2d");
		ctx.lineWidth = 1;
		ctx.strokeStyle = CasovyGraf.timelineStrokeStyle;
		ctx.fillStyle = CasovyGraf.timelineFillStyle;
		ctx.font = CasovyGraf.timelineFont;
		ctx.beginPath();
		
		for ( var t = time1; t <= time2; t += timeStep )
		{
			var actX = Math.floor(CasovyGraf.xFromYear(t)) - 0.5;
			
			ctx.moveTo(actX, CasovyGraf.y1);
			ctx.lineTo(actX, CasovyGraf.y2);
			
			ctx.fillText(t.toString(), actX, textY);
		}
		
		ctx.stroke();
	},

	drawChart: function(data, lineWidth, strokeStyle)
	{
		var ctx = CasovyGraf.container.find("canvas.charts")[0].getContext("2d");
		ctx.lineWidth = lineWidth;
		ctx.strokeStyle = strokeStyle;
		ctx.beginPath();
		
		for ( var year in data )
		{
			var actX = CasovyGraf.xFromYear(year);
			var actY = CasovyGraf.yFromCount(data[year]);
			
			ctx.lineTo(actX, actY);
		}
		
		ctx.stroke();
	},
	
	xFromYear: function(year)
	{
		var k = (year - CasovyGraf.actRokOd) / (CasovyGraf.actRokDo - CasovyGraf.actRokOd);
		var x = CasovyGraf.x1 + k * (CasovyGraf.x2 - CasovyGraf.x1);
		return x;
	},
	
	yearFromX: function(x)
	{
		var k = (x - CasovyGraf.x1) / (CasovyGraf.x2 - CasovyGraf.x1);
		var y = CasovyGraf.actRokOd + k * (CasovyGraf.actRokDo - CasovyGraf.actRokOd);
		return y;
	},
	
	yFromCount: function(count)
	{
		return CasovyGraf.y2 - (count / CasovyGraf.maxCount ) * (CasovyGraf.y2 - CasovyGraf.y1)
	},
	
};
