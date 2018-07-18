package com.disnel.knihoveda.mapa.data;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.wicketstuff.openlayers3.api.util.Color;

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

	private String name;
	
	private Color color;
	
	private Map<String, FieldValues> fieldsValues;
	
	public DataSet()
	{
		this.color = new Color("#000000");
		this.fieldsValues = new HashMap<>();
	}
	
	public DataSet(String name, Color color)
	{
		this();
		this.name = name;
		this.color = color;
	}

	public String getName()
	{
		return name;
	}
	
	public Color getColor()
	{
		return color;
	}

	public Collection<FieldValues> getFieldsValues()
	{
		return fieldsValues.values();
	}

	public void addFieldValues(FieldValues fv)
	{
		this.fieldsValues.put(fv.getName(), fv);
	}
	
	public void removeFieldValues(FieldValues fv)
	{
		this.fieldsValues.remove(fv.getName());
	}

	public FieldValues getFieldValues(String name)
	{
		return this.fieldsValues.get(name);
	}
	
	public void setFieldValues(String name, Collection<String> values)
	{
		if ( values.isEmpty() )
		{
			fieldsValues.remove(name);
		}
		else 
		{
			FieldValues fv = fieldsValues.get(name);
			if ( fv == null )
			{
				fv = new FieldValues(name, values);
				fieldsValues.put(name, fv);
			}
			else
			{
				fv.setValues(values);
			}
		}
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
		DataSet other = (DataSet) obj;
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
		
		sb.append("DataSet {\n");
		
		for ( String key : fieldsValues.keySet() )
		{
			sb.append('\t');
			sb.append(fieldsValues.get(key).toString());
			sb.append('\n');
		}
		
		sb.append("}\n");
		
		return sb.toString();
	}
}
