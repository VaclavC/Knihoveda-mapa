package com.disnel.knihoveda.mapa;

import java.util.ArrayList;

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
import org.apache.wicket.model.Model;
import org.wicketstuff.openlayers3.api.util.Color;

import com.disnel.knihoveda.mapa.data.DataSet;
import com.disnel.knihoveda.mapa.data.FieldValues;
import com.disnel.knihoveda.mapa.events.AjaxEvent;
import com.disnel.knihoveda.mapa.events.DataSetChangedEvent;
import com.disnel.knihoveda.mapa.events.FieldValuesChangedEvent;

public class DataSetSwitcherPanel extends Panel
{

	public DataSetSwitcherPanel(String id)
	{
		super(id);
		
		setOutputMarkupId(true);
		
		add(new ListView<DataSet>("dataSet", MapaSession.get().dataSets())
		{
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
							@Override
							protected void onEvent(AjaxRequestTarget target)
							{
								DataSet dataSet = (DataSet) getComponent().getDefaultModelObject();
								
								DataSet mainDataSet = MapaSession.get().dataSets().get(0); 
								
								if ( dataSet == mainDataSet )
									return;
								
								MapaSession.get().removeDataSet(dataSet);
								MapaSession.get().freeDataSetColor(dataSet.getColor());
								
								setCurrentDataSet(target, mainDataSet);
							}
						}));
				item.add(openDetail = new WebMarkupContainer("openDetail"));
				
				if ( dataSet == MapaSession.get().currentDataSet() )
					setActive.setVisible(false);
				else
					isActive.setVisible(false);
				
				if ( dataSet == MapaSession.get().dataSets().get(0) )
					delete.setVisible(false);
				
				// Rozklikavaci detail
				WebMarkupContainer detail;
				item.add(detail = new WebMarkupContainer("detail"));
				detail.add(new AttributeAppender("style",
						String.format("background-color: %s;", dataSet.getColor().toString()),
						";"));
				detail.setOutputMarkupId(true);
				
				Component fieldValuesList;
				detail.add(fieldValuesList = new ListView<FieldValues>("fieldValues", new ArrayList<FieldValues>(dataSet.getFieldsValues()))
				{
					@Override
					protected void populateItem(ListItem<FieldValues> item)
					{
						FieldValues fieldValues = item.getModelObject();
						
						item.add(new Label("name", VyberDlePole.getFieldNameModel(fieldValues.getName())));
						
						item.add(new ListView<String>("value", new ArrayList<String>(fieldValues.getValues()))
						{
							@Override
							protected void populateItem(ListItem<String> item)
							{
								String value = item.getModelObject();
								
								item.add(new Label("content", value));
							}
						});
					}
				});
				
				Component empty;
				detail.add(empty = new WebMarkupContainer("empty"));
				
				if ( dataSet.getFieldsValues().isEmpty() )
					fieldValuesList.setVisible(false);
				else
					empty.setVisible(false);
				
				// Zobrazovani a mizeni detailu
				openDetail.add(new AttributeModifier("data-detail-id", detail.getMarkupId()));
			}
		});
		
		Component dataSetNew;
		add(dataSetNew = new WebMarkupContainer("dataSetNew"));
		
		
		if ( MapaSession.get().hasNewDataSetColor() )
		{
			Color dataSetNewColor = MapaSession.get().newDataSetColor();
			dataSetNew.add(new AttributeAppender("style", "color: " + dataSetNewColor.toString(), "; "));
			dataSetNew.setDefaultModel(Model.of(dataSetNewColor));
			
			dataSetNew.add(new AjaxEventBehavior("click")
			{
				@Override
				protected void onEvent(AjaxRequestTarget target)
				{
					Color dataSetNewColor = (Color) getComponent().getDefaultModelObject();
					DataSet dataSet = new DataSet(dataSetNewColor);
					MapaSession.get().addDataSet(dataSet);
					MapaSession.get().useDataSetColor(dataSetNewColor);
					
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
		MapaSession.get().currentDataSet(dataSet);
		send(getPage(), Broadcast.BREADTH, new DataSetChangedEvent(target, dataSet));

		redraw(target);
	}

	@Override
	public void onEvent(IEvent<?> event)
	{
		if ( event.getPayload() instanceof FieldValuesChangedEvent )
		{
			AjaxEvent ev = (AjaxEvent) event.getPayload();
			
			redraw(ev.getTarget());
		}
	}

}
