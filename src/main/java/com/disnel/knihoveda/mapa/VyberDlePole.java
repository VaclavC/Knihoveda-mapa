package com.disnel.knihoveda.mapa;

import java.util.ArrayList;
import java.util.List;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import com.disnel.knihoveda.mapa.events.AjaxEvent;
import com.disnel.knihoveda.mapa.events.DataSetChangedEvent;
import com.disnel.knihoveda.mapa.events.FieldValuesChangedEvent;
import com.disnel.knihoveda.wicket.VyberDlePoleMultiSelect;
import com.disnel.knihoveda.wicket.model.PossibleFieldValuesModel;
import com.googlecode.wicket.kendo.ui.form.multiselect.lazy.MultiSelect;

import de.agilecoders.wicket.core.markup.html.bootstrap.behavior.CssClassNameAppender;

public class VyberDlePole extends Panel
{

	private String fieldName;
	
	private IModel<List<Count>> possibleValuesModel;
	
	private MultiSelect<Count> selectInst;
	
	public VyberDlePole(String id, String fieldName)
	{
		super(id);
		
		this.fieldName = fieldName;
		
		setOutputMarkupId(true);
		
		add(new CssClassNameAppender(this.fieldName));
		
		add(new Label("titul", this.fieldName));
		
		Form<Void> form;
		add(form = new Form<Void>("form"));

		possibleValuesModel = new PossibleFieldValuesModel(fieldName);
		
//		List<Count> selected = new ArrayList<>();
//		DataSet dataSet = MapaSession.get().currentDataSet();
//		FieldValues fieldValues = dataSet.getFieldValues(this.fieldName);
//		if ( fieldValues != null )
//			for ( Count count : fieldValuesModel.getObject() )
//				if ( fieldValues.getValues().contains(count.getName()) )
//					selected.add(count);
		
		form.add(selectInst = new VyberDlePoleMultiSelect("select", fieldName, possibleValuesModel)
		{
			@Override
			public void onSelectionChanged(AjaxRequestTarget target)
			{
				// Potlacit refresh sama sebe (suppressRefresh)
				// DataSet by si mel sam upravit mozne vybery v ostatnich polich
				// To by si pak selecty mely vzit samy
				// A na ziskani List<Count> uz by to ten Vyber mel vratit sam, ne to resit tady
				
				@SuppressWarnings("unchecked")
				List<Count> selected = (List<Count>) getDefaultModelObject();
				
				List<String> values = new ArrayList<>();
				for ( Count count : selected )
					values.add(count.getName());
				
				MapaSession.get().currentDataSet().setFieldValues(fieldName, values);
				
				send(getPage(), Broadcast.BREADTH,
						new FieldValuesChangedEvent(target, fieldName));
			}
		});
	}
	
	@Override
	public void onEvent(IEvent<?> event)
	{
		if ( event.getPayload() instanceof FieldValuesChangedEvent 
				|| event.getPayload() instanceof DataSetChangedEvent )
		{
			AjaxEvent ev = (AjaxEvent) event.getPayload();
			
			selectInst.refresh(ev.getTarget());
		}
	}
	
}
