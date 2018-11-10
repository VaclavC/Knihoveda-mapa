package com.disnel.knihoveda.mapa.events;

import org.apache.wicket.ajax.AjaxRequestTarget;

public class TimeSelectEvent extends UserSelectionChangedEvent
{

	private Integer yearFrom, yearTo;
	
	public TimeSelectEvent(AjaxRequestTarget target,
			Integer yearFrom, Integer yearTo)
	{
		super(target);
		
		this.yearFrom = yearFrom;
		this.yearTo = yearTo;
	}

	public Integer yearFrom()
	{
		return yearFrom;
	}

	public Integer yearTo()
	{
		return yearTo;
	}

}
