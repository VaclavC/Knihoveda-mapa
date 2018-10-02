package com.disnel.knihoveda.mapa.events;

import java.util.HashSet;
import java.util.Set;

import org.apache.wicket.ajax.AjaxRequestTarget;

public class AjaxEvent
{

	private AjaxRequestTarget target;
	
	private Set<Class<?>> processedBy = new HashSet<>();

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
	
	public boolean isProcessedBy(Class<?> clazz)
	{
		return processedBy.contains(clazz);
	}
	
	public void setProcessedBy(Class<?> clazz)
	{
		processedBy.add(clazz);
	}
	
}
