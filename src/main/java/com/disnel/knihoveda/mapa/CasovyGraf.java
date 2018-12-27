package com.disnel.knihoveda.mapa;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.NumberTextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import com.disnel.knihoveda.dao.SolrDAO;
import com.disnel.knihoveda.mapa.data.DataSet;
import com.disnel.knihoveda.mapa.events.AjaxEvent;
import com.disnel.knihoveda.mapa.events.TimeSelectEvent;
import com.disnel.knihoveda.mapa.events.UserSelectionChangedEvent;
import com.disnel.knihoveda.mapa.timeline.Timeline;
import com.disnel.knihoveda.mapa.timeline.TimelineConf;
import com.disnel.knihoveda.mapa.timeline.TimelineDataset;
import com.googlecode.wicket.kendo.ui.form.button.AjaxButton;

public class CasovyGraf extends Panel
{

	private Timeline timeline;
	
	private Integer minYear = Integer.MAX_VALUE,
			        maxYear = Integer.MIN_VALUE;
	
	private Integer yearFrom, yearTo;
	
	private YearInput inputYearFrom, inputYearTo;

	private Form<Void> form;
	
	public CasovyGraf(String id)
	{
		super(id);
		
		// Vlastni casovy graf
		TimelineConf conf = new TimelineConf();
		conf.setDetailPanelId("timelineRecordInfo");
		
		add(timeline = new Timeline("timeline", conf));
		timeline.setData(createTimelineData());
		
		// Ovladaci panel vlevo
		add(form = new Form<Void>("form"));
		form.setOutputMarkupId(true);
		form.add(new AttributeModifier("style", getCssBorderColorProperty()));
		
		form.add(new AjaxFormSubmitBehavior("change")
		{
			@Override
			protected void onSubmit(AjaxRequestTarget target)
			{
				if ( yearFrom == null ) yearFrom = minYear;
				if ( yearTo == null ) yearTo = maxYear;
				
				if ( yearFrom > yearTo )
				{
					Integer tmp = yearFrom;
					yearFrom = yearTo;
					yearTo = tmp;
				}
				
				updateCurrentDataSet();
				updateInputs(target);

				send(getPage(), Broadcast.BREADTH,
						new TimeSelectEvent(target, yearFrom, yearTo));
			}
		});

		form.add(inputYearFrom = new YearInput("inputOd",
				new PropertyModel<Integer>(this, "yearFrom")));
		
		form.add(inputYearTo = new YearInput("inputDo",
				new PropertyModel<Integer>(this, "yearTo")));
		
		form.add(new AjaxButton("butClear")
		{
			@Override
			protected void onSubmit(AjaxRequestTarget target)
			{
				yearFrom = yearTo = null;
				
				updateCurrentDataSet();
				
				send(getPage(), Broadcast.BREADTH,
						new TimeSelectEvent(target, yearFrom, yearTo));
			}
		});
	}

	private TimelineDataset createTimelineDataSet(DataSet dataSet)
	{
		TimelineDataset tlDataset =
				new TimelineDataset(dataSet.getColor());

		for ( Count count : SolrDAO.getCountByYear(dataSet) )
		{
			String countName = count.getName();

			if ( !countName.isEmpty() )
			{
				Integer year = Integer.parseInt(count.getName());
				Long countVal = count.getCount();

				tlDataset.addCount(year, countVal);
				
				if ( year < minYear ) minYear = year;
				if ( year > maxYear ) maxYear = year;
			}
		}
		
		return tlDataset;
	}
	
	private List<TimelineDataset> createTimelineData()
	{
		List<TimelineDataset> datasetList =
				new ArrayList<>(MapaSession.get().dataSets().size());
		
		for ( DataSet dataSet : MapaSession.get().dataSets() )
			if ( dataSet != MapaSession.get().currentDataSet() )
				datasetList.add(createTimelineDataSet(dataSet));
		
		datasetList.add(createTimelineDataSet(MapaSession.get().currentDataSet()));
		
		return datasetList;
	}
	
	@Override
	public void onEvent(IEvent<?> event)
	{
		if ( event.getPayload() instanceof UserSelectionChangedEvent )
		{
			AjaxEvent ev = (AjaxEvent) event.getPayload();
			AjaxRequestTarget target = ev.getTarget();
			
			timeline.setData(createTimelineData());
			
			DataSet currentDataSet = MapaSession.get().currentDataSet();
			timeline.setYearFrom(yearFrom = currentDataSet.getYearFrom());
			timeline.setYearTo(yearTo = currentDataSet.getYearTo());
			
			target.appendJavaScript(timeline.getJSSetData());
			target.appendJavaScript(timeline.getJSDraw());
			updateInputs(target);
			target.appendJavaScript(getJSChangeBorderColor());
			
			System.out.println(getJSChangeBorderColor());
		}
	}
	
	private void updateCurrentDataSet()
	{
		DataSet currentDataSet = MapaSession.get().currentDataSet();
		currentDataSet.setYearFrom(yearFrom);
		currentDataSet.setYearTo(yearTo);
	}
	
	private void updateInputs(AjaxRequestTarget target)
	{
		inputYearFrom.modelChanged();
		inputYearTo.modelChanged();
		
		target.add(inputYearFrom, inputYearTo);
	}
	
	private class YearInput extends NumberTextField<Integer>
	{
		public YearInput(String id, IModel<Integer> model)
		{
			super(id, model);
			
			setOutputMarkupId(true);
			
			setMinimum(minYear);
			setMaximum(maxYear);
			
//			options.set("format", "'####'");
//			options.set("min", minYear);
//			options.set("max", maxYear);
		}
	}
	
	private String getCssBorderColorProperty()
	{
		return "border-color: " + MapaSession.get().currentDataSet().getColor().toString() + ";";
	}

	private String getJSChangeBorderColor()
	{
		return String.format("$('#%s').css('border-color', '%s');",
				form.getMarkupId(),
				MapaSession.get().currentDataSet().getColor().toString());
	}
	
}
