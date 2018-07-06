package com.disnel.knihoveda.mapa;

import java.util.Iterator;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;

import com.disnel.knihoveda.dao.SolrDAO;

public class CasovyGraf extends Panel
{

	private Integer rokOd, rokDo;
	
	private Long maxCount = 0L;
	
	public CasovyGraf(String id, Integer rokOd, Integer rokDo)
	{
		super(id);
		
		this.rokOd = rokOd;
		this.rokDo = rokDo;
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new PackageResourceReference(getClass(), "CasovyGraf.js")));
		
		response.render(OnDomReadyHeaderItem.forScript(
				String.format("CasovyGraf.init('%s', %d, %d);",
						getMarkupId(), rokOd, rokDo)));
		
		PageParameters searchParams = ((MainPage) getPage()).getCommonSearchParams();
		response.render(OnDomReadyHeaderItem.forScript(
				String.format("CasovyGraf.addChart('main', %s);",
						getJsArrayOfResults(searchParams))));

		response.render(OnDomReadyHeaderItem.forScript(
				String.format("CasovyGraf.setMaxCount(%d);", maxCount)));
		
		response.render(OnDomReadyHeaderItem.forScript("CasovyGraf.redraw();"));
	}


	public String getJsArrayOfResults(PageParameters searchParams)
	{
		SolrQuery query = new SolrQuery();
		SolrDAO.addQueryParameters(query, searchParams);
		query.addFacetField("publishDate");
//		query.addSort("publishDate", ORDER.asc);
		query.setFacetMinCount(1);
		query.setRows(0);
		
		QueryResponse response = SolrDAO.getResponse(query);
		FacetField publishPlaceFF = response.getFacetField("publishDate");
		
		StringBuilder sb = new StringBuilder("{ ");
		Iterator<Count> it = publishPlaceFF.getValues().iterator();
		while ( it.hasNext() )
		{
			Count count = it.next();
			
			if ( count.getCount() > maxCount )
				maxCount = count.getCount();
			
			String name = count.getName();
			if ( name.isEmpty() )
				continue;
			
			sb.append(count.getName());
			sb.append(':');
			sb.append(count.getCount());
			
			if ( it.hasNext() )
				sb.append(", ");
		}
		
		sb.append(" }");
		
		return sb.toString();
	}
	
}
