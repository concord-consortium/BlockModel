package waba.ui;

import waba.fx.*;
import waba.sys.*;

/** this class implements a scrollable message box window with customized buttons, delayed unpop and scroll text. 
    <br>for example, to create an automatic unpop after 5 seconds, do:
    <pre>
      MessageBox mb = new MessageBox("SuperWaba","SuperWaba is an enhanced version|of the Waba Virtual Machine.|Programmed by:|Guilherme Campos Hazan",null);               
      mb.setUnpopDelay(5000);
      popupModal(mb);
    </pre>
created by guich@SuperWaba_1.1 */

public class MessageBox extends Window
{
   Label msg;
   PushButtonGroup btns;
   int selected = -1;
   boolean hasScroll = false;
   int xa,ya,wa=14,ha=6; // arrow coords
   Timer unpopTimer;
    
   /** constructs a message box with the text and one "Ok" button */
   public MessageBox(String title, String msg)
   {
      this(title, msg, new String[]{"Ok"});
   }
   
   /** constructs a message box with the text and the specified button captions. 
   the text may be separated by '|' as the line delimiters. 
   if buttonCaptions is null, no buttons are displayed and you must dismiss the dialog by calling unpop or 
   by setting the delay using setUnpopDelay method */
   public MessageBox(String title, String text, String[] buttonCaptions)
   {
      super(title,ROUND_BORDER);
      if (buttonCaptions != null)
         btns = new PushButtonGroup(buttonCaptions,false,-1,4,6,1,false,PushButtonGroup.BUTTON);
      msg = new Label(text,Label.CENTER);
      
      int wb = btns==null?0:btns.getPreferredWidth();
      int hb = btns==null?0:btns.getPreferredHeight();
      int wm = msg.getPreferredWidth();
      int hm = msg.getPreferredHeight();
      if (20+hb+hm > Settings.screenHeight) // needs scroll?
      {
         if (hb == 0) hb = ha; 
         hm = Settings.screenHeight - 20 - hb - ha;
         hasScroll = true;
      }
      int h = 20 + hb + hm;
      int w = waba.sys.Convert.max(wb,wm)+6;
      setRect(CENTER,CENTER,w,h);
      add(msg);
      if (btns != null) add(btns);
      msg.setRect(4,15,wm,hm);
      if (btns != null) btns.setRect(CENTER,17+hm,wb,hb);
      Rect r = msg.getRect();
      xa = r.x+r.width-wa*2;
      ya = r.y+r.height+1;
   }
   
   /** sets a delay for the unpop of this dialog */
   public void setUnpopDelay(int unpopDelay)
   {
      if (unpopDelay > 0)
         unpopTimer = addTimer(unpopDelay);
   }

   public void onPaint(Graphics g)
   {
      if (hasScroll)
      {
         g.setColor(0,0,0);
         int m = wa/2;
         for (int hh=0; hh < ha; hh++)
         {
            g.drawLine(xa+m-hh,ya+hh,xa+m+hh,ya+hh); // up
            g.drawLine(xa+wa+m-hh,ya+ha-hh-1,xa+wa+m+hh-1,ya+ha-hh-1); // down
         }
      }
   }
   
   /** handle scroll buttons and normal buttons */
   public void onEvent(Event e)
   {
      if (e.type == ControlEvent.TIMER && e.target == this)
      {
         removeTimer(unpopTimer);
         unpop();
      }
      if (hasScroll && e.type == PenEvent.PEN_DOWN)
      {
         int px=((PenEvent)e).x;
         int py=((PenEvent)e).y;
         
         if (ya <= py && py <= ya+ha) // at the arrow points?
         {
            if (xa <= px && px < xa+2*wa && msg.scroll((px-xa)/wa != 0)) 
               Sound.beep();
         }
      }
      if (e.type == ControlEvent.PRESSED && e.target == btns)
      {
         selected = btns.getSelected();
         btns.setSelected(-1);
         unpop();
      }
   }
   
   /** returns the pressed button index */
   public int getPressedButtonIndex()
   {
      return selected;
   }
}