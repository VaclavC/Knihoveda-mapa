package com.disnel.knihoveda.mapa;

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.html.SecurePackageResourceGuard;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.resource.JQueryResourceReference;
import org.apache.wicket.settings.ExceptionSettings.AjaxErrorStrategy;
import com.disnel.knihoveda.dao.SolrDAO;

import de.agilecoders.wicket.core.Bootstrap;
import de.agilecoders.wicket.core.settings.BootstrapSettings;
import de.agilecoders.wicket.core.settings.SingleThemeProvider;
import de.agilecoders.wicket.core.settings.Theme;
import de.agilecoders.wicket.sass.BootstrapSass;
import de.agilecoders.wicket.sass.SassResourceReference;
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
		
		BootstrapSettings bSettings = new BootstrapSettings();
		bSettings.setThemeProvider(new SingleThemeProvider(new Theme("knihoveda")
		{
		    @Override
		    public List<HeaderItem> getDependencies()
		    {
		    	return Arrays.asList(
		    			CssHeaderItem.forReference(new SassResourceReference(BasePage.class, "bootstrap-knihoveda.scss"))
		    			);
		    }
		}));
		Bootstrap.install(this, bSettings);
		
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
		return new KnihovedaMapaSession(request);
	}
	
}
