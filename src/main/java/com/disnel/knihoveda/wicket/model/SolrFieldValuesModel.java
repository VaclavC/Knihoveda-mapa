package com.disnel.knihoveda.wicket.model;

import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.wicket.Session;
import org.apache.wicket.model.LoadableDetachableModel;

import com.disnel.knihoveda.dao.SolrDAO;
import com.disnel.knihoveda.mapa.MapaSession;

public class SolrFieldValuesModel extends LoadableDetachableModel<List<Count>>
{
	
	private String fieldName;

	public SolrFieldValuesModel(String fieldName)
	{
		this.fieldName = fieldName;
	}

	@Override
	protected List<Count> load()
	{
		SolrQuery query = new SolrQuery();
		SolrDAO.addDataSetQueryParameters(query, MapaSession.get().currentDataSet());
		query.addFacetField(fieldName);
		query.setFacetLimit(-1);
		query.setFacetMinCount(1);
		query.setRows(0);
		
		QueryResponse response = SolrDAO.getResponse(query);
		
		FacetField ff = response.getFacetField(fieldName);
		
		return ff.getValues();
	}
	
}
