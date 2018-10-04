package com.disnel.knihoveda.mapa.events;

import org.apache.wicket.ajax.AjaxRequestTarget;

public class UserSelectionChangedEvent extends AjaxEvent
{

	public UserSelectionChangedEvent(AjaxRequestTarget target)
	{
		super(target);
	}

}
