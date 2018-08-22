package com.disnel.knihoveda.mapa.chartjs;

import java.io.Serializable;

import lombok.experimental.Accessors;

@Accessors(chain = true)
@lombok.Data
public class Annotation implements Serializable
{
	private static final long serialVersionUID = 1L;
	private String[] events;
	private Integer dblClickSpeed;
	private AnnotationInst[] annotations;
}
