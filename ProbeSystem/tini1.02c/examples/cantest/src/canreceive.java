
import com.dalsemi.comm.*;
import com.dalsemi.system.*;

public class canreceive
{
    /* 125Kbit/s with crystal of 18.432MHz */
    static final int CAN_DIVISOR = 7;
    static final int CAN_TSEG1 = 13;
    static final int CAN_TSEG2 = 7;
    static final int CAN_SJW = 1;
    static final byte CANBUSNUM = CanBus.CANBUS0;
//    static final byte CANBUSNUM = CanBus.CANBUS1;

    static void dumpFrame(CanFrame frame)
    {
        System.out.println("Done receiving frame");
        System.out.println("ID: "+Integer.toHexString(frame.ID));
        if (frame.extendedID)
            System.out.println("Extended ID");
        else
            System.out.println("Standard ID");
        if (frame.remoteFrameRequest)
            System.out.println("Remote Frame");
        else
            System.out.println("Data Frame");
        System.out.println("Length: "+frame.length);
        for (int i = 0;i < frame.length;i++)
            System.out.print(Integer.toHexString(frame.data[i] & 0xFF)+" ");
        System.out.println();
    }

    static final int MAXCOUNT = 100;
//    static final int MAXCOUNT = 100000000;
    static final boolean dopassive = false;

    static void doTest() throws Exception
    {
        final int DUMPCOUNT = 1;
        long start,stop;

        CanBus a = new CanBus(CANBUSNUM);
        a.setBaudRatePrescaler(CAN_DIVISOR);
        a.setTSEG1(CAN_TSEG1);
        a.setTSEG2(CAN_TSEG2);
        a.setSynchronizationJumpWidth(CAN_SJW);

//        a.setReceiveQueueLimit(1);
//        a.setTransmitQueueLimit(1);
        System.out.println("Frames available to read: "+a.receiveFramesAvailable());

        // Now, we tell the CAN Controller to jump on the bus.
        if (dopassive)
        {
            System.out.println("Enabling passive receive");
            a.enableControllerPassive();
        }
        else
        {
            System.out.println("Enabling regular receive");
            a.enableController();
        }
        
        // Set message center one to receive
        a.setMessageCenterRXMode(1);
        // Set message center one to not use mask filtering
        a.setMessageCenterMessageIDMaskEnable(1,false);
        // Set message center one to match this 29 bit extended id
//        a.set29BitMessageCenterArbitrationID(1,0x55F6575C);
        // Set message center one to match this 11 bit extended id
        a.set11BitMessageCenterArbitrationID(1,0x55F6575C);
        // Set message center one to allow reception of messages.
        a.enableMessageCenter(1);

        // Create the frame for recieve() to fill
        CanFrame frame = new CanFrame();

        System.out.println("Continous Receive");
        int count = 0;
        int endcount = MAXCOUNT;
        int firstpacket = 0;
        start = TINIOS.uptimeMillis();
        boolean error = false;
        while ((count < endcount) && (!error))
        {
            // Block waiting on a frame reception.
            a.receive(frame);

//            while (!a.receivePoll(frame))
            {
//                System.out.println("hey");
            }

            byte[] temp = frame.data;

            if (count == 0)
            {
                count = (temp[2] & 0xFF) | (temp[3] & 0xFF) << 8;
                endcount = count + MAXCOUNT;
                firstpacket = count;
            }
            
            int reccount = (temp[2] & 0xFF) | (temp[3] & 0xFF) << 8;
            if (dopassive)
            {
                if (reccount != firstpacket)
                {
                    System.out.println("Received count of "+reccount+" expecting count of "+count);
                    error = true;
                }
            }
            else
            {
                if (reccount != count)
                {
                    System.out.println("Received count of "+reccount+" expecting count of "+count);
                    error = true;
                }
            }
            
            if ((count < DUMPCOUNT) || error)
            {
                dumpFrame(frame);
            }
            
            if ((count % 10) == 0)
            {
//                Debug.hexDump(count);
//                System.out.println(TINIOS.getFreeRAM());
            }
            
            count++;
        }
        stop = TINIOS.uptimeMillis();
        System.out.println("Completion time: "+(stop-start));
        System.out.println(start+":"+stop);
        System.out.println("Exited after "+(count-firstpacket)+" iterations");

        a.close();
    }

    static void main(String args[])
    {
        try
        {
            if (TINIOS.isCurrentTaskInit())
                Debug.setDefaultStreams();
                
            System.out.println("CAN Receive tester");

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
