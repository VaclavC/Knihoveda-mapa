package com.disnel.knihoveda.dao;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.Serializer;
import org.wicketstuff.openlayers3.api.View;
import org.wicketstuff.openlayers3.api.coordinate.LongLat;
import org.wicketstuff.openlayers3.api.geometry.Point;

import com.disnel.knihoveda.wicket.AppError;
import com.opencsv.CSVReader;

public class PlaceLocationDAO
{
	
	public static String LOCATIONS_FILE = "/home/disnel/Desktop/0Work/Knihoveda/locations.db";
	public static String PLACES_MAP_NAME = "places";
	
	private static DB placesDB;
	private static ConcurrentMap<String, Point> placesMap;
	
	public static void init()
	{
		placesDB = DBMaker
				.fileDB(LOCATIONS_FILE)
				.fileMmapEnable()
				.closeOnJvmShutdown()
				.make();
		
		placesMap = placesDB
				.hashMap(PLACES_MAP_NAME, Serializer.STRING, new PointSerializer())
				.createOrOpen();
	}
	
	public static class PointSerializer implements Serializable, Serializer<Point>
	{

		@SuppressWarnings("unchecked")
		@Override
		public void serialize(DataOutput2 out, Point value) throws IOException
		{
			Serializer.JAVA.serialize(out, value);
		}

		@Override
		public Point deserialize(DataInput2 input, int available) throws IOException
		{
			return (Point) Serializer.JAVA.deserialize(input, available);
		}
		
	}
	
	public static Point getPointForPlace(String placeName)
	{
		return placesMap.get(placeName);
	}
	
	public static void setPointForPlace(String placeName, Point point)
	{
		placesMap.put(placeName, point);
		
		placesDB.commit();
	}
	
	public static void readFromCSV(byte[] csvFile, Set<String> placeNames)
	{
		CSVReader csvReader = new CSVReader(
				new InputStreamReader(new ByteArrayInputStream(csvFile)));
		
		try
		{
			String[] header = csvReader.readNext();
			
			if ( header == null )
				return;
			
			Integer iName = null, iLat = null, iLon = null;
			int index = 0;
			for ( String head : header )
			{
				if ( stringContains(head, "obec", "nazev", "název") )
					iName = index;
				
				if ( stringContains(head, "lon") )
					iLon = index;
				
				if ( stringContains(head, "lat") )
					iLat = index;
				
				index++;
			}
			
			if ( iName == null || iLon == null || iLat == null )
				return;
			
			String[] line = csvReader.readNext();
			while ( line != null )
			{
				String nazev = line[iName];
				Double lon = Double.valueOf(line[iLon]);
				Double lat = Double.valueOf(line[iLat]);
				
				if ( placeNames.contains(nazev.toLowerCase()) )
					setPointForPlace(nazev, new Point(
							new LongLat(lon, lat, "EPSG:4326").transform(View.DEFAULT_PROJECTION)));
				
				line = csvReader.readNext();
			}
		}
		catch (IOException e)
		{
			throw new AppError("Chyba cteni CSV souboru");
		}
		finally
		{
			try
			{
				csvReader.close();
			}
			catch (IOException e)
			{
				throw new AppError("Chyba zavreni CSV souboru");
			}
		}
	}

	private static boolean stringContains(String src, String... what)
	{
		String srcLC = src.toLowerCase();
		
		for ( String w : what )
			if ( srcLC.contains(w.toLowerCase()) )
				return true;
		
		return false;
	}
	
}
