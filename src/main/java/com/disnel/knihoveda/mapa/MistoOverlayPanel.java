package com.disnel.knihoveda.mapa;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import com.disnel.knihoveda.mapa.events.MistoSelectEvent;

public class MistoOverlayPanel extends Panel
{

	private boolean isSelected = false;
	
	public MistoOverlayPanel(String id, String nazevMista, String pocetTisku)
	{
		super(id);
		
		setOutputMarkupId(true);
		
		WebMarkupContainer link;
		add(link = new WebMarkupContainer("link"));
		
		link.add(new Label("misto", nazevMista));
		link.add(new Label("pocet", pocetTisku));
		
		link.add(new AjaxEventBehavior("click")
		{
			@Override
			protected void onEvent(AjaxRequestTarget target)
			{
				if ( isSelected )
				{
					send(getPage(), Broadcast.BREADTH,
							new MistoSelectEvent(target, null));
					
					target.appendJavaScript(
							"$('#" + MistoOverlayPanel.this.getMarkupId() + " .mistoOverlay').removeClass('active');");
					
					isSelected = false;
				}
				else
				{
					send(getPage(), Broadcast.BREADTH,
							new MistoSelectEvent(target, nazevMista));
					
					target.appendJavaScript("$('.mistoOverlay').removeClass('active');"
							+ " $('#" + MistoOverlayPanel.this.getMarkupId() + " .mistoOverlay').addClass('active');");
					
					isSelected = true;
				}
			}
		});
	}
	
}
