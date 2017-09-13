package com.disnel.knihoveda.dao;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.wicket.request.mapper.parameter.INamedParameters.NamedPair;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class SolrDAO
{

	public static String SOLR_URL = "http://localhost:8090/solr/biblio/";
	
	private static SolrClient solrClient;
	
	public static void init()
	{
		if ( solrClient == null )
			solrClient = new HttpSolrClient(SOLR_URL);
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
			return client().query(query);
		}
		catch (SolrServerException | IOException e)
		{
			throw new IllegalStateException("Solr query failed");
		}
	}
	
	public static void addQueryParameters(SolrQuery query, PageParameters params)
	{
		StringBuilder qPars = new StringBuilder();
		if ( params == null || params.isEmpty() )
		{
			qPars.append("*:*");
		}
		else
		{
			String delim = null;
			for ( NamedPair pair : params.getAllNamed() )
			{
				if ( delim != null )
					qPars.append(delim);
				
				qPars.append(pair.getKey());
				qPars.append(":");
				qPars.append('"');
				qPars.append(pair.getValue());
				qPars.append('"');
				
				if ( delim == null )
					delim = " AND ";
			}
		}
	
		query.add("q", qPars.toString());
	}
		
	public static void addQueryEmptyParameters(SolrQuery query)
	{
		addQueryParameters(query, null);
	}
	
}
