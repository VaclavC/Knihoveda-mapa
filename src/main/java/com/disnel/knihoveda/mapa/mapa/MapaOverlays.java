package com.disnel.knihoveda.mapa.mapa;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.wicketstuff.openlayers3.api.View;
import org.wicketstuff.openlayers3.api.coordinate.LongLat;
import org.wicketstuff.openlayers3.api.overlay.Overlay;

import com.disnel.knihoveda.mapa.data.ResultsInPlace;

public class MapaOverlays extends Panel
{
	private static final long serialVersionUID = 1L;

	private List<Overlay> overlays;

	public MapaOverlays(String id, IModel<List<ResultsInPlace>> model)
	{
		super(id, model);
		
		setOutputMarkupId(true);
		
		List<ResultsInPlace> resultsInPlaces = model.getObject();
		
		overlays = new ArrayList<>(resultsInPlaces.size());
		
		RepeatingView overlaysRV;
		add(overlaysRV = new RepeatingView("overlay"));
		
		for ( ResultsInPlace resultsInPlace : resultsInPlaces )
		{
			Component overlay;
			overlaysRV.add(overlay = new MapaMistoOverlay(overlaysRV.newChildId(), resultsInPlace));
			
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
