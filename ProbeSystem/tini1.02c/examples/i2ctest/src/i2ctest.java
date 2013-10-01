
import com.dalsemi.system.*;

/**
 * Simple test of the I2C subsystem.
 */
public class i2ctest
{
    /**
     * Talk to a Dallas DS1621 temperature part on the I2C Port
     * object given.  Print out the result.
     *
     * @param port Port object to talk with
     */
    static void doTempConvert(I2CPort port)
    {
        byte[] b = new byte[5];
        
        try
        {
            // Send "do temperature convert" command
            b[0] = (byte)0xEE;
            if (port.write(b,0,1) < 0)
            {
                System.out.println("Fail on conv command");
                return;
            }

            try {Thread.sleep(1000);} catch (InterruptedException e) {}
            
            // Send "read temperature" command
            b[0] = (byte)0xAA;
            if (port.write(b,0,1) < 0)
            {
                System.out.println("Fail on read temp command");
                return;
            }
                
            // Read the two byte temperature value.
            if (port.read(b,0,2) < 0)
            {
                System.out.println("Fail on read temp");
                return;
            }
            else
            {
                // Dump the temperature value as two hex digits
                for (int j = 0;j < 2;j++)
                    System.out.print(Integer.toHexString(b[j] & 0xFF)+" ");
                System.out.println();
            }
        }
        catch (IllegalAddressException e)
        {
            System.out.println("Illegal Address on Memory mapped I2C");
            return;
        }
    }
    
    static void testDefault()
    {
        // Set up I2C driver parameters
        // This uses the default pins P5.0(SCL) and P5.1(SDA)
        I2CPort port = new I2CPort();

        // Set I2C slave we want to talk to.  0x90 in this case.
        port.slaveAddress = (byte)0x48;
        port.setClockDelay((byte)2);
        doTempConvert(port);
    }

    static void testMappedIO()
    {
        // Set up I2C driver parameters
        // For I2C SCL:
        // CE3 chip select, Offset 0x0F0003, Mask bit 0x01 in that byte
        // For I2C SCL:
        // CE3 chip select, Offset 0x0F0003, Mask bit 0x02 in that byte
        I2CPort port = new I2CPort(0x00380001,(byte)0x80,0x00380001,(byte)0x40);
        
        // Set I2C slave we want to talk to.  0x90 in this case.
        port.slaveAddress = (byte)0x48;
        port.setClockDelay((byte)0);
        doTempConvert(port);
    }

    static void main(String[] args)
    {
        Debug.setDefaultStreams();
        System.out.println("I2C Tester");

        for (;;)
        {
            // Uncomment the line below for default port pin I2C.
            testDefault();
            // Uncomment the line below for memory mapped IO I2C.
//            testMappedIO();
//            try {Thread.sleep(500);}catch(Exception e){}
        }
    }
}
