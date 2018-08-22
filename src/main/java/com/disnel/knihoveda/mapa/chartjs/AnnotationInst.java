package com.disnel.knihoveda.mapa.chartjs;

import java.io.Serializable;

import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@lombok.Data
public class AnnotationInst implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	@Setter(value = AccessLevel.PROTECTED)
	private String type;
	
	private String drawTime;
	private String id;
}
