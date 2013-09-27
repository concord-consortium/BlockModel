import waba.util.*;
import waba.fx.*;

public class Layer
{
    Vector curObjects;
    int width, height;
    Object [] objArray;
    CanvasObject test;
    CanvasObject border;
    int index;
    Canvas canvas;
    int gridSpace = 1;
    int gridDist = 0;

    public Layer()
    {
	canvas = null;
	test = new CanvasObject();
	border = new CanvasObject();
	objArray = null;
	curObjects = new Vector();
    }

    CanvasObject checkObject(CanvasObject skip, CanvasObject o, 
			     int x, int y)
    {
	int i;

	if(x < 0 || y < 0 ||
	   x+o.width >= width ||
	   y+o.height >= height){
	    return border;
	}

	if(objArray == null){
	    return null;
	}

	for(i=0; i<objArray.length; i++){
	    if(((CanvasObject)objArray[i] != skip) &&
	       ((CanvasObject)objArray[i]).overlapShape(o)){
		return (CanvasObject)objArray[i];
	    }
	}
	
	return null;

    } 

    CanvasObject checkObject(CanvasObject o, int x, int y)
    {
	int i;
	CanvasObject tmpO;

	if(x < 0 || y < 0 ||
	   x+o.width >= width ||
	   y+o.height >= height){
	    return border;
	}

	if(objArray == null){
	    return null;
	}

	tmpO = o.copy();
	tmpO.x = x;
	tmpO.y = y;
	for(i=0; i<objArray.length; i++){
	    if(((CanvasObject)objArray[i] != o) &&
	       ((CanvasObject)objArray[i]).overlapShape(tmpO)){
		return (CanvasObject)objArray[i];
	    }
	}
	
	return null;
    }

    public void redraw(Graphics g, Rect rect)
    {
	int i;
	
	if(objArray == null){
	    return;
	}

	for(i=0; i<objArray.length; i++){
	    if(((CanvasObject)objArray[i]).overlapBounds(rect)){
		((CanvasObject)objArray[i]).draw(g);
	    }
	}
	
    }	

    public void redrawAll(Graphics g)
    {
	int i;
	
	if(objArray == null){
	    return;
	}

	for(i=0; i<objArray.length; i++){
	    ((CanvasObject)objArray[i]).draw(g);
	}
    }


    public boolean addObject(CanvasObject o)
    {
	int i;

	if(o.x == -1 || o.y == -1){
	    findSpace(o);
	} else {
	    o.x = (o.x+gridDist)/gridSpace*gridSpace;
	    o.y = (o.y+gridDist)/gridSpace*gridSpace;

	    if(checkObject(o,o.x,o.y) != null){
		return false;
	    }
	}
	curObjects.add(o);
	objArray = curObjects.toObjectArray();
	o.layer = this;
	o.canvas = canvas;
	o.add();
	return true;
    }

    // Should find space in a spiral out from 
    // the location
    public void findSpace(CanvasObject o)
    {
	int x,y;

	for(x=0; x<width; x+= gridSpace){
	    for(y=0; y<height; y+= gridSpace){
		if(checkObject(o,x,y) == null){
		    o.x = x;
		    o.y = y;
		    return;
		}
	    }
	}
	o.x = -1;
	o.y = -1;

    }

    public boolean removeObject(CanvasObject o)
    {
	int index = curObjects.find(o);
	if(index == -1){
	    return false;
	}
	curObjects.del(index);
	objArray = curObjects.toObjectArray();
	o.remove();
	return true;
    }

    public CanvasObject getObject(int x, int y)
    {
	CanvasObject ret;
	ret = checkObject(test,x,y);
	if(ret == border){
	    ret = null;
	}
	return ret;
    }

    public boolean rotateObject(CanvasObject o, int rot)
    {
	int x,y;
	CanvasObject oldO = o.copy();

	oldO.rotate(rot);

	x = oldO.x;
	y = oldO.y;
	if(canvas != null){
	    x = (x+gridDist)/gridSpace*gridSpace;
	    y = (y+gridDist)/gridSpace*gridSpace;
	}

	if(checkObject(o, oldO, x, y) != null){
	    return false;
	}
	o.move(rot, x, y);

	return true;
	
    }
    
    public boolean moveObject(CanvasObject oldO, int x, int y)
    {

	x = (x+gridDist)/gridSpace*gridSpace;
	y = (y+gridDist)/gridSpace*gridSpace;

	if(x == oldO.x && y == oldO.y){
	    return false;
	}

	CanvasObject o = checkObject(oldO, x, y);
	if(o == null){
	    oldO.move(x, y);
	    return true;
	}

	if(o instanceof GarbageObject &&
	   oldO.disposable){
	    oldO.move(x, y);
	    return true;
	}  

	return false;

    }

}










