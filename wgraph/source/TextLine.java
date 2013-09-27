package graph;

import waba.fx.*;
import waba.ui.*;
import extra.util.Maths;


public class TextLine extends Object {
    // Constant
    public final static int RIGHT = 0;
    public final static int LEFT = 1;
    public final static int UP = 2;
    public final static int DOWN = 3;


    public final static int RIGHT_EDGE = 0;
    public final static int LEFT_EDGE = 1;
    public final static int TOP_EDGE = 2;
    public final static int BOTTOM_EDGE = 3;

    public int minDigits = 1;
    public int maxDigits = 4;

  /**
   * Font to use for text
   */
    public Font font     = null;
    public FontMetrics fontMet = null;
  /**
   * Text color
   */
    public Color color   = null;
  /**
   * Background Color
   */
    public Color background   = null;
    
    

  /**
   * The text to display
   */
    public  String text   = null;

    /**
     * the text direction
     */
    public int direction = 0;

    protected boolean cleared = false;
    protected int width = 0;
    protected int height = 0;
    protected int textWidth = 0;
    protected int textHeight = 0;

    public int edge;

    public TextLine(String s, Font f) {
	text = s;
	font = f;
	fontMet = MainWindow.getMainWindow().getFontMetrics(font);
	parseText();	
    }

    public TextLine(String s){
	this(s, MainWindow.defaultFont);
	font = null;
    }

    public TextLine(String s, Font f, Color c) {
	this(s, f);
	color = c;
    }

    public TextLine(String s, int d){
	this(s);
	font = null;
	direction = d;
    }

    

      /**
   * Convert float with correct digits
   */
  public String fToString(float val)
  {
    // We don't want any exponents printed so we need to do this ourselves.
        int j;
	char intChars[];
	char fltChars[];
	int len, nLen;
	int start=0, end;
	int exp=0;
	int multExp;
	int count;
	String absLabel;

	for(j=0; j<maxDigits; j++) val *= (float)10;
	if(val < 0) val -= (float)0.5;
	else val += (float)0.5;

	if(((int)val) == 0){
	    if(minDigits != 0)
		return new String("0.0");
	    else
		return new String("0");
	}

	intChars = String.valueOf((int)Maths.abs(val)).toCharArray();
	len = intChars.length;

	if(len <= maxDigits){
	    fltChars = new char[maxDigits + 2];
	    fltChars[0] = '0';
	    fltChars[1] = '.';
	    for(j=0; j < maxDigits - len; j++){
		fltChars[2+j] = '0';
	    }
	    start = 2+j;
	    for(j=0; j < len; j++)
		fltChars[start + j] = intChars[j];
	} else {
	    fltChars = new char [len + 1];
	    for(j=0; j < len - maxDigits; j++){
		fltChars[j] = intChars[j];
	    }
	    fltChars[j] = '.';
	    for(; j < len; j++)
		fltChars[j + 1] = intChars[j];
	}

	end = fltChars.length - 1;
	for(j=0; j < maxDigits - minDigits; j++){
	    if(fltChars[end - j] != '0') break;
	}
	

	absLabel = new String(fltChars, 0, fltChars.length - j);

	if(val < 0)
	    return new String("-" + absLabel);
	else 
	    return absLabel;

  }    



    public boolean  parseText()
    {
	textWidth = fontMet.getTextWidth(text);
	textHeight = fontMet.getHeight();
	if(direction < 2){
	    width = textWidth;
	    height = textHeight;
	} else {
	    height = textWidth;
	    width = textHeight;
	}

	return true;
    }

    public void setText(String s)
    {
	text = s;
	parseText();
    }

    /* Draw starting at the upper left hand corner
     */

    public void drawRight(JGraphics g, int x, int y)
    {

	if(background != null && !cleared) {
	    g.setColor(background);
	    g.fillRect(x, y, textWidth, textHeight);
	}

	if(font != null) g.setFont(font);
	if(color != null) g.setColor(color);

	g.drawString(text, x, y);

	cleared = false;
	return;
    }

    public void draw(JGraphics g, int x, int y)
    {
	Image offsI = null;
	Image rotImage = null;
	JGraphics offsG = null;
	Graphics rotG = null;

	if(direction == RIGHT){
	    drawRight(g, x, y);
	    return;
	}

	offsI = new Image(textWidth, textHeight);
	offsG = new JGraphics((ISurface)offsI);

	offsG.setFont(g.getFont());
	offsG.setColor(g.getColor());

	drawRight(offsG, 0, 0);

	rotImage = new Image(width, height);
	rotG =  new Graphics(rotImage);
	rotateImage(offsI, rotG);
	rotG.free();
	offsG.dispose();
	offsI.free();

	g.drawImage(rotImage, x, y);
	rotImage.free();
    }

    public void clear(JGraphics g, int x, int y)
    {
	if(cleared == false && background != null){
	    g.setColor(background);
	    g.fillRect(x,y,width,height);
	    cleared = true;
	}
    }

    public void drawCenter(JGraphics g, int x, int y, int edge)
    {
	int x0, y0;

	switch(edge){
	case RIGHT_EDGE:
	    x0 = x - width - 1;
	    y0 = y - height/2;
	    break;
	case LEFT_EDGE:
	    x0 = x;
	    y0 = y - height/2;
	    break;
	case TOP_EDGE:
	    x0 = x - width/2;
	    y0 = y;
	    break;
	case BOTTOM_EDGE:
	default :
	    x0 = x - width/2;
	    y0 = y - height - 1;
	    break;
	}

	draw(g, x0, y0);
    }

    public int getXOffset(int edge)
    {

	switch(edge){
	case RIGHT_EDGE:
	    return -width - 1;
	case LEFT_EDGE:
	    return 0;
	case TOP_EDGE:
	case BOTTOM_EDGE:
	default :
	    return  -(width/2);
	}
    }

    public int getYOffset(int edge)
    {
	switch(edge){
	case RIGHT_EDGE:
	case LEFT_EDGE:
	    return  -(height/2);
	case TOP_EDGE:
	    return 0;
	case BOTTOM_EDGE:
	default :
	    return -height - 1;
	}
    }

    public void rotateImage(Image srcImg, Graphics destG) 
    {
	int x, y;
	int tmpOffset;

	switch(direction){
	case UP:
	    tmpOffset = height - 1;
	    for(y = 0 ; y < textHeight; y++) {
		for(x = 0; x < textWidth; x++) {
		    destG.copyRect(srcImg, x, y, 1, 1, y, tmpOffset - x);
		}
	    }
	    break;
	case DOWN:
	    tmpOffset = width - 1;
	    for(y =0; y < textHeight; y++) {
		for(x = 0; x < textWidth; x++){
		    destG.copyRect(srcImg, x, y, 1, 1, tmpOffset - y, x);
		}
	    }
	    break;
	case LEFT:
	    for(y = 0; y < textHeight; y++) {
		for( x=0; x< textWidth; x++){
		    destG.copyRect(srcImg, x, y, 1, 1, width - x - 1, height - y - 1); 
		}
	    }
	    break;
	default:
	}
	
	return;

    }

}













