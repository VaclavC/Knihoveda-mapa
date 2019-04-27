package com.disnel.knihoveda.mapa;

import java.util.ArrayList;
import java.util.Set;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.wicketstuff.openlayers3.api.util.Color;

import com.disnel.knihoveda.mapa.data.DataSet;
import com.disnel.knihoveda.mapa.data.FieldValues;
import com.disnel.knihoveda.mapa.events.AjaxEvent;
import com.disnel.knihoveda.mapa.events.DataSetChangedEvent;
import com.disnel.knihoveda.mapa.events.UserSelectionChangedEvent;

public class DataSetSwitcherPanel extends Panel
{
	private static final long serialVersionUID = 1L;

	public DataSetSwitcherPanel(String id)
	{
		super(id);
		
		setOutputMarkupId(true);
		
		add(new ListView<DataSet>("dataSet", KnihovedaMapaSession.get().dataSets())
		{
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<DataSet> item)
			{
				DataSet dataSet = item.getModelObject();
				
				// Prime zobrazeni
				item.add(new AttributeAppender("style", "background-color: " + dataSet.getColor().toString(), ";"));
				
				Component isActive, setActive, delete, openDetail;
				item.add(isActive = new WebMarkupContainer("isActive"));
				item.add(setActive = new WebMarkupContainer("setActive", item.getDefaultModel())
						.add(new AjaxEventBehavior("click")
						{
							/**
							 * 
							 */
							private static final long serialVersionUID = 1L;

							@Override
							protected void onEvent(AjaxRequestTarget target)
							{
								DataSet dataSet = (DataSet) getComponent().getDefaultModelObject();
								
								setCurrentDataSet(target, dataSet);
							}
						}));
				item.add(delete = new WebMarkupContainer("delete", item.getDefaultModel())
						.add(new AjaxEventBehavior("click")
						{
							/**
							 * 
							 */
							private static final long serialVersionUID = 1L;

							@Override
							protected void onEvent(AjaxRequestTarget target)
							{
								DataSet dataSet = (DataSet) getComponent().getDefaultModelObject();
								
								DataSet mainDataSet = KnihovedaMapaSession.get().dataSets().get(0); 
								
								if ( dataSet == mainDataSet )
									return;
								
								KnihovedaMapaSession.get().removeDataSet(dataSet);
								KnihovedaMapaSession.get().freeDataSetColor(dataSet.getColor());
								
								setCurrentDataSet(target, mainDataSet);
							}
						}));
				item.add(openDetail = new WebMarkupContainer("openDetail"));
				
				if ( dataSet == KnihovedaMapaSession.get().currentDataSet() )
					setActive.setVisible(false);
				else
					isActive.setVisible(false);
				
				if ( dataSet == KnihovedaMapaSession.get().dataSets().get(0) )
					delete.setVisible(false);
				
				// Rozklikavaci detail
				WebMarkupContainer detail;
				item.add(detail = new WebMarkupContainer("detail"));
				detail.add(new AttributeAppender("style",
						String.format("background-color: %s;", dataSet.getColor().toString()),
						";"));
				detail.setOutputMarkupId(true);
				
				boolean hasContent = false;
				
				RepeatingView valuesRV;
				detail.add(valuesRV = new RepeatingView("fieldValues"));
				
				// Vybrana mista
				Set<String> selectedPlaces = KnihovedaMapaSession.get().currentDataSet().getSelectedPlaces();
				if ( !selectedPlaces.isEmpty() )
				{
					WebMarkupContainer cont;
					valuesRV.add(cont = new WebMarkupContainer(valuesRV.newChildId()));
					
					cont.add(new Label("name", new ResourceModel("field.place")));

					cont.add(new ListView<String>("value", new ArrayList<String>(selectedPlaces))
					{
						/**
						 * 
						 */
						private static final long serialVersionUID = 1L;

						@Override
						protected void populateItem(ListItem<String> item)
						{
							String value = item.getModelObject();
							
							item.add(new Label("content", value));
						}
					});
					
					hasContent = true;
				}
				
				// Detaily z hornich poli
				for ( FieldValues fv : dataSet.getFieldsValues() )
				{
					WebMarkupContainer cont;
					valuesRV.add(cont = new WebMarkupContainer(valuesRV.newChildId()));
					
//					cont.add(new Label("name", VyberDlePole.getFieldNameModel(fv.getName())));

					cont.add(new ListView<String>("value", new ArrayList<String>(fv.getValues()))
					{
						/**
						 * 
						 */
						private static final long serialVersionUID = 1L;

						@Override
						protected void populateItem(ListItem<String> item)
						{
							String value = item.getModelObject();
							
							item.add(new Label("content", value));
						}
					});
					
					hasContent = true;
				}
				
				// Casove rozmezi
				Integer yearFrom = dataSet.getYearFrom(),
						yearTo = dataSet.getYearTo();
				if ( yearFrom != null || yearTo != null )
				{
					WebMarkupContainer cont;
					valuesRV.add(cont = new WebMarkupContainer(valuesRV.newChildId()));
					
					cont.add(new Label("name", new ResourceModel("field.casoveRozmezi")));

					StringBuilder sb = new StringBuilder();
					if ( yearFrom != null )
						sb.append(yearFrom);
					sb.append(" - ");
					if ( yearTo != null )
						sb.append(yearTo);
					
					WebMarkupContainer  value;
					cont.add(value = new WebMarkupContainer("value"));
					
					value.add(new Label("content", sb.toString()));
					
					hasContent = true;
				}
				
				
				Component empty;
				detail.add(empty = new WebMarkupContainer("empty"));
				
				if ( !hasContent )
					valuesRV.setVisible(false);
				else
					empty.setVisible(false);
				
				// Zobrazovani a mizeni detailu
				openDetail.add(new AttributeModifier("data-detail-id", detail.getMarkupId()));
			}
		});
		
