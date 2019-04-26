package com.disnel.knihoveda.mapa;

import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.SecurePackageResourceGuard;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.resource.JQueryResourceReference;
import org.apache.wicket.settings.ExceptionSettings.AjaxErrorStrategy;
import com.disnel.knihoveda.dao.SolrDAO;

import de.agilecoders.wicket.core.Bootstrap;
import de.agilecoders.wicket.core.settings.BootstrapSettings;
import de.agilecoders.wicket.sass.BootstrapSass;
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
		return BasePage.class;
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
		
		WicketWebjars.install(this);
		Bootstrap.install(this, new BootstrapSettings());
		BootstrapSass.install(this);
		
		// Nastavit ruzne veci
		getMarkupSettings().setStripWicketTags(true);
		getExceptionSettings().setAjaxErrorHandlingStrategy(AjaxErrorStrategy.REDIRECT_TO_ERROR_PAGE);
		
		getJavaScriptLibrarySettings().setJQueryReference(JQueryResourceReference.getV1());

		SecurePackageResourceGuard guard = (SecurePackageResourceGuard) getResourceSettings().getPackageResourceGuard();
		guard.addPattern("+*.scss");
	}
	
	@Override
	public Session newSession(Request request, Response response)
	{
		return new MapaSession(request);
	}
	
}
