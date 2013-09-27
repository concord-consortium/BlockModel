package waba.ui;

import waba.ui.*;
import waba.fx.*;
import waba.sys.*;

/** group of pushbuttons in just one control. created by guich from the original 
    WExtras class Pushbutton of Stefan Kellner. */

public class PushButtonGroup extends Control
{
   /** normal: only one selected at a time */
   public static final byte NORMAL = 0;
   /** the button will be selected and de-selected immediatly, acting like an real button */
   public static final byte BUTTON = 1;
   /** one click in the button will select it and another click will de-select it. perhaps, only one button can be clicked at a time */
   public static final byte CHECK = 2;
   protected String []names;
   protected int widths[];
   protected int selectedIndex = -1;
   protected int gap;
   protected int insideGap;
   protected int rows, cols;
   protected boolean atLastOne,actLikeButton,actLikeCheck;
   private int cellH;
   private int lastSel=-1;
   /** not used. can be useful if you have a group of PushButtonGroup and want to know easily which group was pressed. Used in the Keyboard class. */
   public int id;
   public int maxWidth=-1;

   /** create the buttons.
       @param names captions of the buttons
       @param atLastOne if true, at least one button must be selected
       @param selected default index to appear selected, or -1 if none
       @param gap space between the buttons, -1 glue them.
       @param insideGap space between the text and the button border. the ideal is 6.
       @param rows if > 1, creates an button matrix
       @param allSameWidth if true, all the buttons will have the width if the most large one.
       @param type can be NORMAL, BUTTON or CHECK
   */
   public PushButtonGroup(String[] names, boolean atLastOne, int selected, int gap, int insideGap, int rows, boolean allSameWidth, byte type)
   {
      this.names = names;
      this.insideGap = insideGap;
      this.atLastOne = atLastOne;
      this.actLikeButton = type == BUTTON;
      this.actLikeCheck = type == CHECK;
      // computes the best width for all controls
      widths = new int[names.length];
      maxWidth=-1;
      for (int i =0; i < names.length; i++)
      {
         widths[i] = fm.getTextWidth(names[i])+insideGap;
         if (allSameWidth) maxWidth = Convert.max(maxWidth,widths[i]);
      }
      ////
      this.selectedIndex = selected;
      this.gap = gap;
      if (rows < 1) rows = 1;
      this.rows = rows;
      cols = names.length / rows;
   }
   private int getControlWidth(int ind)
   {
      return (maxWidth == -1)?widths[ind]:maxWidth;
   }
   public void onBoundsChanged()
   {
      cellH = height / rows;
   }

   /** returns the index of the selected button */
   public int getSelected()
   {
      return selectedIndex;
   }
   public int getPreferredWidth()
   {
      int wc = 0;      
      int w = 0;
      for (int i =0; i < names.length; i++)
      {
         wc += getControlWidth(i)+gap;
         if (i != 0 && ((i+1)%cols == 0))
         {
            wc -= gap;
            w = (wc > w)?wc:w; 
            wc = 0;
         }
      }
      w = (wc > w)?wc:w;
      return w+1;  
   }
  
   public int getPreferredHeight()
   {
      return fm.getHeight()*rows;
   }
   
   public void onPaint(Graphics g)
   {
      drawComponent(g);
      if (selectedIndex != -1) drawCursor(g,selectedIndex);
   }

   protected void drawComponent(Graphics g)
   {
      int x = 0;
      int y = 0;
      g.setColor(255,255,255);
      g.fillRect(0,0,width,height);
      for (int i =0; i < names.length; i++)
      {
         int w = getControlWidth(i);
         g.setColor(0, 0, 0);
         g.drawRect(x,y,w,cellH);
         g.setClip(x+1,y+1,w-2,cellH-2);
         int sw = fm.getTextWidth(names[i]);
         g.drawText(names[i], x+insideGap/2+(w-widths[i])/2, y); // if allSameWidth, center the label in the button
         g.clearClip();
         x += w + gap;
         if ((i+1)%cols == 0)
         {
            x = 0;
            y += cellH+gap;
         }
      }
   }

   /** sets the selected button index */
   public void setSelected(int ind)
   {
      int min = atLastOne?0:-1;
      if ((actLikeCheck || selectedIndex != ind) && min <= ind && ind < names.length)
      {         
         Graphics g = createGraphics();
         if (g == null) return;
         if (selectedIndex >= 0) drawCursor(g,selectedIndex); // remove from the last
         selectedIndex = ind;
         if (selectedIndex >= 0) drawCursor(g,ind); // draw on the new            
         g.free();
      }
   }
   private void drawCursor(Graphics g, int ind)
   {
      Rect r = getRect(ind);
      g.drawCursor(r.x,r.y,r.width,r.height);
   }
   
   /** returns the client rect of the component ind */
   private Rect getRect(int ind)
   {
      int x = 0;
      int y = 0;
      for (int i =0; i < ind; i++)
         if ((i+1) % cols == 0) 
         {
            x = 0;
            y += cellH+gap;
         } else x += getControlWidth(i) + gap;
      return new Rect(x+1,y+1,getControlWidth(ind)-2,cellH-2);
   }

   private int findButtonAt(int px, int py)
   {
      if (0 <= px && px <= width && 0 <= py && py <= height)
      {
         int x = 0;
         int r = py / cellH;
         for (int i =0; i < names.length; i++)
         {
            int w = getControlWidth(i);
            if (x <= px && px <= x+w)
               return r*cols+ (i%cols);
            if ((i+1) % cols == 0) 
               x = 0;
            else
               x += w + gap;
         }
      }
      return -1;
   }

   public void onEvent(int type, int x, int y)
   {
      int sel = findButtonAt(x,y);
      switch (type)
      {
         case PenEvent.PEN_MOVE:
         case PenEvent.PEN_DRAG:
         case PenEvent.PEN_DOWN:
            if (sel != selectedIndex && (!atLastOne || sel != -1))
               setSelected(sel);
            break;
         case PenEvent.PEN_UP:
            if (!atLastOne || sel != -1)
               postEvent(new ControlEvent(ControlEvent.PRESSED,this));
            if (actLikeCheck)
            {
               if (lastSel != selectedIndex)
                  lastSel = selectedIndex;
               else
               {
                  lastSel = -1;
                  setSelected(-1); 
               }
            } else
            if (actLikeButton || (sel == -1 && !atLastOne)) {Vm.sleep(150); setSelected(-1);}
            break;
      }
   }
   
   public void onEvent(Event event)
   {
      if (event instanceof PenEvent)
         onEvent(event.type, ((PenEvent)event).x,((PenEvent)event).y);
   }
}