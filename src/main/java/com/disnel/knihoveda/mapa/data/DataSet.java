package com.disnel.knihoveda.mapa.data;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.stream.Collectors;

import org.wicketstuff.openlayers3.api.util.Color;

import com.disnel.knihoveda.mapa.KnihovedaMapaSession;

/**
 * Reprezentace datove sady vysledku
 * 
 * Obsahuje omezeni hodnot pro jednotliva vyhledavaci pole
 * 
 * @author Vaclav Cermak <disnel@disnel.com>
 *
 */
public class DataSet implements Serializable
{
	private static final long serialVersionUID = 1L;

	
	private Color color;
	
	private Map<String, FieldValues> fieldsValues;
	
	private Integer yearFrom, yearTo;
	
	
	/* Constructors */
	
	public DataSet(Color color)
	{
		this.fieldsValues = new HashMap<>();
		this.color = color;
	}

	
	/* Getters and setters */
	
	public Color getColor()
	{
		return color;
	}
	
	
	/* Utility methods */
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("DataSet {\n");
		
		for ( String key : fieldsValues.keySet() )
		{
			sb.append('\t');
			sb.append(fieldsValues.get(key).toString());
			sb.append('\n');
		}

		if ( yearFrom != null || yearTo != null )
		{
			sb.append("\tTime interval [");
			if ( yearFrom != null )
				sb.append(yearFrom);
			sb.append(" - ");
			if ( yearTo != null )
				sb.append(yearTo);
			sb.append("]\n");
		}
		
		sb.append("}\n");
		
		return sb.toString();
	}


	/**
	 * Returns true if anything is selected
	 * 
	 * @return
	 */
	public boolean isActive()
	{
		if ( this == KnihovedaMapaSession.get().currentDataSet() )
			return true;
		
		if ( ! fieldsValues.isEmpty() )
			return true;
		
		if ( yearFrom != null || yearTo != null )
			return true;
		
		return false;
	}

	
	/* Clear */
	
	public void clear()
	{
		fieldsValues.clear();
		
		yearFrom = null;
		yearTo = null;
	}
	

	/* Work with timeline */
	
	public Integer getYearFrom()
	{
		return yearFrom;
	}

	public DataSet setYearFrom(Integer yearFrom)
	{
		this.yearFrom = yearFrom;
		
		return this;
	}
	
	public Integer getYearTo()
	{
		return yearTo;
	}

	public DataSet setYearTo(Integer yearTo)
	{
		this.yearTo = yearTo;
		
		return this;
	}
	
	public boolean hasTimeRange()
	{
		return yearFrom != null && yearTo != null;
	}
	
	
	/* Get all field values */
	
	public Collection<FieldValues> getFieldsValues()
	{
		return fieldsValues.values();
	}
	
	public Collection<FieldValues> getFieldsValuesExcept(String fieldName)
	{
		return fieldsValues.values().stream()
				.filter( fv -> { return ! fv.name.equals(fieldName); })
				.collect(Collectors.toList());
	}
	
	/* Work with individual field values */
	
	public FieldValues removeFieldValues(String fieldName)
	{
		return this.fieldsValues.remove(fieldName);
	}

	public FieldValues getFieldValues(String name)
	{
		return this.fieldsValues.get(name);
	}
	
	public boolean hasFieldValue(String fieldName)
	{
		return fieldsValues.containsKey(fieldName);
	}
	
	public boolean hasFieldValue(String fieldName, String value)
	{
		if ( fieldsValues.containsKey(fieldName) )
			return fieldsValues.get(fieldName).hasValue(value);
		
		return false;
	}

	public void addFieldValue(String fieldName, String value)
	{
		if ( fieldsValues.containsKey(fieldName) )
		{
			fieldsValues.get(fieldName).addValue(value);
		}
		else
		{
			fieldsValues.put(fieldName, new FieldValues(fieldName, value));
		}
	}
	
	public void removeFieldValue(String fieldName, String value)
	{
		if ( fieldsValues.containsKey(fieldName) )
		{
			FieldValues fv = fieldsValues.get(fieldName); 
			
			fv.removeValue(value);
			
			if ( fv.isEmpty() )
				fieldsValues.remove(fieldName);
		}
	}
	
	public boolean toggleFieldValue(String fieldName, String value)
	{
		if ( hasFieldValue(fieldName, value) )
		{
			removeFieldValue(fieldName, value);
			
			return false;
		}
		else
		{
			addFieldValue(fieldName, value);
			
			return true;
		}
	}	
	
	
	/* Field values class */
	
	public static class FieldValues implements Serializable
	{
		private static final long serialVersionUID = 1L;

		
		private String name;
		
		private LinkedHashSet<String> values;
		
		
		/* Constructors */
		
		public FieldValues(String name, Collection<String> values)
		{
			this.name = name;
			this.values = new LinkedHashSet<>(values);
		}
		
		public FieldValues(String name, String... values)
		{
			this(name, Arrays.asList(values));
		}

		
		/* Getters and setters */
		
		public String getName()
		{
			return name;
		}
		
		public LinkedHashSet<String> getValues()
		{
			return this.values;
		}
		
		
		/* Utility methods */
		
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
		

		/* Work with values */

		protected boolean isEmpty()
		{
			return values.isEmpty();
		}

		protected boolean hasValue(String value)
		{
			return values.contains(value);
		}
		
		protected void addValue(String value)
		{
			values.add(value);
		}
		
		protected void removeValue(String value)
		{
			values.remove(value);
		}

	}

}
