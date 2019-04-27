package com.disnel.knihoveda.mapa;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
		
		// Inicializovat praci s barvami datovych sad
		initDataSetsColors();
		
		// Pripravit datove sady
		initDataSets();
		
		// Pripravit maximalni pocet vysledku v jednom miste
		initMaxCountInPlace();
	}

	
	/* Various state variables */
	public Integer sidePanelIndex;
	
	
	/* Barvy pro datove sady a souvisejici metody */
	
	private Set<Color> usedColors;
	
	private void initDataSetsColors()
	{
		usedColors = new HashSet<>();
	}
	
	/**
	 * Otestuje, zda nova barva je k dispozici
	 * @return
	 */
	public boolean hasNewDataSetColor()
	{
		return usedColors.size() < KnihovedaMapaConfig.DATA_SET_COLORS.length;
	}
	
	/**
	 * Vrati novou barvu, ale zatim ji neoznaci jako pouzitou
	 * @return
	 */
	public Color newDataSetColor()
	{
		for ( int i=0; i < KnihovedaMapaConfig.DATA_SET_COLORS.length; i++ )
		{
			Color candidate = KnihovedaMapaConfig.DATA_SET_COLORS[i];
			if ( !usedColors.contains(candidate) )
				return candidate;
		}
		
		throw new IllegalStateException("MapaSession.newDataSetColor() called but no new color available!");
	}

	/**
	 * Nastavi barvu jako pouzitou
	 * @param color
	 */
	public void useDataSetColor(Color color)
	{
		usedColors.add(color);
	}

	/**
	 * Uvolni barvu k pouziti
	 * @param color
	 */
	public void freeDataSetColor(Color color)
	{
		usedColors.remove(color);
	}
	
	
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
	
	
	/* Maximalni pocet zaznamu pro misto a souvisejici metody */

	private long maxCountInPlace;
	
	private void initMaxCountInPlace()
	{
		maxCountInPlace = SolrDAO.findMaxCountInPlace(currentDataSet);
	}
	
	public long maxCountInPlace()
	{
		return maxCountInPlace;
	}

	public static KnihovedaMapaSession get()
	{
		return (KnihovedaMapaSession) WebSession.get();
	}
	
	
	/* Reakce na eventy */
	
	

}
