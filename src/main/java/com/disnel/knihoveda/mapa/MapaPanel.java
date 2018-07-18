package com.disnel.knihoveda.mapa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.wicketstuff.openlayers3.DefaultOpenLayersMap;
import org.wicketstuff.openlayers3.api.View;
import org.wicketstuff.openlayers3.api.coordinate.LongLat;
import org.wicketstuff.openlayers3.api.layer.Layer;
import org.wicketstuff.openlayers3.api.layer.Tile;
import org.wicketstuff.openlayers3.api.overlay.Overlay;
import com.disnel.knihoveda.mapa.data.DataSet;
import com.disnel.knihoveda.mapa.events.DataSetChangedEvent;
import com.disnel.knihoveda.mapa.events.FieldValuesChangedEvent;
import com.disnel.knihoveda.mapa.ol.CustomTileSource;
import com.disnel.knihoveda.wicket.AjaxOLMap;

public class MapaPanel extends Panel
{

	private AjaxOLMap mapa;

	private WebMarkupContainer mapaOverlays;
	
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

		add(mapaOverlays = new WebMarkupContainer("overlays"));
		mapaOverlays.setOutputMarkupId(true);
		
		List<Overlay> overlays =
				createOverlays(mapaOverlays);
		
		mapa.setOverlays(overlays);
	}

	private List<Overlay> createOverlays(WebMarkupContainer cont)
	{
		List<Overlay> overlays = new ArrayList<>();
		
		RepeatingView rv;
		cont.add(rv = new RepeatingView("overlaySet"));
		
		for ( DataSet dataSet : MapaSession.get().dataSets() )
		{
			MapaOverlaySetPanel mapaOSPanel;
			rv.add(mapaOSPanel = new MapaOverlaySetPanel(rv.newChildId(), dataSet));
			
			overlays.addAll(mapaOSPanel.getOverlaysList());
		}
		
		return overlays;
	}
	
	@Override
	public void onEvent(IEvent<?> event)
	{
		if ( event.getPayload() instanceof FieldValuesChangedEvent)
		{
			FieldValuesChangedEvent ev = (FieldValuesChangedEvent) event.getPayload();
			
			updateOverlays(ev.getTarget());
		}
		else if ( event.getPayload() instanceof DataSetChangedEvent )
		{
			DataSetChangedEvent ev = (DataSetChangedEvent) event.getPayload();
			
			updateOverlays(ev.getTarget());
		}
	}
	
	private void updateOverlays(AjaxRequestTarget target)
	{
		mapaOverlays.replaceWith(mapaOverlays =
				new WebMarkupContainer(mapaOverlays.getId()));
		mapaOverlays.setOutputMarkupId(true);
		
		List<Overlay> overlays =
				createOverlays(mapaOverlays);

		mapa.setOverlays(overlays);
		
		target.add(mapaOverlays);
		target.appendJavaScript(mapa.overlaysChangedJs());
	}
	
}
