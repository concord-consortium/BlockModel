
import com.dalsemi.comm.*;
import com.dalsemi.system.*;
import com.dalsemi.system.*;

public class cantransmit
{
    /* 125Kbit/s with crystal of 18.432MHz */
    static final int CAN_DIVISOR = 7;
    static final int CAN_TSEG1 = 13;
    static final int CAN_TSEG2 = 7;
    static final int CAN_SJW = 1;
    static final byte CANBUSNUM = CanBus.CANBUS0;
//    static final byte CANBUSNUM = CanBus.CANBUS1;
    
    static void doTest() throws Exception
    {
        CanBus a = new CanBus(CANBUSNUM);
        a.setBaudRatePrescaler(CAN_DIVISOR);
        a.setTSEG1(CAN_TSEG1);
        a.setTSEG2(CAN_TSEG2);
        a.setSynchronizationJumpWidth(CAN_SJW);

        // Now, we tell the CAN Controller to jump on the bus.
        a.enableController();
        
        // Set message center one to transmit.  This allows any outgoing messages
        // to use this register.
        a.setMessageCenterTXMode(1);

        byte[] temp = new byte[8];
        System.out.println("Continous send");

        temp[0] = (byte)0xAA;
        temp[1] = 0x55;
        int count = 0;
        while (true)
        {
            temp[2] = (byte)count;
            temp[3] = (byte)(count >>> 8);
            
            // Send a frame using extended (29 bit) ID, block until frame is ACKed
//            a.sendDataFrame(0x55F6575C, true, temp);
            
            // Send a frame using standard (11 bit) ID, block until frame is ACKed
            a.sendDataFrame(0x55F6575C, false, temp);

            if ((count % 10) == 0)
            {
                Debug.hexDump(count);
            }
            count++;
        }
//        a.close();
//        System.out.println("After close()");
    }

    static void main(String args[])
    {
        try
        {
            if (TINIOS.isCurrentTaskInit())
                Debug.setDefaultStreams();
                
            System.out.println("CAN Transmit tester");
            
            doTest();
            
            System.out.println("Normal Exit");
        }
        catch (Throwable e)
        {
            System.out.println("Exception");
            System.out.println(e);
        }
    }
}
