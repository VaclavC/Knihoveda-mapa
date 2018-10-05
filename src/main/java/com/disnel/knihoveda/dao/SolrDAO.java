package com.disnel.knihoveda.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.FacetParams;
import org.wicketstuff.openlayers3.api.coordinate.Coordinate;
import org.wicketstuff.openlayers3.api.geometry.Point;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.Group;
import org.apache.solr.client.solrj.response.QueryResponse;
import com.disnel.knihoveda.mapa.KnihovedaMapaConfig;
import com.disnel.knihoveda.mapa.MapaSession;
import com.disnel.knihoveda.mapa.data.DataSet;
import com.disnel.knihoveda.mapa.data.FacetFieldCountWrapper;
import com.disnel.knihoveda.mapa.data.FieldValues;
import com.disnel.knihoveda.mapa.data.ResultsInPlace;

public class SolrDAO
{

	private static HttpSolrClient solrClient;
	
	public static void init()
	{
		if ( solrClient == null )
		{
			solrClient = new HttpSolrClient(KnihovedaMapaConfig.solrURL);
			solrClient.setFollowRedirects(true);
		}
	}
	
	public static SolrClient client()
	{
		if ( solrClient == null )
			throw new IllegalStateException("You must call SolrDAO.init() first");
		
		return solrClient;
	}
	
	public static QueryResponse getResponse(SolrQuery query)
	{
		try
		{
			query.add("wt", "javabin");
			
			System.out.println("Query: " + query);
			
			return client().query(query);
		}
		catch (SolrServerException | IOException e)
		{
			StringBuilder msg = new StringBuilder();
			
			if ( e instanceof SolrServerException )
			{
				msg.append(e.getCause());
				msg.append('\n');
			}
			
			msg.append("SolrJ query failed");
			
			throw new IllegalStateException(msg.toString());
		}
	}

	private static String emptyQueryParams()
	{
		return "geo:*";
	}
	
	private static String fieldValuesQueryParams(
			Collection<FieldValues> fieldValues, String exceptField)
	{
		StringBuilder qPars = new StringBuilder();
		
		String qParsDelim = " AND (";
		String qParsEnd = "";
		for ( FieldValues fv : fieldValues )
			if ( !fv.isEmpty())
			{
				if ( exceptField != null && exceptField.contains(fv.getName()) )
					continue;
				
				qPars.append(qParsDelim);
				
				String fvDelim = "";
				for ( String value : fv.getValues())
				{
					qPars.append(fvDelim);
					qPars.append(fv.getName());
					qPars.append(":");
					qPars.append('"');
					qPars.append(value);
					qPars.append('"');
					
					fvDelim = " OR ";
				}
				
				qParsDelim = ") AND (";
				qParsEnd = ")";
			}
		
		qPars.append(qParsEnd);

		return qPars.toString();
	}
	
	private static String timeRangeQueryParams(
			Integer yearFrom, Integer yearTo)
	{
		StringBuilder qPars = new StringBuilder();
		
		qPars.append(" AND ");
		qPars.append(KnihovedaMapaConfig.FIELD_TIME);
		qPars.append(":[");
		
		if ( yearFrom != null )
			qPars.append(yearFrom);
		else
			qPars.append('*');
		
		qPars.append(" TO ");
		
		if ( yearTo != null )
			qPars.append(yearTo);
		else
			qPars.append('*');
		
		qPars.append("]");
		
		return qPars.toString();
	}
	
	public static List<Count> getCountByYear(DataSet dataSet)
	{
		SolrQuery query = new SolrQuery();
		
		String qParams = emptyQueryParams()
				+ fieldValuesQueryParams(dataSet.getFieldsValues(), null);
		query.add("q", qParams);
		
		query.addFacetField(KnihovedaMapaConfig.FIELD_TIME);
		query.setFacetMinCount(1);
		query.setFacetSort(FacetParams.FACET_SORT_INDEX);
		query.setFacetLimit(-1);
		query.setRows(0);
		
		QueryResponse response = SolrDAO.getResponse(query);
		FacetField publishPlaceFF = response.getFacetField("publishDate");
		
		return publishPlaceFF.getValues();
	}
	
	/**
	 * Vrati nazev pole pro facetove vyhledavani
	 * 
	 * @param fieldName
	 * @return
	 */
	private static String facetFieldName(String fieldName)
	{
		if ( KnihovedaMapaConfig.FILEDS_WITH_SEPARATE_FACET.contains(fieldName) )
			return fieldName + KnihovedaMapaConfig.FACET_FIELD_SUFFIX;
		else
			return fieldName;
	}
	
