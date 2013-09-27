package graph;
import waba.fx.*;

public class ColorAxis extends Axis
{
	
    public ColorAxis(float minValue, float max, int len)
    {
	super(minValue, max, len);
    }

    public void setTempColor(Graphics graphics, float temp)
    {
        int percent;
	int r, g, b;
	r = 0;
	b = 0;
	g = 0;


	percent = (int)((temp - dispMin)*1000/ (length/scale ));
	setBarColor(graphics, percent);
    
    }

    // Notice this used 10*percent
    void setBarColor(Graphics graphics, int percent)
    {
	int r, g, b;
	r = 0;
	b = 0;
	g = 0;

	if(percent < 250){
	    b = 255;
	    g = (255 * percent) / 250;
	} else if(percent < 500){
	    g = 255;
	    b = (255 * (500 - percent)) / 250;
	} else if(percent < 750){
	    g = 255;
	    r = (255 * (percent - 500)) / 250;
	} else {
	    r = 255;
	    g = (255 * (1000 - percent)) / 250;
	} 
	
	graphics.setColor(r, g, b);

	return;
    }

}
