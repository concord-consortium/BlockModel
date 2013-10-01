
/**
 * Title:        SerialToEthernet<p>
 * Description:  <p>
 * Copyright:    Copyright (c) cw<p>
 * Company:      Dallas Semiconductor<p>
 * @author cw
 * @version 1.0
 */
package serialtoethernet;

import javax.swing.UIManager;
import java.awt.*;
import java.net.*;
import javax.comm.*;

public class Serial
{
  boolean packFrame = false;

  //Construct the application
  public Serial(String portName)
  {
    SerialFrame frame = new SerialFrame(portName);
    //Validate frames that have preset sizes
    //Pack frames that have useful preferred size info, e.g. from their layout
    if (packFrame)
    {
      frame.pack();
    }
    else
    {
      frame.validate();
    }
    //Center the window
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = frame.getSize();
    if (frameSize.height > screenSize.height)
    {
      frameSize.height = screenSize.height;
    }
    if (frameSize.width > screenSize.width)
    {
      frameSize.width = screenSize.width;
    }
    frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
    frame.setVisible(true);
  }

  //Main method
  public static void main(String[] args)
  {
    try
    {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }

    if (args.length != 0)
    {

      if (args[0].equalsIgnoreCase("-h"))
      {
        System.out.println("java serialtoethernet.Serial <portName>");
        System.out.println("<portName> COM1");
        System.exit(0);
      }
      new Serial(args[0]);
    }
    else
      new Serial(null);

  }
}