package com.disnel.knihoveda.mapa.events;

import org.apache.wicket.ajax.AjaxRequestTarget;

public class MistoSelectEvent extends AjaxEvent
{

	private String nazevMista;
	
	public MistoSelectEvent(AjaxRequestTarget target, String nazevMista)
	{
		super(target);
		this.nazevMista = nazevMista;
	}
	
	public String getNazevMista()
	{
		return nazevMista;
	}

	public void setNazevMista(String nazevMista)
	{
		this.nazevMista = nazevMista;
	}

}
