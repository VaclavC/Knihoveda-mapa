package com.disnel.knihoveda.mapa;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.NumberTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.validation.validator.RangeValidator;

import com.disnel.knihoveda.mapa.events.MistoSelectEvent;
import com.disnel.knihoveda.mapa.events.SearchEvent;

@SuppressWarnings("unused")
public class VyberPanel extends Panel
{

	private SearchForm form;
	
	public VyberPanel(String id)
	{
		super(id);
		
		setOutputMarkupId(true);
		
		add(form = new SearchForm("form"));
		
		add(new AjaxLink<Void>("places_edit")
		{
			@Override
			public void onClick(AjaxRequestTarget target)
			{
				Panel panel;
				VyberPanel.this.replaceWith(panel = new MistaPanel(VyberPanel.this.getId()));
				target.add(panel);
			}
		});
	}

	private class SearchForm extends Form<SearchForm>
	{
		protected String title;
		protected String author;
		protected Boolean mainAuthor;
		protected String publishPlace;
		protected Integer publishYearFrom;
		protected Integer publishYearTo;
		protected String publisher;
		protected String masterPrinter;
		protected String topic;
		protected String genre;

		public SearchForm(String id)
		{
			super(id);
			setDefaultModel(new CompoundPropertyModel<SearchForm>(this));
			
			add(new TextField<String>("title"));
			add(new TextField<String>("author"));
			add(new CheckBox("mainAuthor"));
			add(new TextField<String>("publishPlace"));
			add(new NumberTextField<Integer>("publishYearFrom")
					.add(new RangeValidator<Integer>(1000, 2000)));
			add(new NumberTextField<Integer>("publishYearTo")
					.add(new RangeValidator<Integer>(1000, 2000)));
			add(new TextField<String>("publisher"));
			add(new TextField<String>("masterPrinter"));
			add(new TextField<String>("topic"));
			add(new TextField<String>("genre"));
			
			add(new AjaxButton("submit")
			{
				@Override
				protected void onSubmit(AjaxRequestTarget target)
				{
					PageParameters searchParameters = searchParameters();
					
					send(getPage(), Broadcast.BREADTH,
							new SearchEvent(target, searchParameters));
				}
			});
		}
	}
	
	private PageParameters searchParameters()
	{
		PageParameters params = new PageParameters();
		
		addStringParameter(params, "title", form.title);
		
		if ( form.mainAuthor != null && form.mainAuthor )
			addStringParameter(params, "author2", form.author);
		else
			addStringParameter(params, "author", form.author);
	
		addStringParameter(params, "publishPlace", form.publishPlace);
		
		if ( form.publishYearFrom != null || form.publishYearTo != null )
		{
			StringBuilder sb = new StringBuilder("[");
			
			if ( form.publishYearFrom != null )
				sb.append(form.publishYearFrom);
			else
				sb.append("*");
			
			sb.append(" TO ");
			
			if ( form.publishYearTo != null )
				sb.append(form.publishYearTo);
			else
				sb.append("*");
			
			sb.append("]");
			
			params.add("publishDate", sb.toString());
		}
		
		addStringParameter(params, "publisher", form.publisher);
		addStringParameter(params, "masterPrinter", form.masterPrinter);
		addStringParameter(params, "topic", form.topic);
		addStringParameter(params, "genre", form.genre);
		
		return params;
	}
	
	private void addStringParameter(PageParameters params, String name, String value)
	{
		if ( value != null && !value.isEmpty() )
			params.add(name, value);
	}
	
	@Override
	public void onEvent(IEvent<?> event)
	{
		if ( event.getPayload() instanceof MistoSelectEvent )
		{
			MistoSelectEvent ev = (MistoSelectEvent) event.getPayload();
			
			form.publishPlace = ev.getNazevMista();
			form.modelChanged();
			
			ev.getTarget().add(form);
		}
	}
	
}
