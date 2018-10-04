package com.disnel.knihoveda.mapa.events;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.disnel.knihoveda.mapa.data.DataSet;

public class DataSetChangedEvent extends UserSelectionChangedEvent
{

	private DataSet dataSet;
	
	public DataSetChangedEvent(AjaxRequestTarget target, DataSet dataSet)
	{
		super(target);
		this.dataSet = dataSet;
	}

	public DataSet getDataSet()
	{
		return dataSet;
	}

}