		Component dataSetNew;
		add(dataSetNew = new WebMarkupContainer("dataSetNew"));
		
		
		if ( KnihovedaMapaSession.get().hasNewDataSetColor() )
		{
			Color dataSetNewColor = KnihovedaMapaSession.get().newDataSetColor();
			dataSetNew.add(new AttributeAppender("style", "color: " + dataSetNewColor.toString(), "; "));
			dataSetNew.setDefaultModel(Model.of(dataSetNewColor));
			
			dataSetNew.add(new AjaxEventBehavior("click")
			{
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
				protected void onEvent(AjaxRequestTarget target)
				{
					Color dataSetNewColor = (Color) getComponent().getDefaultModelObject();
					DataSet dataSet = new DataSet(dataSetNewColor);
					KnihovedaMapaSession.get().addDataSet(dataSet);
					KnihovedaMapaSession.get().useDataSetColor(dataSetNewColor);
					
					setCurrentDataSet(target, dataSet);
				}
			});
		}
		else
		{
			dataSetNew.setVisible(false);
		}
	}
	
	private void redraw(AjaxRequestTarget target)
	{
		DataSetSwitcherPanel newPanel;
		this.replaceWith(newPanel = new DataSetSwitcherPanel(getId()));
		target.add(newPanel);
	}
	
	private void setCurrentDataSet(AjaxRequestTarget target, DataSet dataSet)
	{
		KnihovedaMapaSession.get().currentDataSet(dataSet);
		send(getPage(), Broadcast.BREADTH, new DataSetChangedEvent(target, dataSet));

		redraw(target);
	}

	@Override
	public void onEvent(IEvent<?> event)
	{
		if ( event.getPayload() instanceof UserSelectionChangedEvent 
				&& !(event.getPayload() instanceof DataSetChangedEvent) )
		{
			AjaxEvent ev = (AjaxEvent) event.getPayload();
			
			redraw(ev.getTarget());
		}
	}

}
