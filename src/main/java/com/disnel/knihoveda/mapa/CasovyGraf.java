package com.disnel.knihoveda.mapa;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.panel.Panel;
import org.wicketstuff.openlayers3.api.util.Color;

import com.disnel.knihoveda.dao.SolrDAO;
import com.disnel.knihoveda.mapa.data.DataSet;
import com.disnel.knihoveda.mapa.events.AjaxEvent;
import com.disnel.knihoveda.mapa.events.DataSetChangedEvent;
import com.disnel.knihoveda.mapa.events.FieldValuesChangedEvent;

import de.adesso.wickedcharts.chartjs.ChartConfiguration;import de.adesso.wickedcharts.chartjs.chartoptions.Animation;
import de.adesso.wickedcharts.chartjs.chartoptions.AxesScale;
import de.adesso.wickedcharts.chartjs.chartoptions.ChartType;
import de.adesso.wickedcharts.chartjs.chartoptions.Data;
import de.adesso.wickedcharts.chartjs.chartoptions.Dataset;
import de.adesso.wickedcharts.chartjs.chartoptions.Legend;
import de.adesso.wickedcharts.chartjs.chartoptions.Options;
import de.adesso.wickedcharts.chartjs.chartoptions.Position;
import de.adesso.wickedcharts.chartjs.chartoptions.Scales;
import de.adesso.wickedcharts.chartjs.chartoptions.colors.RgbaColor;
import de.adesso.wickedcharts.wicket8.chartjs.Chart;

public class CasovyGraf extends Panel
{

	private Integer rokOd, rokDo;
	
	private Long maxCount = 0L;
	
	private Options chartOptions;
	private ChartConfiguration chartConfiguration;
	private Chart chart;
	
	public CasovyGraf(String id, Integer rokOd, Integer rokDo, Long maxCount)
	{
		super(id);
		
		this.rokOd = rokOd;
		this.rokDo = rokDo;
		this.maxCount = maxCount;

		
		chartOptions = new Options()
				.setResponsive(true)
				.setMaintainAspectRatio(false)
				.setScales(new Scales()
						.setYAxes(new AxesScale()
								.setDisplay(true))
						.setXAxes(new AxesScale()
								.setDisplay(true)
								.setType("linear")
								.setPosition(Position.BOTTOM)))
				.setLegend(new Legend()
						.setDisplay(false))
				.setAnimation(new Animation()
						.setDuration(0));

		chartConfiguration = new GrafConfig()
				.setType(ChartType.LINE)
				.setOptions(chartOptions)
				.setData(createChartData());
				
		add(chart = new Chart("chart", chartConfiguration));
	}

	private Data createChartData()
	{
		List<Dataset> chartDatasets = new ArrayList<>();
		
		chartDatasets.add(createChartDataset(MapaSession.get().currentDataSet()));
		
		for ( DataSet dataSet : MapaSession.get().dataSets())
			if ( dataSet != MapaSession.get().currentDataSet())
				chartDatasets.add(createChartDataset(dataSet));
		
		return new Data()
				.setDatasets(chartDatasets);
	}
	
	private Dataset createChartDataset(DataSet dataSet)
	{
		Color dataSetColor = dataSet.getColor();
		RgbaColor chartColor =
				new RgbaColor(dataSetColor.red, dataSetColor.green, dataSetColor.blue, 255);
		
		return new Dataset()
				.setBackgroundColor(chartColor)
				.setBorderColor(chartColor)
				.setBorderWidth(1)
				.setPointBorderWidth(0)
				.setPointRadius(2)
				.setData(SolrDAO.getCountByYearAsPoints(dataSet))
				.setFill(false)
				.setLineTension(0);
	}
	
	@Override
	public void onEvent(IEvent<?> event)
	{
		if ( event.getPayload() instanceof FieldValuesChangedEvent 
				|| event.getPayload() instanceof DataSetChangedEvent )
		{
			AjaxEvent ev = (AjaxEvent) event.getPayload();
			
			chartConfiguration.setData(createChartData());
			
			ev.getTarget().add(chart);
		}
	}
	
	private class GrafConfig extends ChartConfiguration implements Serializable
	{
		// Only beacause ChartConfiguration isn't Serializable in current version
	}
	
}
