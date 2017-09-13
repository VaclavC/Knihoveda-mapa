package com.disnel.knihoveda.mapa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.openlayers3.DefaultOpenLayersMap;
import org.wicketstuff.openlayers3.api.View;
import org.wicketstuff.openlayers3.api.coordinate.LongLat;
import org.wicketstuff.openlayers3.api.geometry.Point;
import org.wicketstuff.openlayers3.api.layer.Layer;
import org.wicketstuff.openlayers3.api.layer.Tile;
import org.wicketstuff.openlayers3.api.overlay.Overlay;
import com.disnel.knihoveda.dao.PlaceLocationDAO;
import com.disnel.knihoveda.dao.SolrDAO;
import com.disnel.knihoveda.mapa.events.SearchEvent;
import com.disnel.knihoveda.mapa.ol.CustomTileSource;
import com.disnel.knihoveda.wicket.AjaxOLMap;

public class MapaPanel extends Panel
{

	private WebMarkupContainer mapaOverlays;
	
	private AjaxOLMap mapa;
	
	public MapaPanel(String id)
	{
		super(id);

		add(new DefaultOpenLayersMap("mapa", Model.of(mapa = new AjaxOLMap(
				
				Arrays.<Layer>asList(
						new Tile(
								"Mapa",
								new CustomTileSource("http://localhost/osm_tiles/{z}/{x}/{y}.png"))
				),
				
				new View(new LongLat(15.335125, 49.741807, "EPSG:4326" ).transform(View.DEFAULT_PROJECTION), 7)))));
		
		add(mapaOverlays = new WebMarkupContainer("mapa-overlays"));
		mapaOverlays.setOutputMarkupId(true);
		
		OverlaySet os = new OverlaySet(null, "overlay");
		mapa.setOverlays(os.getOverlaysList());
		mapaOverlays.add(os.getOverlayrsRV());
	}

	@Override
	public void onEvent(IEvent<?> event)
	{
		if ( event.getPayload() instanceof SearchEvent)
		{
			SearchEvent ev = (SearchEvent) event.getPayload();
	
			OverlaySet os = new OverlaySet(ev.getParameters(), "overlay");
			mapa.setOverlays(os.getOverlaysList());
			mapaOverlays.replace(os.getOverlayrsRV());
			
			ev.getTarget().add(mapaOverlays);
			ev.getTarget().appendJavaScript(mapa.overlaysChangedJs());
		}
	}
	
	private static class OverlaySet
	{
		private List<Overlay> overlaysList;
		private RepeatingView overlayrsRV;

		public OverlaySet(PageParameters searchParams, String rvId)
		{
			// Pripravit dotaz
			SolrQuery query = new SolrQuery();
			SolrDAO.addQueryParameters(query, searchParams);
			query.addFacetField("publishPlace");
			query.setFacetMinCount(1);
			query.setRows(0);
			
			System.out.println("Query for mapa: " + query);
			
			// Ziskat odpoved
			QueryResponse response = SolrDAO.getResponse(query);
			FacetField publishPlaceFF = response.getFacetField("publishPlace");
			
			// Zpracovat vysledky
			overlaysList = new ArrayList<>(publishPlaceFF.getValueCount());
			overlayrsRV = new RepeatingView(rvId);
			
			for ( Count count : publishPlaceFF.getValues() )
			{
				String placeName = count.getName();
				Point placePoint = PlaceLocationDAO.getPointForPlace(placeName);
				
				if ( placePoint != null )
				{
					MistoOverlayPanel placeOverlay = new MistoOverlayPanel(overlayrsRV.newChildId(),
							placeName,
							Long.toString(count.getCount()).toString()); 

					overlayrsRV.add(placeOverlay);

					overlaysList.add(new Overlay(
							placeOverlay,
							new LongLat(placePoint.getCoordinate(), "EPSG:4326" ).transform(View.DEFAULT_PROJECTION)));
				}
			}	
		}

		public List<Overlay> getOverlaysList()
		{
			return overlaysList;
		}

		public RepeatingView getOverlayrsRV()
		{
			return overlayrsRV;
		}
		
	}
	
}
