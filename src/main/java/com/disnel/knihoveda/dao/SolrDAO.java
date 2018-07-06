package com.disnel.knihoveda.dao;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.wicket.request.mapper.parameter.INamedParameters.NamedPair;

import com.disnel.knihoveda.mapa.KnihovedaMapaConfig;

import org.apache.wicket.request.mapper.parameter.PageParameters;

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
	
	public static void addQueryParameters(SolrQuery query, PageParameters params)
	{
		StringBuilder qPars = new StringBuilder();
		
		qPars.append("geo:*");
		
		if ( params != null && !params.isEmpty() )
		{
			String delim = " AND ";
			for ( NamedPair pair : params.getAllNamed() )
			{
				qPars.append(delim);
				
				qPars.append(pair.getKey());
				qPars.append(":");
				qPars.append('"');
				qPars.append(pair.getValue());
				qPars.append('"');
			}
		}
	
		query.add("q", qPars.toString());
	}
		
	public static void addQueryEmptyParameters(SolrQuery query)
	{
		addQueryParameters(query, null);
	}
	
}
