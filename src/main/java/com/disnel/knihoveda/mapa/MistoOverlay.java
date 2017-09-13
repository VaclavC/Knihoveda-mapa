package com.disnel.knihoveda.mapa;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import com.disnel.knihoveda.mapa.events.MistoSelectEvent;

public class MistoOverlay extends Panel
{

	public MistoOverlay(String id, String nazevMista, String pocetTisku)
	{
		super(id);
		
		WebMarkupContainer link;
		add(link = new WebMarkupContainer("link"));
		
		link.add(new Label("misto", nazevMista));
		link.add(new Label("pocet", pocetTisku));
		
		link.add(new AjaxEventBehavior("click")
		{
			@Override
			protected void onEvent(AjaxRequestTarget target)
			{
				send(getPage(), Broadcast.BREADTH,
						new MistoSelectEvent(target, nazevMista));
			}
		});
	}
	
}
