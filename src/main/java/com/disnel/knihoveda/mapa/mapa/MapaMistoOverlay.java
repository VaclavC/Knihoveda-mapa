package com.disnel.knihoveda.mapa.mapa;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import org.apache.wicket.model.Model;

import com.disnel.knihoveda.mapa.KnihovedaMapaApplication;
import com.disnel.knihoveda.mapa.KnihovedaMapaConfig;
import com.disnel.knihoveda.mapa.MapaSession;
import com.disnel.knihoveda.mapa.data.DataSet;
import com.disnel.knihoveda.mapa.data.ResultsInPlace;
import com.disnel.knihoveda.mapa.events.MistoSelectEvent;

public class MapaMistoOverlay extends Panel
{

	private enum DisplayState { NORMAL, SELECTED, SHADED };
	
	public MapaMistoOverlay(String id, ResultsInPlace resultsInPlace)
	{
		super(id);
		
		setOutputMarkupId(true);
		
		// Zjistit, jestli jak se ma zobrazit vhledem k moznemu vyberu
		DisplayState dState;
		DataSet currentDataSet = MapaSession.get().currentDataSet();
		if ( !currentDataSet.isAnyPlaceSelected() )
		{
			dState = DisplayState.NORMAL;
		}
		else
		{
			if ( currentDataSet.isPlaceSelected(resultsInPlace.getPlaceName()))
				dState = DisplayState.SELECTED;
			else
				dState = DisplayState.SHADED;
		}
		
		// Tecka v miste obce
		Component bottomDot;
		add(bottomDot = new WebMarkupContainer("bottomDot"));

		// Vyhodit sady s nulovymi vysledky
		List<DataSet> dataSetsToDisplay = new ArrayList<>(MapaSession.get().dataSets());
		Iterator<DataSet> it = dataSetsToDisplay.iterator();
		while ( it.hasNext() )
			if ( resultsInPlace.getNumResultsForDataSet(it.next()) == 0)
				it.remove();
		
		// Graf s vysledky a jeho sloupce
		RepeatingView bars;
		add(bars = new RepeatingView("result"));
		
		if ( dState != DisplayState.SHADED )
			for ( DataSet dataSet : dataSetsToDisplay )
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
		add(detail = new WebMarkupContainer("detail", Model.of(resultsInPlace.getPlaceName())));
		detail.setOutputMarkupId(true);
		
		detail.add(new Label("nazevMista", resultsInPlace.getPlaceName()));
		
		detail.add(new ListView<DataSet>("vysledek", dataSetsToDisplay)
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
		
		detail.add(new AjaxEventBehavior("click")
		{
			@Override
			protected void onEvent(AjaxRequestTarget target)
			{
				String placeName = (String) getComponent().getDefaultModelObject();
				
				boolean selected = MapaSession.get().currentDataSet()
						.toggleSelectedPlace(placeName);
				
				if ( selected )
					target.appendJavaScript(String.format(
							"$('#%s').addClass('selected')", MapaMistoOverlay.this.getMarkupId()));
				else
					target.appendJavaScript(String.format(
							"$('#%s').removeClass('selected')", MapaMistoOverlay.this.getMarkupId()));
				
				send(getPage(), Broadcast.BREADTH,
						new MistoSelectEvent(target, placeName));
			}
		});
		
		// Nejaky ten JS pro zobrazovani detailu
		bottomDot.add(new AttributeAppender("onmouseover",
				String.format("$('#%s').show();", detail.getMarkupId())));
		detail.add(new AttributeAppender("onmouseleave",
				String.format("$('#%s').hide();", detail.getMarkupId())));
		
		// Nastavit, jak se ma zobrazovat
		add(new AttributeAppender("class", dState.name().toLowerCase(), " "));
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
