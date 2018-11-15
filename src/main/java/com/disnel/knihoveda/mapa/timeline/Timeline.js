

class Timeline {
	
	constructor(tagID, config)
	{
		// Nastaveni parametru
		this.tagID = tagID;
		this.config = config;
		this.tagSel = "#" + tagID;
		this.detailPanelSel = "#" + config.detailPanelId;
		this.ajaxCallback = $(this.tagSel).data("callback");
		this.wrapSel = this.tagSel + "-wrap";
		
		this.yearSelectFrom = null;
		this.yearSelectTo = null;

		// Dalsi hladiny pro vykreslovani
		$(this.tagSel).wrap('<div id="' + this.tagID + '-wrap" style="position: absolute; width: 100%; height: 100%; left: 0; top: 0;"></div>');
		$(this.tagSel).parent().prepend('<canvas id="' + this.tagID + '-bottom" style="z-index: -1; pointer-events: none"></canvas>' );
		$(this.tagSel).css("z-index", 0);
		$(this.tagSel).parent().append('<canvas id="' + this.tagID + '-top" style="z-index: 1; pointer-events: none"></canvas>' );
		
		// Callbacky
		$(window).on('resize', () => { this.draw(); });
		$(this.tagSel).on('mouseleave', (ev) => { this.mouseLeave(ev); });
		$(this.tagSel).on('mousedown', (ev) => { this.mouseDown(ev); });
		$("body").on('mouseup', (ev) => { this.mouseUp(ev); });
		$(this.tagSel).on('mousemove', (ev) => { this.mouseMove(ev); });
		$(this.tagSel).on('dblclick', (ev) => { this.dblclick(ev); });
		$(this.tagSel).on('wheel', (ev) => { this.wheel(ev); });
	}

	setData(dataSets)
	{
		this.dataSets = dataSets;
	}
	
	draw()
	{
		this.findDataLimits();
		this.initDraw();
		this.drawOsy();
		this.dataSets.forEach(function(dataset) {
			this.drawDataset(dataset);
		}, this);
		this.drawSelection(this.yearSelectFrom, this.yearSelectTo);
	}
	
	/************************************
	 * Minima a maxima zobrazovanych dat
	 */
	findDataLimits()
	{
		var yearMin = Number.MAX_SAFE_INTEGER,
			yearMax = Number.MIN_SAFE_INTEGER;
		var countMin = Number.MAX_SAFE_INTEGER,
			countMax = Number.MIN_SAFE_INTEGER;
			
		this.dataSets.forEach(function(dataset)
		{
			var dsYearMin = Number.MAX_SAFE_INTEGER,
			    dsYearMax = Number.MIN_SAFE_INTEGER;
			
			for (const [year, count] of Object.entries(dataset.data))
			{
				dsYearMin = Math.min(dsYearMin, year);
				dsYearMax = Math.max(dsYearMax, year);
				
				countMin = Math.min(countMin, count);
				countMax = Math.max(countMax, count);
			}
			
			dataset.yearMin = dsYearMin;
			dataset.yearMax = dsYearMax;
			
			yearMin = Math.min(yearMin, dsYearMin);
			yearMax = Math.max(yearMax, dsYearMax);
		});
		
		if ( yearMax - yearMin < 50)
		{
			var mid = Math.floor((yearMin + yearMax) / 2);
			yearMin = mid - 25;
			yearMax = mid + 25;
		}
		
		if ( countMax < 10 )
			countMax = 10;
		
		this.yearMin = yearMin;
		this.yearMax = yearMax;
		this.countMin = countMin;
		this.countMax = countMax;
	}
	
	/*************************
	 * Priprava na vykresleni
	 */
	initDraw()
	{
		// Najit elementy a inicializovat kontext
		this.tag = $(this.tagSel);
		this.canvas = this.tag[0];
		this.ctx = this.canvas.getContext("2d");
		this.canvasTop = $(this.tagSel + "-top")[0];
		this.ctxTop =  this.canvasTop.getContext("2d");
		this.canvasBottom = $(this.tagSel + "-bottom")[0];
		this.ctxBottom =  this.canvasBottom.getContext("2d");
		
		// Nastavit velikost canvasu
		this.canvas.width = this.tag.width();
		this.canvas.height = this.tag.height();
		this.canvasTop.width = this.tag.width();
		this.canvasTop.height = this.tag.height();
		this.canvasBottom.width = this.tag.width();
		this.canvasBottom.height = this.tag.height();
		
		this.contWidth = $(this.wrapSel).parent().width();
		this.wrapWidth = $(this.wrapSel).width();
		this.wrapLeft = -$(this.wrapSel).position().left;
		
		// Nastavit limity pro graf
		this.x1 = this.config.paddingLeft;
		this.x2 = this.canvas.width - this.config.paddingRight;
		this.y1 = this.config.paddingBottom;
		this.y2 = this.canvas.height - this.config.paddingTop;
	}
	
