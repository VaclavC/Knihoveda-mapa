package com.disnel.knihoveda.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.Group;
import org.apache.solr.client.solrj.response.QueryResponse;
import com.disnel.knihoveda.mapa.KnihovedaMapaConfig;
import com.disnel.knihoveda.mapa.data.DataSet;
import com.disnel.knihoveda.mapa.data.FieldValues;

import de.adesso.wickedcharts.chartjs.chartoptions.valueType.PointValue;

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
	
	public static void addEmptyQueryParameters(SolrQuery query)
	{
		query.add("q", "geo:*");
	}
	
	public static void addDataSetQueryParameters(SolrQuery query, Collection<FieldValues> fieldValues)
	{
		StringBuilder qPars = new StringBuilder();
		
		qPars.append("geo:*");
		
		String qParsDelim = " AND (";
		String qParsEnd = "";
		for ( FieldValues fv : fieldValues )
			if ( !fv.isEmpty() )
			{
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
		
		query.add("q", qPars.toString());
	}
	
	public static void addDataSetQueryParameters(SolrQuery query, DataSet dataSet)
	{
		addDataSetQueryParameters(query, dataSet.getFieldsValues());
	}
	
	public static List<Group> getMapOverlays(DataSet dataSet)
	{
		// Pripravit dotaz
		SolrQuery query = new SolrQuery();
		SolrDAO.addDataSetQueryParameters(query, dataSet);
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
	
	public static List<Count> getCountByYear(DataSet dataSet)
	{
		SolrQuery query = new SolrQuery();
		SolrDAO.addDataSetQueryParameters(query, dataSet);
		query.addFacetField("publishDate");
		query.setFacetMinCount(1);
		query.setFacetSort(FacetParams.FACET_SORT_INDEX);
		query.setFacetLimit(-1);
		query.setRows(0);
		
		QueryResponse response = SolrDAO.getResponse(query);
		FacetField publishPlaceFF = response.getFacetField("publishDate");
		
		return publishPlaceFF.getValues();
	}
	
	public static List<PointValue> getCountByYearAsPoints(DataSet dataSet)
	{
		List<Count> countList = getCountByYear(dataSet);
		
		List<PointValue> ret = new ArrayList<>(countList.size());
		Iterator<Count> it = countList.iterator();
		while ( it.hasNext() )
		{
			Count count = it.next();
			
			if ( count.getName() != null && !count.getName().isEmpty() )
				ret.add(new PointValue(
						new Integer(count.getName()),
						new Integer((int) count.getCount())));
		}
		
		return ret;
	}
	
	/**
	 * Vrati nazev pole pro facetove vyhledavani
	 * 
	 * @param fieldName
	 * @return
	 */
	public static String facetFieldName(String fieldName)
	{
		return fieldName + "_facet";
	}
	
	/**
	 * Vrati mozne hodnoty pole s ohledem na ostatni vybrana pole
	 * 
	 * @param fieldName
	 * @param dataSet
	 * @return
	 */
	public static List<Count> getFieldValues(String fieldName, DataSet dataSet)
	{
		SolrQuery query = new SolrQuery();
		
		// Budeme pracovat s verzi pole uzpusobenou pro facet
		String facetFieldName = facetFieldName(fieldName);
		
		// Tadu potrebujeme vsechny krome toho, pro ktere pole to delame
		Collection<FieldValues> dataSetFieldValues = dataSet.getFieldsValues();
		List<FieldValues> fieldValuesList = new ArrayList<>(dataSetFieldValues.size());
		for ( FieldValues fv : dataSetFieldValues )
			if ( !fieldName.contains(fv.getName()) )
				fieldValuesList.add(fv);
		
		SolrDAO.addDataSetQueryParameters(query, fieldValuesList);
		
		query.addFacetField(facetFieldName);
		query.setFacetLimit(-1);
		query.setFacetMinCount(1);
		query.setRows(0);
		
		QueryResponse response = SolrDAO.getResponse(query);
		
		FacetField ff = response.getFacetField(facetFieldName);
		
		return ff.getValues();
	}
	
}
