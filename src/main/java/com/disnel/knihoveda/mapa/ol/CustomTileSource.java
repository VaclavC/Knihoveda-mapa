package com.disnel.knihoveda.mapa.ol;

import org.wicketstuff.openlayers3.api.source.tile.TileSource;

public class CustomTileSource extends TileSource
{
	private static final long serialVersionUID = 1L;

	private String url;
	
	public CustomTileSource(String url) {
		super();
		
		this.url = url;
	}

	@Override
	public String getJsType() {
		return "ol.source.XYZ";
	}

	@Override
	public String renderJs() {
		return String.format(
				"{ 'url' : '%s', 'crossOrigin': null }",
				url);
	}

}
