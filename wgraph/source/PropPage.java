package graph;

import waba.ui.*;
import waba.util.*;

public class PropPage extends Container
{
    public Vector props = new Vector();
    int curYpos = 2;
    PropObject po;

    public final static int UPDATE = 1;
    public final static int REFRESH = 2;

    public PropPage(PropObject o)
    {
	po = o;
    }

    public void addEdit(String label, int fieldLength)
    {
	Label tmpLabel;
	Edit tmpEdit;

	tmpLabel = new Label(label);
	tmpLabel.setRect(2,curYpos,65,17);
	add(tmpLabel);
	
	tmpEdit = new Edit();
	tmpEdit.setRect(70,curYpos, fieldLength, 17);
	add(tmpEdit);
	props.add(tmpEdit);
	
	curYpos += 19;
    }

    public void showProp()
    {
	PropWindow pwin = new PropWindow();
	pwin.showProp(po, this);
    }
}
