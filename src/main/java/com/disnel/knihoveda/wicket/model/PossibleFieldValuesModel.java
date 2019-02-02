package com.disnel.knihoveda.wicket.model;

import java.util.List;

import org.apache.wicket.model.LoadableDetachableModel;

import com.disnel.knihoveda.dao.SolrDAO;
import com.disnel.knihoveda.mapa.MapaSession;
import com.disnel.knihoveda.mapa.data.FacetFieldCountWrapper;

public class PossibleFieldValuesModel extends LoadableDetachableModel<List<FacetFieldCountWrapper>>
{
	private static final long serialVersionUID = 1L;

	private String fieldName;

	public PossibleFieldValuesModel(String fieldName)
	{
		this.fieldName = fieldName;
	}

	@Override
	protected List<FacetFieldCountWrapper> load()
	{
		return SolrDAO.getFieldValues(fieldName, MapaSession.get().currentDataSet());
	}

	public String getFieldName()
	{
		return fieldName;
	}
	
}
