import waba.fx.*;
import waba.ui.*;
import waba.io.*;

class GarbageObject extends CanvasObject
{
    Image trash;

    public GarbageObject(){
	trash = new Image("trash.bmp");
	width = trash.getWidth();
	height = trash.getHeight();
    }

    public void writeExt(DataStream ds)
    {
	ds.writeString("GarbageObject");
	super.writeExt(ds);
    }

    public void interact(CanvasObject o, int action)
    {
	if(o == (CanvasObject)this){
	    return;
	}

	if(o.disposable && 
	   overlapShape(o) && action == MOVE &&
	   o.canvas == canvas){
	    // this guy is overlapping us
	    canvas.removeObject(o);
	}

    }

    public void draw(Graphics g){
	g.drawImage(trash,x,y);
    }




}
