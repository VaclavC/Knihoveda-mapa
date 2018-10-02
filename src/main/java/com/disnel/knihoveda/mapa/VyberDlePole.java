package com.disnel.knihoveda.mapa;

import java.util.ArrayList;
import java.util.List;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.wicketstuff.openlayers3.api.util.Color;

import com.disnel.knihoveda.mapa.data.FacetFieldCountWrapper;
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
	
	private IModel<List<FacetFieldCountWrapper>> possibleValuesModel;
	
	private MultiSelect<FacetFieldCountWrapper> selectInst;
	
	public VyberDlePole(String id, String fieldName)
	{
		super(id);
		
		this.fieldName = fieldName;
		
		setOutputMarkupId(true);
		
		add(new CssClassNameAppender(this.fieldName));
		
		add(new Label("titul", getFieldNameModel(this.fieldName)));
		
		Form<Void> form;
		add(form = new Form<Void>("form"));

		possibleValuesModel = new PossibleFieldValuesModel(fieldName);
		
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
				List<FacetFieldCountWrapper> selected = (List<FacetFieldCountWrapper>) getDefaultModelObject();
				
				List<String> values = new ArrayList<>();
				for ( FacetFieldCountWrapper count : selected )
					values.add(count.getValue());
				
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
			
			if ( ev instanceof DataSetChangedEvent && !ev.isProcessedBy(getClass()) )
			{
				DataSetChangedEvent myEv = (DataSetChangedEvent) ev;
				Color color = myEv.getDataSet().getColor();
	
				ev.getTarget().appendJavaScript(getJSSetSelectColor(color));
				ev.setProcessedBy(getClass());
			}
		}
	}
	
	public static String getJSSetSelectColor(Color color)
	{
		return String.format(
				"$('.vyberDlePole .k-multiselect-wrap').css('background-color', '%s');",
				String.format("#%02x%02x%02x",
						color.red, color.green, color.blue));
	}
	
	public static ResourceModel getFieldNameModel(String fieldName)
	{
		return new ResourceModel("field." + fieldName);
	}
	
}
