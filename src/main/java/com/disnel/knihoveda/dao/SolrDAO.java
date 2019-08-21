package com.disnel.knihoveda.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.FacetParams;
import org.apache.wicket.Application;
import org.wicketstuff.openlayers3.api.coordinate.Coordinate;
import org.wicketstuff.openlayers3.api.geometry.Point;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FieldStatsInfo;
import org.apache.solr.client.solrj.response.Group;
import org.apache.solr.client.solrj.response.QueryResponse;
import com.disnel.knihoveda.mapa.KnihovedaMapaConfig;
import com.disnel.knihoveda.mapa.KnihovedaMapaSession;
import com.disnel.knihoveda.mapa.data.DataSet;
import com.disnel.knihoveda.mapa.data.DataSet.FieldValues;
import com.disnel.knihoveda.mapa.data.ResultsInPlace;

public class SolrDAO
{

	
	/* Link to SOLR */
	private static HttpSolrClient solrClient;
	
	
	/* Initialization */
	
	public static void init()
	{
		if ( solrClient == null )
		{
			solrClient = new HttpSolrClient(KnihovedaMapaConfig.solrURL);
			solrClient.setFollowRedirects(true);
		}
	}
	
	
	/* Internal methods for SOLR interaction */
	
	private static SolrClient client()
	{
		if ( solrClient == null )
			throw new IllegalStateException("You must call SolrDAO.init() first");
		
		return solrClient;
	}
	
