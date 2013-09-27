package graph;

import waba.ui.*;

public class PropWindow extends Window
{
    Button set;
    Button cancel;
    JustifiedContainer jc;

    public PropWindow()
    {
	super("Settings", true);
	setRect(CENTER,TOP, 150, 150);

	jc = new JustifiedContainer();
	jc.setColumnCount(2);
	jc.setGaps(0,4,0);
	jc.setJustify(0, LEFT, CENTER);
	
	jc.add(0,set=new Button("Set"),PREFERRED,PREFERRED);

	jc.add(1,cancel=new Button("Cancel"),PREFERRED,PREFERRED);

	jc.setRect(0,height-20,width,20);
	add(jc);

    }

    PropObject po;
    PropPage pp;

    public void showProp(PropObject po, PropPage pp)
    {
	this.po = po;
	this.pp = pp;

	pp.setRect(3,15,width, height-40);
	po.updateProp(pp, PropPage.REFRESH);
	add(pp);
	MainWindow.getMainWindow().popupModal(this);

    }

    public void onEvent(Event e)
    {
	if(e.type == ControlEvent.PRESSED){
	    if(e.target == cancel){
		po = null;
		unpop();
	    } else {
		if(po != null){
		    po.updateProp(pp, pp.UPDATE);
		    unpop();
		}
	    }
	}
    }
}






