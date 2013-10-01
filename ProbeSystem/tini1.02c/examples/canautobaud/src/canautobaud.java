
import com.dalsemi.comm.*;
import com.dalsemi.system.*;

public class canautobaud
{
    static void dumpFrame(CanFrame frame)
    {
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

    /**
     * Autobaud to given canbus with specified timeout (in milliseconds)
     *
     * @param cb CAN Bus object
     * @param timeout Timeout for each baud rate tested in milliseconds.
     */
    public static int autobaud(CanBus cb, int timeout)
    {
        int[] bps = { 20000,50000,125000 };
        int[] prescaler = { 44 , 23, 7 };
        int[] tseg1 = { 13, 10, 13 };
        int[] tseg2 = { 7, 5, 7 };
        CanFrame frame;

        try
        {
            // Make sure the controller is off.
            cb.disableController();

            // Create the frame for recieve() to fill
            frame = new CanFrame();

            // Set message center one to receive
            cb.setMessageCenterRXMode(1);
            // Set message center one to not use global mask filtering
            cb.setMessageCenterMessageIDMaskEnable(1,true);
        }
        catch (CanBusException e)
        {
            return -1;
        }

        // Try to get any message on the bus.
        for (int i = 0;i < bps.length;i++)
        {
            System.out.println("Trying "+bps[i]);
            try
            {
                // Make sure the controller is off.
                cb.disableController();

                cb.setBaudRatePrescaler(prescaler[i]);
                cb.setTSEG1(tseg1[i]);
                cb.setTSEG2(tseg2[i]);

                // Set message center one to allow reception of messages.
                cb.enableMessageCenter(1);

                cb.enableControllerPassive();

                int sleeptime = 0;
                while (sleeptime < timeout)
                {
                    if (cb.receivePoll(frame))
                    {
                        System.out.println("Found "+bps[i]);
                        return bps[i];
                    }
                    try
                    {
                        Thread.sleep(20);
                    }
                    catch (Exception e)
                    {
                        // drain
                    }
                    sleeptime += 20;
                }
            }
            catch (CanBusException e)
            {
                System.out.println(e);
            }
        }

        return -1;
    }

    static void main(String args[])
    {
        try
        {
            if (TINIOS.isCurrentTaskInit())
                Debug.setDefaultStreams();

            System.out.println("CAN Autobaud tester");

            CanBus a = new CanBus(CanBus.CANBUS1);
            autobaud(a,2000);
            a.close();

            System.out.println("Normal Exit");
            int i = 0;
            while (true)
            {
                i++;
            }
        }
        catch (Throwable e)
        {
            System.out.println("Exception");
            System.out.println(e);
        }
    }
}
