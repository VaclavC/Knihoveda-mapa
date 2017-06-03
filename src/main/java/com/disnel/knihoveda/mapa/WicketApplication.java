package com.disnel.knihoveda.mapa;

import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;

public class WicketApplication extends WebApplication
{

	public WicketApplication()
	{
		// TODO Auto-generated constructor stub
	}

	@Override
	public Class<? extends Page> getHomePage()
	{
		return MainPage.class;
	}

}
