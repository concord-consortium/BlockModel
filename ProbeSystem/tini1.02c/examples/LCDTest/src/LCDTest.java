import com.dalsemi.comm.*;

public class LCDTest
{
   // on my LCD, 4 lines.  The address of the beginning
   // of each line(decimal) is 0, 40, 20, 84
   public static void main(String[] args)
   {
      int addr;
      System.out.println("Starting LCDTest");
      if (args.length < 1)
         addr = 0x00;
      else
         addr = (new Integer(args[0])).intValue();
      System.out.println("Using address " + addr);
      
      LCDPort.sendControl(0x38);  // Set up the display mode
      LCDPort.sendControl(0x0C);  // Turn on display with no cursor
      LCDPort.sendControl(1);     // Clear.
      LCDPort.setAddress(addr);   // set the address of the first character
      String s = "Testing";       // String to display   
      byte[] d = s.getBytes();
      for (int i = 0; i < d.length; i++)
      {
         LCDPort.sendData(d[i]);   
      }
   }
}