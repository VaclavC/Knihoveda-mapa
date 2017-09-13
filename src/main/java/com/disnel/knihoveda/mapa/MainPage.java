package com.disnel.knihoveda.mapa;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;

public class MainPage extends WebPage
{

	public MainPage(PageParameters parameters)
	{
		super(parameters);
		
		add(new MapaPanel("mapa"));
		
		add(new VyberPanel("lcol"));
		
		add(new VysledkyPanel("rcol"));
	}

	
	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(new CssResourceReference(MainPage.class, "MainPage.css")));
	}
	
}
