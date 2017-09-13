package com.disnel.knihoveda.mapa.events;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class SearchEvent
{
	private AjaxRequestTarget target;
	private PageParameters parameters;
	
	public SearchEvent(AjaxRequestTarget target, PageParameters parameters)
	{
		this.target = target;
		this.parameters = parameters;
	}

	public AjaxRequestTarget getTarget()
	{
		return target;
	}

	public void setTarget(AjaxRequestTarget target)
	{
		this.target = target;
	}
	
	public PageParameters getParameters()
	{
		return parameters;
	}

	public void setParameters(PageParameters parameters)
	{
		this.parameters = parameters;
	}

}
