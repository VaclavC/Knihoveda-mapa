package com.disnel.knihoveda.wicket.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.disnel.knihoveda.mapa.KnihovedaMapaSession;
import com.disnel.knihoveda.mapa.data.DataSet;
import com.disnel.knihoveda.mapa.data.FieldValues;

public class SelectedFieldValuesModel extends LoadableDetachableModel<List<Count>>
{
	private static final long serialVersionUID = 1L;

	private String fieldName;
	private IModel<List<Count>> possibleValuesModel;
	
	public SelectedFieldValuesModel(String fieldName, IModel<List<Count>> possibleValuesModel)
	{
		this.fieldName = fieldName;
		this.possibleValuesModel = possibleValuesModel;
	}

	@Override
	protected List<Count> load()
	{
		List<Count> ret = new ArrayList<>();
		DataSet currentDataSet = KnihovedaMapaSession.get().currentDataSet();
		FieldValues fieldValues = currentDataSet.getFieldValues(fieldName);
		
		for ( Count count : possibleValuesModel.getObject() )
			if ( fieldValues != null && fieldValues.getValues().contains(count.getName() ))
				ret.add(count);
		
		return ret;
	} 

}
