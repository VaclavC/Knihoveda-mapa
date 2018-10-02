
class Timeline {
	
	constructor(tagID, config)
	{
		// Nastaveni parametru
		this.tagID = tagID;
		this.config = config;
		this.tagSel = "#" + tagID;
		this.detailPanelSel = "#" + config.detailPanelId;
		
		// Dalsi hladiny pro vykreslovani
		$(this.tagSel).parent().prepend('<canvas id="' + this.tagID + '-bottom" style="z-index: -1; pointer-events: none"></canvas>' );
		$(this.tagSel).css("z-index", 0);
		$(this.tagSel).parent().append('<canvas id="' + this.tagID + '-top" style="z-index: 1; pointer-events: none"></canvas>' );
		
		// Callbacky
		$(window).on('resize', () => { this.draw(); });
		$(this.tagSel).on('mousemove', (ev) => { this.mouseMove(ev); });
		$(this.tagSel).on('mouseleave', (ev) => { this.mouseLeave(ev); });
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
			for (const [year, count] of Object.entries(dataset.data))
			{
				yearMin = Math.min(yearMin, year);
				yearMax = Math.max(yearMax, year);
				
				countMin = Math.min(countMin, count);
				countMax = Math.max(countMax, count);
			}
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
		var timeLen = this.yearMax - this.yearMin;
		var timeStep = (timeLen > 100) ? 50 : ( (timeLen > 10) ? 5: 1 );
		var time1 = timeStep * Math.floor(this.yearMin / timeStep);
		var time2 = timeStep * Math.floor(this.yearMax / timeStep + 1);
		
		var textY = this.config.timeAxisTextY;
		
		this.ctx.lineWidth = 1;
		this.ctx.strokeStyle = this.config.timeAxisStyle;
		this.ctx.fillStyle = this.config.timeAxisStyle;
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
		var countStep = (this.countMax > 100) ? 100 : ( (this.countMax > 10) ? 10: 1 );
		
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
		for (var year in dataset.data)
		{
			var actX = this.xFromYear(year);
			var actY = this.yFromCount(dataset.data[year]);
			
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
	 * Interaktivita
	 */
	
	mouseMove(ev)
	{
		this.ctxTop.clearRect(0, 0, this.canvas.width, this.canvas.height);
	
		var parentOffset = $(this.canvas).offset();
		var relX = ev.clientX - parentOffset.left;
		var relY = ev.clientY - parentOffset.top;
		
		// Kdyz jsme mimo, nic neresime
		if ( relX < this.config.paddingLeft || relY > this.canvas.width - this.config.paddingRight )
			return;
		
		// Najit rok se zaznamem nejblizsi kurzoru
		var yearSelected = this.yearFromX(relX);
		var yearWithData = this.closestYearTo(yearSelected);

		// Namalovat vertikalni caru
		var x = this.xFromYear(yearWithData);

		this.ctxTop.lineWidth = this.config.cursorLineWidth;
		this.ctxTop.strokeStyle = this.config.cursorStrokeStyle;
		this.ctxTop.fillStyle = this.config.cursorFillStyle;
		
		this.ctxTop.beginPath();
		this.ctxTop.moveTo(x, 0);
		this.ctxTop.lineTo(x, this.canvas.height);
		this.ctxTop.stroke();
		
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
		
		let xPos = ev.clientX;
		if ( relX > 4*this.canvas.width/5)
			xPos -= $(this.detailPanelSel).width();
		
		let yPos = ev.clientY, panelHeight = $(this.detailPanelSel).height(); 
		if ( relY + panelHeight > this.canvas.height )
			yPos -= panelHeight;
		
		$(this.detailPanelSel).offset({ top: yPos.toString(), left: xPos.toString()});
	}
	
	mouseLeave(ev)
	{
		this.ctxTop.clearRect(0, 0, this.canvas.width, this.canvas.height);
		$(this.detailPanelSel).hide();
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
	
}
