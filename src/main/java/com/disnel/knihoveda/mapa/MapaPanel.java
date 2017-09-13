package com.disnel.knihoveda.mapa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.openlayers3.DefaultOpenLayersMap;
import org.wicketstuff.openlayers3.api.Feature;
import org.wicketstuff.openlayers3.api.Map;
import org.wicketstuff.openlayers3.api.PersistentFeature;
import org.wicketstuff.openlayers3.api.View;
import org.wicketstuff.openlayers3.api.coordinate.LongLat;
import org.wicketstuff.openlayers3.api.geometry.Point;
import org.wicketstuff.openlayers3.api.layer.Layer;
import org.wicketstuff.openlayers3.api.layer.Tile;
import org.wicketstuff.openlayers3.api.source.tile.Osm;
import org.wicketstuff.openlayers3.api.source.vector.VectorSource;
import com.disnel.knihoveda.dao.PlaceLocationDAO;
import com.disnel.knihoveda.dao.SolrDAO;
import com.disnel.knihoveda.mapa.events.SearchEvent;
import com.disnel.knihoveda.mapa.ol.CustomTileSource;
import com.disnel.knihoveda.wicket.AjaxVector;
import com.disnel.knihoveda.wicket.TiskyStyle;

public class MapaPanel extends Panel
{

	private AjaxVector mapaVectorLayer;
	
	public MapaPanel(String id)
	{
		super(id);

		add(new DefaultOpenLayersMap("mapa", Model.of(new Map(
				
				Arrays.<Layer>asList(
						new Tile(
								new Osm())
//								"Mapa",
//								new CustomTileSource("http://localhost/osm_tiles/{z}/{x}/{y}.png")),
//						
//						mapaVectorLayer = new AjaxVector(
//								new VectorSource(getMapFeatures(null)))
				),
				
				new View(new LongLat(15.335125, 49.741807, "EPSG:4326" ).transform(View.DEFAULT_PROJECTION), 7)))));
	}

	@Override
	public void onEvent(IEvent<?> event)
	{
		if ( event.getPayload() instanceof SearchEvent)
		{
			SearchEvent ev = (SearchEvent) event.getPayload();
	
			// Vytvorit novy seznam veci ke zobrazeni
			List<Feature> mapFeatures = getMapFeatures(ev.getParameters());
			
			// Zobrazit na mape
			mapaVectorLayer.setSource(ev.getTarget(),
					new VectorSource(mapFeatures));
		}
	}
	
	private List<Feature> getMapFeatures(PageParameters searchParams)
	{
		SolrQuery query = new SolrQuery();
		SolrDAO.addQueryParameters(query, searchParams);
		query.addFacetField("publishPlace");
		query.setFacetMinCount(1);
		query.setRows(0);
		
		System.out.println("Query for mapa: " + query);
		
		QueryResponse response = SolrDAO.getResponse(query);
		
		FacetField publishPlaceFF = response.getFacetField("publishPlace");
		
		List<Feature> listOfFeatures = new ArrayList<>();
		for ( Count count : publishPlaceFF.getValues() )
		{
			String placeName = count.getName();
			Point placePoint = PlaceLocationDAO.getPointForPlace(placeName);
			
			if ( placePoint != null )
				listOfFeatures.add(new PersistentFeature(
						placePoint,
						placeName,
						new TiskyStyle(Long.toString(count.getCount()))));
		}	
		
		return listOfFeatures;
	}
	
}
