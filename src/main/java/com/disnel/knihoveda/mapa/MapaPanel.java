package com.disnel.knihoveda.mapa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.wicketstuff.openlayers3.DefaultOpenLayersMap;
import org.wicketstuff.openlayers3.api.View;
import org.wicketstuff.openlayers3.api.coordinate.LongLat;
import org.wicketstuff.openlayers3.api.layer.Layer;
import org.wicketstuff.openlayers3.api.layer.Tile;
import org.wicketstuff.openlayers3.api.overlay.Overlay;
import com.disnel.knihoveda.mapa.data.ResultsInPlace;
import com.disnel.knihoveda.mapa.events.AjaxEvent;
import com.disnel.knihoveda.mapa.events.UserSelectionChangedEvent;
import com.disnel.knihoveda.mapa.ol.CustomTileSource;
import com.disnel.knihoveda.wicket.AjaxOLMap;
import com.disnel.knihoveda.wicket.model.ResultsInPlacesModel;

public class MapaPanel extends Panel
{

	private AjaxOLMap mapa;

	private OverlaysPanel mapaOverlays;
	
	private ResultsInPlacesModel resultsInPlacesModel;
	
	public MapaPanel(String id)
	{
		super(id);

		add(new DefaultOpenLayersMap("mapa", Model.of(mapa = new AjaxOLMap(
				
				Arrays.<Layer>asList(
						new Tile(
								"Mapa",
								new CustomTileSource(KnihovedaMapaConfig.osmURL))
				),
				
				new View(new LongLat(15.335125, 49.741807, "EPSG:4326" ).transform(View.DEFAULT_PROJECTION), 8)))));

		add(mapaOverlays = new OverlaysPanel("overlays",
				resultsInPlacesModel = new ResultsInPlacesModel()));
		mapaOverlays.setOutputMarkupId(true);
		
		mapa.setOverlays(mapaOverlays.getOverlays());
	}

	@Override
	public void onEvent(IEvent<?> event)
	{
		if ( event.getPayload() instanceof UserSelectionChangedEvent )
		{
			AjaxEvent ev = (AjaxEvent) event.getPayload();
			
			resultsInPlacesModel.detach();
			mapaOverlays.replaceWith(mapaOverlays = new OverlaysPanel(mapaOverlays.getId(), resultsInPlacesModel));
			mapaOverlays.setOutputMarkupId(true);
			
			mapa.setOverlays(mapaOverlays.getOverlays());
			
			ev.getTarget().add(mapaOverlays);
			ev.getTarget().appendJavaScript(mapa.overlaysChangedJs());
		}
	}
	
	@Override
	public void onDetach()
	{
		resultsInPlacesModel.detach();
		
		super.onDetach();
	}
	
	private class OverlaysPanel extends Panel
	{

		private List<Overlay> overlays;
		
		public OverlaysPanel(String id, IModel<List<ResultsInPlace>> model)
		{
			super(id, model);
			
			List<ResultsInPlace> resultsInPlaces = model.getObject();
			
			overlays = new ArrayList<>(resultsInPlaces.size());
			
			RepeatingView overlaysRV;
			add(overlaysRV = new RepeatingView("overlay"));
			
			for ( ResultsInPlace resultsInPlace : resultsInPlaces )
			{
				Component overlay;
				overlaysRV.add(overlay = new MapaMistoOverlayPanel(overlaysRV.newChildId(), resultsInPlace));
				
				overlays.add(new Overlay(
						overlay,
						new LongLat(resultsInPlace.getPlacePoint().getCoordinate(), "EPSG:4326" ).transform(View.DEFAULT_PROJECTION)
						));
			}
		}
		
		public List<Overlay> getOverlays()
		{
			return overlays;
		}
	}
	
}
