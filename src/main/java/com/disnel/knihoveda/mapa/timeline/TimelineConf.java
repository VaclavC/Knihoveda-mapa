package com.disnel.knihoveda.mapa.timeline;

import java.io.Serializable;

import lombok.experimental.Accessors;

@Accessors(chain = true)
@lombok.Data
public class TimelineConf implements Serializable
{
	private static final long serialVersionUID = 1L;

	private Integer yearMin, yearMax;
	
	private Float lineWidth = 1.5f;
	private Float dotSize = 5.0f;
	private Integer paddingLeft = 32;
	private Integer paddingRight = 8;
	private Integer paddingTop = 16;
	private Integer paddingBottom = 8;
	private String timeAxisStyle = "#b0b0b0";
	private String timeAxisFont = "13px Arial";
	private String timeAxisFontStyle = "#000000";
	private Integer timeAxisTextY = 14;
	private String countAxisStyle = "#b0b0b0";
	private String countAxisFont = "10px Arial";
	private String countAxisFontStyle = "#000000";
	private Float cursorLineWidth1 = 1.0f;
	private Float cursorLineWidth2 = 2.0f;
	private String cursorStyle1 = "#FF9C00";
	private String cursorStyle2 = "#000000";
	private String detailPanelId = null;
	private String selectStyle = "#e0e0e0";
	private String selectFont = "14px Arial Black";
	private String selectFontStyle = "#ffffff";
	private Integer selectTextDist = 19;
	private Float wheelScaleK = 1.1f;
}
