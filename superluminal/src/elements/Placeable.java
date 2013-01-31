package elements;

import java.io.Serializable;

import org.eclipse.swt.graphics.Rectangle;

public abstract class Placeable implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7043441115581049853L;
	public Rectangle rect;
	
	public Placeable () {
		rect = new Rectangle(0,0,0,0);
	}
}
