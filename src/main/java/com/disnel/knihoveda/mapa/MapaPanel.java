package com.disnel.knihoveda.mapa;

import java.util.Arrays;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.wicketstuff.openlayers3.DefaultOpenLayersMap;
import org.wicketstuff.openlayers3.api.View;
import org.wicketstuff.openlayers3.api.coordinate.LongLat;
import org.wicketstuff.openlayers3.api.layer.Layer;
import org.wicketstuff.openlayers3.api.layer.Tile;
import com.disnel.knihoveda.mapa.events.AjaxEvent;
import com.disnel.knihoveda.mapa.events.UserSelectionChangedEvent;
import com.disnel.knihoveda.mapa.mapa.MapaOverlays;
import com.disnel.knihoveda.mapa.ol.CustomTileSource;
import com.disnel.knihoveda.wicket.AjaxOLMap;
import com.disnel.knihoveda.wicket.model.ResultsInPlacesModel;

public class MapaPanel extends Panel
{

	private AjaxOLMap mapa;

	private MapaOverlays mapaOverlays;
	
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

		add(mapaOverlays = new MapaOverlays("overlays",
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
			mapaOverlays.replaceWith(mapaOverlays =
					new MapaOverlays(mapaOverlays.getId(), resultsInPlacesModel));
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
	
}
