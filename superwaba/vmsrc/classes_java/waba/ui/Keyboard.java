package waba.ui;

import waba.ui.*;
import waba.fx.*;
import waba.sys.*;

/** a popup keyboard to be used with the edit class. guich@102 */

public final class Keyboard extends Window
{
   private Edit destEdit;
   private Rect destRect;
   private Container destCont;
   private Window parent;
   public boolean isActive;
   private PushButtonGroup pbs[] = new PushButtonGroup[6];
   private KeyEvent ke = new KeyEvent();
   private int inside=-1;

   private boolean isCaps, isShift;
   private final int NUMERIC_PAD = 0;
   private final int SYMBOLS_PAD = 1;
   private final int ACCENT_PAD = 2; 
   private final int TEXT_PAD = 3;
   private final int CAPS_PAD = 4;
   private final int SPECIAL_PAD = 5;
   /** when the user press the edit's abc keyboard, this windows is poped up. then, it adds the edit 
   so its new parent is this. since the event (abc_keypressed) is propagating up, this class will receive 
   it, closing the just opened keyboard. so this flag makes this window ignore that event. */
   private boolean ignoreNextEvent; 

   private String names[][] = 
   {
/* in the future, put all in one string; it consumes less 1k...
	  {"123456789.0,"},
	  {"+$&()-#@[]*%|{}/=\\<>"},
	  {"багавйкинуфхцъзс"}, 
	  {"qwertyuiop\"_asdfghjkl:~^zxcvbnm!?;\"`"},
	  {"Shift","Caps","          ","«",},
*/
	  {"1","2","3","4","5","6","7","8","9",".","0",","},
	  {"+","$","&","(",")","-","#","@","[","]","*","%","|","{","}","/","=","\\","<",">"},
	  {"б","а","г","а","в","й","к","и","н","у","ф","х","ц","ъ","з","с"}, 
	  {"q","w","e","r","t","y","u","i","o","p","'","_","a","s","d","f","g","h","j","k","l",":","~","^","z","x","c","v","b","n","m","!","?",";","\"","`"},
	  {"Caps","Shift"},
	  {"                   ","«","Done"},
   };
   public Keyboard()
   {
	  super("Popup Keyboard",true);
	  setDoubleBuffer(true);
	  setRect(CENTER,CENTER,144,140);
	  ke.type = KeyEvent.KEY_PRESS;
   }   
   private void cancelShift()
   {
	  if (isShift)
	  {
		 isShift = false;
		 pbs[CAPS_PAD].setSelected(-1);
	  }
   }   
   private void close()
   {
	  if (ignoreNextEvent) 
		 ignoreNextEvent = false; 
	  else
		 unpop();
   }   
   boolean handlePenEvent(int type, int x, int y)
   {
	  x -= this.x;
	  y -= this.y;
	  int in = -1;
	  int call = -1;
	  for (int i =0; i < pbs.length && in == -1; i++)
		 if (pbs[i].contains(x,y))
			in = i;
	  if (in == -1 && inside != -1) // was inside but isnt anymore? (focus_out)
		 call = inside;
	  if (in != -1)
		 call = in;
	  if (call != -1)
		 pbs[call].onEvent(type,x-pbs[call].x,y-pbs[call].y); 
	  inside = in;
	  return call != -1;
   }   
   private void insertKey(int key)
   {
	  ke.key = key;
	  destEdit.onEvent(ke);
	  //Vm.sleep(100);
   }   
   public void onEvent(Event event)
   {
	  if (event.type == KeyEvent.KEY_PRESS)
		 switch (((KeyEvent)event).key)
		 {
			case IKeys.KEYBOARD_ABC:
			case IKeys.KEYBOARD_123:
			   close();
			   break;
		 }
	  else
	  if (event.type == ControlEvent.PRESSED && event.target instanceof PushButtonGroup)
	  {
		 PushButtonGroup pb = (PushButtonGroup)event.target;
		 String st = names[pb.id][pb.getSelected()];
		 if (pb.id == SPECIAL_PAD || pb.id == CAPS_PAD) // special char?
		 {
			int key = -1;
			switch (st.charAt(0))
			{
			   case 'D': pb.setSelected(-1); close(); break;
			   case '«': key = IKeys.BACKSPACE; break;
			   case ' ': key = ' '; break;
			   case 'S': isShift = !isShift; isCaps = false; break;
			   case 'C': isCaps =  !isCaps; isShift = false; break;
			}
			if (key != -1) insertKey(key);
		 } 
		 else 
		 {
			char c = st.charAt(0);
			if (isShift || isCaps) c = Convert.toUpperCase(c);
			insertKey(c); 
			cancelShift();
		 }
	  }
   }   
   protected void onPopup()
   {      
	  ignoreNextEvent = true;
	  isCaps = false;      
	  isShift = false;
	  
	  if (pbs[0] == null)
	  {
		 int []rows = new int[]{4,4,4,3,1,1};
		 byte []x = new byte[]{5,44,97,5,5,53};
		 byte []y = new byte[]{37,37,37,85,122,122};
		 byte []ig = new byte[]{6,2,6,4,4,5};
		 for (int i =0; i < pbs.length; i++) 
		 {
			add(pbs[i] = new PushButtonGroup(names[i], false, -1, i!=SPECIAL_PAD?-1:2, ig[i], rows[i], i < 4, (i != 4) ? PushButtonGroup.BUTTON : PushButtonGroup.CHECK)); 
			pbs[i].setRect(x[i],y[i],PREFERRED,PREFERRED);
			pbs[i].id = i;
		 }      
	  }
	  parent = Window.getTopMost();
	  destEdit = (Edit)parent.getFocus();
	  destRect = destEdit.getRect();
	  destCont = destEdit.getParent();
	  
	  add(destEdit);
	  destEdit.setRect(CENTER,20,Convert.min(destRect.width,width-20),PREFERRED);
	  ke.target = destEdit;      
   }   
   protected void onUnpop()
   {
	  destEdit.pushPosState();
	  destEdit.setRect(destRect.x,destRect.y,destRect.width,destRect.height);
	  destCont.add(destEdit);
   }   
   protected void postPopup()
   {
	  setStatePosition(130,121);
	  setFocus(destEdit);
   }   
   protected void postUnpop()
   {
	  destEdit.popPosState();
   }   
}