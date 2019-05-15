package com.disnel.knihoveda.mapa.mapa;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
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
		Component placeName;
		add(placeName = new Label("placeName", resultsInPlace.getPlaceName())
				.add(new AttributeAppender("style", "color: " + currentDataSet.getColor() + ";", ";")));
		
		// Tecka v miste obce
		Component dot;
		add(dot = new WebMarkupContainer("dot"));

		// Detail vysledku
		WebMarkupContainer detail;
		add(detail = new WebMarkupContainer("detail", Model.of(resultsInPlace.getPlaceName())));
		detail.setOutputMarkupId(true);
		
		detail.add(new ListView<DataSet>("dataSet", KnihovedaMapaSession.get().dataSets())
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<DataSet> item)
			{
				DataSet dataSet = item.getModelObject();
				
				WebMarkupContainer indicator;
				item.add(indicator = new WebMarkupContainer("indicator"));
				indicator.add(new AttributeAppender("style", "background-color: " + dataSet.getColor() + ";", ";"));
				
				Component results;
				item.add(results = new Label("results", resultsInPlace.getNumResultsForDataSet(dataSet)));
				
				if ( ! dataSet.isActive()
						|| resultsInPlace.getNumResultsForDataSet(dataSet) == 0 )
					results.setVisible(false);
			}
		});
				
		// Nejaky ten JS pro zobrazovani detailu
		AttributeAppender showOnOver = new AttributeAppender("onmouseover",
				String.format("$('#%s').removeClass('hidden');", detail.getMarkupId())); 
		dot.add(showOnOver);
		detail.add(showOnOver);
		
		AttributeAppender hideOnLeave = new AttributeAppender("onmouseleave",
				String.format("$('#%s').addClass('hidden');", detail.getMarkupId())); 
		detail.add(hideOnLeave);
		placeName.add(hideOnLeave);
		dot.add(hideOnLeave);
		
		// Nastavit, jak se ma zobrazovat
		add(new AttributeAppender("class", dState.name().toLowerCase(), " "));
	}
	
}
