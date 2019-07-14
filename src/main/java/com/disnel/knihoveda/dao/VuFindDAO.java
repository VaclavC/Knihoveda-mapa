package com.disnel.knihoveda.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.disnel.knihoveda.mapa.KnihovedaMapaConfig;
import com.disnel.knihoveda.mapa.data.DataSet;
import com.disnel.knihoveda.mapa.data.DataSet.FieldValues;

public class VuFindDAO
{

	public static String linkToVuFind(DataSet dataSet, FieldValues... additionalFieldValues)
	{
		StringBuilder sb = new StringBuilder(KnihovedaMapaConfig.vuFindURL);
		
		char sep = '?';
		
		/* Fields */
		List<FieldValues> fvList = new ArrayList<>(dataSet.getFieldsValues());
		fvList.addAll(Arrays.asList(additionalFieldValues));
		for ( FieldValues fv : fvList )
		{
			sb.append(sep); sep = '&';
			
			String vuFindFilterName = KnihovedaMapaConfig.SOLR_VUFIND_FIELDS_MAPPING.get(fv.getName());
			
			for ( String val : fv.getValues() )
			{
				sb.append("filter[]=~");
				sb.append(vuFindFilterName);
				sb.append(":\"");
				sb.append(val);
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
		
		/* Return */
		return sb.toString();
	}
	
}
