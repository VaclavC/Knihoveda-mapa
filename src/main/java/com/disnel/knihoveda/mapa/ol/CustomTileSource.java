package com.disnel.knihoveda.mapa.ol;

import org.wicketstuff.openlayers3.api.source.tile.TileSource;

public class CustomTileSource extends TileSource {

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
		StringBuilder builder = new StringBuilder();
		builder.append("{ 'url' : '" + url + "' }");
		return builder.toString();
	}

}
