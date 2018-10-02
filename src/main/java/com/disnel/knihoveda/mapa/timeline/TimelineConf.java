package com.disnel.knihoveda.mapa.timeline;

import java.io.Serializable;

import lombok.experimental.Accessors;

@Accessors(chain = true)
@lombok.Data
public class TimelineConf implements Serializable
{
	private Float lineWidth = 1.5f;
	private Float dotSize = 5.0f;
	private Integer paddingLeft = 32;
	private Integer paddingRight = 8;
	private Integer paddingTop = 8;
	private Integer paddingBottom = 8;
	private String timeAxisStyle = "#b0b0b0";
	private String timeAxisFont = "13px Arial";
	private Integer timeAxisTextY = 14;
	private String countAxisStyle = "d0d0d0";
	private String countAxisFont = "10px Arial";
	private Float cursorLineWidth = 1.0f;
	private String cursorStrokeStyle = "#000000";
	private String cursorFillStyle = "#000000";
	private String detailPanelId = null;
}
