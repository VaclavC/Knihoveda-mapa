package com.disnel.knihoveda.wicket.model;

import java.util.List;

import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.wicket.model.LoadableDetachableModel;

import com.disnel.knihoveda.dao.SolrDAO;
import com.disnel.knihoveda.mapa.MapaSession;

public class PossibleFieldValuesModel extends LoadableDetachableModel<List<Count>>
{
	
	private String fieldName;

	public PossibleFieldValuesModel(String fieldName)
	{
		this.fieldName = fieldName;
	}

	@Override
	protected List<Count> load()
	{
		return SolrDAO.getFieldValues(fieldName, MapaSession.get().currentDataSet());
	}

	public String getFieldName()
	{
		return fieldName;
	}
	
}
