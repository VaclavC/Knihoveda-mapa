
/** Main class **/
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
		
		this.selectionYearFrom 	= null;
		this.selectionYearTo	= null;
		
		// Get timeline container
		this.cont = $('#' + this.contId);
		
		// Callbacks
		$(window).resize( () => { cgThis.draw(); });
		$('#' + this.contId).on('mousedown',	(ev) => { this.mouseDown(ev); });
		$('body')           .on('mouseup',		(ev) => { this.mouseUp(ev); });
		$('#' + this.contId).on('mousemove',	(ev) => { this.mouseMove(ev); });
		$('#' + this.contId).on('wheel',		(ev) => { this.mouseWheel(ev); });
		$('#' + this.contId).on('mouseleave',	(ev) => { this.mouseLeave(ev); });
		$('#' + this.contId).on('dblclick',		(ev) => { this.dblclick(ev); });
	}
	
	
	/* Data set methods */
	
	datasetSetData(index, color, years, counts)
	{
		this.dataSets.set(index, new DataSet(index, color, years, counts));
	}
	
	datasetClear(index)
	{
		this.dataSets.delete(index);
	}
	
	timeRangeSet(yearFrom, yearTo)
	{
		this.selectionYearFrom 	= yearFrom;
		this.selectionYearTo	= yearTo;
	}
	
	timeRangeClear()
	{
		this.selectionYearFrom 	= null;
		this.selectionYearTo	= null;
	}
	
	
	/* Drawing methods */
	
	draw()
	{
		var cgThis = this;
		
		this.initDraw();
		
		// Set size for all canvases
		$('#' + this.contId + " canvas").each(function () {
			this.width = cgThis.cont.width();
			this.height = cgThis.cont.height();
		});
		
		this.drawOsy();
		
		var dataSetsCtx = this.cont.find('canvas.dataSets')[0].getContext("2d");
		dataSetsCtx.clearRect(0, 0, this.contW, this.contH);
		this.dataSets.forEach(function (dataSet) {
			cgThis.drawDataSet(dataSetsCtx, dataSet);
		});
		
		this.drawSelection();
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
			// Rok - muzeme vzit rovnou z rozmezi datove sady
			cgThis.yearMin = Math.min(cgThis.yearMin, dataSet.yearMin);
			cgThis.yearMax = Math.max(cgThis.yearMax, dataSet.yearMax);
			
			// Pocet vysledku - musime najit aktualne nejvetsi ve vyrezu
			let actCountMax = Number.MIN_SAFE_INTEGER;
			let year1 = cgThis.xToYear(0);
			let year2 = cgThis.xToYear(cgThis.contW);
			dataSet.data.forEach(function (count, year) {
				if ( year >= year1 && year <= year2 )
					actCountMax = Math.max(actCountMax, count);
			});
			
			cgThis.countMax = Math.max(cgThis.countMax, actCountMax);
		});
	}
	
	drawOsy()
	{
		var ctx = this.cont.find('canvas.grid')[0].getContext("2d");
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
	
	drawDataSet(ctx, dataSet)
	{
		var cgThis = this;
		
		ctx.lineWidth = this.conf.lineWidth;
		ctx.strokeStyle = dataSet.color;
		ctx.fillStyle = dataSet.color;
		
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
	
	drawSelection()
	{
		var ctx = this.cont.find('canvas.selection')[0].getContext("2d");
		ctx.clearRect(0, 0, this.contW, this.contH);

		if ( this.selectionYearFrom === null  || this.selectionYearTo === null )
			return;
		
		let x1 = this.yearToX(this.selectionYearFrom);
		let x2 = this.yearToX(this.selectionYearTo);

		// Podbarveni
		ctx.fillStyle = this.conf.selectStyle;
		ctx.fillRect(x1, 0, x2 - x1, this.contH);
		
		// Letopocty na zacatku a konci
		ctx.fillStyle = this.conf.selectFontStyle;
		ctx.textAlign = "center";
		ctx.font = this.conf.selectFont;
		let k = (x2 > x1) ? 1 : -1;
		let textY = this.contH / 2;
		
		ctx.save();
		ctx.translate(x1, textY);
		ctx.rotate(-k*Math.PI/2);
		ctx.fillText(this.selectionYearFrom.toString(), 0, this.conf.selectTextDist);
		ctx.restore();

		ctx.save();
		ctx.translate(x2, textY);
		ctx.rotate(k*Math.PI/2);
		ctx.fillText(this.selectionYearTo.toString(), 0, this.conf.selectTextDist);
		ctx.restore();
	}
	
	
	/* Interactive controls */
	
	mouseDown(ev)
	{
		this.mousePrevPageX 	= ev.pageX;
		this.mouseDownButton	= ev.originalEvent.button;
		this.lastMouseDownTime 	= new Date().getTime();
		
		// Vyber casoveho intervalu
		if ( this.mouseDownButton == 0 )
		{
			var inChart, relX;
			[ inChart, relX ] = this.mousePos(ev);
			
			if ( inChart )
			{
				this.selectionYearFrom = Math.round(this.xToYear(relX));
				this.selectionYearTo = null;
			}			
		}
	}
	
	mouseUp(ev)
	{
		// Vyber casoveho intervalu
		if ( this.mouseDownButton == 0 && this.selectionYearFrom != null
				&& new Date().getTime() - this.lastMouseDownTime >= 200 )
		{
			let inChart, relX;
			[ inChart, relX ] = this.mousePos(ev);
			
			let year = Math.round(this.xToYear(relX));
			
			this.selectionYearTo = Math.max(year, this.selectionYearFrom);
			this.selectionYearFrom = Math.min(year, this.selectionYearFrom);
			
			this.drawSelection();
			
			console.log("Selection: " + this.selectionYearFrom + "-" + this.selectionYearTo);
			this.ajaxCall("S" + this.selectionYearFrom + "-" + this.selectionYearTo);
		}
		
		this.mouseDownButton = -1;
	}
	
	mouseMove(ev)
	{
		// Kurzor
		{
			let ctx = this.cont.find('canvas.cursor')[0].getContext("2d");
			ctx.clearRect(0, 0, this.contW, this.contH);
			
			let recordInfo = this.cont.parent().find('.timelineRecordInfo'); 
			
			var inChart, relX, relY;
			[ inChart, relX, relY ] = this.mousePos(ev);

			if ( inChart )
			{
				// Vybrany rok
				let yearSelected = Math.round(this.xToYear(relX));
				
				// Nejblizsi rok se zaznamy
				let yearWithData = Number.MIN_SAFE_INTEGER;
				this.dataSets.forEach(function (dataSet) {
					let yearBefore = Number.MIN_SAFE_INTEGER, yearAfter = Number.MAX_SAFE_INTEGER;
					for ( const year of dataSet.years )
						if ( year < yearSelected )
						{
							yearBefore = year;
						}
						else
						{
							yearAfter = year;
							break;
						}
					
					let yearCandidate = yearSelected - yearBefore > yearAfter - yearSelected ? yearAfter : yearBefore; 
					
					yearWithData = Math.abs(yearCandidate - yearSelected) < Math.abs(yearWithData - yearSelected) ?
							yearCandidate : yearWithData;
				});
				
				// Cara a popis pro vybrany rok
				let xSelected = this.yearToX(yearSelected);
				
				ctx.lineWidth = this.conf.cursorLineWidth1;
				ctx.strokeStyle = this.conf.cursorStyle1;
				ctx.fillStyle = this.conf.cursorStyle1;
				ctx.beginPath();
				ctx.moveTo(xSelected, 0);
				ctx.lineTo(xSelected, this.contH);
				ctx.stroke();
				
				ctx.fillStyle = this.conf.cursorStyle1;
				ctx.font = this.conf.timeAxisFont;
				ctx.textAlign = "right";
				ctx.fillText(yearSelected.toString() + " ", xSelected, this.conf.timeAxisTextY);

				// Cara pro rok se zaznamy 
				let xWithData = this.yearToX(yearWithData);
				
				ctx.lineWidth = this.conf.cursorLineWidth2;
				ctx.strokeStyle = this.conf.cursorStyle2;
				ctx.fillStyle = this.conf.cursorStyle2;
				ctx.beginPath();
				ctx.moveTo(xWithData, 0);
				ctx.lineTo(xWithData, this.contH);
				ctx.stroke();
				
				// Panel pro rok se zaznamy
				$(recordInfo).show();
				
				$(recordInfo).find(".vysledky").empty();
				$(recordInfo).find(".year").html(yearWithData.toString());
				this.dataSets.forEach(function(dataset)
				{
					if ( dataset.data.has(yearWithData) )
						$(recordInfo).find(".vysledky")
							.append(`<div class="vysledek">` +
									`<span class="dataSetIndicator" style="background-color: ${dataset.color}">&nbsp;</span>` +
										`&nbsp;`+
										`<span class="pocet">${dataset.data.get(yearWithData)}</span>`
									+ `</div>`);
				}, this);

				let panelXPos = xWithData;
				if ( panelXPos > 4*this.contW/5)
				{
					panelXPos -= $(recordInfo).width();
					$(recordInfo).addClass("mirrored");
				}
				else
				{
					$(recordInfo).removeClass("mirrored");
				}
				$(recordInfo).css({ left: panelXPos})
			}
			else
			{
				$(recordInfo).hide();
			}
		}
		
		// Vyber casoveho intervalu
		if ( this.mouseDownButton == 0 && this.selectionYearFrom != null )
		{
			let inChart, relX;
			[ inChart, relX ] = this.mousePos(ev);
			
			let year = Math.round(this.xToYear(relX));
			
			this.selectionYearTo = Math.max(year, this.selectionYearFrom);
			this.selectionYearFrom = Math.min(year, this.selectionYearFrom);
			
			this.drawSelection();
		}
		
		// Posun grafu
		if ( this.mouseDownButton == 1 )
		{
			let deltaX = ev.pageX - this.mousePrevPageX;
			this.mousePrevPageX = ev.pageX;
			
			this.shiftX -= deltaX;

			this.fixViewport();
			
			this.draw();
		}
	}
	
	mouseWheel(ev)
	{
		var whEv = ev.originalEvent;
		var inChart, mouseX;
		[ inChart, mouseX ] = this.mousePos(whEv);
		
		if ( inChart )
		{
			var year = this.xToYear(mouseX);
			
			var oldScaleX = this.scaleX;
			if ( whEv.deltaY > 0 )
				this.scaleX /= this.conf.wheelScaleK;
			else
				this.scaleX *= this.conf.wheelScaleK;
		
			this.shiftX += this.graphW * (year - this.yearMin) * (this.scaleX - oldScaleX) / (this.yearMax - this.yearMin);
			
			this.fixViewport();
			
			this.draw();
		}
	}
	
	mouseLeave(ev)
	{
		// Kurzor
		{
			let ctx = this.cont.find('canvas.cursor')[0].getContext("2d");
			ctx.clearRect(0, 0, this.contW, this.contH);
			
			let recordInfo = this.cont.parent().find('.timelineRecordInfo');
			$(recordInfo).hide();
		}
	}
	
	dblclick(ev)
	{
		this.selectionYearTo = null;
		this.selectionYearFrom = null;
		
		this.drawSelection();
		
		console.log("Clear selection");
		this.ajaxCall("C");
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

	fixViewport()
	{
		var x1 = this.yearToX(this.yearMin);
		if ( x1 > this.conf.paddingLeft )
			this.shiftX += x1 - this.conf.paddingLeft;
		
		var x2 = this.yearToX(this.yearMax);
		if ( x2 < this.contW - this.conf.paddingRight )
		{
			this.shiftX -= this.contW - this.conf.paddingRight - x2;
			
			if ( this.shiftX < 0 )
			{
				this.scaleX = 1.0;
				this.shiftX = 0;
			}
		}
	}
	
	mousePos(ev)
	{
		var parentOffset = $('#' + this.contId).offset();
		var relX = ev.pageX - parentOffset.left;
		var relY = this.contH - (ev.pageY - parentOffset.top);
		
		if ( relX < this.conf.paddingLeft || relX > this.contW - this.conf.paddingRight
				|| relY < this.conf.paddingBottom || relY > this.contH - this.conf.paddingTop )
			return [false, relX, relY];
		
		return [true, relX, relY];
	}

	
	/* Ajax */
	
	ajaxCall(data)
	{
		Wicket.Ajax.post({
			"u": $('#' + this.contId).data("callback"),
			"ep": {
				"data" : data,
			},
			"i": "ajaxIndicator",
		});
	}
	
}


/** Data class */
class DataSet
{
	constructor(index, color, years, counts)
	{
		var dsThis = this;
		
		this.index = index;
		this.color = color;
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
