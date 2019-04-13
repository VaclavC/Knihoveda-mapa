package com.disnel.knihoveda.dao;

import java.lang.reflect.Field;
import java.util.List;

import com.github.openjson.JSONException;
import com.github.openjson.JSONObject;

public class JSON
{

	public static JSONObject toJSON(Object obj) throws IllegalArgumentException, IllegalAccessException, JSONException
	{
		JSONObject ret = new JSONObject();
		
		Class<?> objClass = obj.getClass();
		for ( Field field : objClass.getDeclaredFields())
		{
			field.setAccessible(true);
			
			String name = field.getName();
			Object value = field.get(obj);
			
			if ( value == null )
				value = new String("");
			
			ret.put(name, value);
		}
		
		return ret;
	}

	public static String listToJSON(List<?> list)
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append('[');
		
		boolean first = true;
		for ( Object obj : list )
		{
			if ( !first )
				sb.append(',');
			else
				first = false;
			
			sb.append(obj);
		}

		sb.append(']');

		return sb.toString();
	}
	
}
