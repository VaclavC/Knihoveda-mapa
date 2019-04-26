package com.disnel.knihoveda.mapa.data;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Uklada mozne hodnoty pole, podle kterych bude omezena mnozina vysledku
 * 
 * @author Vaclav Cermak <disnel@disnel.com>
 *
 */
public class FieldValues implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String name;
	
	private LinkedHashSet<String> values;
	
	public FieldValues(String name, Collection<String> values)
	{
		this.name = name;
		this.values = new LinkedHashSet<>(values);
	}
	
	public FieldValues(String name, String... values)
	{
		this(name, Arrays.asList(values));
	}

	public String getName()
	{
		return name;
	}
	
	public LinkedHashSet<String> getValues()
	{
		return this.values;
	}
	
	public void setValues(Collection<String> values)
	{
		this.values = new LinkedHashSet<>(values);
	}
	
	public void addValue(String value)
	{
		values.add(value);
	}
	
	public void removeValue(String value)
	{
		values.remove(value);
	}
	
	public void replaceValues(List<String> values)
	{
		this.values = new LinkedHashSet<>(values);
	}

	public boolean isEmpty()
	{
		return values.isEmpty();
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		FieldValues other = (FieldValues) obj;
		if (name == null)
		{
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("FieldValues { ");
		sb.append(name);
		sb.append(" -> ");
		
		String delim = "";
		for ( String value : values )
		{
			sb.append(delim);
			sb.append(value);
			
			delim = ", ";
		}
		
		sb.append(" }");
		
		return sb.toString();
	}
	
}
