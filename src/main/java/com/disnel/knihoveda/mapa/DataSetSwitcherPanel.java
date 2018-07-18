package com.disnel.knihoveda.mapa;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.wicketstuff.openlayers3.api.util.Color;

import com.disnel.knihoveda.mapa.data.DataSet;
import com.disnel.knihoveda.mapa.events.DataSetChangedEvent;

import de.agilecoders.wicket.core.markup.html.bootstrap.behavior.CssClassNameAppender;

public class DataSetSwitcherPanel extends Panel
{

	public DataSetSwitcherPanel(String id)
	{
		super(id);
		
		setOutputMarkupId(true);
		
		add(new ListView<DataSet>("dataSets", MapaSession.get().dataSets())
		{
			@Override
			protected void populateItem(ListItem<DataSet> item)
			{
				DataSet dataSet = item.getModelObject();
				
				Component symbol;
				item.add(symbol = new WebMarkupContainer("dataSetSymbol", Model.of(dataSet)));
				symbol.add(new AttributeAppender("style", "color: " + dataSet.getColor().toString(), ";"));
				
				symbol.add(new AjaxEventBehavior("click")
				{
					@Override
					protected void onEvent(AjaxRequestTarget target)
					{
						DataSet dataSet = (DataSet) getComponent().getDefaultModelObject();
						
						setCurrentDataSet(target, dataSet);
					}
				});
				
				if ( dataSet == MapaSession.get().currentDataSet() )
					symbol.add(new CssClassNameAppender("current"));
			}
		});
		
		Component dataSetNew;
		add(dataSetNew = new WebMarkupContainer("dataSetNew"));
		
		Color dataSetNewColor = MapaSession.get().newDataSetColor();
		if ( dataSetNewColor == null )
		{
			dataSetNew.setVisible(false);
		}
		else
		{
			dataSetNew.add(new AttributeAppender("style", "color: " + dataSetNewColor.toString(), "; "));
			dataSetNew.setDefaultModel(Model.of(dataSetNewColor));
			
			dataSetNew.add(new AjaxEventBehavior("click")
			{
				@Override
				protected void onEvent(AjaxRequestTarget target)
				{
					Color dataSetNewColor = (Color) getComponent().getDefaultModelObject();
					DataSet dataSet = new DataSet(null, dataSetNewColor);
					MapaSession.get().addDataSet(dataSet);
					
					setCurrentDataSet(target, dataSet);
				}
			});
		}
	}
	
	private void setCurrentDataSet(AjaxRequestTarget target, DataSet dataSet)
	{
		MapaSession.get().currentDataSet(dataSet);
		send(getPage(), Broadcast.BREADTH, new DataSetChangedEvent(target, dataSet));

		Component newPanel = new DataSetSwitcherPanel(getId());
		replaceWith(newPanel);
		target.add(newPanel);
	}

}
