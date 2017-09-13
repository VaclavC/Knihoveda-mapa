package com.disnel.knihoveda.mapa.events;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class SearchEvent extends AjaxEvent
{
	private PageParameters parameters;
	
	public SearchEvent(AjaxRequestTarget target, PageParameters parameters)
	{
		super(target);
		this.parameters = parameters;
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
