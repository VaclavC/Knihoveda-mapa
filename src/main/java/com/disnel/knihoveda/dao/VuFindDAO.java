package com.disnel.knihoveda.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.disnel.knihoveda.mapa.KnihovedaMapaConfig;
import com.disnel.knihoveda.mapa.data.DataSet;
import com.disnel.knihoveda.mapa.data.DataSet.FieldValues;

public class VuFindDAO
{

	public static String linkToVuFindForDataset(DataSet dataSet, FieldValues... additionalFieldValues)
	{
		StringBuilder sb = new StringBuilder(KnihovedaMapaConfig.vuFindURL);
		
		char sep = '?';
		
		/* Fields */
		List<FieldValues> fvList = new ArrayList<>(dataSet.getFieldsValues());
		fvList.addAll(Arrays.asList(additionalFieldValues));
		for ( FieldValues fv : fvList )
		{
			String vuFindFilterName = KnihovedaMapaConfig.SOLR_VUFIND_FIELDS_MAPPING.get(fv.getName());
			
			for ( String val : fv.getValues() )
			{
				sb.append(sep); sep = '&';
				
				// Hack for languages
				String vfVal = val;
				if ( "language_cs".equals(fv.getName()) )
					if ( SOLR_TO_VUFIND_LANG.containsKey(val) )
						vfVal = SOLR_TO_VUFIND_LANG.get(val);
					else
						System.err.println("Lang translation missing for " + vfVal);
				// Hack end
				
				sb.append("filter[]=~");
				sb.append(vuFindFilterName);
				sb.append(":\"");
				sb.append(vfVal);
				sb.append('"');
			}
		}
		
		/* Time range */
		if ( dataSet.getYearFrom() != null || dataSet.getYearTo() != null )
		{
			sb.append(sep); sep = '&';
			
			sb.append("daterange[]=publishDateSingle");
			
			if ( dataSet.getYearFrom() != null )
			{
				sb.append("&publishDateSinglefrom=");
				sb.append(dataSet.getYearFrom());
			}
			
			if ( dataSet.getYearTo() != null )
			{
				sb.append("&publishDateSingleto=");
				sb.append(dataSet.getYearTo());
			}
		}
		
		/* Replace spaces with %20 */
		String ret = sb.toString();
		ret.replace(" ", "%20");
		
		/* Return */
		return ret;
	}
	
	public static String linkToVuFindForTimeRange(DataSet dataSet)
	{
		StringBuilder sb = new StringBuilder(KnihovedaMapaConfig.vuFindURL);
		
		if ( dataSet.getYearFrom() != null || dataSet.getYearTo() != null )
		{
			sb.append("?daterange[]=publishDateSingle");
			
			if ( dataSet.getYearFrom() != null )
			{
				sb.append("&publishDateSinglefrom=");
				sb.append(dataSet.getYearFrom());
			}
			
			if ( dataSet.getYearTo() != null )
			{
				sb.append("&publishDateSingleto=");
				sb.append(dataSet.getYearTo());
			}
		}
		
		return sb.toString();
	}
	
	
	/* Solr -> VuFind langage map (hack) */
	
	private static Map<String, String> SOLR_TO_VUFIND_LANG = new HashMap<>();
	static
	{
		SOLR_TO_VUFIND_LANG.put("čeština", "Czech");
		SOLR_TO_VUFIND_LANG.put("latina", "Latin");
		SOLR_TO_VUFIND_LANG.put("němčina", "German");
		SOLR_TO_VUFIND_LANG.put("italština", "Italian");
		SOLR_TO_VUFIND_LANG.put("francouzština", "French");
		SOLR_TO_VUFIND_LANG.put("řečtina", "Greek");
		SOLR_TO_VUFIND_LANG.put("angličtina", "English");
		SOLR_TO_VUFIND_LANG.put("slovenština", "Slovak");
		SOLR_TO_VUFIND_LANG.put("polština", "Polish");
		SOLR_TO_VUFIND_LANG.put("maďarština", "Hungarian");
		SOLR_TO_VUFIND_LANG.put("holandština", "Dutch");
		SOLR_TO_VUFIND_LANG.put("španělština", "Spanish");
		SOLR_TO_VUFIND_LANG.put("ruština", "Russian");
		SOLR_TO_VUFIND_LANG.put("slovanština", "Slavic");
		SOLR_TO_VUFIND_LANG.put("hebrejština", "Hebrew");
		SOLR_TO_VUFIND_LANG.put("švédština", "Swedish");
		SOLR_TO_VUFIND_LANG.put("vícejazyčné", "Multiple");
		SOLR_TO_VUFIND_LANG.put("bulharština", "Bulgarian");
		SOLR_TO_VUFIND_LANG.put("dánština", "Danish");
		SOLR_TO_VUFIND_LANG.put("rumunština", "Romanian");
		SOLR_TO_VUFIND_LANG.put("neurčeno", "Undetermined");
		SOLR_TO_VUFIND_LANG.put("aramejština", "Aramic");
		SOLR_TO_VUFIND_LANG.put("slovinština", "Slovenian");
		SOLR_TO_VUFIND_LANG.put("starodávná řečtina", "Angient Greek");
		SOLR_TO_VUFIND_LANG.put("portugalština", "Portugalese");
		SOLR_TO_VUFIND_LANG.put("arabština", "Arabic");
		SOLR_TO_VUFIND_LANG.put("syrština", "Syriac");
		SOLR_TO_VUFIND_LANG.put("chorvatština", "Croatian");
		SOLR_TO_VUFIND_LANG.put("lužická srbština", "Sorbian");
		SOLR_TO_VUFIND_LANG.put("církevní slovanština", "Church Slavic");
	}

}
