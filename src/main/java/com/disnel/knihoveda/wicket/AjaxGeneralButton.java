package com.disnel.knihoveda.wicket;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.EventPropagation;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

/**
 * General Ajax button
 * 
 * <p>If dblclick is true, requires doubleclick to activate
 * </p>
 * 
 * @author Vaclav Cermak <disnel@disnel.com>
 *
 */
public abstract class AjaxGeneralButton extends WebMarkupContainer
{
	private static final long serialVersionUID = 1L;

	public AjaxGeneralButton(String id, IModel<?> model, String event)
	{
		super(id, model);
		
		add(new AjaxEventBehavior(event)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onEvent(AjaxRequestTarget target)
			{
				onClick(target);
			}
			
			@Override
		    protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
			{
				super.updateAjaxAttributes(attributes);
				
				attributes.setEventPropagation(EventPropagation.STOP);
			}
		});
	}

	public AjaxGeneralButton(String id, String event)
	{
		this(id, null, event);
	}

	protected abstract void onClick(AjaxRequestTarget target);
	
}
