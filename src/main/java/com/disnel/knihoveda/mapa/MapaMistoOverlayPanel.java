package com.disnel.knihoveda.mapa;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import com.disnel.knihoveda.mapa.data.DataSet;
import com.disnel.knihoveda.mapa.data.ResultsInPlace;
import com.disnel.knihoveda.mapa.events.MistoSelectEvent;

public class MapaMistoOverlayPanel extends Panel
{

	private boolean isSelected = false;
	
	public MapaMistoOverlayPanel(String id, ResultsInPlace resultsInPlace)
	{
		super(id);
		
		// Tecka v miste obce
		Component bottomDot;
		add(bottomDot = new WebMarkupContainer("bottomDot"));
		
		// Graf s vysledky a jeho sloupce
		RepeatingView bars;
		add(bars = new RepeatingView("result"));
		
		for ( DataSet dataSet : MapaSession.get().dataSets() )
		{
			Component bar;
			bars.add(bar = new WebMarkupContainer(bars.newChildId()));
			bar.add(new AttributeAppender("style",
					String.format("height: %dpx; background-color: %s;",
							sizeFromPocet(resultsInPlace.getNumResultsForDataSet(dataSet)),
							dataSet.getColor().toString()),
					";"));
		}

		// Detail vysledku
		WebMarkupContainer detail;
		add(detail = new WebMarkupContainer("detail"));
		detail.setOutputMarkupId(true);
		
		detail.add(new Label("nazevMista", resultsInPlace.getPlaceName()));
		
		detail.add(new ListView<DataSet>("vysledek", MapaSession.get().dataSets())
		{
			@Override
			protected void populateItem(ListItem<DataSet> item)
			{
				DataSet dataSet = item.getModelObject();
				
				item.add(new WebMarkupContainer("dataSetIndicator")
						.add(new AttributeAppender("style",
								String.format("background-color: %s;", dataSet.getColor().toString()),
								";")));
				
				item.add(new Label("pocet", resultsInPlace.getNumResultsForDataSet(dataSet)));
			}
		});
		
		// Nejaky ten JS pro zobrazovani detailu
		bottomDot.add(new AttributeAppender("onmouseover",
				String.format("$('#%s').show();", detail.getMarkupId())));
		detail.add(new AttributeAppender("onmouseleave",
				String.format("$('#%s').hide();", detail.getMarkupId())));
		
//		detail.add(new AjaxEventBehavior("click")
//		{
//			@Override
//			protected void onEvent(AjaxRequestTarget target)
//			{
//				if ( isSelected )
//				{
//					send(getPage(), Broadcast.BREADTH,
//							new MistoSelectEvent(target, null));
//					
//					target.appendJavaScript(
//							"$('#" + MapaMistoOverlayPanel.this.getMarkupId() + " .mistoOverlay').removeClass('active');");
//					
//					isSelected = false;
//				}
//				else
//				{
//					send(getPage(), Broadcast.BREADTH,
//							new MistoSelectEvent(target, resultsInPlace.getPlaceName()));
//					
//					target.appendJavaScript("$('.mistoOverlay').removeClass('active');"
//							+ " $('#" + MapaMistoOverlayPanel.this.getMarkupId() + " .mistoOverlay').addClass('active');");
//					
//					isSelected = true;
//				}
//			}
//		});
	}
	
	private int sizeFromPocet(long pocet)
	{
		if ( pocet == 0 )
			return 0;
		
		double k = (double) pocet / MapaSession.get().maxCountInPlace();
		k = Math.max(Math.min(Math.log10(1.0 + 9.0*k), 1.0), 0.0);
		int size = (int) Math.round(KnihovedaMapaConfig.MIN_PLACE_SIZE
				+ k * KnihovedaMapaConfig.PLACE_SIZE_DIFF);
		
		return size;
	}
	
}
