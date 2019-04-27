package com.disnel.knihoveda.mapa.panel;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.NumberTextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;

import com.disnel.knihoveda.dao.SolrDAO;
import com.disnel.knihoveda.mapa.KnihovedaMapaConfig;
import com.disnel.knihoveda.mapa.KnihovedaMapaSession;
import com.disnel.knihoveda.mapa.data.DataSet;
import com.disnel.knihoveda.mapa.data.FieldValues;
import com.disnel.knihoveda.mapa.events.AjaxEvent;
import com.disnel.knihoveda.mapa.events.DataSetChangedEvent;
import com.disnel.knihoveda.mapa.events.FieldValuesChangedEvent;
import com.disnel.knihoveda.mapa.events.TimeSelectEvent;
import com.disnel.knihoveda.mapa.events.UserSelectionChangedEvent;
import com.disnel.knihoveda.wicket.AjaxGeneralButton;

import de.agilecoders.wicket.core.markup.html.bootstrap.behavior.CssClassNameAppender;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.select.BootstrapSelect;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.select.BootstrapSelectConfig;

public class Search extends Panel
{
	private static final long serialVersionUID = 1L;

	
	/* Constructor */
	
	public Search(String id)
	{
		super(id);
		
		setOutputMarkupId(true);
		
		/* Data sets */
		add(new ListView<DataSet>("dataSet", KnihovedaMapaSession.get().dataSets())
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<DataSet> item)
			{
				DataSet dataSet = item.getModelObject();
				
				if ( dataSet.equals(KnihovedaMapaSession.get().currentDataSet()) )
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
						DataSet dataSet = item.getModelObject();
						
						KnihovedaMapaSession.get().currentDataSet(dataSet);
						
						send(getPage(), Broadcast.BREADTH,
								new DataSetChangedEvent(target, dataSet));
					}
				});
			}
		});
		
		/* Content */
		WebMarkupContainer content;
		add(content = new WebMarkupContainer("content"));
		content.add(new AttributeModifier("style",
				"border-color: " + KnihovedaMapaSession.get().currentDataSet().getColor().toString() + ";"));
		
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
		
		/* Time range */
		content.add(new TimeRange("timeRange"));
	}
	
	
	/* React to events */
	
	@Override
	public void onEvent(IEvent<?> event)
	{
		if ( event.getPayload() instanceof UserSelectionChangedEvent )
		{
			AjaxEvent ev = (AjaxEvent) event.getPayload();
		
			if ( ev instanceof DataSetChangedEvent )
			{
				ev.getTarget().add(replaceWith(new Search(getId())));
			}
			else
			{
				ev.getTarget().add(this);
			}
		}
	}
	
	
	/**
	 * Class for field values selector
	 * 
	 * @author Vaclav Cermak <disnel@disnel.com>
	 *
	 */
	private class Field extends Panel
	{
		private static final long serialVersionUID = 1L;

		private String fieldName;
		
		public Field(String id, String fieldName)
		{
			super(id);
			this.fieldName = fieldName;

			/* Title and possible number of results */
			add(new Label("title", new ResourceModel("field." + fieldName)));
			add(new Label("resultsNum", "TODO: num")); // TODO: Number of possible results
			
			/* Already selected values */
			add(new ListView<String>("valueRow", svModel)
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected void populateItem(ListItem<String> item)
				{
					Count count = pvModel.getCountByName(item.getModelObject());
					
					if ( count != null )
					{
						item.add(new Label("value", count.getName()));
						item.add(new Label("count", count.getCount()));
					}
					else
					{
						item.add(new Label("value", item.getModel())
								.add(new CssClassNameAppender("text-muted")));
						item.add(new Label("count", "").setVisible(false));
					}
					
					item.add(new AjaxGeneralButton("trashButton", "click")
					{
						private static final long serialVersionUID = 1L;

						@Override
						protected void onClick(AjaxRequestTarget target)
						{
							KnihovedaMapaSession.get().currentDataSet()
								.getFieldValuesNotNull(fieldName)
								.removeValue(item.getModelObject());
							
							send(getPage(), Broadcast.BREADTH,
									new FieldValuesChangedEvent(target, fieldName));
						}
					});
				}
			});
					
			/* Select a new value */
			IModel<Count> selectedValue = new Model<>();
			
			FormComponent<?> select;
			add(select = new BootstrapSelect<Count>("select",
					selectedValue, pvModel.map( lc ->
					{
						return lc.stream()
								.filter( c -> { return ! svModel.isSelected(c.getName()); } )
								.collect(Collectors.toList());
					}))
					.with(new BootstrapSelectConfig()
							.withLiveSearch(true)));
			
			select.add(new OnChangeAjaxBehavior()
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected void onUpdate(AjaxRequestTarget target)
				{
					KnihovedaMapaSession.get().currentDataSet()
						.getFieldValuesNotNull(fieldName)
						.addValue(selectedValue.getObject().getName());
					
					send(getPage(), Broadcast.BREADTH,
							new FieldValuesChangedEvent(target, fieldName));
				}
			});
		}
		
		
		/* Model for field selected values */

		private SelectedValuesModel svModel = new SelectedValuesModel();
		
		private class SelectedValuesModel extends LoadableDetachableModel<List<String>>
		{
			private static final long serialVersionUID = 1L;

			private Set<String> valuesSet;
			
			@Override
			protected List<String> load()
			{
				DataSet currentDataSet = KnihovedaMapaSession.get().currentDataSet();
				FieldValues fieldValues = currentDataSet.getFieldValues(fieldName);
				
				if ( fieldValues != null )
				{
					valuesSet = fieldValues.getValues();
					
					return valuesSet.stream()
							.collect(Collectors.toList());
				}
				
				return Collections.emptyList();
			}
			
			
			public boolean isSelected(String value)
			{
				getObject();
				
				if ( valuesSet == null )
					return false;
				
				return valuesSet.contains(value);
			}
		}
		
		
		/* Model for field possible values */
		
		private PossibleValuesModel pvModel = new PossibleValuesModel();
		
		private class PossibleValuesModel extends LoadableDetachableModel<List<Count>>
		{
			private static final long serialVersionUID = 1L;

			private Map<String, Count> byName;
			
			@Override
			protected List<Count> load()
			{
				List<Count> ret =
						SolrDAO.getFieldCounts(fieldName, KnihovedaMapaSession.get().currentDataSet());
				
				byName = ret.stream().collect(
						Collectors.toMap(Count::getName, ffc -> ffc));
				
				return ret;
			}
			
			@Override
			public void onDetach()
			{
				byName = null;
				
				super.onDetach();
			}
			
			public Count getCountByName(String name)
			{
				getObject();
				
				return byName.get(name);
			}
		}
	}
	
	
	/**
	 * Time range selector
	 * 
	 * @author Vaclav Cermak <disnel@disnel.com>
	 *
	 */
	private class TimeRange extends Panel
	{
		private static final long serialVersionUID = 1L;
		
		private Integer minYear = KnihovedaMapaSession.get().minYear();
		private Integer maxYear = KnihovedaMapaSession.get().maxYear();

		private IModel<Integer> yearFromModel = new Model<>(), yearToModel = new Model<>();
		
		public TimeRange(String id)
		{
			super(id);
			
			setOutputMarkupId(true);
			
			DataSet currentDataSet = KnihovedaMapaSession.get().currentDataSet();
			yearFromModel.setObject(currentDataSet.getYearFrom());
			yearToModel.setObject(currentDataSet.getYearTo());
			
			add(new TimeInput("yearFrom", yearFromModel));
			add(new TimeInput("yearTo", yearToModel));
			
			add(new AjaxGeneralButton("clear", "click")
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected void onClick(AjaxRequestTarget target)
				{
					updateCurrentDataSet(target, null, null);
				}
			});
		}

		private void updateCurrentDataSet(AjaxRequestTarget target, Integer yearFrom, Integer yearTo)
		{
			yearFromModel.setObject(yearFrom);
			yearToModel.setObject(yearTo);
			
			DataSet currentDataSet = KnihovedaMapaSession.get().currentDataSet();
			currentDataSet.setYearFrom(yearFrom);
			currentDataSet.setYearTo(yearTo);
			
			send(getPage(), Broadcast.BREADTH,
					new TimeSelectEvent(target, yearFrom, yearTo));
		}

		@Override
		public void onEvent(IEvent<?> event)
		{
			if ( event.getPayload() instanceof TimeSelectEvent )
			{
				TimeSelectEvent ev = (TimeSelectEvent) event.getPayload();
			
				yearFromModel.setObject(ev.yearFrom());
				yearToModel.setObject(ev.yearTo());
				
				ev.getTarget().add(this);
			}
		}

		
		/* Time input */
		
		private class TimeInput extends NumberTextField<Integer>
		{
			private static final long serialVersionUID = 1L;

			public TimeInput(String id, IModel<Integer> model)
			{
				super(id, model, Integer.class);
				
				add(new AjaxFormComponentUpdatingBehavior("change")
				{
					private static final long serialVersionUID = 1L;

					@Override
					protected void onUpdate(AjaxRequestTarget target)
					{
						Integer yearFrom = yearFromModel.getObject();
						Integer yearTo = yearToModel.getObject();
						
						if ( yearFrom == null ) yearFrom = minYear;
						if ( yearTo == null ) yearTo = maxYear;
						
						yearFrom = Math.max(yearFrom, minYear);
						yearTo = Math.min(yearTo, maxYear);
						
						if ( yearTo < yearFrom )
						{
							Integer tmp = yearFrom;
							yearFrom = yearTo;
							yearTo = tmp;
						}
						
						updateCurrentDataSet(target, yearFrom, yearTo);
					}
				});
			}
			
		}
	}

}
