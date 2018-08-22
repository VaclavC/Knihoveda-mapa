package com.disnel.knihoveda.mapa.chartjs;

import de.adesso.wickedcharts.chartjs.chartoptions.Callback;
import de.adesso.wickedcharts.chartjs.chartoptions.colors.Color;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@lombok.Data
@EqualsAndHashCode(callSuper = true)
public class LineAnnotation extends AnnotationInst
{
	private static final long serialVersionUID = 1L;
	
	public LineAnnotation()
	{
		setType("line");
	}
	
	private LineAnnotationMode mode;
	private String scaleID;
	private Integer value;
	private Integer endValue;
	private Color borderColor;
	private Integer borderWidth;
	private Integer[] borderDash;
	private Integer borderDashOffset;
	private LineAnnotationLabel label;
	private Callback onMouseenter;
	private Callback onMouseover;
	private Callback onMouseleave;
	private Callback onMouseout;
	private Callback onMousemove;
	private Callback onMousedown;
	private Callback onMouseup;
	private Callback onClick;
	private Callback onDblclick;
	private Callback onContextmenu;
	private Callback onWheel;
}
