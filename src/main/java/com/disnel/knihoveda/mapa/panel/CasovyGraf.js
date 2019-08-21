
class CasovyGraf
{

	/* Constructor */
	
	constructor(contId, conf)
	{
		var cgThis = this;
		
		// Parameters
		this.contId = contId;
		this.conf = conf;
		
		// Init state variables
		this.scaleX = 1.0;
		this.scaleY = 1.0;
		this.shiftX = 0.0;
		
		this.dataSets = new Map();
		
		// Get timeline container
		this.cont = $('#' + this.contId);
		
		// Set size for all canvases
		$('#' + this.contId + " canvas").each(function () {
			this.width = cgThis.cont.width();
			this.height = cgThis.cont.height();
		});
		
		// Callbacks
		$(window).resize( () => { cgThis.draw(); });
	}
	
	
	/* Data set methods */
	
	datasetSetData(index, years, counts)
	{
		this.dataSets.set(index, new DataSet(index, years, counts));
	}
	
	datasetClear(index)
	{
		this.dataSets.delete(index);
	}
	
	
	/* Drawing methods */
	
	draw()
	{
		var cgThis = this;
		
		this.initDraw();
		this.drawOsy();
		this.dataSets.forEach(function (dataSet) {
			cgThis.drawDataSet(dataSet);
		});
	}
	
	
	initDraw()
	{
		var cgThis = this;
		
		// Get container dimensions
		this.contW = this.cont.width();
		this.contH = this.cont.height();
		this.graphW = this.contW - this.conf.paddingLeft - this.conf.paddingRight;
		this.graphH = this.contH - this.conf.paddingTop - this.conf.paddingBottom;
		
		// Get display data dimensions
		this.gW = this.conf.yearMax - this.conf.yearMin;
		this.gH = this.conf.countMax;
		
		// Find limits for displayed data
		this.countMin = 0;
		this.countMax = Number.MIN_SAFE_INTEGER
		this.yearMin = Number.MAX_SAFE_INTEGER;
		this.yearMax = Number.MIN_SAFE_INTEGER;
		
		this.dataSets.forEach(function (dataSet) {
			cgThis.countMax = Math.max(cgThis.countMax, dataSet.countMax);
			cgThis.yearMin = Math.min(cgThis.yearMin, dataSet.yearMin);
			cgThis.yearMax = Math.max(cgThis.yearMax, dataSet.yearMax);
		});
		
		console.log("countMax: " + this.countMax + " years: " + this.yearMin + " - " + this.yearMax);
	}
	
	drawOsy()
	{
		var ctx = this.cont.find('.grid canvas')[0].getContext("2d");
		ctx.clearRect(0, 0, this.contW, this.contH);
		
		// Casova osa
		var timeLen = this.yearMax - this.yearMin;
		var timeStep = (timeLen > 100) ? 50 : ( (timeLen > 20) ? 5 : 1 );
		var time1 = timeStep * Math.floor(this.yearMin / timeStep);
		var time2 = timeStep * Math.floor(this.yearMax / timeStep + 1);
		
		ctx.lineWidth = 1;
		ctx.strokeStyle = this.conf.timeAxisStyle;
		ctx.fillStyle = this.conf.timeAxisFontStyle;
		ctx.font = this.conf.timeAxisFont;
		
		ctx.beginPath();
		for ( var t = time1; t <= time2; t += timeStep )
		{
			var actX = Math.floor(this.yearToX(t)) + 0.5;
			
			ctx.moveTo(actX, 0);
			ctx.lineTo(actX, this.contH);
			
			if ( actX < this.contW - timeStep)
				ctx.textAlign = "left";
			else
				ctx.textAlign = "right"; 
			ctx.fillText(" " + t.toString() + " ", actX, this.conf.timeAxisTextY);
		}
		ctx.stroke();

		// Osa poctu vysledku
		var countStep = (this.countMax > 100) ? 100 : ( (this.countMax > 10) ? 10 : 2 );
		
		ctx.lineWidth = 1;
		ctx.strokeStyle = this.conf.countAxisStyle;
		ctx.fillStyle = this.conf.countAxisFontStyle;
		ctx.font = this.conf.countAxisFont;
		
		ctx.beginPath();
		for ( var c = 0; c < this.countMax; c += countStep)
		{
			var actY = Math.floor(this.countToY(c)) + 0.5;
			
			ctx.moveTo(0, actY);
			ctx.lineTo(this.contW, actY);
			
			ctx.textAlign = "left";
			ctx.fillText(" " + c.toString(), this.conf.countAxisTextX, actY - 3);
		}
		ctx.stroke();
	}
	
	drawDataSet(dataSet)
	{
		var cgThis = this;
		
		var dataSetCont = $('#' + this.contId + " .dataSet")[dataSet.index];
		var color = $(dataSetCont).data('color');
		
		var ctx = $(dataSetCont).find('canvas')[0].getContext("2d");
		ctx.clearRect(0, 0, this.contW, this.contH);
		
		ctx.lineWidth = this.conf.lineWidth;
		ctx.strokeStyle = color;
		ctx.fillStyle = color;
		
		ctx.beginPath();
		for ( var year = dataSet.yearMin; year <= dataSet.yearMax; year++ )
		{
			var actX = this.yearToX(year);
			var actY;
			if ( dataSet.data.has(year) )
				actY = this.countToY(dataSet.data.get(year));
			else
				actY = this.countToY(0);
			
			ctx.lineTo(actX, actY);
		}
		ctx.stroke();
		
		dataSet.data.forEach(function (count, year) {
			var actX = cgThis.yearToX(year);
			var actY = cgThis.countToY(count);

			ctx.beginPath();
			ctx.arc(actX, actY, cgThis.conf.dotSize/2, 0, 2*Math.PI, false);
			ctx.fill();
		});
	}
	
	drawDataSetByIndex(index)
	{
		this.drawDataSet(this.dataSets.get(index));
	}
	
	
	
	/* Coordinate transformations */
	
	yearToX(year)
	{
		return this.scaleX * this.graphW * ( year - this.yearMin) / (this.yearMax - this.yearMin) - this.shiftX + this.conf.paddingLeft;
	}
	
	xToYear(x)
	{
		return this.yearMin + (x + this.shiftX - this.conf.paddingLeft) * (this.yearMax - this.yearMin) / this.scaleX / this.graphW;
	}
	
	countToY(count)
	{
		return this.conf.paddingTop + this.graphH - this.scaleY * this.graphH * count / this.countMax;
	}
	
	yToCount(y)
	{
		return (this.graphH + this.conf.paddingTop - y) * this.countMax / this.scaleY / this.graphH;
	}
	
}



class DataSet
{
	constructor(index, years, counts)
	{
		var dsThis = this;
		
		this.index = index;
		this.years = years;
		this.counts = counts;
		
		this.data = new Map();
		years.forEach(function(year, i) {
			dsThis.data.set(year, counts[i]);
		});
		
		this.yearMin = Math.min(...years);
		this.yearMax = Math.max(...years);
		this.countMin = Math.min(...counts);
		this.countMax = Math.max(...counts);
	}	
}
