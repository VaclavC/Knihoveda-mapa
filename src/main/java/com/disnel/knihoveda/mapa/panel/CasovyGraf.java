package com.disnel.knihoveda.mapa.panel;

import java.util.List;

import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.PackageResourceReference;

import com.disnel.knihoveda.dao.SolrDAO;
import com.disnel.knihoveda.mapa.KnihovedaMapaSession;
import com.disnel.knihoveda.mapa.data.DataSet;
import com.disnel.knihoveda.mapa.events.AjaxEvent;
import com.disnel.knihoveda.mapa.events.DataSetChangedEvent;
import com.disnel.knihoveda.mapa.events.TimeSelectEvent;
import com.disnel.knihoveda.mapa.events.UserSelectionChangedEvent;

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
		
		// Kontejner
		add(timelineCont = new WebMarkupContainer("timelineCont"));
		timelineCont.setOutputMarkupId(true);
		
		timelineCont.add(new CGAjaxBehavior());
	}

	
	@Override
	public void renderHead(IHeaderResponse response)
	{
		response.render(JavaScriptReferenceHeaderItem.forReference(
				new PackageResourceReference(getClass(), "CasovyGraf.js")));
		
		response.render(OnDomReadyHeaderItem.forScript(
				jsInit() + jsSetAllData() + jsDraw(false)));
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
				target.appendJavaScript(jsDraw(false));
			}
			else
			{
				int index = KnihovedaMapaSession.get().currentDataSetIndex();
				target.appendJavaScript(jsDataSetData(index, KnihovedaMapaSession.get().currentDataSet()));
				target.appendJavaScript(jsDraw(ev instanceof TimeSelectEvent));
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
	
	public String jsDraw(boolean zoomToTimeRange)
	{
		return String.format("%s.draw(%b);", jsVar(), zoomToTimeRange);
	}
	
	public String jsDataSetData(int dataSetIndex, DataSet dataSet, List<Count> data)
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
		sb.append(",'");
		sb.append(dataSet.getColor().toString());
		sb.append("',");
		sb.append(sbYears);
		sb.append(',');
		sb.append(sbCounts);
		sb.append(");");
		
		if ( dataSet.hasTimeRange() )
		{
			sb.append(jsVar());
			sb.append(".timeRangeSet(");
			sb.append(dataSet.getYearFrom());
			sb.append(',');
			sb.append(dataSet.getYearTo());
			sb.append(");");
		}
		else
		{
			sb.append(jsVar());
			sb.append(".timeRangeClear();");
		}
		
		return sb.toString();
	}
	
	public String jsDataSetData(int index, DataSet dataSet)
	{
		List<Count> listCount = SolrDAO.getCountByYear(dataSet);
		return jsDataSetData(index, dataSet, listCount);
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
	
	
	/* Ajax behavior */
	
	private class CGAjaxBehavior extends AbstractDefaultAjaxBehavior
	{
		private static final long serialVersionUID = 1L;

		public static final String AJAX_CALLBACK_DATA_NAME = "callback";
		
		@Override
	    protected void onComponentTag(ComponentTag tag) {
	        tag.put("data-" + AJAX_CALLBACK_DATA_NAME,
	        		getCallbackUrl().toString());
		}
		
		@Override
		protected void respond(AjaxRequestTarget target)
		{
			RequestCycle requestCycle = RequestCycle.get();
			String data = requestCycle.getRequest().getRequestParameters()
					.getParameterValue("data").toString();
			
			DataSet currentDataSet = KnihovedaMapaSession.get().currentDataSet(); 
			
			// Tady predpokladame vstup ve formatu [SC]yyyy-yyyy
			switch ( data.charAt(0) )
			{
			case 'S':
				int yearFrom = Integer.parseInt(data.substring(1, 5));
				int yearTo = Integer.parseInt(data.substring(6, 10));
				
				currentDataSet
					.setYearFrom(yearFrom)
					.setYearTo(yearTo);
				
				break;
				
			case 'C':
				currentDataSet
					.setYearFrom(null)
					.setYearTo(null);
				
				break;
				
			default:
				return;
			}
			
			send(getPage(), Broadcast.BREADTH,
					new TimeSelectEvent(target,
							currentDataSet.getYearFrom(), currentDataSet.getYearTo()));
		}
	}
	
}
