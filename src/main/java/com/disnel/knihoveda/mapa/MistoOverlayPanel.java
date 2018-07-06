package com.disnel.knihoveda.mapa;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import com.disnel.knihoveda.mapa.events.MistoSelectEvent;

public class MistoOverlayPanel extends Panel
{

	private boolean isSelected = false;
	
	public MistoOverlayPanel(String id, String nazevMista, long pocetTisku, long pocetTiskuMax)
	{
		super(id);
		
		setOutputMarkupId(true);
		
		WebMarkupContainer symbol;
		add(symbol = new WebMarkupContainer("symbol"));
		double k = (double) pocetTisku / pocetTiskuMax;
		k = Math.log10(1.0 + 9.0*k);
		int dotSize = (int) Math.round(KnihovedaMapaConfig.MIN_DOT_SIZE
				+ k * KnihovedaMapaConfig.DOT_SIZE_DIFF);
		symbol.add(new AttributeAppender("style",
				String.format("font-size: %dpx;", dotSize)));
		
		WebMarkupContainer detail;
		add(detail = new WebMarkupContainer("detail"));
		detail.setOutputMarkupId(true);
		
		detail.add(new Label("misto", nazevMista));
		detail.add(new Label("pocet", pocetTisku));
		
		symbol.add(new AttributeAppender("onmouseover",
				String.format("$('#%s').show();", detail.getMarkupId())));
		detail.add(new AttributeAppender("onmouseleave",
				String.format("$('#%s').hide();", detail.getMarkupId())));
		
		detail.add(new AjaxEventBehavior("click")
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
