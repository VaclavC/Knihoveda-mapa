package com.disnel.knihoveda.mapa.panel;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.panel.Panel;
import com.disnel.knihoveda.dao.SolrDAO;
import com.disnel.knihoveda.mapa.KnihovedaMapaSession;
import com.disnel.knihoveda.mapa.data.DataSet;
import com.disnel.knihoveda.mapa.events.AjaxEvent;
import com.disnel.knihoveda.mapa.events.UserSelectionChangedEvent;
import com.disnel.knihoveda.mapa.timeline.Timeline;
import com.disnel.knihoveda.mapa.timeline.TimelineConf;
import com.disnel.knihoveda.mapa.timeline.TimelineDataset;

public class CasovyGraf extends Panel
{
	private static final long serialVersionUID = 1L;

	private Timeline timeline;
	

	public CasovyGraf(String id)
	{
		super(id);
		
		// Vlastni casovy graf
		TimelineConf conf = new TimelineConf();
		conf.setDetailPanelId("timelineRecordInfo");
		conf.setYearMin(KnihovedaMapaSession.get().minYear());
		conf.setYearMax(KnihovedaMapaSession.get().maxYear());
		
		add(timeline = new Timeline("timeline", conf));
		timeline.setData(createTimelineData());
	}

	private TimelineDataset createTimelineDataSet(DataSet dataSet)
	{
		TimelineDataset tlDataset =
				new TimelineDataset(dataSet.getColor());

		for ( Count count : SolrDAO.getCountByYear(dataSet) )
		{
			String countName = count.getName();

			if ( !countName.isEmpty() )
			{
				Integer year = Integer.parseInt(count.getName());
				Long countVal = count.getCount();

				tlDataset.addCount(year, countVal);
			}
		}
		
		return tlDataset;
	}
	
	private List<TimelineDataset> createTimelineData()
	{
		List<TimelineDataset> datasetList =
				new ArrayList<>(KnihovedaMapaSession.get().dataSets().size());
		
		for ( DataSet dataSet : KnihovedaMapaSession.get().dataSets() )
			if ( dataSet != KnihovedaMapaSession.get().currentDataSet()
				&& ( dataSet.isActive() || dataSet == KnihovedaMapaSession.get().firstDataSet() ))
				datasetList.add(createTimelineDataSet(dataSet));
		
		datasetList.add(createTimelineDataSet(KnihovedaMapaSession.get().currentDataSet()));
		
		return datasetList;
	}
	
	@Override
	public void onEvent(IEvent<?> event)
	{
		if ( event.getPayload() instanceof UserSelectionChangedEvent )
		{
			AjaxEvent ev = (AjaxEvent) event.getPayload();
			AjaxRequestTarget target = ev.getTarget();
			
			DataSet currentDataSet = KnihovedaMapaSession.get().currentDataSet();
			timeline.setYearFrom(currentDataSet.getYearFrom());
			timeline.setYearTo(currentDataSet.getYearTo());
			
			timeline.setData(createTimelineData());
			
			target.appendJavaScript(timeline.getJSSetData());
			target.appendJavaScript(timeline.getJSDraw());
		}
	}
	
}
