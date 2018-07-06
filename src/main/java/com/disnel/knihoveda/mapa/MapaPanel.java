package com.disnel.knihoveda.mapa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.Group;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.openlayers3.DefaultOpenLayersMap;
import org.wicketstuff.openlayers3.api.View;
import org.wicketstuff.openlayers3.api.coordinate.Coordinate;
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
								new CustomTileSource(KnihovedaMapaConfig.osmURL))
				),
				
				new View(new LongLat(15.335125, 49.741807, "EPSG:4326" ).transform(View.DEFAULT_PROJECTION), 8)))));
		
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
			query.addField("publishPlace");
			query.addField("long_lat");
			query.add("group", "true");
			query.add("group.field", "publishPlace");
			query.setRows(-1);
			
			// Ziskat odpoved
			QueryResponse response = SolrDAO.getResponse(query);
			List<Group> resGroups = response.getGroupResponse().getValues().get(0).getValues();
			
			// Zpracovat vysledky
			overlaysList = new ArrayList<>(resGroups.size());
			overlayrsRV = new RepeatingView(rvId);
			
			// Najit maximalni hodnotu
			long countMax = 0;
			for ( Group group : resGroups )
			{
				long count = group.getResult().getNumFound();
				if ( countMax < count )
					countMax = count;
			}
			
			// Zobrazit overlays
			for ( Group group : resGroups )
			{
				if ( group.getGroupValue() != null )
				{
					long count = group.getResult().getNumFound();
					
					SolrDocument doc = group.getResult().get(0);
					String placeName = (String) doc.getFieldValue("publishPlace");
					Point placePoint = pointFromString((String) doc.getFieldValue("long_lat"));
				
					MistoOverlayPanel placeOverlay = new MistoOverlayPanel(overlayrsRV.newChildId(),
							placeName, count, countMax);

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
	
	private static Point pointFromString(String coordinates)
	{
		String[] parts = coordinates.split(",");
		double lat = Double.parseDouble(parts[0]);
		double lon = Double.parseDouble(parts[1]);
		
		return new Point(new Coordinate(lon, lat));
	}
	
}
