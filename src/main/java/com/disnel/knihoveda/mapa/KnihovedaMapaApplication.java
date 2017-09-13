package com.disnel.knihoveda.mapa;

import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.settings.ExceptionSettings.AjaxErrorStrategy;
import com.disnel.knihoveda.dao.PlaceLocationDAO;
import com.disnel.knihoveda.dao.SolrDAO;

import de.agilecoders.wicket.webjars.WicketWebjars;

public class KnihovedaMapaApplication extends WebApplication
{

	public KnihovedaMapaApplication()
	{
		// TODO Auto-generated constructor stub
	}

	@Override
	public Class<? extends Page> getHomePage()
	{
		return MainPage.class;
	}

	@Override
	protected void init()
	{
		super.init();
		
		SolrDAO.init();
		PlaceLocationDAO.init();
		
		WicketWebjars.install(this);
		
		getMarkupSettings().setStripWicketTags(true);
		getExceptionSettings().setAjaxErrorHandlingStrategy(AjaxErrorStrategy.REDIRECT_TO_ERROR_PAGE);
	}
	
}
