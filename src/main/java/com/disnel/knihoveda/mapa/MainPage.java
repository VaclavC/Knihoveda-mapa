package com.disnel.knihoveda.mapa;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;

import de.agilecoders.wicket.extensions.markup.html.bootstrap.icon.FontAwesomeCssReference;

public class MainPage extends WebPage
{

	private PageParameters commonSearchParams;
	
	public MainPage(PageParameters parameters)
	{
		super(parameters);
		
		add(new MapaPanel("mapa"));
	
		add(new CasovyGraf("casovyGraf", 1500, 2000));
		
//		add(new VyberPanel("lcol"));
		
//		add(new VysledkyPanel("rcol"));
		
//		add(new MistaPanel("mistaPanel"));
		
		RepeatingView vybery;
		add(vybery = new RepeatingView("vyber"));
		
		vybery.add(new VyberDlePole(vybery.newChildId(), "masterPrinter"));
		vybery.add(new VyberDlePole(vybery.newChildId(), "topic"));
		vybery.add(new VyberDlePole(vybery.newChildId(), "genre"));
	}

	public PageParameters getCommonSearchParams()
	{
		return commonSearchParams;
	}

	public void setCommonSearchParams(PageParameters commonSearchParams)
	{
		this.commonSearchParams = commonSearchParams;
	}
	
	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(FontAwesomeCssReference.instance()));
		response.render(CssHeaderItem.forReference(new CssResourceReference(MainPage.class, "MainPage.css")));
	}

}
