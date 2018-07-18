package com.disnel.knihoveda.mapa;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.response.Group;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.Request;

import com.disnel.knihoveda.dao.SolrDAO;
import com.disnel.knihoveda.mapa.data.DataSet;

public class MapaSession extends WebSession
{

	private List<DataSet> dataSets;
	
	private DataSet currentDataSet;
	
	private long maxCountInPlace;
	
	public MapaSession(Request request)
	{
		super(request);
		
		dataSets = new ArrayList<>();
		
		// Pridat vychozi dataset bez nazvu
		dataSets.add(currentDataSet = new DataSet());
		
		// Najit maximalni pocet vysledku v jednom miste
		//  (pro vychozi dataset, ktery je bez parametru, tedy maximalni mozny)
		List<Group> solrGroups = SolrDAO.getMapOverlays(currentDataSet);

		maxCountInPlace = 0;
		for ( Group group : solrGroups )
		{
			long count = group.getResult().getNumFound();
			if ( maxCountInPlace < count )
				maxCountInPlace = count;
		}
	}

	public List<DataSet> dataSets()
	{
		return dataSets;
	}
	
	public void addDataSet(DataSet dataSet)
	{
		dataSets.add(dataSet);
	}
	
	public void removeDataSet(DataSet dataSet)
	{
		dataSets.remove(dataSet);
	}
	
	public DataSet currentDataSet()
	{
		return currentDataSet;
	}

	public void currentDataSet(DataSet currentDataSet)
	{
		this.currentDataSet = currentDataSet;
	}
	
	public long maxCountInPlace()
	{
		return maxCountInPlace;
	}

	public static MapaSession get()
	{
		return (MapaSession) WebSession.get();
	}

}
