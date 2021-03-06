package com.disnel.knihoveda.mapa.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.wicketstuff.openlayers3.api.geometry.Point;

public class ResultsInPlace implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String placeName;
	
	private Point placePoint;
	
	private String placeLink;
	
	private Map<DataSet, Long> numResults = new HashMap<>();
	
	public ResultsInPlace(String placeName, Point placePoint, String placeLink)
	{
		this.placeName = placeName;
		this.placePoint = placePoint;
		this.placeLink = placeLink;
	}

	public String getPlaceName()
	{
		return placeName;
	}
	
	public Point getPlacePoint()
	{
		return placePoint;
	}
	
	public String getPlaceLink()
	{
		return placeLink;
	}

	public void setNumResultsForDataSet(DataSet dataSet, Long number)
	{
		numResults.put(dataSet, number);
	}
	
	public Long getNumResultsForDataSet(DataSet dataSet)
	{
		Long ret = numResults.get(dataSet);
		
		if ( ret != null )
			return ret;
		else
			return 0L;
	}
	
	public boolean hasAnyNonZeroResult()
	{
		for (Entry<DataSet, Long> e : numResults.entrySet() )
			if ( e.getKey().isActive() && e.getValue() != 0L )
				return true;
		
		return false;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((placeName == null) ? 0 : placeName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ResultsInPlace other = (ResultsInPlace) obj;
		if (placeName == null)
		{
			if (other.placeName != null)
				return false;
		} else if (!placeName.equals(other.placeName))
			return false;
		return true;
	}
	
}
