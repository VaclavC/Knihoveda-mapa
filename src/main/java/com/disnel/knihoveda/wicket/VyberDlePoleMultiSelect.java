package com.disnel.knihoveda.wicket;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.disnel.knihoveda.dao.ResourceDAO;
import com.disnel.knihoveda.mapa.MapaSession;
import com.disnel.knihoveda.mapa.data.DataSet;
import com.disnel.knihoveda.mapa.data.FacetFieldCountWrapper;
import com.disnel.knihoveda.mapa.data.FieldValues;
import com.googlecode.wicket.jquery.core.JQueryBehavior;
import com.googlecode.wicket.kendo.ui.form.multiselect.lazy.AjaxMultiSelect;
import com.googlecode.wicket.kendo.ui.renderer.ChoiceRenderer;

public abstract class VyberDlePoleMultiSelect extends AjaxMultiSelect<FacetFieldCountWrapper>
{
	
	private String fieldName;
	private IModel<? extends List<FacetFieldCountWrapper>> possibleValuesModel;
	private Collection<FacetFieldCountWrapper> selectedValues;
	
	private boolean suppressNextRefresh = false;

	public VyberDlePoleMultiSelect(String id, String fieldName,
			IModel<? extends List<FacetFieldCountWrapper>> possibleValuesModel)
	{
		super(id,
				new ChoiceRenderer<FacetFieldCountWrapper>("text", "value"));
		
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
		List<FacetFieldCountWrapper> possibleValues = possibleValuesModel.getObject();
		DataSet currentDataSet = MapaSession.get().currentDataSet();
		FieldValues fieldValue = currentDataSet.getFieldValues(fieldName);

		selectedValues.clear();
		
		if ( fieldValue != null )
			for ( FacetFieldCountWrapper count : possibleValues )
				if ( fieldValue.getValues().contains(count.getValue()))
					selectedValues.add(count);
	}
	
	@Override
	public List<FacetFieldCountWrapper> getChoices()
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
		for ( FacetFieldCountWrapper count : selectedValues )
		{
			setValuesJS.append(delim);
			setValuesJS.append('"');
			setValuesJS.append(count.getValue());
			setValuesJS.append('"');
			
			delim = ",";
		}
		setValuesJS.append(" ]); } ");
		target.appendJavaScript(setValuesJS);
	}

	@Override
	public void onDetach()
	{
		super.onDetach();
		possibleValuesModel.detach();
	}
	
	@Override
	public void onConfigure(JQueryBehavior behavior)
	{
		super.onConfigure(behavior);

		behavior.setOption("filter", "'contains'");
		behavior.setOption("placeholder", ResourceDAO.jsResourceString("field.empty", "..."));
	}
	
}
