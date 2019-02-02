package com.disnel.knihoveda.wicket;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.wicketstuff.openlayers3.api.Feature;
import org.wicketstuff.openlayers3.api.layer.Vector;
import org.wicketstuff.openlayers3.api.source.vector.VectorSource;
import org.wicketstuff.openlayers3.api.style.Style;

public class AjaxVector extends Vector
{
	private static final long serialVersionUID = 1L;

	public AjaxVector(VectorSource source)
	{
		super(source);
	}

	public AjaxVector(VectorSource source, Style style)
	{
		super(source, style);
	}

	public void setSource(AjaxRequestTarget target, VectorSource source)
	{
		super.setSource(source);
		
		StringBuilder js = new StringBuilder();

		// Create new features from source
		if ( source.getFeatures() != null )
			for ( Feature feature : source.getFeatures() )
			{
				js.append(feature.getJsId());
				js.append(" = new ");
				js.append(feature.getJsType());
				js.append("(");
				js.append(feature.renderJs());
				js.append(");\n");
				
				js.append(feature.getJsId());
				js.append(".setStyle(new ");
				js.append(feature.getStyle().getJsType());
				js.append("(");
				js.append(feature.getStyle().renderJs());
				js.append("));\n");
			}
		
		// Create new source
		js.append(source.getJsId());
		js.append(" = new ");
		js.append(source.getJsType());
		js.append("(");
		js.append(source.renderJs());
		js.append(");\n");

		// Set new source to this layer
		js.append(getJsId());
		js.append(".setSource(");
		js.append(source.getJsId());
		js.append(");\n");
		
		// Pass it to Wicket
		target.appendJavaScript(js);
	}

}
