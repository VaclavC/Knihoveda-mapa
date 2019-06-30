package com.disnel.knihoveda.mapa.panel;

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.wicketstuff.openlayers3.DefaultOpenLayersMap;
import org.wicketstuff.openlayers3.api.JavascriptObject;
import org.wicketstuff.openlayers3.api.View;
import org.wicketstuff.openlayers3.api.coordinate.LongLat;
import org.wicketstuff.openlayers3.api.layer.Layer;
import org.wicketstuff.openlayers3.api.layer.Tile;

import com.disnel.knihoveda.dao.SolrDAO;
import com.disnel.knihoveda.mapa.KnihovedaMapaConfig;
import com.disnel.knihoveda.mapa.KnihovedaMapaSession;
import com.disnel.knihoveda.mapa.data.ResultsInPlace;
import com.disnel.knihoveda.mapa.events.AjaxEvent;
import com.disnel.knihoveda.mapa.events.FieldValuesChangedEvent;
import com.disnel.knihoveda.mapa.events.UserSelectionChangedEvent;
import com.disnel.knihoveda.mapa.mapa.MapaOverlays;
import com.disnel.knihoveda.mapa.ol.CustomTileSource;
import com.disnel.knihoveda.wicket.AjaxOLMap;

public class Mapa extends Panel
{
	private static final long serialVersionUID = 1L;

	private WebMarkupContainer mapaCont;
	
	private AjaxOLMap mapa;

	private MapaOverlays mapaOverlays;
	
	private IModel<List<ResultsInPlace>> resultsInPlacesModel;
	
	public Mapa(String id)
	{
		super(id);

		add(mapaCont = new DefaultOpenLayersMap("mapa", Model.of(mapa = new AjaxOLMap(
				
				Arrays.<Layer>asList(
						new Tile(
								"Mapa",
								new CustomTileSource(KnihovedaMapaConfig.osmURL))
				),
				
//				new View(new LongLat(15.335125, 49.741807, "EPSG:4326" )
				new View(new LongLat(16.644, 49.493, "EPSG:4326" )
						.transform(View.DEFAULT_PROJECTION), 8)
						.minZoom(8)
						.maxZoom(11)
						))));
		mapaCont.setOutputMarkupId(true);

		add(mapaOverlays = new MapaOverlays("overlays",
				resultsInPlacesModel = new LoadableDetachableModel<List<ResultsInPlace>>()
				{
					private static final long serialVersionUID = 1L;

					@Override
					protected List<ResultsInPlace> load()
					{
						return SolrDAO.getResultsInPlaces();
					}
				}));
		mapaOverlays.setOutputMarkupId(true);
		
		mapa.setOverlays(mapaOverlays.getOverlays());
		
		// Na dvojklik zrusit jakykoliv vyber mista
		add(new AjaxEventBehavior("dblclick")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onEvent(AjaxRequestTarget target)
			{
				if ( KnihovedaMapaSession.get().currentDataSet()
					.removeFieldValues(KnihovedaMapaConfig.FIELD_PLACE_NAME) != null )
				{
					
					target.appendJavaScript("$('.mistoOverlay').removeClass('selected');");
					
					send(getPage(), Broadcast.BREADTH,
							new FieldValuesChangedEvent(target, KnihovedaMapaConfig.FIELD_PLACE_NAME));
				}
			}
		});
		
	}

	@Override
	public void onEvent(IEvent<?> event)
	{
		if ( event.getPayload() instanceof UserSelectionChangedEvent )
		{
			AjaxEvent ev = (AjaxEvent) event.getPayload();
			
			resultsInPlacesModel.detach();
			mapaOverlays.replaceWith(mapaOverlays =
					new MapaOverlays(mapaOverlays.getId(), resultsInPlacesModel));
			mapaOverlays.setOutputMarkupId(true);
			
			mapa.setOverlays(mapaOverlays.getOverlays());
			
			ev.getTarget().add(mapaOverlays);
			ev.getTarget().appendJavaScript(mapa.overlaysChangedJs());
			ev.getTarget().appendJavaScript("scaleMapOverlays();");
		}
	}
	
	@Override
	public void onDetach()
	{
		resultsInPlacesModel.detach();
		
		super.onDetach();
	}
	
	@Override
	public void renderHead(IHeaderResponse response)
	{
		// Ulozit mapu jako data do tagu
		response.render(OnDomReadyHeaderItem.forScript(
				String.format("$('#%s').data('mapObject', %s);",
						mapaCont.getMarkupId(), mapa.getJsId())));
		
		// Zakazat zoom na doubleclick (potrebujeme ho na ruseni vyberu)
		String removeInteraction = "var dblClickInteraction;\n" +
				JavascriptObject.JS_GLOBAL + "['map_mapa-content']" +
				".getInteractions().getArray().forEach(function(interaction) {\n" + 
				"  if (interaction instanceof ol.interaction.DoubleClickZoom) {\n" + 
				"    dblClickInteraction = interaction;\n" + 
				"  }\n" + 
				"});\n" +
				JavascriptObject.JS_GLOBAL + "['map_mapa-content']" +
				".removeInteraction(dblClickInteraction);";
		
		response.render(OnDomReadyHeaderItem.forScript(removeInteraction));
		
		// Pripnout callback pro zoomovani overlays
		String attachOverlayZoom =
				String.format(
						"%s.getView().on('change:resolution', scaleMapOverlays);",
						mapa.getJsId());
		
		response.render(OnDomReadyHeaderItem.forScript(attachOverlayZoom));
	}
	
}
