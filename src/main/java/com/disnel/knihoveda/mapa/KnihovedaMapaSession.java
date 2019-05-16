package com.disnel.knihoveda.mapa;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.Request;
import org.wicketstuff.openlayers3.api.util.Color;

import com.disnel.knihoveda.dao.SolrDAO;
import com.disnel.knihoveda.mapa.data.DataSet;

/**
 * Session pro mapu
 * 
 * Drzi ruzne uzitecne informace konktretne
 * 
 * - datove sady
 * - stav barevne palety pro datove sady
 * - maximalni pocet zaznamu pro jedno misto
 * 
 * @author Vaclav Cermak <disnel@disnel.com>
 *
 */
public class KnihovedaMapaSession extends WebSession
{
	private static final long serialVersionUID = 1L;

	/* Konstruktor */
	
	public KnihovedaMapaSession(Request request)
	{
		super(request);
		
		// Pripravit datove sady
		initDataSets();
		
		// Pripravit maximalni pocet vysledku v jednom miste, casovy interval
		initTimeRange();
		
		// Nastavit locale
		setLocale(new Locale("cs"));
	}


	/* Utilities */

	public static KnihovedaMapaSession get()
	{
		return (KnihovedaMapaSession) WebSession.get();
	}
	
	
	/* Various state variables */
	public Integer sidePanelIndex;
	
	
	/* Datove sady a souvisejici metody */

	private List<DataSet> dataSets;
	private DataSet currentDataSet;

	private void initDataSets()
	{
		dataSets = new ArrayList<>();
		
		for ( Color color : KnihovedaMapaConfig.DATA_SET_COLORS )
			dataSets.add(new DataSet(color));

		currentDataSet = dataSets.get(0);
	}

	public List<DataSet> dataSets()
	{
		return dataSets;
	}
	
	public DataSet currentDataSet()
	{
		return currentDataSet;
	}

	public void currentDataSet(DataSet currentDataSet)
	{
		this.currentDataSet = currentDataSet;
	}
	
	
	/* Minimalni a maximalni rok */
	private int minYear, maxYear;
	
	private void initTimeRange()
	{
		int[] timeRange = SolrDAO.findTimeRange();
		
		minYear = timeRange[0];
		maxYear = timeRange[1];
	}
	
	public int minYear()
	{
		return minYear;
	}

	public int maxYear()
	{
		return maxYear;
	}

}
