package com.disnel.knihoveda.wicket;

import java.util.List;

import org.wicketstuff.openlayers3.api.Map;
import org.wicketstuff.openlayers3.api.View;
import org.wicketstuff.openlayers3.api.interaction.Interaction;
import org.wicketstuff.openlayers3.api.layer.Layer;
import org.wicketstuff.openlayers3.api.overlay.Overlay;

public class AjaxOLMap extends Map
{
	private static final long serialVersionUID = 1L;

	public AjaxOLMap(List<Layer> layers)
	{
		super(layers);
	}

	public AjaxOLMap(RenderType renderer, List<Layer> layers)
	{
		super(renderer, layers);
	}

	public AjaxOLMap(List<Layer> layers, View view)
	{
		super(layers, view);
	}

	public AjaxOLMap(List<Layer> layers, List<Overlay> overlays)
	{
		super(layers, overlays);
	}

	public AjaxOLMap(RenderType renderer, List<Layer> layers, View view)
	{
		super(renderer, layers, view);
	}

	public AjaxOLMap(List<Layer> layers, List<Overlay> overlays, View view)
	{
		super(layers, overlays, view);
	}

	public AjaxOLMap(RenderType renderer, List<Layer> layers, List<Overlay> overlays, View view)
	{
		super(renderer, layers, overlays, view);
	}

	public AjaxOLMap(RenderType renderer, List<Layer> layers, List<Overlay> overlays, View view,
			List<Interaction> interactions)
	{
		super(renderer, layers, overlays, view, interactions);
	}

	public String overlaysChangedJs()
	{
		StringBuilder builder = new StringBuilder();
        
		builder.append(getJsId());
		builder.append(".getOverlays().clear();\n");
		
        if ( getOverlays() != null )
            for (Overlay overlay : getOverlays())
            {
            	builder.append(getJsId());
            	builder.append(".addOverlay(");
            	builder.append("new " + overlay.getJsType() + "(");
                builder.append(overlay.renderJs());
                builder.append("));");
            }

		return builder.toString();
	}
	
}
