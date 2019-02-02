package com.disnel.knihoveda.wicket;

import org.wicketstuff.openlayers3.api.style.Fill;
import org.wicketstuff.openlayers3.api.style.Stroke;
import org.wicketstuff.openlayers3.api.style.Style;
import org.wicketstuff.openlayers3.api.style.Text;
import org.wicketstuff.openlayers3.api.util.Color;

public class TiskyStyle extends Style
{
	private static final long serialVersionUID = 1L;

	public static Fill FILL = new Fill(new Color(0, 0, 0));
	public static Stroke STROKE = new Stroke(new Color(0, 0, 0));
	
	public TiskyStyle(String pocet)
	{
		super();
		
		setFill(FILL);
		setStroke(STROKE);
		
		Text text = new Text(pocet, FILL);
		text.setFont("small-caps bold 16px/1 sans-serif");
		setText(text);
	}
	
    @Override
    public String renderAttributesJs() {

        StringBuilder builder = new StringBuilder();

        builder.append(super.renderAttributesJs());
        
        // TODO

        return builder.toString();
    }

}
