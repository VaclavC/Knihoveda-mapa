package com.disnel.knihoveda.mapa;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.danekja.java.util.function.serializable.SerializableFunction;

import com.disnel.knihoveda.dao.SolrDAO;
import com.disnel.knihoveda.mapa.data.DataSet;
import com.disnel.knihoveda.mapa.data.DataSet.FieldValues;
import com.disnel.knihoveda.mapa.events.AjaxEvent;
import com.disnel.knihoveda.mapa.events.UserSelectionChangedEvent;
import com.disnel.knihoveda.mapa.panel.CasovyGraf;
import com.disnel.knihoveda.mapa.panel.Help;
import com.disnel.knihoveda.mapa.panel.Info;
import com.disnel.knihoveda.mapa.panel.Mapa;
import com.disnel.knihoveda.mapa.panel.Search;
import com.disnel.knihoveda.wicket.AjaxGeneralButton;

import de.agilecoders.wicket.core.markup.html.bootstrap.behavior.CssClassNameAppender;
import de.agilecoders.wicket.webjars.request.resource.WebjarsCssResourceReference;

public class BasePage extends WebPage implements IAjaxIndicatorAware
{
	private static final long serialVersionUID = 1L;

	private Panel sidePanel;
	
	public BasePage()
	{
		/* Side panel select */
		for ( TabDef tDef : sidePanelTabs )
		{
			AjaxGeneralButton button;
			add(button  = new AjaxGeneralButton(tDef.tabId, Model.of(tDef), "click")
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected void onClick(AjaxRequestTarget target)
				{
					TabDef def = (TabDef) getDefaultModelObject();
					KnihovedaMapaSession.get().sidePanelIndex = sidePanelTabs.indexOf(def);
					
					sidePanel.replaceWith(sidePanel = def.panelCreator.apply(sidePanel.getId()));
					sidePanel.setOutputMarkupId(true);
					
					target.add(sidePanel);
					target.appendJavaScript(String.format(
							"$('#Title .buttons .btn').removeClass('active'); $('#%s').addClass('active');",
							getMarkupId()));
				}
			});
			button.setOutputMarkupId(true);

			button.add(new AttributeModifier("title", new StringResourceModel("main." + tDef.tabId + ".tooltip")));
			
			/* Set CSS class for active panel */
			Integer actPanelIndex = KnihovedaMapaSession.get().sidePanelIndex;
			int thisPanelIndex = sidePanelTabs.indexOf(tDef);
			if ( actPanelIndex != null && actPanelIndex == thisPanelIndex
					|| actPanelIndex == null && thisPanelIndex == 0 )
				button.add(new CssClassNameAppender("active"));
		}
		
		/* Side panel */
		int actPanelIndex = KnihovedaMapaSession.get().sidePanelIndex != null ? KnihovedaMapaSession.get().sidePanelIndex : 0;
		TabDef actTabDef = sidePanelTabs.get(actPanelIndex);
		
		add(sidePanel = actTabDef.panelCreator.apply("sidePanelContent"));
		sidePanel.setOutputMarkupId(true);
		
		/* Results for print */
		add(new ResultsForPrint("resultsForPrint"));
		
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
	}
	
	/* Definition of tabs in side panel */
	
	private static class TabDef implements Serializable
	{
		private static final long serialVersionUID = 1L;
		
		public String tabId;
		public SerializableFunction<String, Panel> panelCreator;
		
		public TabDef(String tabId, SerializableFunction<String, Panel> panelCreator)
		{
			super();
			this.tabId = tabId;
			this.panelCreator = panelCreator;
		}
	}
	
	private List<TabDef> sidePanelTabs = Arrays.asList(
		new TabDef("tab-search",	Search::new),
		new TabDef("tab-help",		Help::new),
		new TabDef("tab-info",		Info::new)
	);

	
	public static final String AJAX_INDICATOR_ID = "ajaxIndicator";
	
	@Override
	public String getAjaxIndicatorMarkupId()
	{
		return AJAX_INDICATOR_ID;
	}
	
	
	private static class ResultsForPrint extends Panel
	{
		private static final long serialVersionUID = 1L;
		
		public ResultsForPrint(String id)
		{
			super(id);
			
			setOutputMarkupId(true);
			
			add(new ListView<DataSet>("resultSet", new LoadableDetachableModel<List<DataSet>>()
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected List<DataSet> load()
				{
					return KnihovedaMapaSession.get().dataSets().stream()
							.filter( ds -> ds.isActive() )
							.collect(Collectors.toList());
				}
			})
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected void populateItem(ListItem<DataSet> itemDataSet)
				{
					DataSet dataSet = itemDataSet.getModelObject();
					
					/* Data set head */
					itemDataSet.add(new WebMarkupContainer("indicatorColor")
							.add(new AttributeAppender("style", String.format("fill:%s;", dataSet.getColor().toString(), ";"))));
					itemDataSet.add(new Label("indicatorIndex", itemDataSet.getIndex() + 1));
					
					itemDataSet.add(new Label("resultsCount", SolrDAO.getCountForDataSet(dataSet)));
					
					/* Field values */
					itemDataSet.add(new ListView<FieldValues>("field", new LoadableDetachableModel<List<FieldValues>>()
					{
						private static final long serialVersionUID = 1L;

						@Override
						protected List<FieldValues> load()
						{
							List<FieldValues> ret = new ArrayList<>();
							for ( String fieldName : Arrays.asList(KnihovedaMapaConfig.FIELDS))
							{
								FieldValues fv = dataSet.getFieldValues(fieldName);
								if ( fv != null )
									ret.add(fv);
							}
							
							return ret;
						}
					})
					{
						private static final long serialVersionUID = 1L;

						@Override
						protected void populateItem(ListItem<FieldValues> itemFieldValues)
						{
							FieldValues fv = itemFieldValues.getModelObject();
							
							itemFieldValues.add(new Label("title", new ResourceModel("field." + fv.getName())));
							
							itemFieldValues.add(new ListView<String>("value", new ArrayList<>(fv.getValues()))
							{
								private static final long serialVersionUID = 1L;

								@Override
								protected void populateItem(ListItem<String> itemValue)
								{
									itemValue.add(new Label("content", itemValue.getModelObject()));
								}
								
							});
						}
						
					});
					
					/* Time range */
					WebMarkupContainer timeRange;
					itemDataSet.add(timeRange = new WebMarkupContainer("timeRange"));
					if ( dataSet.hasTimeRange() )
					{
						timeRange.add(new Label("timeRangeFrom", dataSet.getYearFrom()));
						timeRange.add(new Label("timeRangeTo", dataSet.getYearTo()));
					}
					else
					{
						timeRange.setVisible(false);
					}
				}
			});
		}

		
		@Override
		public void onEvent(IEvent<?> event)
		{
			if ( event.getPayload() instanceof UserSelectionChangedEvent )
			{
				AjaxEvent ev = (AjaxEvent) event.getPayload();
			
				ev.getTarget().add(this);
			}
		}
	}
	
}
