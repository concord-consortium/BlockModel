import waba.ui.*;

public class TabManager extends Container
{
    Tab [] tabs;
    Container [] panes;
    int curPane;
    TabBar tBar;
    int totalHeight;
    Container curContainer;
    Container above;

    public TabManager(int width, int height,
		      Container above)
    {
	totalHeight = height;
	tabs = null;
	panes = null;
	curPane = 0;
	tBar = new TabBar();
	tBar.setRect(0,0,width,17);
	add(tBar);
	setRect(0,0,width,17);
	curContainer = null;
	this.above = above; 
    }

    public void setActiveTab(Container pane)
    {
	if(panes != null){
	    for(int i=0; i<panes.length; i++){
		if(pane == panes[i]){
		    tBar.setActiveTab(tabs[i]);
		    return;
		}
	    }
	}
	return;

    }

    public void setPos(int x,int y)
    {
	setRect(x,y,width,height);
    }

    public Container addPane(String name, Container newPane)
    {

	if(tabs == null){
	    tabs = new Tab [1];
	    panes = new Container [1];
	    curPane = 0;
	} else {
	    int i;
	    Tab [] oldTabs;
	    Container [] oldPanes;

	    oldTabs = tabs;
	    oldPanes = panes;
	    tabs = new Tab [curPane + 1];
	    panes = new Container [curPane + 1];
	    for(i=0; i<curPane; i++){
		tabs[i] = oldTabs[i];
		panes[i] = oldPanes[i];
	    }
	}
	    
	tabs[curPane] = new Tab(name);
	tBar.add(tabs[curPane]);
	if(newPane == null){
	    newPane = new Container();
	}
	panes[curPane] = newPane;
	panes[curPane].setRect(x,y+17,width,totalHeight-17);
	if(curContainer == null){
	    curContainer = panes[curPane];
	    above.add(panes[curPane]);
	    curContainer.setEnabled(true);
	}
	return panes[curPane++];
    }

    public void onEvent(Event e)
    {
	int i;

	if(e.type == ControlEvent.PRESSED){
	    for(i=0; i<curPane; i++){
		if(e.target == tabs[i]){
		    if(curContainer != null){
			above.remove(curContainer);
			curContainer.setEnabled(false);
		    }
		    above.add(panes[i]);
		    curContainer = panes[i];
		    curContainer.setEnabled(true);
		    return;
		}
	    }
	}
    }
}





