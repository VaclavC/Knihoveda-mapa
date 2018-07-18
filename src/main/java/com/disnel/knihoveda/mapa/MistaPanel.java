package com.disnel.knihoveda.mapa;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.NumberTextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.wicketstuff.openlayers3.api.View;
import org.wicketstuff.openlayers3.api.coordinate.LongLat;
import org.wicketstuff.openlayers3.api.geometry.Point;

import com.disnel.knihoveda.dao.PlaceLocationDAO;
import com.disnel.knihoveda.dao.SolrDAO;

public class MistaPanel extends Panel
{
	
	private Set<String> placeNames;
	
	private FileUploadField fileUploadField;

	public MistaPanel(String id)
	{
		super(id);
		
		setOutputMarkupId(true);
	
		// Nacist vsechna mista, ktera potrebujeme lokalizovat
		SolrQuery query = new SolrQuery();
		SolrDAO.addEmptyQueryParameters(query);
		query.addFacetField("publishPlace");
		query.setFacetMinCount(1);
		query.setRows(0);

		QueryResponse response = SolrDAO.getResponse(query);
		
		FacetField publishPlaceFF = response.getFacetField("publishPlace");

		placeNames = new HashSet<>();
		
		// Zobrazit editacni formular
		add(new ListView<Count>("place", publishPlaceFF.getValues())
		{
			@Override
			protected void populateItem(ListItem<Count> item)
			{
				String placeName = item.getModelObject().getName();
				Point placePoint = PlaceLocationDAO.getPointForPlace(placeName);
				
				placeNames.add(placeName);
				
				item.setMarkupId(placeMarkupId(placeName.toLowerCase()));
				item.setOutputMarkupId(true);
				
				if ( placePoint == null )
					item.add(new AttributeAppender("class", "undefined", " "));
				
				final PlacePos pp = new PlacePos(placeName,
						placePoint != null ? placePoint.getCoordinate().getY().doubleValue() : null,
						placePoint != null ? placePoint.getCoordinate().getX().doubleValue(): null);
				
				item.add(new Label("label", placeName));
				
				item.add(new NumberTextField<Double>("lon",
						new PropertyModel<Double>(pp, "lon"))
						.add(new AjaxFormComponentUpdatingBehavior("change")
						{
							@Override
							protected void onUpdate(AjaxRequestTarget target)
							{
								updatePos(target, pp);
							}
						}));
				
				item.add(new NumberTextField<Double>("lat",
						new PropertyModel<Double>(pp, "lat"))
						.add(new AjaxFormComponentUpdatingBehavior("change")
						{
							@Override
							protected void onUpdate(AjaxRequestTarget target)
							{
								updatePos(target, pp);
							}
						}));
			}
		});

		// File upload
		Form<?> uploadForm;
		add(uploadForm = new Form<>("uploadForm"));
		
		uploadForm.add(fileUploadField = new FileUploadField("fileUpload"));
		
		uploadForm.add(new AjaxButton("load")
		{
			@Override
            protected void onSubmit(AjaxRequestTarget target)
            {
				FileUpload fileUpload = fileUploadField.getFileUpload();
				
				if ( fileUpload != null )
				{
					PlaceLocationDAO.readFromCSV(fileUpload.getBytes(), placeNames);
					
					MistaPanel.this.replaceWith(
							new MistaPanel(MistaPanel.this.getId())
							.setOutputMarkupId(true));
				}
            }
		});
		
		// Zaviraci tlacitko
		add(new AjaxLink<Void>("close")
		{
			@Override
			public void onClick(AjaxRequestTarget target)
			{
				Panel panel;
				MistaPanel.this.replaceWith(panel = new VyberPanel(MistaPanel.this.getId()));
				target.add(panel);
			}
		});
	}

	private class PlacePos implements Serializable
	{
		protected String place;
		
		protected Double lat, lon;

		public PlacePos(String place, Double lat, Double lon)
		{
			super();
			this.place = place;
			this.lat = lat;
			this.lon = lon;
		}
	}
	
	private void updatePos(AjaxRequestTarget target, PlacePos pos)
	{
		if ( pos.lat == null || pos.lon == null )
		{
			target.appendJavaScript("$('#" + placeMarkupId(pos.place) + "').addClass('unsaved');");
		}
		else
		{
			PlaceLocationDAO.setPointForPlace(pos.place, new Point(
					new LongLat(pos.lon, pos.lat, "EPSG:4326").transform(View.DEFAULT_PROJECTION)));
			
			target.appendJavaScript("$('#" + placeMarkupId(pos.place) + "').removeClass('unsaved');");
			target.appendJavaScript("$('#" + placeMarkupId(pos.place) + "').removeClass('undefined');");
		}
	}

	private String placeMarkupId(String placeName)
	{
		return "place-" + placeName;
	}
	
}
