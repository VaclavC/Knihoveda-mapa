package com.disnel.knihoveda.mapa;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.wicket.protocol.http.WebApplication;
import org.wicketstuff.openlayers3.api.util.Color;

public class KnihovedaMapaConfig
{
	// Properties file name property
	public static final String APP_CONFIG_PROPERTY = "appConfigFile";
	
	// Property names
	public static final String PROPERTY_OSM_URL = "osmURL";
	public static final String PROPERTY_SOLR_URL = "solrURL";
	public static final String PROPERTY_LOCATIONS_FILE = "locationsFile";
	
	// Live configuration
	public static String osmURL;
	public static String solrURL;
	public static String locationsFileName;

	// Predefined values
	public static final float MIN_PLACE_SIZE =  4.0f;
	public static final float MAX_PLACE_SIZE = 48.0f;
	public static final float PLACE_SIZE_DIFF = MAX_PLACE_SIZE - MIN_PLACE_SIZE;
	
	public static final Color[] DATA_SET_COLORS = new Color[]
	{
		new Color("#000000"),
		new Color("#F3D449"),
		new Color("#E86329"),
		new Color("#D92D28"),
		new Color("#752722"),
		new Color("#73814A"),
		new Color("#935794"),
		new Color("#726FDB"),
		new Color("#F263B7"),
		new Color("#92E787"),
		new Color("#58E00F"),
		new Color("#C44DB0"),
		new Color("#FBCD9A"),
		new Color("#BCECC9"),
		new Color("#DC9FD4"),
		new Color("#F7871C"),
//		new Color("#F4A586"),
//		new Color("#D58118"),
//		new Color("#C9EA4C"),
//		new Color("#4B229F"),
	};
	
	public static final int CASOVY_GRAF_OD = 1500, CASOVY_GRAF_DO = 1800;
	
	/**
	 * Load configuration
	 */
	public static void load()
	{
		String configFileName = System.getProperty(APP_CONFIG_PROPERTY);
		if ( configFileName == null )
			throw new IllegalStateException("Chybi parametr " + APP_CONFIG_PROPERTY);
		
		Properties props = new Properties();
		InputStream input = null;
		
		try
		{
			input = new FileInputStream(configFileName);
			
			props.load(input);
			
			osmURL = getProperty(props, PROPERTY_OSM_URL);
			solrURL = getProperty(props, PROPERTY_SOLR_URL);
			locationsFileName = getProperty(props, PROPERTY_LOCATIONS_FILE);
		}
		catch (IOException e)
		{
			throw new IllegalStateException("Nemohu nacist konfiguraci: " + e.getMessage());
		}
		finally
		{
			if ( input != null )
				try
				{
					input.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();	
				}
		}
	}
	
	/**
	 * Get configuration property by its name
	 * 
	 * @param props
	 * @param propertyName
	 * @return
	 */
	private static String getProperty(Properties props, String propertyName)
	{
		String ret;

		ret = WebApplication.get().getServletContext().getInitParameter(propertyName);
		
		if ( ret == null )
			ret = props.getProperty(propertyName);
		
		if ( ret == null )
			throw new IllegalStateException(propertyName + " init parameter missing");
		
		return ret;
	}

}
