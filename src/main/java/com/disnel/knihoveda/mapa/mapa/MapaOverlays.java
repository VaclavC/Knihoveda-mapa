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

import com.disnel.knihoveda.mapa.KnihovedaMapaSession;
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
			if ( resultsInPlace.getNumResultsForDataSet(KnihovedaMapaSession.get().currentDataSet()) > 0 )
			{
				Component overlay;
				overlaysRV.add(overlay = new MapaMistoOverlay(overlaysRV.newChildId(), resultsInPlace));
				
				Overlay overlayInst = new MyOverlay(
						overlay,
						new LongLat(resultsInPlace.getPlacePoint().getCoordinate(), "EPSG:4326" ).transform(View.DEFAULT_PROJECTION)
					);
				overlayInst.setStopEvent(false);
				
				overlays.add(overlayInst);
			}
	}

	public List<Overlay> getOverlays()
	{
		return overlays;
	}
	
	
	private class MyOverlay extends Overlay
	{
		private static final long serialVersionUID = 1L;

		public MyOverlay(Component overlay, LongLat transform)
		{
			super(overlay, transform);
		}

		@Override
		protected String renderAttributesJs() {

			StringBuilder builder = new StringBuilder();

			if (getElement() != null) {
				builder.append("'element': document.getElementById('" + element.getMarkupId() + "'),");
			}

			if (getPosition() != null) {
				builder.append("'position': " + position.renderJs() + ",");
			}

			if (getPositioning() != null) {
				builder.append("'positioning': '" + getPositioning() + "',");
			}

			if ( getStopEvent() != null ) {
				builder.append("'stopEvent': " + getStopEvent() + ",");
			}

			return builder.toString();
		}
	}
	
}
