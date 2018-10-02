package com.disnel.knihoveda.mapa.timeline;

import java.util.List;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.json.JSONException;

import com.disnel.knihoveda.dao.JSON;

public class Timeline extends WebMarkupContainer
{

	private TimelineConf conf;
	
	private List<TimelineDataset> data;
	
	public Timeline(String id, TimelineConf conf)
	{
		super(id);
		this.conf = conf;
		setOutputMarkupId(true);
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

	public String getJSSetData()
	{
		StringBuilder sb = new StringBuilder();
		
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
		
		return sb.toString();
	}
	
	public String getJSDraw()
	{
		return String.format("%s.draw()", getJSVarName());
	}
	
	@Override
	public void renderHead(IHeaderResponse response)
	{
		// Dependencies
		response.render(JavaScriptReferenceHeaderItem.forReference(
				new PackageResourceReference(getClass(), "TimelineDataset.js")));
		response.render(JavaScriptReferenceHeaderItem.forReference(
				new PackageResourceReference(getClass(), "Timeline.js")));
		
		// Initialization
		response.render(OnDomReadyHeaderItem.forScript(getJSInit()));
		response.render(OnDomReadyHeaderItem.forScript(getJSSetData()));
		response.render(OnDomReadyHeaderItem.forScript(getJSDraw()));
	}
	
}
