package com.disnel.knihoveda.mapa.mapa;

import java.util.stream.Collectors;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.disnel.knihoveda.dao.VuFindDAO;
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
		WebMarkupContainer placeNameCont;
		add(placeNameCont = new WebMarkupContainer("placeNameCont"));
		
		AbstractLink placeLink;
		placeNameCont.add(placeLink = new ExternalLink("placeLink", resultsInPlace.getPlaceLink()));
		placeLink.add(new AttributeModifier("target", "_blank"));
		
		Component placeName;
		placeLink.add(placeName = new Label("placeName", resultsInPlace.getPlaceName()));
				//.add(new AttributeAppender("style", "color: " + currentDataSet.getColor() + ";", ";")));
		
		// Tecka v miste obce
		Component dot;
		add(dot = new WebMarkupContainer("dot"));
		
		dot.add(new AjaxEventBehavior("click")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onEvent(AjaxRequestTarget target)
			{
				String placeName = resultsInPlace.getPlaceName();
				
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

		// Detail vysledku
		WebMarkupContainer detail;
		add(detail = new WebMarkupContainer("detail", Model.of(resultsInPlace.getPlaceName())));
		detail.setOutputMarkupId(true);
		
		detail.add(new ListView<DataSet>("dataSet",
				KnihovedaMapaSession.get().dataSets()
				.stream()
				.filter( ds -> ds.isActive() && resultsInPlace.getNumResultsForDataSet(ds) > 0 )
				.collect(Collectors.toList()))
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
				
				item.add(new ExternalLink("vuFindLink", new IModel<String>()
				{
					private static final long serialVersionUID = 1L;

					@Override
					public String getObject()
					{
						return VuFindDAO.linkToVuFind(KnihovedaMapaSession.get().currentDataSet(),
								new DataSet.FieldValues(KnihovedaMapaConfig.FIELD_PLACE_NAME, resultsInPlace.getPlaceName()));
					}
				}));
				
				if ( ! dataSet.isActive()
						|| resultsInPlace.getNumResultsForDataSet(dataSet) == 0 )
					results.setVisible(false);
			}
		});
				
		// Nejaky ten JS pro zobrazovani detailu
		AttributeAppender showOnOver = new AttributeAppender("onmouseover",
				String.format("$('#%s').removeClass('hidden'); $('#%s').addClass('active'); $('#%s').addClass('active');",
						detail.getMarkupId(), placeNameCont.getMarkupId(), dot.getMarkupId())); 
		dot.add(showOnOver);
		detail.add(showOnOver);
		
		AttributeAppender hideOnLeave = new AttributeAppender("onmouseleave",
				String.format("$('#%s').addClass('hidden'); $('#%s').removeClass('active'); $('#%s').removeClass('active');",
						detail.getMarkupId(), placeNameCont.getMarkupId(), dot.getMarkupId())); 
		detail.add(hideOnLeave);
		placeName.add(hideOnLeave);
		dot.add(hideOnLeave);
		
		// Nastavit, jak se ma zobrazovat
		add(new AttributeAppender("class", dState.name().toLowerCase(), " "));
	}
	
}
