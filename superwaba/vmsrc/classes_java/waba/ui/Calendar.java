package waba.ui;

import waba.ui.*;
import waba.fx.*;
import waba.sys.*;
import waba.util.*;

/**
 The Calendar class is a mimic of the palms Calendar.  It looks and functions the basically the same.
 It uses the Date class for all operations.<br>
 Guich made some modifications to it act like an Window. Instead of create a new instance (which consumes memory), you may
 use the Edit's static field <i>calendar</i>
 
 <b>Added by Allan C. Solomon</b>
 @version 1.0 16 Aug 2000
 @author Allan C. Solomon
*/

public class Calendar extends Window
{

 private int day=-1;
 private int month;
 private int year;
 private int sentDay=0;
 private int sentMonth;
 private int sentYear;
 private final int CELLW = 16;
 private final int CELLH = 16;
 private final int CELLX0 = 6;
 private final int CELLY0 = 28;
/**
 Constructs Calendar set to the current day.
*/ 
 public Calendar()
 {
   super("",true); // no title yet
   setDoubleBuffer(true);
   setRect(CENTER,CENTER,CELLX0*2+CELLW*7,CELLY0+CELLX0+CELLH*6); // same gap in all corners
 } 
 private void drawArrow(Graphics g, int x, int y, boolean leftArrow, boolean enabled)
 {
	if (enabled)
	   g.setColor(255,255,255);
	else
	   g.setColor(0,0,0);
	y += 5;
	if (!leftArrow) x += 5;
	for (int i = 1; i <= 6; i++, x += (leftArrow?1:-1))
	{
	   int r = i*2-1;
	   if (enabled)
		  g.drawLine(x,y-r/2,x,y+r/2);
	   else
		  g.drawDots(x,y-r/2,x,y+r/2);
	}
 } 
/**
 Returns selected Date.
 
 @returns Date object set to the selected day.
*/ 
 public Date getSelectedDate()
 {
  if (day==-1){return null;}
  Date date = new Date(day,month,year);
  return date;
 } 
 public void onEvent(Event event) 
 {
   int rightArrowX = width-8;
   int leftArrowX = width-8-8-4;
   if (event.type == KeyEvent.KEY_PRESS && (((KeyEvent)event).key == IKeys.KEYBOARD_ABC || ((KeyEvent)event).key == IKeys.KEYBOARD_123)) // closes this window without selecting any day
	  unpop();
   else
  if (event.type == PenEvent.PEN_DOWN)
   {
	PenEvent penEvent = (PenEvent)event;
	if ((penEvent.y >=CELLY0) && ((penEvent.x >=CELLX0)&&(penEvent.x <= (width-CELLX0*2)))) // in dates?
	{    	
	 	int col = ((penEvent.x -CELLX0) / CELLW);
		int row = ((penEvent.y-CELLY0) / CELLH); 
	 	Date date = new Date(1,month,year);
	 	int d = ((row * 7) - date.getDayOfWeek()) + (col+1);     	     	
	 	if (d <= date.getDaysInMonth()){
		 Graphics g = createGraphics();
		 g.fillRect(CELLX0 +(col * CELLW),CELLY0+(row * CELLH),CELLW,CELLH);        
		 g.setColor(255,255,255);
		 g.drawText(Convert.toString(d),CELLX0+2 +(col * CELLW),CELLY0+(row * CELLH)); 
		}
	}
   else if  (penEvent.y <=13) // in title?
	{
	Graphics g = createGraphics();
	if ((penEvent.x >= leftArrowX) && (penEvent.x <= leftArrowX+6))
	 {
	  drawArrow(g,leftArrowX,1,true,false); // g.drawImage(gleftArrow,140,1);  
	 }
	if ((penEvent.x >=rightArrowX) && (penEvent.x <= rightArrowX+6))
	 {
	 	drawArrow(g,rightArrowX,1,false,false); // g.drawImage(grightArrow,153,1);
	 }
	}
   } 
  else if (event.type == PenEvent.PEN_UP)
   {
	PenEvent penEvent = (PenEvent)event;
	if (penEvent.y <=13)
	 {
	 	if ((penEvent.x >=leftArrowX) && (penEvent.x <=leftArrowX+6))
	 	{
	 	 month--;
	 	 if (month== 0)	{year--;month=12;}
	   repaint();
		} else     	
	 	if ((penEvent.x >=rightArrowX) && (penEvent.x <=rightArrowX+6))
	 	{
	 	 month++;
	 	 if (month==13){month=1;year++;}     	 
	   repaint();
	 	}
	 }
	else if ((penEvent.y >=CELLY0) && ((penEvent.x >=CELLX0)&&(penEvent.x <= (width-CELLX0*2))))
	{
		Date date = new Date(1,month,year);
		day = -1;
	 	int row = ((penEvent.y-CELLY0) / CELLH); 
	 	int col = ((penEvent.x -CELLX0) / CELLW)+1;
	 	int selectedDay = ((row * 7) - date.getDayOfWeek()) + col;
	 	if ((selectedDay > date.getDaysInMonth())||(selectedDay < 1)){repaint();return;}
	 	day = selectedDay;
	 	unpop(); // closes this window
	}
   }
 } 
 public void onPaint(Graphics g)
 {
  //Draw title with appropriote month and year
  paintTitle(Date.getMonthName(month) + " " + year,g);
  
  //Draw arrows
  int rightArrowX = width-8;
  int leftArrowX = width-8-8-4;
  drawArrow(g,leftArrowX,1,true,true); // g.drawImage(leftArrow,140,1);
  drawArrow(g,rightArrowX,1,false,true); // g.drawImage(rightArrow,153,1);
	
  g.setColor(0,0,0);
  //Draw day of week labels
  g.setFont(new Font("Helvetica", Font.BOLD, 14));
  String[] s = {"S","M","T","W","T","F","S"};
  int xPos = CELLX0;
  int yPos = CELLY0-1;
  FontMetrics fm = getFontMetrics(new Font("Helvetica", Font.BOLD, 14));
  for(int x = 0;x<s.length;x++)
  {
	g.drawText(s[x],xPos+((CELLW-fm.getTextWidth(s[x]))/2),CELLY0/2);
	xPos+=CELLW;
  }
  g.setFont(new Font("Helvetica", Font.PLAIN, 12));
  
  
  //Draw horz. lines
  for(int i=0;i<7;i++)
  {
	g.drawLine(CELLX0,yPos,CELLX0+7*CELLW,yPos);
	yPos+=CELLH;
  }
  
  //Draw Vert. lines
  xPos = CELLX0;
  for(int i=0;i<8;i++)
  {
	g.drawLine(xPos,CELLY0-1,xPos,6*CELLH+CELLY0-1);
	xPos+=CELLW;
  }
  
  //Draw dates out
  Date date = new Date(1,month,year);
  int cPos = date.getDayOfWeek()+1;
  g.setColor(0,0,0);
  yPos = CELLY0;
  xPos = CELLX0+2 + (CELLW*(cPos-1));
  
  for(int i=1;i<=date.getDaysInMonth();i++)
  {
   g.drawText(Convert.toString(i),xPos,yPos);
   if (cPos%7==0){yPos+=CELLH;xPos=CELLX0+2;cPos=1;}
   else {xPos+=CELLW;cPos++;}
  }
  
  //check for and select current day
  
  int d;
  if (sentDay==0){d = date.getDay();}
  else {d=sentDay;} 
  date = new Date(1,month,year);
  
  if ((date.getMonth() == sentMonth) && (date.getYear() == sentYear)) {
   int col = ((d + date.getDayOfWeek()) % 7)-1;   
   int row = (d + date.getDayOfWeek()) / 7;   
   if (col ==-1){col=6;row--;}
   if (d >9){g.fillRect(CELLX0 +(col * CELLW),CELLY0+(row * CELLH),12,11);}
   else {g.fillRect(CELLX0 +(col * CELLW),CELLY0+(row * CELLH),10,11);}
   g.setColor(255,255,255);
   g.drawText(Convert.toString(d),CELLX0+2+(col*CELLW),CELLY0+(row*CELLH));
  }
  g.setFont(new Font("Helvetica", Font.PLAIN, 12));
  g.setColor(0,0,0); 
 } 
 protected void onPopup()
 {
	day = -1;
	setTitle(Date.getMonthName(month) + " " + (new Date().getYear()));
	setSelectedDate(null);
 } 
 /** sets the current day to the Date specified. if its null, sets the date to today. */
 public void setSelectedDate(Date d)
 {
   if (d == null) d = new Date();
   sentMonth = month = d.getMonth();
   sentYear = year = d.getYear();
   sentDay = d.getDay();
 } 
}