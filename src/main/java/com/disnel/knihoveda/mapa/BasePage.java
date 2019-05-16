package com.disnel.knihoveda.mapa;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.danekja.java.util.function.serializable.SerializableFunction;

import com.disnel.knihoveda.mapa.panel.CasovyGraf;
import com.disnel.knihoveda.mapa.panel.Help;
import com.disnel.knihoveda.mapa.panel.Mapa;
import com.disnel.knihoveda.mapa.panel.PdfPrint;
import com.disnel.knihoveda.mapa.panel.Search;

import de.agilecoders.wicket.core.markup.html.bootstrap.behavior.CssClassNameAppender;
import de.agilecoders.wicket.sass.SassResourceReference;
import de.agilecoders.wicket.webjars.request.resource.WebjarsCssResourceReference;

public class BasePage extends WebPage implements IAjaxIndicatorAware
{
	private static final long serialVersionUID = 1L;

	private Panel sidePanel;
	
	public BasePage()
	{
		/* Side panel select */
		add(new ListView<TabDef>("sidePanelButton", sidePanelTabs)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<TabDef> item)
			{
				TabDef def = item.getModelObject();
				
				item.setOutputMarkupId(true);
				
				/* Set CSS class for active panel */
				Integer actPanelIndex = KnihovedaMapaSession.get().sidePanelIndex;
				if ( actPanelIndex != null && actPanelIndex == item.getIndex()
						|| actPanelIndex == null && item.getIndex() == 0 )
					item.add(new CssClassNameAppender("active"));
				
				/* Tab */
				item.add(def.tab);
				
				/* Open corresponding panel on click */
				item.add(new AjaxEventBehavior("click")
				{
					private static final long serialVersionUID = 1L;

					@Override
					protected void onEvent(AjaxRequestTarget target)
					{
						KnihovedaMapaSession.get().sidePanelIndex = item.getIndex();
						
						TabDef def = item.getModelObject();
						
						sidePanel.replaceWith(sidePanel = def.panelCreator.apply(sidePanel.getId()));
						sidePanel.setOutputMarkupId(true);
						
						target.add(sidePanel);
						target.appendJavaScript(String.format(
								"$('#SidePanel .head .button').removeClass('active'); $('#%s').addClass('active');",
								item.getMarkupId()));
					}
				});
			}
		});
		
		/* Side panel */
		int actPanelIndex = KnihovedaMapaSession.get().sidePanelIndex != null ? KnihovedaMapaSession.get().sidePanelIndex : 0;
		TabDef actTabDef = sidePanelTabs.get(actPanelIndex);
		
		add(sidePanel = actTabDef.panelCreator.apply("sidePanelContent"));
		sidePanel.setOutputMarkupId(true);
		
		/* Map */
		add(new Mapa("map"));
		
		/* Timeline */
		add(new CasovyGraf("timeline"));
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		response.render(CssHeaderItem.forReference(
				new WebjarsCssResourceReference("font-awesome/5.7.2/css/all.css")));
	
		response.render(CssHeaderItem.forReference(new SassResourceReference(getClass(), "BasePage.scss")));
	}
	
	/* Definition of tabs in side panel */
	
	private static class TabDef implements Serializable
	{
		private static final long serialVersionUID = 1L;
		
		public static final String tabID = "tab";
		
		public Fragment tab;
		public SerializableFunction<String, Panel> panelCreator;
		
		public TabDef(Fragment tab, SerializableFunction<String, Panel> panelCreator)
		{
			super();
			this.tab = tab;
			this.panelCreator = panelCreator;
		}
	}
	
	private List<TabDef> sidePanelTabs = Arrays.asList(
		new TabDef(new Fragment(TabDef.tabID, "tab-search", this), 		Search::new),
		new TabDef(new Fragment(TabDef.tabID, "tab-pdfprint", this),	PdfPrint::new),
		new TabDef(new Fragment(TabDef.tabID, "tab-help", this),		Help::new)
	);

	
	public static final String AJAX_INDICATOR_ID = "ajaxIndicator";
	
	@Override
	public String getAjaxIndicatorMarkupId()
	{
		return AJAX_INDICATOR_ID;
	}
	
}