	private static QueryResponse getResponse(SolrQuery query)
	{
		try
		{
			query.add("wt", "javabin");
			
			if ( Application.get().usesDevelopmentConfig() )
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

	
	/* Internal methods for query parameters */
	
	/**
	 * Field name for faceted search
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
	 * Parameters for "empty" query
	 * 
	 * @return
	 */
	private static String emptyQueryParams()
	{
		return "geo:*";
	}
	
	/**
	 * Field values as search parameter
	 * 
	 * @param fieldValues
	 * @param exceptField
	 * @return
	 */
	private static String fieldValuesQueryParams(
			Collection<FieldValues> fieldValues, String exceptField)
	{
		StringBuilder qPars = new StringBuilder();
		
		String qParsDelim = " AND (";
		String qParsEnd = "";
		for ( FieldValues fv : fieldValues )
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
	
	
	/**
	 * Time range as search parameter
	 * 
	 * @param yearFrom
	 * @param yearTo
	 * @return
	 */
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
	
	
	/* Public methods for various numbers of results */
	
	/**
	 * Get number of results for a dataset
	 * 
	 * @param dataSet
	 * @return
	 */
	public static long getCountForDataSet(DataSet dataSet)
	{
		SolrQuery query = new SolrQuery();
		
		String qParams = emptyQueryParams()
				+ fieldValuesQueryParams(dataSet.getFieldsValues(), null)
				+ timeRangeQueryParams(dataSet.getYearFrom(), dataSet.getYearTo());
		query.add("q", qParams);
		
		query.setRows(0);
		
		QueryResponse response = SolrDAO.getResponse(query);
		
		return response.getResults().getNumFound();
	}
	
	/**
	 * Get number of results for each year in dataset
	 * 
	 * @param dataSet
	 * @return
	 */
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
	 * Get number of results for a field in dataset
	 * 
	 * @param fieldName
	 * @param dataSet
	 * @return
	 */
	public static FieldCounts getFieldCounts(String fieldName, DataSet dataSet)
	{
		SolrQuery query = new SolrQuery();
		
		// Budeme pracovat s verzi pole uzpusobenou pro facet
		String facetFieldName = facetFieldName(fieldName);
		
		// Tadu potrebujeme vsechny krome toho, pro ktere pole to delame
		String qParams = emptyQueryParams()
				+ " AND " + fieldName + ":*"
				+ fieldValuesQueryParams(dataSet.getFieldsValues(), fieldName)
				+ timeRangeQueryParams(dataSet.getYearFrom(), dataSet.getYearTo());
		query.add("q", qParams);
		
		query.setFacetSort(FacetParams.FACET_SORT_COUNT);
		
		query.addFacetField(facetFieldName);
		query.setFacetLimit(-1);
		query.setFacetMinCount(1);
		query.setRows(0);
		
		QueryResponse response = SolrDAO.getResponse(query);
		FacetField ff = response.getFacetField(facetFieldName);
		
		return new FieldCounts(response.getResults().getNumFound(), ff.getValues());
	}
	
	public static class FieldCounts
	{
		public Long totalCount;
		public List<Count> counts;

		public FieldCounts(Long totalCount, List<Count> counts)
		{
			this.totalCount = totalCount;
			this.counts = counts;
		}
	}
	
	
	/**
	 * Get number of results for each place in dataset
	 * 
	 * @param dataSet
	 * @return
	 */
	private static List<Group> getResultsInPlaces(DataSet dataSet)
	{
		// Pripravit dotaz
		SolrQuery query = new SolrQuery();

		String qParams = emptyQueryParams()
				+ fieldValuesQueryParams(dataSet.getFieldsValuesExcept(KnihovedaMapaConfig.FIELD_PLACE_NAME), null)
				+ timeRangeQueryParams(dataSet.getYearFrom(), dataSet.getYearTo());
		query.add("q", qParams);
		
		query.addField(KnihovedaMapaConfig.FIELD_PLACE_NAME);
		query.addField(KnihovedaMapaConfig.FIELD_GEOLOC);
		query.addField(KnihovedaMapaConfig.FIELD_PLACE_LINK);
		query.add("group", "true");
		query.add("group.field", KnihovedaMapaConfig.FIELD_PLACE_NAME);
		query.setRows(-1);
		
		
		// Ziskat odpoved
		QueryResponse response = SolrDAO.getResponse(query);
		List<Group> resGroups = response.getGroupResponse().getValues().get(0).getValues();
	
		// Vratit vysledek
		return resGroups;
	}
	
	
	/**
	 * Get number of results in all places
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<ResultsInPlace> getResultsInPlaces()
	{
		LinkedHashMap<String, ResultsInPlace> resultsInPlaces = new LinkedHashMap<>();
		
		for ( DataSet dataSet : KnihovedaMapaSession.get().dataSets() )
		{
			for ( Group group : getResultsInPlaces(dataSet) )
			{
				if ( group.getGroupValue() != null )
				{
					Long count = group.getResult().getNumFound();
					
					SolrDocument doc = group.getResult().get(0);
					String placeName = (String) doc.getFieldValue(KnihovedaMapaConfig.FIELD_PLACE_NAME);
					String placePointS = ((List<String>) doc.getFieldValue(KnihovedaMapaConfig.FIELD_GEOLOC)).get(0);
					Point placePoint = pointFromString(placePointS);
					List<String> placeLinkAS = (List<String>) doc.getFieldValue(KnihovedaMapaConfig.FIELD_PLACE_LINK);
					String placeLink = placeLinkAS != null && placeLinkAS.size() > 0 ? placeLinkAS.get(0) : null; 
					
					ResultsInPlace resultsInPlace = resultsInPlaces.get(placeName);
					if ( resultsInPlace == null )
					{
						resultsInPlace = new ResultsInPlace(placeName, placePoint, placeLink);
						resultsInPlaces.put(placeName, resultsInPlace);
					}
					
					resultsInPlace.setNumResultsForDataSet(dataSet, count);
				}			
			}
		}

		return new ArrayList<ResultsInPlace>(resultsInPlaces.values());
	}
	
	private static Point pointFromString(String coordinates)
	{
		String[] parts = coordinates.split(",");
		double lat = Double.parseDouble(parts[0]);
		double lon = Double.parseDouble(parts[1]);

		return new Point(new Coordinate(lon, lat));
	}

	
	/* Public methods for various data limits */
	
	/**
	 * Find time range covering available data
	 * 
	 * @return
	 */
	public static int[] findTimeRange()
	{
		// Pripravit dotaz
		SolrQuery query = new SolrQuery();

		String qParams = emptyQueryParams()
				+ " AND " + KnihovedaMapaConfig.FIELD_TIME + ":[ 0 TO *]";
		query.add("q", qParams);

		query.setGetFieldStatistics(true);
		query.setGetFieldStatistics(KnihovedaMapaConfig.FIELD_TIME);
		
		query.setRows(-0);
		
		// Ziskat odpoved
		QueryResponse response = SolrDAO.getResponse(query);
		FieldStatsInfo info = response.getFieldStatsInfo().get(KnihovedaMapaConfig.FIELD_TIME);
		
		return new int[] {
				Integer.valueOf((String) info.getMin()),
				Integer.valueOf((String) info.getMax())
				};
	}
	
	/**
	 * Find maximal count per year
	 * 
	 * @return
	 */
	public static int findMaxYearCount()
	{
		SolrQuery query = new SolrQuery();
		
		String qParams = emptyQueryParams();
		query.add("q", qParams);
		
		query.addFacetField(KnihovedaMapaConfig.FIELD_TIME);
		query.setFacetMinCount(1);
		query.setFacetSort(FacetParams.FACET_SORT_COUNT);
		query.setFacetLimit(-1);
		query.setRows(0);
		
		QueryResponse response = SolrDAO.getResponse(query);
		FacetField publishPlaceFF = response.getFacetField("publishDate");
		
		List<Count> listCount = publishPlaceFF.getValues();
		return Math.toIntExact(listCount.get(0).getCount());
	}
	
	/**
	 * Find maximal count of results in a place 
	 * 
	 * @return
	 */
	public static long findMaxCountInPlace(DataSet dataSet)
	{
		long maxCountInPlace = 0;
		for ( Group group : getResultsInPlaces(dataSet) )
		{
			long count = group.getResult().getNumFound();
			if (maxCountInPlace < count)
				maxCountInPlace = count;
		}
		
		return maxCountInPlace;
	}
	
}
