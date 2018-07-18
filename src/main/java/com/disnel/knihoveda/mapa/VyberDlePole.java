package com.disnel.knihoveda.mapa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.disnel.knihoveda.mapa.data.DataSet;
import com.disnel.knihoveda.mapa.data.FieldValues;
import com.disnel.knihoveda.mapa.events.FieldValuesChangedEvent;
import com.disnel.knihoveda.wicket.model.SolrFieldValuesModel;
import com.googlecode.wicket.kendo.ui.form.multiselect.MultiSelect;

import de.agilecoders.wicket.core.markup.html.bootstrap.behavior.CssClassNameAppender;

public class VyberDlePole extends Panel
{

	private String fieldName;
	
	private IModel<List<Count>> fieldValuesModel;
	private IModel<Collection<Count>> selectedValues;
	
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
		
		form.add(selectInst = createSelect());
	}
	
	private MultiSelect<Count> createSelect()
	{
		fieldValuesModel = new SolrFieldValuesModel(this.fieldName + "_facet"); 

		List<Count> selected = new ArrayList<>();
		DataSet dataSet = MapaSession.get().currentDataSet();
		FieldValues fieldValues = dataSet.getFieldValues(this.fieldName);
		if ( fieldValues != null )
			for ( Count count : fieldValuesModel.getObject() )
				if ( fieldValues.getValues().contains(count.getName()) )
					selected.add(count);

		selectedValues = Model.of(selected);

		MultiSelect<Count> select;
		select = new MultiSelect<Count>("select", selectedValues, fieldValuesModel);
		select.setOutputMarkupId(true);
		
		select.add(new AjaxFormComponentUpdatingBehavior("change")
		{
			@Override
			protected void onUpdate(AjaxRequestTarget target)
			{
				List<String> values = new ArrayList<>();
				for ( Count count : selectedValues.getObject() )
					values.add(count.getName());
				
				MapaSession.get().currentDataSet().setFieldValues(fieldName, values);
				
				send(getPage(), Broadcast.BREADTH,
						new FieldValuesChangedEvent(target, fieldName));
			}
		});
		
		return select;
	}

	@Override
	public void onEvent(IEvent<?> event)
	{
		if ( event.getPayload() instanceof FieldValuesChangedEvent)
		{
			FieldValuesChangedEvent ev = (FieldValuesChangedEvent) event.getPayload();
			
			if ( !ev.getFieldName().equals(fieldName) )
			{
				AjaxRequestTarget target = ev.getTarget();

				// Tohle nefunguje, nejak pak blbne ten Kendo select
				//				fieldValuesModel.detach();
				//				selectInst.updateModel();
				//				
				//				target.add(selectInst);
				
				// Takze to holt zatim bude takhle
				VyberDlePole newPanel = new VyberDlePole(getId(), fieldName);
				replaceWith(newPanel);
				target.add(newPanel);
			}
		}
	}
	
}
