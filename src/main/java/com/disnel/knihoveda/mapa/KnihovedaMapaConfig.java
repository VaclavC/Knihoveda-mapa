package com.disnel.knihoveda.mapa;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.wicket.protocol.http.WebApplication;
import org.wicketstuff.openlayers3.api.util.Color;

public class KnihovedaMapaConfig
{
	// Properties file name property
	public static final String APP_CONFIG_PROPERTY = "appConfigFile";
	
	// Property names
	public static final String PROPERTY_OSM_URL = "osmURL";
	public static final String PROPERTY_SOLR_URL = "solrURL";
	public static final String PROPERTY_VUFIND_URL = "vuFindURL";
	public static final String PROPERTY_LOCATIONS_FILE = "locationsFile";
	
	// Live configuration
	public static String osmURL;
	public static String solrURL;
	public static String vuFindURL;

	// Predefined values
	public static final float MIN_PLACE_SIZE = 12.0f;
	public static final float MAX_PLACE_SIZE = 48.0f;
	
	public static final String FIELD_PLACE_NAME = "publishPlace_geo";
	public static final String FIELDS[] = new String[] { "publishPlace_geo", "masterPrinter", "topic", "genre", "language_cs" };
	public static final String FIELD_TIME = "publishDate";
	public static final String FIELD_GEOLOC = "long_lat";
	public static final String FIELD_PLACE_LINK = "location_link";
	
	public static final Map<String, String> SOLR_VUFIND_FIELDS_MAPPING = new HashMap<>();
	static
	{
		SOLR_VUFIND_FIELDS_MAPPING.put(FIELDS[0], "publishPlace_facet");
		SOLR_VUFIND_FIELDS_MAPPING.put(FIELDS[1], "printer_publisher_facet");
		SOLR_VUFIND_FIELDS_MAPPING.put(FIELDS[2], "topic_facet");
		SOLR_VUFIND_FIELDS_MAPPING.put(FIELDS[3], "genre_facet");
		SOLR_VUFIND_FIELDS_MAPPING.put(FIELDS[4], "language");
	}
	
	public static final Color[] DATA_SET_COLORS = new Color[]
	{
		new Color("#800000"),
		new Color("#8C8B0E"),
		new Color("#08324D"),
		new Color("#0A81CC"),
		new Color("#330505")
	};
	
	// Pole, ktera potrebuji pridavat _facet pro facetove vyhledavani
	public static final Set<String> FILEDS_WITH_SEPARATE_FACET = 
			new HashSet<>(Arrays.asList(
					"author", "author2", "author_KVO", "avail", "sublocation", "era",
					"format", "genre", "geographic", "masterPrinter",  "topic"));
	public static final String FACET_FIELD_SUFFIX = "_facet";
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
			vuFindURL = getProperty(props, PROPERTY_VUFIND_URL);
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
