package com.disnel.knihoveda.mapa.timeline;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.wicketstuff.openlayers3.api.util.Color;

import com.disnel.knihoveda.dao.JSON;

import lombok.experimental.Accessors;

@Accessors(chain = true)
@lombok.Data
public class TimelineDataset implements Serializable
{
	private static final long serialVersionUID = 1L;

	private Color color;
	
	private List<Integer> yearData;
	private List<Long> countData;

	public TimelineDataset(Color color)
	{
		this.color = color;
		
		this.yearData = new ArrayList<>();
		this.countData = new ArrayList<>();
	}

	public void addCount(Integer year, Long count)
	{
		this.yearData.add(year);
		this.countData.add(count);
	}
	
	public String getJSConstruct()
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("new TimelineDataset('");
		sb.append(String.format("#%02x%02x%02x",
				color.red, color.green, color.blue));
		sb.append("',\n");
		sb.append(JSON.listToJSON(yearData));
		sb.append(",\n");
		sb.append(JSON.listToJSON(countData));
		sb.append(')');
		
		return sb.toString();
	}
	
}
