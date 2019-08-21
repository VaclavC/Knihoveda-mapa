package com.disnel.knihoveda.mapa.panel;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.resource.PackageResourceReference;

import com.disnel.knihoveda.dao.SolrDAO;
import com.disnel.knihoveda.mapa.KnihovedaMapaSession;
import com.disnel.knihoveda.mapa.data.DataSet;
import com.disnel.knihoveda.mapa.events.AjaxEvent;
import com.disnel.knihoveda.mapa.events.DataSetChangedEvent;
import com.disnel.knihoveda.mapa.events.UserSelectionChangedEvent;
import com.disnel.knihoveda.mapa.timeline.Timeline;
import com.disnel.knihoveda.mapa.timeline.TimelineDataset;

public class CasovyGraf extends Panel
{
	private static final long serialVersionUID = 1L;

	private CasovyGrafOptions timelineConf;
	private WebMarkupContainer timelineCont;
	
	public CasovyGraf(String id)
	{
		super(id);
		
		// Konfigurace casoveho grafu
		timelineConf = new CasovyGrafOptions();
		timelineConf.setDetailPanelId("timelineRecordInfo");
		timelineConf.setYearMin(KnihovedaMapaSession.get().minYear());
		timelineConf.setYearMax(KnihovedaMapaSession.get().maxYear());
		timelineConf.setCountMax(KnihovedaMapaSession.get().maxCount());
		
		// Vnejsi kontejner
		add(timelineCont = new WebMarkupContainer("timelineCont"));
		timelineCont.setOutputMarkupId(true);
		
		// Datove sady
		timelineCont.add(new ListView<DataSet>("dataSet", KnihovedaMapaSession.get().dataSets())
		{

			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<DataSet> item)
			{
				DataSet dataSet = item.getModelObject();
				
				item.add(new AttributeModifier("data-color", dataSet.getColor().toString()));
			}
		});
	}

	
	@Override
	public void renderHead(IHeaderResponse response)
	{
		response.render(JavaScriptReferenceHeaderItem.forReference(
				new PackageResourceReference(getClass(), "CasovyGraf.js")));
		
		response.render(OnDomReadyHeaderItem.forScript(
				jsInit() + jsSetAllData() + jsDraw()));
	}

	@Override
	public void onEvent(IEvent<?> event)
	{
		if ( event.getPayload() instanceof UserSelectionChangedEvent )
		{
			AjaxEvent ev = (AjaxEvent) event.getPayload();
			AjaxRequestTarget target = ev.getTarget();

			if ( ev instanceof DataSetChangedEvent )
			{
				target.appendJavaScript(jsSetAllData());
				target.appendJavaScript(jsDraw());
			}
			else
			{
				int index = KnihovedaMapaSession.get().currentDataSetIndex();
				target.appendJavaScript(jsDataSetData(index, KnihovedaMapaSession.get().currentDataSet()));
				target.appendJavaScript(jsDraw());
			}
		}
	}

	
	/* JS related methods */
	
	public String jsVar()
	{
		return timelineCont.getMarkupId() + "CG";
	}
	
	public String jsInit()
	{
		return String.format("%s = new CasovyGraf('%s', %s);",
				jsVar(), timelineCont.getMarkupId(), timelineConf.json());
	}
	
	public String jsDraw()
	{
		return String.format("%s.draw();", jsVar());
	}
	
	public String jsDataSetData(int dataSetIndex, List<Count> data)
	{
		StringBuilder sbYears = new StringBuilder();
		StringBuilder sbCounts = new StringBuilder();
		sbYears.append('[');
		sbCounts.append('[');
		for ( Count count : data)
			if ( ! count.getName().isEmpty() )
			{
				int year = Integer.parseInt(count.getName());
				if ( year > 0 )
				{
					sbYears.append(year);
					sbCounts.append(count.getCount());
					
					sbYears.append(',');
					sbCounts.append(',');
				}
			}
		sbYears.append(']');
		sbCounts.append(']');
		
		StringBuilder sb = new StringBuilder();
		sb.append(jsVar());
		sb.append(".datasetSetData(");
		sb.append(dataSetIndex);
		sb.append(',');
		sb.append(sbYears);
		sb.append(',');
		sb.append(sbCounts);
		sb.append(");");
		
		return sb.toString();
	}
	
	public String jsDataSetData(int index, DataSet dataSet)
	{
		List<Count> listCount = SolrDAO.getCountByYear(dataSet);
		return jsDataSetData(index, listCount);
	}
	
	public String jsDataSetClear(int index)
	{
		return String.format(String.format("%s.datasetClear(%d);",
				jsVar(), index));
	}
	
	public String jsSetAllData()
	{
		StringBuilder sb = new StringBuilder();
		
		List<DataSet> listDS = KnihovedaMapaSession.get().dataSets();
		for ( int i = 0; i < listDS.size(); i++ )
		{
			DataSet ds = listDS.get(i);
			if ( ds.isActive() )
				sb.append(jsDataSetData(i, ds));
			else
				sb.append(jsDataSetClear(i));
		}
		
		return sb.toString();
	}
	
	public String jsInitDraw()
	{
		return String.format("%s.initDraw();", jsVar());
	}
	
	public String jsDrawDataSet(int dataSetIndex)
	{
		return String.format("%s.drawDataSetByIndex(%d);", jsVar(), dataSetIndex);
	}
	
}
