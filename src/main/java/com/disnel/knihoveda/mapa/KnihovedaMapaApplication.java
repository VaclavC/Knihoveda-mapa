package com.disnel.knihoveda.mapa;

import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.settings.ExceptionSettings.AjaxErrorStrategy;
import com.disnel.knihoveda.dao.PlaceLocationDAO;
import com.disnel.knihoveda.dao.SolrDAO;

import de.agilecoders.wicket.webjars.WicketWebjars;

public class KnihovedaMapaApplication extends WebApplication
{
	/**
	 * Constructor
	 */
	public KnihovedaMapaApplication()
	{
		// TODO Auto-generated constructor stub
	}

	/**
	 * Homepage
	 */
	@Override
	public Class<? extends Page> getHomePage()
	{
		return MainPage.class;
	}

	/**
	 * Init
	 */
	@Override
	protected void init()
	{
		super.init();
		
		// Nacist konfiguraci
		KnihovedaMapaConfig.load();
		
		// Inicializovat
		SolrDAO.init();
		PlaceLocationDAO.init();
		
		WicketWebjars.install(this);
		
		// Nastavit ruzne veci
		getMarkupSettings().setStripWicketTags(true);
		getExceptionSettings().setAjaxErrorHandlingStrategy(AjaxErrorStrategy.REDIRECT_TO_ERROR_PAGE);
	}
	
}
