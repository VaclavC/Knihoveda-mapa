package com.disnel.knihoveda.wicket;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.disnel.knihoveda.mapa.MapaSession;
import com.disnel.knihoveda.mapa.data.DataSet;
import com.disnel.knihoveda.mapa.data.FieldValues;
import com.googlecode.wicket.kendo.ui.form.multiselect.lazy.AjaxMultiSelect;

public abstract class VyberDlePoleMultiSelect extends AjaxMultiSelect<Count>
{
	
	private String fieldName;
	private IModel<? extends List<Count>> possibleValuesModel;
	private Collection<Count> selectedValues;
	
	private boolean suppressNextRefresh = false;

	public VyberDlePoleMultiSelect(String id, String fieldName,
			IModel<? extends List<Count>> possibleValuesModel)
	{
		super(id);
		
		this.fieldName = fieldName;
		this.possibleValuesModel = possibleValuesModel;
		
		this.selectedValues = new ArrayList<>();
		lookForSelectedValues();
		setModel(Model.of(this.selectedValues));
	}

	public void suppressNextRefresh(boolean value)
	{
		suppressNextRefresh = value;
	}
	
	private void lookForSelectedValues()
	{
		List<Count> possibleValues = possibleValuesModel.getObject();
		DataSet currentDataSet = MapaSession.get().currentDataSet();
		FieldValues fieldValue = currentDataSet.getFieldValues(fieldName);

		selectedValues.clear();
		
		if ( fieldValue != null )
			for ( Count count : possibleValues )
				if ( fieldValue.getValues().contains(count.getName()))
					selectedValues.add(count);
	}
	
	@Override
	public List<Count> getChoices()
	{
		return possibleValuesModel.getObject();
	}

	@Override
	public void refresh(AjaxRequestTarget target)
	{
		if ( suppressNextRefresh )
		{
			suppressNextRefresh = false;
			return;
		}
		
		super.refresh(target);

		// Nastavit nove prednastevene hodnoty
		lookForSelectedValues();
		
		StringBuilder setValuesJS = new StringBuilder();
		setValuesJS.append(String.format("var $w = %s; if ($w) { $w.value([ ", this.widget()));
		String delim = "";
		for ( Count count : selectedValues )
		{
			setValuesJS.append(delim);
			setValuesJS.append('"');
			setValuesJS.append(count.toString());
			setValuesJS.append('"');
			
			delim = ",";
		}
		setValuesJS.append(" ]); console.log('Values set: ' + $w.value()); console.log('Options: ' + $w.options); } ");
		target.appendJavaScript(setValuesJS);
		
		System.out.println("setValuesJS: " + setValuesJS);
	}

	@Override
	public void onDetach()
	{
		super.onDetach();
		possibleValuesModel.detach();
	}
	
}
