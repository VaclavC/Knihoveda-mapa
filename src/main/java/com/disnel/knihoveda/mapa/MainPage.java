package com.disnel.knihoveda.mapa;

import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;

import de.agilecoders.wicket.extensions.markup.html.bootstrap.icon.FontAwesomeCssReference;

public class MainPage extends WebPage implements IAjaxIndicatorAware
{

	private PageParameters commonSearchParams;
	
	public MainPage(PageParameters parameters)
	{
		super(parameters);
		
		add(new MapaPanel("mapa"));
//		add(new WebMarkupContainer("mapa"));
	
		add(new CasovyGraf("casovyGraf",
			KnihovedaMapaConfig.CASOVY_GRAF_OD, KnihovedaMapaConfig.CASOVY_GRAF_DO,
			MapaSession.get().maxCountInPlace()));
//		add(new WebMarkupContainer("casovyGraf"));
		
		RepeatingView vybery;
		add(vybery = new RepeatingView("vyber"));
		
		vybery.add(new VyberDlePole(vybery.newChildId(), "masterPrinter"));
		vybery.add(new VyberDlePole(vybery.newChildId(), "topic"));
		vybery.add(new VyberDlePole(vybery.newChildId(), "genre"));
		
		add(new DataSetSwitcherPanel("dataSetSwitcher"));
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

	@Override
	public String getAjaxIndicatorMarkupId()
	{
		return "ajaxIndicator";
	}
}
