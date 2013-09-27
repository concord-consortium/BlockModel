import waba.ui.*;
import waba.fx.*;
import waba.io.*;
import waba.sys.*;
import waba.util.*;

public class BlockModel extends MainWindow
{
    final static int ENTRY_TOP = 190;

    boolean firstTime = true;
    Graphics mwG = null;
    Canvas modelCanvas;
    Canvas addCanvas;
    Canvas dataCanvas;
    PSConnection connection;
    Layer modelLayer;
    Layer probeLayer;
    Button rotate;
    Button discover;
    Button flip, load, save, set;
    Label status;
    ThermalPlane tp = null;
    TabManager tabMan;
    OpenFileWindow opWin;
    String curFileName = "untitled.dnm";

    ModelPage mp;
    DataPage dp;

    public void onStart()
    {
	Container current;
	Layer layer;

	opWin = new OpenFileWindow();

	// set size
	setRect(0,0,240,290);

	// add a canvas
	tabMan = new TabManager(238,270, this);
	tabMan.setPos(1,1);

	mp = new ModelPage();
	mp.bm = this;
	tabMan.addPane("Model", mp);

	dp = new DataPage();
	dp.bm = this;
	tabMan.addPane("Data", dp);

	add(tabMan);

	status = new Label("Status Bar Just Testing");
	status.setRect(1, 260, 150, 14);
	add(status);

	rotate = new Button("Rotate");
	rotate.setRect(1,278,37,14);
	add(rotate);

	flip = new Button("Flip");
	flip.setRect(39,278,35,14);
	add(flip);
	
	set = new Button("Set");
	set.setRect(77, 278, 30, 14);
	add(set);

	save = new Button("Save");
	save.setRect(123, 278, 35, 14);
	add(save);
	
	load = new Button("Load");
	load.setRect(160, 278, 35, 14);
	add(load);


    }

    public BlockModel()
    {
    }

    /*
     * Write all the objects
     */
    public void writeExt(DataStream ds)
    {
	if(tabMan.curContainer instanceof DataPage){
	    ((DataPage)tabMan.curContainer).writeExt(ds);
	} else if(tabMan.curContainer instanceof ModelPage){
	    ((ModelPage)tabMan.curContainer).writeExt(ds);
	}
    }

    public void readExt(DataStream ds)
    {
	Container newCont;

	String name = ds.readString();
	if(name.equals("DataPage")){
	    newCont = new DataPage();
	    tabMan.addPane("Data", newCont);
	    ((DataPage)newCont).readExt(ds);
	} else if(name.equals("ModelPage")){
	    newCont = new ModelPage();
	    tabMan.addPane("Model", newCont);
	    ((ModelPage)newCont).readExt(ds);
	}

    }

    public void onExit()
    {
    }

    public void load(String dirName, String fileName)
    {
	if(fileName != null){
	    DataStream ds = new DataStream(new File(dirName + fileName, File.READ_WRITE));
	    readExt(ds);
	    ds.close();
	    curFileName = fileName;
	} else {
	}
    }

    public void save(String dirName, String fileName)
    {
	if(dirName != null && fileName != null){
	    File createFile = new File(dirName + fileName, File.CREATE);
	    createFile.close();

	    DataStream ds = new DataStream(new File(dirName + fileName, File.READ_WRITE));
	    writeExt(ds);
	    ds.close();
	    
	    curFileName = fileName;
	}

    }

    public void setStatus(String text)
    {
	Graphics g = createGraphics();
	g.setColor(255,255,255);
	g.fillRect(1,260, 150, 14);
	status.setText(text);
	status.repaintNow();
	g.free();
    }

    public void onEvent(Event e)
    {
	if(e.type == ControlEvent.PRESSED){
	    Control target = (Control)e.target;
	    if(target == rotate && Canvas.selected != null){
		Canvas.selected.canvas.rotateSelected(1);
		setFocus(Canvas.selected.canvas);
	    } else if(target == flip && Canvas.selected != null){
		Canvas.selected.canvas.rotateSelected(2);
		setFocus(Canvas.selected.canvas);		
	    } else if(target == set && Canvas.selected != null){
		Canvas.selected.canvas.propSelected();
	    } else if(target == save){
		opWin.save(this, curFileName);    
	    } else if(target == load){
		opWin.load(this);
	    } else if(target == discover){
		connection.discover();
	    } 
	} 

    }    
}


















