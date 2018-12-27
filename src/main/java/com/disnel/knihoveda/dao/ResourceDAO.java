package com.disnel.knihoveda.dao;

import org.apache.wicket.Application;

public class ResourceDAO
{

	public static String resourceString(String resourceKey, String defaultValue)
	{
		return Application.get()
				.getResourceSettings()
				.getLocalizer()
				.getString(resourceKey, null, null, null, null, defaultValue);
	}

	public static String jsResourceString(String resourceKey, String defaultValue)
	{
		return "'" + resourceString(resourceKey, defaultValue) + "'";
	}
	
}
