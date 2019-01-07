package com.disnel.knihoveda.mapa;



import java.util.Arrays;

import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;

import com.googlecode.wicket.jquery.ui.settings.JQueryUILibrarySettings;

import de.agilecoders.wicket.extensions.markup.html.bootstrap.icon.FontAwesomeCssReference;

public class MainPage extends WebPage implements IAjaxIndicatorAware
{

	private PageParameters commonSearchParams;
	
	public MainPage(PageParameters parameters)
	{
		super(parameters);
		
		add(new MapaPanel("mapa"));
	
		add(new CasovyGraf("casovyGraf"));
		
		add(new ListView<String>("vyberyDlePoli", Arrays.asList(KnihovedaMapaConfig.FIELDS))
		{
			@Override
			protected void populateItem(ListItem<String> item)
			{
				String fieldName = item.getModelObject();
				
				item.add(new VyberDlePole("vyber", fieldName));
			}
		});
		
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
		
		// JS knihovny
		response.render(JavaScriptHeaderItem.forReference(JQueryUILibrarySettings.get().getJavaScriptReference()));

		// CSS
		response.render(CssHeaderItem.forReference(FontAwesomeCssReference.instance()));
		response.render(CssHeaderItem.forReference(JQueryUILibrarySettings.get().getStyleSheetReference()));
		response.render(CssHeaderItem.forReference(new CssResourceReference(MainPage.class, "MainPage.css")));
		
		// Prebarvit selecty
		response.render(OnDomReadyHeaderItem.forScript(
				VyberDlePole.getJSSetSelectColor(MapaSession.get().currentDataSet().getColor())));
		
	}

	@Override
	public String getAjaxIndicatorMarkupId()
	{
		return "ajaxIndicator";
	}
}
