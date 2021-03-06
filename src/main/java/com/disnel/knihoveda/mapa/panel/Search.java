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
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;

import com.disnel.knihoveda.dao.SolrDAO;
import com.disnel.knihoveda.dao.SolrDAO.FieldCounts;
import com.disnel.knihoveda.dao.VuFindDAO;
import com.disnel.knihoveda.mapa.KnihovedaMapaConfig;
import com.disnel.knihoveda.mapa.KnihovedaMapaSession;
import com.disnel.knihoveda.mapa.data.DataSet;
import com.disnel.knihoveda.mapa.data.DataSet.FieldValues;
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
				
				item.add(new AttributeModifier("title",
						new StringResourceModel("search.dataSet.tooltip")
						.setParameters(item.getIndex() + 1)));
				
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
		
		/* Title */
		WebMarkupContainer title;
		add(title = new WebMarkupContainer("title"));
		title.add(new AttributeModifier("style",
				"background-color: " + KnihovedaMapaSession.get().currentDataSet().getColor().toString() + ";"));
		
		title.add(new Label("titleMessage",
				new StringResourceModel("search.title.message")
					.setParameters(new LoadableDetachableModel<Long>()
					{
						private static final long serialVersionUID = 1L;

						@Override
						protected Long load()
						{
							return SolrDAO.getCountForDataSet(KnihovedaMapaSession.get().currentDataSet());
						}}))
				.setEscapeModelStrings(false));
		
		/* Link to VuFind */
		title.add(new ExternalLink("linkToVuFind", new IModel<String>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public String getObject()
			{
				return VuFindDAO.linkToVuFindForDataset(KnihovedaMapaSession.get().currentDataSet());
			}
		}));
		
		/* Clear selection */
		title.add(new AjaxGeneralButton("clearSelection", "click")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onClick(AjaxRequestTarget target)
			{
				KnihovedaMapaSession.get().currentDataSet().clear();
				
				send(getPage(), Broadcast.BREADTH,
						new UserSelectionChangedEvent(target));
			}
		});
		
		/* Content */
		WebMarkupContainer content;
		add(content = new WebMarkupContainer("content"));
		
		/* Search fields */
		content.add(new ListView<String>("fields", Arrays.asList(KnihovedaMapaConfig.FIELDS))
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
			add(new Label("resultsNum", pvModel.getNumOfValues()));
			
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
								.removeFieldValue(fieldName, item.getModelObject());
							
							send(getPage(), Broadcast.BREADTH,
									new FieldValuesChangedEvent(target, fieldName));
						}
					});
				}
			});
					
			/* Select a new value */
			IModel<Count> selectedValue = new Model<>();
			
			FormComponent<?> select;
			add(select = new BootstrapSelect<Count>("fieldSelect",
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
						.addFieldValue(fieldName, selectedValue.getObject().getName());
					
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
		
		protected class PossibleValuesModel extends LoadableDetachableModel<List<Count>>
		{
			private static final long serialVersionUID = 1L;

			private Long totalCount;
			private Map<String, Count> byName;
			
			@Override
			protected List<Count> load()
			{
				FieldCounts result = SolrDAO.getFieldCounts(fieldName, KnihovedaMapaSession.get().currentDataSet()); 
				
				totalCount = result.totalCount;
				
				byName = result.counts.stream().collect(
						Collectors.toMap(Count::getName, ffc -> ffc));
				
				return result.counts;
			}
			
			@Override
			public void onDetach()
			{
				byName = null;
				
				super.onDetach();
			}
			
			public Integer getNumOfValues()
			{
				getObject();
				
				return byName.size();
			}
			
			@SuppressWarnings("unused")
			public Long getTotalCount()
			{
				getObject();
				
				return totalCount;
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
		
		private TimeInput inputFrom, inputTo;
		
		public TimeRange(String id)
		{
			super(id);
			
			setOutputMarkupId(true);
			
			DataSet currentDataSet = KnihovedaMapaSession.get().currentDataSet();
			yearFromModel.setObject(currentDataSet.getYearFrom());
			yearToModel.setObject(currentDataSet.getYearTo());
			
			add(inputFrom = new TimeInput("yearFrom", yearFromModel)
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected void onUpdate(AjaxRequestTarget target)
				{
					target.appendJavaScript(String.format("$('#%s').select();", inputTo.getMarkupId()));
				}
			});
			inputFrom.setOutputMarkupId(true);
			
			add(inputTo = new TimeInput("yearTo", yearToModel)
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected void onUpdate(AjaxRequestTarget target)
				{
					target.appendJavaScript(String.format("$('#%s').select();", inputFrom.getMarkupId()));
				}
			});
			inputTo.setOutputMarkupId(true);
			
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
			if ( event.getPayload() instanceof UserSelectionChangedEvent )
			{
				UserSelectionChangedEvent ev =  (UserSelectionChangedEvent) event.getPayload();
			
				DataSet currentDS = KnihovedaMapaSession.get().currentDataSet();
				
				yearFromModel.setObject(currentDS.getYearFrom());
				yearToModel.setObject(currentDS.getYearTo());
				
				ev.getTarget().add(this);
			}
		}

		
		/* Time input */
		
		private abstract class TimeInput extends NumberTextField<Integer>
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
						
						TimeInput.this.onUpdate(target);
					}
				});
			}
			
			protected abstract void onUpdate(AjaxRequestTarget target);
		}
	}

}
