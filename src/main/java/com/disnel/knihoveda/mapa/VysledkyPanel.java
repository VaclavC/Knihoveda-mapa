package com.disnel.knihoveda.mapa;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import com.disnel.knihoveda.dao.SolrDAO;
import com.disnel.knihoveda.mapa.events.SearchEvent;

public class VysledkyPanel extends Panel
{

	/**
	 * Kolik se nacte vysledku najednou
	 */
	public static int NUM_RESULTS_PER_PART = 20;
	
	private WebMarkupContainer mainCont;
	
	private int recordsDisplayed;
	
	public VysledkyPanel(String id)
	{
		super(id);
		
		add(mainCont = new WebMarkupContainer("main_cont"));
		mainCont.setOutputMarkupId(true);
		
		mainCont.add(new WebMarkupContainer("result").setVisible(false));
	}

	@Override
	public void onEvent(IEvent<?> event)
	{
		if ( event.getPayload() instanceof SearchEvent)
		{
			SearchEvent ev = (SearchEvent) event.getPayload();
	
			// Pripravit dotaz
			SolrQuery query = new SolrQuery();
			SolrDAO.addQueryParameters(query, ev.getParameters());
			query.setRows(NUM_RESULTS_PER_PART);
			
			System.out.println("Query for vysledky: " + query);
			
			// Provest dotaz
			QueryResponse response = SolrDAO.getResponse(query);
			
			// Pripravit zobrazeni novych vysledku
			WebMarkupContainer newCont;
			newCont = new WebMarkupContainer(mainCont.getId());
			newCont.setOutputMarkupId(true);
			
			newCont.add(new ListView<SolrDocument>("result", response.getResults())
			{
				@Override
				protected void populateItem(ListItem<SolrDocument> item)
				{
					SolrDocument sdoc = item.getModelObject();
					
					item.add(labelFromSolrDoc("titul", sdoc, "Nedefinovaný titul", "title"));
					item.add(labelFromSolrDoc("autor", sdoc, "neznámý", "author", "author2", "authorPersonal"));
					item.add(labelFromSolrDoc("rok_vydani", sdoc, "neznámý", "publishDate"));
					item.add(labelFromSolrDoc("vydavatel", sdoc, "neznámý", "publisher"));
					item.add(labelFromSolrDoc("tiskar", sdoc, "neznámý", "masterPrinter"));
					item.add(labelFromSolrDoc("hesla", sdoc, "", "topic"));
					item.add(labelFromSolrDoc("zanry", sdoc, "", "genre"));
				}
			});
			
			// Zobrazit novy obsah
			mainCont.replaceWith(mainCont = newCont);
			ev.getTarget().add(mainCont);

			// Zaznamenat vychozi pocet zobrazenych zaznamu
			recordsDisplayed = NUM_RESULTS_PER_PART;
		}
	}
	
	private Label labelFromSolrDoc(String id, SolrDocument sdoc, String defaultValue, String... fieldValue)
	{
		Object fv = null;
		for ( String fvName : fieldValue )
		{
			fv = sdoc.getFieldValue(fvName);
			
			if ( fv != null )
				break;
		}
		
		
		String content;
		if ( fv != null )
		{
			
			if ( fv instanceof List )
			{
				StringBuilder contentSB = new StringBuilder();
				for ( Object o : (ArrayList<?>)fv )
					contentSB.append(o);
				
				content = contentSB.toString();
			}
			else
			{
				content = fv.toString();
			}
			
		}
		else
		{
			content = defaultValue;
		}
		
		return new Label(id, content);
	}
	
}
