package com.disnel.knihoveda.mapa.chartjs;

import de.adesso.wickedcharts.chartjs.chartoptions.Options;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@lombok.Data
@EqualsAndHashCode(callSuper = true)
public class OptionsAnnotation extends Options
{
	private Annotation annotation;
}
