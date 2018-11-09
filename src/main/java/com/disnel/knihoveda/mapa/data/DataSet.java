package com.disnel.knihoveda.mapa.data;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

	private Color color;
	
	private Set<String> selectedPlaces = new HashSet<>();
	
	private Map<String, FieldValues> fieldsValues;
	
	private Integer yearFrom, yearTo;
	
	public DataSet(Color color)
	{
		this.fieldsValues = new HashMap<>();
		this.color = color;
	}

	public Color getColor()
	{
		return color;
	}

	/**
	 * Vrati mnozinu vsech vybranych mist
	 * 
	 * @return
	 */
	public Set<String> getSelectedPlaces()
	{
		return selectedPlaces;
	}
	
	/**
	 * Vrati zda je misto se zadanym nazvem vybrano
	 * 
	 * @param placeName
	 * @return
	 */
	public boolean isPlaceSelected(String placeName )
	{
		return selectedPlaces.contains(placeName);
	}
	
	/**
	 * Vrati zda je nejake misto vybrano
	 * 
	 * @return
	 */
	public boolean isAnyPlaceSelected()
	{
		return !selectedPlaces.isEmpty();
	}
	
	/**
	 * Vrati zda je po operaci misto vybrano nebo ne
	 * 
	 * @param placeName
	 * @return
	 */
	public boolean toggleSelectedPlace(String placeName)
	{
		if ( selectedPlaces.contains(placeName) )
		{
			selectedPlaces.remove(placeName);
			return false;
		}
		else
		{
			selectedPlaces.add(placeName);
			return true;
		}
	}
	
	public void clearSelectedPlaces()
	{
		selectedPlaces.clear();
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

}
