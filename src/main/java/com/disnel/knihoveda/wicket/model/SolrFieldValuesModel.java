package com.disnel.knihoveda.wicket.model;

import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.wicket.model.LoadableDetachableModel;

import com.disnel.knihoveda.dao.SolrDAO;

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
		SolrDAO.addQueryEmptyParameters(query);
		query.addFacetField(fieldName);
		query.setRows(0);
		
		QueryResponse response = SolrDAO.getResponse(query);
		
		FacetField ff = response.getFacetField(fieldName);
		
		return ff.getValues();
	}
	
}
