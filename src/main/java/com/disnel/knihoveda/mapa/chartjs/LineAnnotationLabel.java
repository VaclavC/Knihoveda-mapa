package com.disnel.knihoveda.mapa.chartjs;

import java.io.Serializable;

import de.adesso.wickedcharts.chartjs.chartoptions.FontStyle;
import de.adesso.wickedcharts.chartjs.chartoptions.Position;
import de.adesso.wickedcharts.chartjs.chartoptions.colors.Color;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@lombok.Data
public class LineAnnotationLabel implements Serializable
{
	private static final long serialVersionUID = 1L;
	private Color backgroundColor;
	private String fontFamily;
	private Integer fontSize;
	private FontStyle fontStyle;
	private Color fontColor;
	private Integer xPadding;
	private Integer yPadding;
	private Integer cornerRadius;
	private Position position;
	private Integer xAdjust;
	private Integer yAdjust;
	private Boolean enabled;
	private String content;
}
