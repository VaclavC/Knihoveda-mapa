package com.disnel.knihoveda.mapa;

import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import com.disnel.knihoveda.wicket.model.SolrFieldValuesModel;
import com.googlecode.wicket.kendo.ui.form.multiselect.MultiSelect;

public class VyberDlePole extends Panel
{

	private String poleId;
	
	public VyberDlePole(String id, String poleId)
	{
		super(id);
		
		this.poleId = poleId;
		
		add(new Label("titul", poleId));
		
		Form<Void> form;
		add(form = new Form<Void>("form"));
		
		form.add(new MultiSelect<Count>("select",
				new SolrFieldValuesModel(this.poleId)));
	}

}
