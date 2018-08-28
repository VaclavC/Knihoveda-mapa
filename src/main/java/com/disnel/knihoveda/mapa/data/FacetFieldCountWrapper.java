package com.disnel.knihoveda.mapa.data;

import java.io.Serializable;

import org.apache.solr.client.solrj.response.FacetField.Count;

public class FacetFieldCountWrapper implements Serializable
{

	private Count count;
	
	public FacetFieldCountWrapper(Count count)
	{
		this.count = count;
	}

	public String getText()
	{
		return count.toString();
	}
	
	public String getValue()
	{
		return count.getName();
	}
	
	@Override
	public String toString()
	{
		return count.toString();
	}
	
}
