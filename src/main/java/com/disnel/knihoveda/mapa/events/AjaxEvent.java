package com.disnel.knihoveda.mapa.events;

import org.apache.wicket.ajax.AjaxRequestTarget;

public class AjaxEvent
{

	private AjaxRequestTarget target;

	public AjaxEvent(AjaxRequestTarget target)
	{
		this.target = target;
	}

	public AjaxRequestTarget getTarget()
	{
		return target;
	}

	public void setTarget(AjaxRequestTarget target)
	{
		this.target = target;
	}
	
}
