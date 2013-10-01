
/**
 * Title:        SerialToEthernet<p>
 * Description:  <p>
 * Copyright:    Copyright (c) cw<p>
 * Company:      Dallas Semiconductor<p>
 * @author cw
 * @version 1.0
 */
package serialtoethernet;

import java.io.*;
import javax.swing.*;

public class CheckData extends Thread
{

  PipedInputStream pin;
  JTextArea jTextArea;
  boolean doTest = false;
  int index = 0;
  byte[] gData;

  public CheckData(PipedOutputStream pout)
  throws IOException
  {
     pin = new PipedInputStream(pout);
     doTest = true;
  }


  public void setTextArea(JTextArea jTextArea)
  {
    this.jTextArea = jTextArea;
  }

  public void setTextAreaMessage(String data)
  {
     jTextArea.append(data + "\n");
  }

  public void setCompareData(byte[] gData)
  {
     this.gData = gData;
  }

  public void run()
  {
    int numBytes = 0;
    int bufSize = SerialFrame.BUFFER_SIZE;

    while (doTest)
    {

        try
        {
          numBytes = bufSize;
          byte[] b = new byte[numBytes];
          numBytes = pin.read(b);

          for (int i = 0; i < numBytes; i++)
          {
            if (gData[(i + index) % bufSize] != b[i])
            {
              System.out.println("Dropped a byte");
              setTextAreaMessage("Dropped a byte");
            }
          }
          index += numBytes;

        }
        catch (IOException e)
        {
          System.out.println("Caught an IOException in thread check");
        }
    }
  }


  public void setIndex(int index)
  {
    this.index = index;
  }
  public void stopChecking()
  {
    doTest = false;
  }
}