	/********************************
	 * Vykresit osy (casova a pocet)
	 */
	drawOsy()
	{
		// Spolecne nastaveni
		
		// Casova osa
		var timeLen = (this.yearMax - this.yearMin) * this.contWidth / this.wrapWidth;
		var timeStep = (timeLen > 100) ? 50 : ( (timeLen > 20) ? 5 : 1 );
		var time1 = timeStep * Math.floor(this.yearMin / timeStep);
		var time2 = timeStep * Math.floor(this.yearMax / timeStep + 1);
		
		var textY = this.config.timeAxisTextY;
		
		this.ctx.lineWidth = 1;
		this.ctx.strokeStyle = this.config.timeAxisStyle;
		this.ctx.fillStyle = this.config.timeAxisFontStyle;
		this.ctx.font = this.config.timeAxisFont;
		
		this.ctx.beginPath();
		for ( var t = time1; t <= time2; t += timeStep )
			if ( t >= this.yearMin && t <= this.yearMax )
			{
				var actX = Math.floor(this.xFromYear(t)) + 0.5;
				
				this.ctx.moveTo(actX, 0);
				this.ctx.lineTo(actX, this.canvas.height);
				
				if ( actX < this.canvas.width - timeStep)
					this.ctx.textAlign = "left";
				else
					this.ctx.textAlign = "right"; 
				this.ctx.fillText(" " + t.toString() + " ", actX, textY);
			}
		this.ctx.stroke();
		
		// Osa poctu vysledku
		var countStep = (this.countMax > 100) ? 100 : ( (this.countMax > 10) ? 10 : 2 );
		
		this.ctx.lineWidth = 1;
		this.ctx.strokeStyle = this.config.countAxisStyle;
		this.ctx.fillStyle = this.config.countAxisStyle;
		this.ctx.font = this.config.countAxisFont;
		
		this.ctx.beginPath();
		for ( var c = 0; c < this.countMax; c += countStep)
		{
			var actY = this.yFromCount(c);
			
			this.ctx.moveTo(0, actY);
			this.ctx.lineTo(this.canvas.width, actY);
			
			this.ctx.textAlign = "left";
			this.ctx.fillText(" " + c.toString(), 0, actY - 3);
		}
		this.ctx.stroke();
	}

	/*******************************
	 * Vykreslit jednu datovou sadu
	 */
	drawDataset(dataset)
	{
		this.ctx.lineWidth = this.config.lineWidth;
		this.ctx.strokeStyle = dataset.color;
		this.ctx.fillStyle = dataset.color;
		
		this.ctx.beginPath();
		for ( year = dataset.yearMin; year <= dataset.yearMax; year++ )
		{
			var actX = this.xFromYear(year);
			var actY;
			if ( year in dataset.data )
				actY = this.yFromCount(dataset.data[year]);
			else
				actY = this.yFromCount(0);
			
			this.ctx.lineTo(actX, actY);
		}
		this.ctx.stroke();
		
		for (var year in dataset.data)
		{
			var actX = this.xFromYear(year);
			var actY = this.yFromCount(dataset.data[year]);

			this.ctx.beginPath();
			this.ctx.arc(actX, actY, this.config.dotSize/2, 0, 2*Math.PI, false);
			this.ctx.fill();
		}
	}
	
	/****************
	 * Vymaze vyber
	 */
	clearSelection()
	{
		this.ctxBottom.clearRect(0, 0, this.canvas.width, this.canvas.height);
	}
	
	/*******************************
	/* Nakresli vyber na casove ose 
	 */
	drawSelection(yearFrom, yearTo)
	{
		if ( yearFrom === null || yearTo === null )
			return;
		
		let x1 = this.xFromYear(yearFrom);
		let x2 = this.xFromYear(yearTo);

		// Vymazat
		this.clearSelection();

		// Podbarveni
		this.ctxBottom.fillStyle = this.config.selectStyle;
		this.ctxBottom.fillRect(x1, 0, x2 - x1, this.canvas.height);
		
		// Letopocty na zacatku a konci
		this.ctxBottom.fillStyle = this.config.selectFontStyle;
		this.ctxBottom.textAlign = "center";
		this.ctxBottom.font = this.config.selectFont;
		let k = (x2 > x1) ? 1 : -1;
		let textY = this.canvas.height/2;
		
		this.ctxBottom.save();
		this.ctxBottom.translate(x1, textY);
		this.ctxBottom.rotate(-k*Math.PI/2);
		this.ctxBottom.fillText(yearFrom.toString(),
				0, this.config.selectTextDist);
		this.ctxBottom.restore();

		this.ctxBottom.save();
		this.ctxBottom.translate(x2, textY);
		this.ctxBottom.rotate(k*Math.PI/2);
		this.ctxBottom.fillText(yearTo.toString(),
				0, this.config.selectTextDist);
		this.ctxBottom.restore();
	}
	
