package com.disnel.knihoveda.mapa.panel;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;

import com.disnel.knihoveda.mapa.KnihovedaMapaConfig;
import com.disnel.knihoveda.mapa.MapaSession;
import com.disnel.knihoveda.mapa.data.DataSet;
import com.disnel.knihoveda.mapa.data.FacetFieldCountWrapper;
import com.disnel.knihoveda.mapa.data.FieldValues;
import com.disnel.knihoveda.mapa.events.FieldValuesChangedEvent;
import com.disnel.knihoveda.wicket.AjaxGeneralButton;
import com.disnel.knihoveda.wicket.model.PossibleFieldValuesModel;

import de.agilecoders.wicket.core.markup.html.bootstrap.behavior.CssClassNameAppender;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.select.BootstrapSelect;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.select.BootstrapSelectConfig;

public class Search extends Panel
{
	private static final long serialVersionUID = 1L;

	public Search(String id)
	{
		super(id);
		
		setOutputMarkupId(true);
		
		/* Data sets */
		add(new ListView<DataSet>("dataSet", MapaSession.get().dataSets())
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<DataSet> item)
			{
				DataSet dataSet = item.getModelObject();
				
				if ( dataSet.equals(MapaSession.get().currentDataSet()) )
					item.add(new CssClassNameAppender("active"));
				
				item.add(new AttributeAppender("style",
						String.format("background-color: %s;", dataSet.getColor().toString()),
						";"));
				
				item.add(new Label("title", Integer.toString(item.getIndex() + 1)));
				
				item.add(new AjaxEventBehavior("click")
				{
					private static final long serialVersionUID = 1L;

					@Override
					protected void onEvent(AjaxRequestTarget target)
					{
						// TODO Auto-generated method stub
						
					}
				});
			}
		});
		
		/* Content */
		WebMarkupContainer content;
		add(content = new WebMarkupContainer("content"));
		content.add(new AttributeModifier("style",
				"border-color: " + MapaSession.get().currentDataSet().getColor().toString() + ";"));
		
		/* Search fields */
		content.add(new ListView<String>("fields", Arrays.asList(KnihovedaMapaConfig.FIELDS)) // TODO: Model
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<String> item)
			{
				item.add(new Field("field", item.getModelObject()));
			}
		});
	}
	
	private class Field extends Panel
	{
		private static final long serialVersionUID = 1L;

		private IModel<FacetFieldCountWrapper> selectedValue = new Model<>();
		
		public Field(String id, String fieldName)
		{
			super(id);

			/* Title and possible number of results */
			add(new Label("title", new ResourceModel("field." + fieldName)));
			
			add(new Label("resultsNum", "TODO: num"));
			
			/* Already selected values */
			add(new ListView<String>("valueRow", new LoadableDetachableModel<List<String>>()
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected List<String> load()
				{
					DataSet currentDataSet = MapaSession.get().currentDataSet();
					FieldValues fieldValues = currentDataSet.getFieldValues(fieldName);
					
					if ( fieldValues != null )
						return fieldValues.getValues()
								.stream()
								.collect(Collectors.toList());
					
					return Collections.emptyList();
				}
			})
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected void populateItem(ListItem<String> item)
				{
					item.add(new Label("value", item.getModel()));
					
					item.add(new AjaxGeneralButton("trashButton", "dblclick")
					{
						private static final long serialVersionUID = 1L;

						@Override
						protected void onClick(AjaxRequestTarget target)
						{
							MapaSession.get().currentDataSet()
								.getFieldValuesNotNull(fieldName)
								.removeValue(item.getModelObject());
							
							send(getPage(), Broadcast.BREADTH,
									new FieldValuesChangedEvent(target, fieldName));
							
							target.add(Search.this); // TODO: Place this into onEvent
						}
					});
				}
			});
					
			/* Select a new value */
			FormComponent<FacetFieldCountWrapper> select;
			add(select = new BootstrapSelect<FacetFieldCountWrapper>("select", selectedValue,
					new PossibleFieldValuesModel(fieldName))
					.with(new BootstrapSelectConfig()
							.withLiveSearch(true)));
			
			select.add(new OnChangeAjaxBehavior()
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected void onUpdate(AjaxRequestTarget target)
				{
					MapaSession.get().currentDataSet()
						.getFieldValuesNotNull(fieldName)
						.addValue(selectedValue.getObject().getText());
					
					send(getPage(), Broadcast.BREADTH,
							new FieldValuesChangedEvent(target, fieldName));
					
					target.add(Search.this); // TODO: Place this into onEvent
				}
			});
		}
	}

}
