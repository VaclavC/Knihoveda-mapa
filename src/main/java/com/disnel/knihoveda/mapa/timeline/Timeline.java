package com.disnel.knihoveda.mapa.timeline;

import java.util.List;

import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.PackageResourceReference;

import com.disnel.knihoveda.dao.JSON;
import com.disnel.knihoveda.mapa.MapaSession;
import com.disnel.knihoveda.mapa.data.DataSet;
import com.disnel.knihoveda.mapa.events.TimeSelectEvent;
import com.github.openjson.JSONException;

public class Timeline extends WebMarkupContainer
{
	private static final long serialVersionUID = 1L;

	public static final String AJAX_CALLBACK_DATA_NAME = "callback";
	
	private TimelineConf conf;
	
	private List<TimelineDataset> data;
	
	private Integer yearFrom, yearTo;
	
	public Timeline(String id, TimelineConf conf)
	{
		super(id);
		this.conf = conf;
		
		add(new AbstractDefaultAjaxBehavior()
		{
			private static final long serialVersionUID = 1L;

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
				
				DataSet currentDataSet = MapaSession.get().currentDataSet(); 
				
				// Tady predpokladame vstup ve formatu [SC]yyyy-yyyy
				switch ( data.charAt(0) )
				{
				case 'S':
					yearFrom = Integer.parseInt(data.substring(1, 5));
					yearTo = Integer.parseInt(data.substring(6, 10));
					
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
		});
	}
	
	public Timeline setConf(TimelineConf conf)
	{
		this.conf = conf;
		
		return this;
	}
	
	public Timeline setData(List<TimelineDataset> data)
	{
		this.data = data;
		
		return this;
	}

	public Timeline setYearFrom(Integer yearFrom)
	{
		this.yearFrom = yearFrom;
		
		return this;
	}

	public Timeline setYearTo(Integer yearTo)
	{
		this.yearTo = yearTo;
		
		return this;
	}
	
	public String getJSVarName()
	{
		return this.getMarkupId() + "Var";
	}
	
	public String getJSConstruct()
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("new Timeline('");
		sb.append(getMarkupId());
		sb.append("', ");
		try
		{
			sb.append(JSON.toJSON(conf));
		}
		catch (IllegalArgumentException | IllegalAccessException | JSONException e)
		{
			e.printStackTrace();
		}
		sb.append(")");
		
		return sb.toString();
	}
	
	public String getJSInit()
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append(getJSVarName());
		sb.append(" = ");
		sb.append(getJSConstruct());
		sb.append(";");
		
		return sb.toString();
	}

	private String yearToString(Integer year)
	{
		if ( year != null )
			return year.toString();
		
		else
			return "null";
	}
	
	public String getJSSetData()
	{
		StringBuilder sb = new StringBuilder();
		
		// Nastavit data
		sb.append(getJSVarName());
		sb.append(".setData([");

		if ( data != null )
		{
			boolean first = true;
			for ( TimelineDataset dataset : data )
			{
				if ( !first )
					sb.append(',');
				else
					first = false;
				
				sb.append(dataset.getJSConstruct());
			}
		}
		
		sb.append("]);");
		
		// Nastavit pripadne casove rozmezi
		sb.append(getJSVarName());
		sb.append(".yearSelectFrom = ");
		sb.append(yearToString(yearFrom));
		sb.append(';');
		
		sb.append(getJSVarName());
		sb.append(".yearSelectTo = ");
		sb.append(yearToString(yearTo));
		sb.append(';');
		
		// Vratit vysledek
		return sb.toString();
	}
	
	public String getJSDraw()
	{
		return String.format("%s.draw()", getJSVarName());
	}
	
	public String getJSDrawSelection(Integer yearFrom, Integer yearTo)
	{
		return String.format("%s.drawSelection(%d, %d); %s.zoomToSelection();",
				getJSVarName(), yearFrom, yearTo,
				getJSVarName());
	}
			
	@Override
	public void renderHead(IHeaderResponse response)
	{
		// Dependencies
		response.render(JavaScriptReferenceHeaderItem.forReference(
				new PackageResourceReference(Timeline.class, "TimelineDataset.js")));
		response.render(JavaScriptReferenceHeaderItem.forReference(
				new PackageResourceReference(Timeline.class, "Timeline.js")));
		
		// Initialization
		response.render(OnDomReadyHeaderItem.forScript(getJSInit()));
		response.render(OnDomReadyHeaderItem.forScript(getJSSetData()));
		response.render(OnDomReadyHeaderItem.forScript(getJSDraw()));
	}

	@Override
	public void onEvent(IEvent<?> event)
	{
		if ( event.getPayload() instanceof TimeSelectEvent )
		{
			TimeSelectEvent ev = (TimeSelectEvent) event.getPayload();
			
			ev.getTarget().appendJavaScript(getJSDrawSelection(ev.yearFrom(), ev.yearTo()));
		}
	}
}
