package com.disnel.knihoveda.mapa;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.response.Group;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.wicketstuff.openlayers3.api.View;
import org.wicketstuff.openlayers3.api.coordinate.Coordinate;
import org.wicketstuff.openlayers3.api.coordinate.LongLat;
import org.wicketstuff.openlayers3.api.geometry.Point;
import org.wicketstuff.openlayers3.api.overlay.Overlay;

import com.disnel.knihoveda.dao.SolrDAO;
import com.disnel.knihoveda.mapa.data.DataSet;

public class MapaOverlaySetPanel extends Panel
{

	private DataSet dataSet;
	
	private List<Overlay> overlaysList;
	
	public MapaOverlaySetPanel(String id, DataSet dataSet, int zIndex)
	{
		super(id);
		this.dataSet = dataSet;

		List<Group> solrGroups = SolrDAO.getMapOverlays(dataSet);
		
		// Zpracovat vysledky
		overlaysList = new ArrayList<>(solrGroups.size());
		
		// Zobrazit overlays
		RepeatingView rv;
		add(rv = new RepeatingView("overlay"));
		
		for ( Group group : solrGroups )
		{
			if ( group.getGroupValue() != null )
			{
				long count = group.getResult().getNumFound();
				
				SolrDocument doc = group.getResult().get(0);
				String placeName = (String) doc.getFieldValue("publishPlace");
				Point placePoint = pointFromString((String) doc.getFieldValue("long_lat"));
			
				MapaMistoOverlayPanel placeOverlay = new MapaMistoOverlayPanel(rv.newChildId(),
						placeName, count, MapaSession.get().maxCountInPlace(),
						dataSet.getColor(), zIndex);

				rv.add(placeOverlay);

				overlaysList.add(new Overlay(
						placeOverlay,
						new LongLat(placePoint.getCoordinate(), "EPSG:4326" ).transform(View.DEFAULT_PROJECTION)));
			}
		}	
	}

	public DataSet getDataSet()
	{
		return dataSet;
	}

	public List<Overlay> getOverlaysList()
	{
		return overlaysList;
	}

	private static Point pointFromString(String coordinates)
	{
		String[] parts = coordinates.split(",");
		double lat = Double.parseDouble(parts[0]);
		double lon = Double.parseDouble(parts[1]);
		
		return new Point(new Coordinate(lon, lat));
	}
}

