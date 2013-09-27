/*
Copyright (c) 1998, 1999 Wabasoft  All rights reserved.

This software is furnished under a license and may be used only in accordance
with the terms of that license. This software and documentation, and its
copyrights are owned by Wabasoft and are protected by copyright law.

THIS SOFTWARE AND REFERENCE MATERIALS ARE PROVIDED "AS IS" WITHOUT WARRANTY
AS TO THEIR PERFORMANCE, MERCHANTABILITY, FITNESS FOR ANY PARTICULAR PURPOSE,
OR AGAINST INFRINGEMENT. WABASOFT ASSUMES NO RESPONSIBILITY FOR THE USE OR
INABILITY TO USE THIS SOFTWARE. WABASOFT SHALL NOT BE LIABLE FOR INDIRECT,
SPECIAL OR CONSEQUENTIAL DAMAGES RESULTING FROM THE USE OF THIS PRODUCT.

WABASOFT SHALL HAVE NO LIABILITY OR RESPONSIBILITY FOR SOFTWARE ALTERED,
MODIFIED, OR CONVERTED BY YOU OR A THIRD PARTY, DAMAGES RESULTING FROM
ACCIDENT, ABUSE OR MISAPPLICATION, OR FOR PROBLEMS DUE TO THE MALFUNCTION OF
YOUR EQUIPMENT OR SOFTWARE NOT SUPPLIED BY WABASOFT.
*/

package waba.ui;

import waba.fx.*;
import waba.sys.*;

/**
 * Welcome is the welcome application.
 * <p>
 * This is the default program run when none is specified or when the VM needs
 * a program to run to show that the VM is functioning on a device.
 */

// ps: i had to remove the message box from this version. maybe in the next one.
public class Welcome extends MainWindow
{
   String version="Version ";
   Button btnHi;

   /** Constructs the welcome application. */
   public Welcome()
   {
      int v = Vm.getVersion();
      version += (v / 100) + "." + (v % 100 / 10) + (v % 10) + " for " + Vm.getPlatform();
      setDoubleBuffer(true);
      Label l;
      add(l = new Label("(Super)Waba Virtual Machine"));
      l.setRect(CENTER,10,PREFERRED,PREFERRED);
      add(l = new Label(version));
      l.setRect(CENTER,20,PREFERRED,PREFERRED);
      add(btnHi = new Button("About"));
      btnHi.setRect(CENTER,35,PREFERRED,PREFERRED);
      add(l = new Label("WabaVM installed and ready"));
      l.setRect(CENTER,60,PREFERRED,PREFERRED);
      add(l = new Label("www.versapalm.com"));
      l.setInvert(true);
      l.setRect(CENTER,100,PREFERRED,PREFERRED);
      add(l = new Label("Copyright (c) Rick Wild - WabaSoft"));
      l.setRect(CENTER,149,PREFERRED,PREFERRED);
   }

   private void showAbout(byte style)
   {
      MessageBox mb = new MessageBox("SuperWaba","SuperWaba is an enhanced version|of the Waba Virtual Machine.|Programmed by:|Guilherme Campos Hazan|Window CE/PocketPC/AppletViewer|Versions by Dave Slaughter",null);
      mb.setBorderStyle(style);
      popupModal(mb);
      Vm.sleep(3000);
      mb.unpop();
   }
   /** Called by the system to pass events to the application. */
   public void onEvent(Event event)
   {
      if (event.type == ControlEvent.PRESSED && event.target == btnHi)
      {
   /*      MessageBox mb = new MessageBox("SuperWaba","SuperWaba is an enhanced version|of the Waba Virtual Machine.|Programmed by:|Guilherme Campos Hazan|see www.versapalm.com");
         mb.setTitleStyle(ROUND_BORDER);
         popupModal(mb);*/
         for (byte i =0; i < 5; i++)
            showAbout(i);
      } else
      if (event.target == this && event.type == PenEvent.PEN_DOWN)
         exit(0);
   }
}