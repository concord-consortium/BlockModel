import waba.ui.*;
import waba.fx.*;
import waba.io.*;
import graph.*;

public class ModelPage extends Container
{
    boolean firstTime = true;
    Graphics mwG = null;
    Canvas modelCanvas;
    Canvas addCanvas;
    GraphView gv;
    Layer modelLayer;
    Layer probeLayer;
    static float maxTemp = 40;
    static float minTemp = 10;
    ThermalPlane tp = null;
    PushButtonGroup selector, modControl;
    Control curControl;
    BlockModel bm;

    // Common to all pages
    String curFileName = "untitled.dnm";

    public ModelPage()
    {
	Layer layer;

	// add a canvas
	modelCanvas = new Canvas(236, 129, 2);
	modelCanvas.live = true;
	modelCanvas.setPos(2,110);
	
	modelLayer = modelCanvas.getLayer(0);
	modelLayer.gridSpace = 7;
	modelLayer.gridDist = 3;

	probeLayer = modelCanvas.getLayer(1);
	probeLayer.gridSpace = 3;
	probeLayer.gridDist = 1;

	modelCanvas.addObject(new GarbageObject(), modelLayer, 10, 90);

	// create the thermal plane
	tp = new ThermalPlane(modelCanvas, 7, 7, 236, 146);
	modelCanvas.tp = tp;
	add(modelCanvas);


	String names [] = new String [2];
	names [0] = "Graph";
	names [1] = "Setup";
	selector = new PushButtonGroup(names, true, 0, 4, 4, 2, true, PushButtonGroup.NORMAL); 
	selector.setRect(1,2,34,30);
	add(selector);

	names = new String [2];
	names [0] = "Start";
	names [1] = "Stop";
	modControl = new PushButtonGroup(names, true, 1, 4, 4, 2, true, PushButtonGroup.NORMAL); 
	modControl.setRect(1,60,34,30);
	add(modControl);
	
	gv = new GraphViewBar(195, 105);
	gv.setPos(35,2);
	modelCanvas.gv = gv;
	modelCanvas.graph = gv.graph;
	modelCanvas.axis = ((BarGraph)(gv.graph)).yaxis;
	add(gv);
	curControl = gv;

	// add a canvas
	addCanvas = new Canvas(190, 105, 1);
	addCanvas.setPos(42,2);
	addCanvas.gv = gv;
	addCanvas.graph = gv.graph;
	addCanvas.axis = modelCanvas.axis;

	layer = addCanvas.getLayer(0);
	ModelObject mo = new ModelObject(null, 14, 42);
	mo.specHeat = (float)1.0;
	mo.conduct = (float)12.5;
	mo.initTemp = 25;
	mo.dragAction = mo.EXT_DRAG_COPY;
	// the order is important here that should be fixed
	addCanvas.addObject(mo, layer);

	mo = new ModelObject(null, 14, 14);
	mo.specHeat = 0;
	mo.conduct = 1;
	mo.initTemp = 40;
	mo.dragAction = mo.EXT_DRAG_COPY;
	addCanvas.addObject(mo, layer);

	mo = new ModelObject(null, 14, 14);
	mo.specHeat = 0;
	mo.conduct = 1;
	mo.initTemp = 10;
	mo.dragAction = mo.EXT_DRAG_COPY;
	addCanvas.addObject(mo, layer);

	VProbeObject vProbe = new VProbeObject(MainWindow.defaultFont, "A");
	vProbe.dragAction = vProbe.EXT_DRAG_COPY;
	vProbe.targetLayerIndex = 1;
	// the order is important here that should be fixed
	vProbe.rotate(-1);
	addCanvas.addObject(vProbe, layer);

	vProbe = new VProbeObject(MainWindow.defaultFont, null);
	vProbe.dragAction = vProbe.EXT_DRAG_COPY;
	vProbe.targetLayerIndex = 1;
	vProbe.rotate(-1);
	addCanvas.addObject(vProbe, layer);

	// add thermal plane control
	tp.graph = gv;
    }

    /*
     * Write all the objects
     */
    public void writeExt(DataStream ds)
    {
	ds.writeString("ModelPage");
	modelCanvas.writeExt(ds);

    }

    public void readExt(DataStream ds)
    {
	remove(modelCanvas);
	modelCanvas.removeAll();
	modelCanvas = new Canvas();
	modelCanvas.tp = tp;
	modelCanvas.graph = gv.graph;
	modelCanvas.axis = ((LineGraph)(gv.graph)).yaxis;
	tp.canvas = modelCanvas;
	modelCanvas.readExt(ds);
	add(modelCanvas);
	modelCanvas.onPaint(null);
	// readCanvasObjs(modelCanvas, ds);
    }

    public void onPaint(Graphics g)
    {
	tp.updateObj();
	System.out.println("Got onPaint");
    }
  
    public void onEvent(Event e)
    {
	if(e.type == ControlEvent.PRESSED){
	    Control target = (Control)e.target;
	    int index;
	    if(target == selector){
		index = selector.getSelected();
		if(index == 0 && curControl != gv){
		    remove(curControl);
		    curControl.setEnabled(false);
		    add(gv);
		    curControl = gv;
		    curControl.setEnabled(true);
		} else if(index == 1 && curControl != addCanvas){
		    remove(curControl);
		    curControl.setEnabled(false);
		    add(addCanvas);
		    curControl = addCanvas;
		    curControl.setEnabled(true);
		}
		
	    } else if(target == modControl){
		index = modControl.getSelected();
		if(index == 0){
		    tp.start();
		} else {
		    tp.stop();
		}
	    }
	} 

    }     
}