	/**
	 * Vrati mozne hodnoty pole s ohledem na ostatni vybrana pole
	 * 
	 * @param fieldName
	 * @param dataSet
	 * @return
	 */
	public static List<FacetFieldCountWrapper> getFieldValues(String fieldName, DataSet dataSet)
	{
		SolrQuery query = new SolrQuery();
		
		// Budeme pracovat s verzi pole uzpusobenou pro facet
		String facetFieldName = facetFieldName(fieldName);
		
		// Tadu potrebujeme vsechny krome toho, pro ktere pole to delame
		String qParams = emptyQueryParams()
				+ fieldValuesQueryParams(dataSet.getFieldsValues(), fieldName)
				+ timeRangeQueryParams(dataSet.getYearFrom(), dataSet.getYearTo());
		query.add("q", qParams);
		
		query.addFacetField(facetFieldName);
		query.setFacetLimit(-1);
		query.setFacetMinCount(1);
		query.setRows(0);
		
		QueryResponse response = SolrDAO.getResponse(query);
		
		FacetField ff = response.getFacetField(facetFieldName);
		List<Count> res = ff.getValues();
		
		List<FacetFieldCountWrapper> ret = new ArrayList<>(res.size());
		Iterator<Count> it = res.iterator();
		while (it.hasNext())
			ret.add(new FacetFieldCountWrapper(it.next()));
				
		
		return ret;
	}

	/**
	 * Zjisti pocty zaznamu pro jednotliva geograficka mista pro danou datovou sadu
	 * 
	 * @param dataSet
	 * @return
	 */
	private static List<Group> resultsInPlacesForDataSet(DataSet dataSet)
	{
		// Pripravit dotaz
		SolrQuery query = new SolrQuery();

		String qParams = emptyQueryParams()
				+ fieldValuesQueryParams(dataSet.getFieldsValues(), null)
				+ timeRangeQueryParams(dataSet.getYearFrom(), dataSet.getYearTo());
		query.add("q", qParams);
		
		query.addField("publishPlace");
		query.addField("long_lat");
		query.add("group", "true");
		query.add("group.field", "publishPlace");
		query.setRows(-1);
		
		
		// Ziskat odpoved
		QueryResponse response = SolrDAO.getResponse(query);
		List<Group> resGroups = response.getGroupResponse().getValues().get(0).getValues();
	
		// Vratit vysledek
		return resGroups;
	}
	
	/**
	 * Nacte pocty vysledku pro datove sady ve vsech mistech
	 * 
	 * @return
	 */
	public static List<ResultsInPlace> loadResultsInPlaces()
	{
		LinkedHashMap<String, ResultsInPlace> resultsInPlaces = new LinkedHashMap<>();
		
		for ( DataSet dataSet : MapaSession.get().dataSets() )
		{
			for ( Group group : resultsInPlacesForDataSet(dataSet) )
			{
				if ( group.getGroupValue() != null )
				{
					Long count = group.getResult().getNumFound();
					
					SolrDocument doc = group.getResult().get(0);
					String placeName = (String) doc.getFieldValue("publishPlace");
					Point placePoint = pointFromString((String) doc.getFieldValue("long_lat"));
					
					ResultsInPlace resultsInPlace = resultsInPlaces.get(placeName);
					if ( resultsInPlace == null )
					{
						resultsInPlace = new ResultsInPlace(placeName, placePoint);
						resultsInPlaces.put(placeName, resultsInPlace);
					}
					
					resultsInPlace.setNumResultsForDataSet(dataSet, count);
				}			
			}
		}

		return new ArrayList<ResultsInPlace>(resultsInPlaces.values());
	}

	/**
	 * Zjisti maximalni pocet zaznamu v jednom geografickem miste
	 * 
	 * @return
	 */
	public static long findMaxCountInPlace(DataSet dataSet)
	{
		long maxCountInPlace = 0;
		for ( Group group : resultsInPlacesForDataSet(dataSet) )
		{
			long count = group.getResult().getNumFound();
			if (maxCountInPlace < count)
				maxCountInPlace = count;
		}
		
		return maxCountInPlace;
	}
	
	/**
	 * Pomocna metoda
	 * 
	 * @param coordinates
	 * @return
	 */
	private static Point pointFromString(String coordinates)
	{
		String[] parts = coordinates.split(",");
		double lat = Double.parseDouble(parts[0]);
		double lon = Double.parseDouble(parts[1]);

		return new Point(new Coordinate(lon, lat));
	}
}