	/****************
	 * Interaktivita
	 */
	
	mouseMove(ev)
	{
		// Vymazat canvas
		this.ctxTop.clearRect(0, 0, this.canvas.width, this.canvas.height);

		// Ziskat pozici mysi v grafu
		var inChart, relX, relY;
		[ inChart, relX, relY ] = this.mousePosInChart(ev);
		
		// Najit rok se zaznamem nejblizsi kurzoru
		var yearSelect = this.yearFromX(relX);
		var yearWithData = this.closestYearTo(yearSelect);

		// Namalovat vertikalni cary
		var xSelected = this.xFromYear(yearSelect);
		var xWithData = this.xFromYear(yearWithData);

		this.ctxTop.lineWidth = this.config.cursorLineWidth2;
		this.ctxTop.strokeStyle = this.config.cursorStyle2;
		this.ctxTop.fillStyle = this.config.cursorStyle2;
		this.ctxTop.beginPath();
		this.ctxTop.moveTo(xWithData, 0);
		this.ctxTop.lineTo(xWithData, this.canvas.height);
		this.ctxTop.stroke();
		
		this.ctxTop.lineWidth = this.config.cursorLineWidth1;
		this.ctxTop.strokeStyle = this.config.cursorStyle1;
		this.ctxTop.fillStyle = this.config.cursorStyle1;
		this.ctxTop.beginPath();
		this.ctxTop.moveTo(xSelected, 0);
		this.ctxTop.lineTo(xSelected, this.canvas.height);
		this.ctxTop.stroke();
		
		// Aktualni vybrany rok
		this.ctxTop.fillStyle = this.config.cursorStyle1;
		this.ctxTop.font = this.config.timeAxisFont;
		this.ctxTop.textAlign = "right";
		this.ctxTop.fillText(yearSelect.toString() + " ", xSelected, this.config.timeAxisTextY);
		
		// Zobrazit panel s vysledky
		$(this.detailPanelSel).show();
		$(this.detailPanelSel + " .head").html(yearWithData.toString());
		$(this.detailPanelSel + " .vysledek").remove();
		this.dataSets.forEach(function(dataset)
		{
			if ( yearWithData in dataset.data )
			{
				$("#" + this.config.detailPanelId)
					.append(`<div class="vysledek">` +
								`<span class="dataSetIndicator" style="background-color: ${dataset.color}">&nbsp;</span>` +
								`&nbsp;`+
								`<span class="pocet">${dataset.data[yearWithData]}</span>` +
							`</div>`);
			}
		}, this);
		
		let xPos = xWithData - this.wrapLeft;
		if ( xPos > 4*this.contWidth/5)
			xPos -= $(this.detailPanelSel).width();
		
		let yPos = relY;
		let panelHeight = $(this.detailPanelSel).height(); 
		if ( relY + panelHeight > this.canvas.height )
			yPos -= panelHeight;
		
		$(this.detailPanelSel).css({ top: yPos, left: xPos})
		
		// Vyber casoveho intervalu
		if ( this.mouseMoveButton == 1 )
		{
			let yearTo = ( this.yearSelectTo !== null ) ? this.yearSelectTo : yearSelect;
			this.drawSelection(this.yearSelectFrom, yearTo);
		}
		
		// Posun grafu
		if ( this.mouseMoveButton == 2 )
		{
			let deltaX = ev.pageX - this.lastMouseX;
			this.lastMouseX = ev.pageX;
			
			let newLeft = this.wrapLeft - deltaX;
			
			if ( newLeft < 0 )
				return;
			
			if ( this.wrapWidth - newLeft <  this.contWidth )
				return;
			
			$(this.wrapSel).css({left: -newLeft});
			this.draw();
		}
	}

	mouseLeave(ev)
	{
		this.ctxTop.clearRect(0, 0, this.canvas.width, this.canvas.height);
		$(this.detailPanelSel).hide();
	}
	
	mouseDown(ev)
	{
		if ( ev.originalEvent.button == 0 )
		{
			this.lastMouseDownTime = new Date().getTime();
			
			var inChart, relX, relY;
			[ inChart, relX, relY ] = this.mousePosInChart(ev);
			
			if ( !inChart )
				return;
			
			var yearSelect = this.yearFromX(relX);
			
			this.yearSelectFrom = yearSelect;
			this.yearSelectTo = null;
			
			this.mouseMoveButton = 1;
		}
		
		if ( ev.originalEvent.button == 1 )
		{
			this.lastMouseX = ev.pageX;
			
			this.mouseMoveButton = 2;
		}
	}
	
