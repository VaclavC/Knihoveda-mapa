package com.disnel.knihoveda.wicket.model;

import java.util.List;

import org.apache.wicket.model.LoadableDetachableModel;

import com.disnel.knihoveda.dao.SolrDAO;
import com.disnel.knihoveda.mapa.data.ResultsInPlace;

public class ResultsInPlacesModel extends LoadableDetachableModel<List<ResultsInPlace>>
{

	@Override
	protected List<ResultsInPlace> load()
	{
		return SolrDAO.loadResultsInPlaces();
	}

}
