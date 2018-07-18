package com.disnel.knihoveda.mapa.events;

import org.apache.wicket.ajax.AjaxRequestTarget;

public class FieldValuesChangedEvent extends AjaxEvent
{

	private String fieldName;
	
	public FieldValuesChangedEvent(AjaxRequestTarget target, String fieldName)
	{
		super(target);
		this.fieldName = fieldName;
	}

	public String getFieldName()
	{
		return fieldName;
	}

}
