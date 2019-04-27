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
import org.apache.wicket.model.Model;

import com.disnel.knihoveda.mapa.KnihovedaMapaConfig;
import com.disnel.knihoveda.mapa.KnihovedaMapaSession;
import com.disnel.knihoveda.mapa.data.DataSet;
import com.disnel.knihoveda.mapa.data.ResultsInPlace;
import com.disnel.knihoveda.mapa.events.FieldValuesChangedEvent;

public class MapaMistoOverlay extends Panel
{
	private static final long serialVersionUID = 1L;

	private enum DisplayState { NORMAL, SELECTED, SHADED };
	
	public MapaMistoOverlay(String id, ResultsInPlace resultsInPlace)
	{
		super(id);
		
		setOutputMarkupId(true);
		
		// Zjistit, jestli jak se ma zobrazit vhledem k moznemu vyberu
		DisplayState dState;
		DataSet currentDataSet = KnihovedaMapaSession.get().currentDataSet();
		if ( ! currentDataSet.hasFieldValue(KnihovedaMapaConfig.FIELD_PLACE_NAME) )
		{
			dState = DisplayState.NORMAL;
		}
		else
		{
			if ( currentDataSet.hasFieldValue(KnihovedaMapaConfig.FIELD_PLACE_NAME, resultsInPlace.getPlaceName()) )
				dState = DisplayState.SELECTED;
			else
				dState = DisplayState.SHADED;
		}

		// Informace o miste
		add(new Label("placeName", resultsInPlace.getPlaceName())
				.add(new AttributeAppender("style", "color: " + currentDataSet.getColor() + ";", ";")));
		add(new Label("count", resultsInPlace.getNumResultsForDataSet(currentDataSet))
				.add(new AttributeAppender("style", "color: " + currentDataSet.getColor() + ";", ";")));
		
		// Tecka v miste obce
		Component dot;
		add(dot = new WebMarkupContainer("dot"));

		// Vyhodit sady s nulovymi vysledky nebo neaktivni
		List<DataSet> dataSetsToDisplay = new ArrayList<>(KnihovedaMapaSession.get().dataSets());
		Iterator<DataSet> it = dataSetsToDisplay.iterator();
		while ( it.hasNext() )
		{
			DataSet dataSet = it.next();
			if ( ! dataSet.isActive()
					|| resultsInPlace.getNumResultsForDataSet(dataSet) == 0 )
				it.remove();
		}

		// Detail vysledku
		WebMarkupContainer detail;
		add(detail = new WebMarkupContainer("detail", Model.of(resultsInPlace.getPlaceName())));
		detail.setOutputMarkupId(true);
		
		detail.add(new Label("nazevMista", resultsInPlace.getPlaceName()));
		
		detail.add(new ListView<DataSet>("vysledek", dataSetsToDisplay)
		{
			private static final long serialVersionUID = 1L;

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
			private static final long serialVersionUID = 1L;

			@Override
			protected void onEvent(AjaxRequestTarget target)
			{
				String placeName = (String) getComponent().getDefaultModelObject();
				
				boolean selected = KnihovedaMapaSession.get()
						.currentDataSet()
						.toggleFieldValue(KnihovedaMapaConfig.FIELD_PLACE_NAME, placeName);
				
				if ( selected )
					target.appendJavaScript(String.format(
							"$('#%s').addClass('selected')", MapaMistoOverlay.this.getMarkupId()));
				else
					target.appendJavaScript(String.format(
							"$('#%s').removeClass('selected')", MapaMistoOverlay.this.getMarkupId()));
				
				send(getPage(), Broadcast.BREADTH,
						new FieldValuesChangedEvent(target, KnihovedaMapaConfig.FIELD_PLACE_NAME));
			}
		});
		
		// Nejaky ten JS pro zobrazovani detailu
		dot.add(new AttributeAppender("onmouseover",
				String.format("$('#%s').show();", detail.getMarkupId())));
		detail.add(new AttributeAppender("onmouseleave",
				String.format("$('#%s').hide();", detail.getMarkupId())));
		
		// Nastavit, jak se ma zobrazovat
		add(new AttributeAppender("class", dState.name().toLowerCase(), " "));
	}
	
}
