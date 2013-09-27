package graph;

import waba.fx.*;

public class Annotation
{
    public static final int width = 10;
    public static final int height = 10;

    static final int xPts [] = {1, width - 2, (width - 2)/2 + 1,};
    static final int yPts [] = {1, 1, height - 2};
    int xPtsTrans [] = new int [3];
    int yPtsTrans [] = new int [3];

    public float time;
    public float value;
    public String label;
    public String text;
    public boolean selected = false;

    public Axis xaxis;
    public Object bin;

    public Annotation(String l, float t, float v, Axis xa)
    {
	time = t;
	label = l;
	value = v;
	xaxis = xa;
    }

    /*
     * Give the top left corner of where to draw
     */
    public void draw(Graphics g, int x, int y)
    {
	if(selected){
	    g.setColor(0,0,0);
	} else {
	    g.setColor(255,255,255);
	}

	g.fillRect(x,y,width,height);

	if(selected){
	    g.setColor(255,255,255);
	} else {
	    g.setColor(0,0,0);
	}

	int i;
	for(i = 0; i < 3; i++){
	    xPtsTrans[i] = xPts[i] + x;
	    yPtsTrans[i] = yPts[i] + y;
	}

	g.fillPolygon(xPtsTrans, yPtsTrans, 3);	
    }
}