	mouseUp(ev)
	{
		if ( ev.originalEvent.button == 0 )
		{
			if ( this.yearSelectFrom == null )
				return;
			
			var inChart, relX, relY;
			[ inChart, relX, relY ] = this.mousePosInChart(ev);
			
			if ( !inChart ||
					new Date().getTime() - this.lastMouseDownTime < 200 )
			{
				this.yearSelectFrom = null;
				return;
			}
			
			var yearSelect = this.yearFromX(relX);
			
			if ( yearSelect < this.yearSelectFrom )
			{
				this.yearSelectTo = this.yearSelectFrom;
				this.yearSelectFrom = yearSelect;
			}
			else
			{
				this.yearSelectTo = yearSelect;
			}
			
			this.ajaxCall("S" + this.yearSelectFrom + "-" + this.yearSelectTo);
		}
	
		this.mouseMoveButton = 0;
	}
	
	dblclick(ev)
	{
		this.ctxBottom.clearRect(0, 0, this.canvas.width, this.canvas.height);
		
		this.yearSelectFrom = null;
		this.yearSelectTo = null;

		this.ajaxCall("C");
	}
	
	wheel(ev)
	{
		var whEv = ev.originalEvent;
		var mouseX = whEv.clientX - $(this.wrapSel).parent().offset().left;
		
		// Nova sirka
		var newWrapW;
		if ( whEv.deltaY > 0 )
			newWrapW = this.wrapWidth / this.config.wheelScaleK;
		else
			newWrapW = this.wrapWidth * this.config.wheelScaleK;
		
		if ( newWrapW < this.contWidth )
			newWrapW = this.contWidth;
		
		if ( newWrapW > 32000 )
			newWrapW = 32000;
		
		// Nova pozice X
		var k = newWrapW / this.wrapWidth;  // Tady musime vzit jak se to skutecne zmenilo, ne co bylo podle kolecka
		var newLeft = k * ( mouseX + this.wrapLeft ) - mouseX;
		
		if ( newLeft < 0 )
			newLeft = 0;
		
		$(this.wrapSel).width(newWrapW);
		$(this.wrapSel).css({left: -newLeft});
		this.draw();
	}
	
	/*****************
	 * Pomocne metody
	 */
	
	xFromYear(year)
	{
		var k = (year - this.yearMin) / (this.yearMax - this.yearMin);
		var x = this.x1 + k * (this.x2 - this.x1);
		return Math.floor(x) + 0.5;
	}
	
	yearFromX(x)
	{
		var k = (x - this.x1) / (this.x2 - this.x1);
		var y = this.yearMin + k * (this.yearMax - this.yearMin);
		return Math.round(y);
	}
	
	yFromCount(count)
	{
		var y = this.y2 - (count / this.countMax ) * (this.y2 - this.y1);
		return Math.floor(y) + 0.5;
	}
	
	closestYearTo(yearToFind)
	{
		var lower = Number.MIN_SAFE_INTEGER,
			upper = Number.MAX_SAFE_INTEGER;
		var res = [];
		
		this.dataSets.forEach(function(dataset)
		{
			let resLower, resUpper;
			[resLower, resUpper] = this.closestYearInDatasetTo(dataset, yearToFind);
			
			lower = Math.max(lower, resLower);
			upper = Math.min(upper, resUpper);
			
		}, this);
		
		if ( yearToFind - lower <= upper - yearToFind )
			return lower;
		else
			return upper;
	}
	
	closestYearInDatasetTo(dataset, yearToFind)
	{
		// Obecne predpokladame, ze je to serazene podle roku (to zaridi uz Solr)
		
		// Zkusit, jestli neni primo
		if ( yearToFind in dataset.data )
			return [yearToFind, yearToFind];
		
		// Kdyz neni, tak najit nejblizsi
		var lower = Number.MIN_SAFE_INTEGER,
			upper = Number.MAX_SAFE_INTEGER;
		for (const year in dataset.data)
		{
			if ( year < yearToFind )
			{
				lower = year;
			}
			else
			{
				upper = year;
				break;
			}
		}
		
		return [lower, upper];
	}
	
	mousePosInChart(ev)
	{
		var parentOffset = $(this.canvas).offset();
		var relX = ev.pageX - parentOffset.left;
		var relY = ev.pageY - parentOffset.top;
		
		// Kdyz jsme mimo, nic neresime
		if ( relX < this.config.paddingLeft || relY > this.canvas.width - this.config.paddingRight )
			return [false, relX, relY];
		
		return [true, relX, relY];
	}
	
	ajaxCall(data)
	{
		Wicket.Ajax.post({
			"u": this.ajaxCallback,
			"ep": {
				"data" : data,
			},
		});
	}
	
}
