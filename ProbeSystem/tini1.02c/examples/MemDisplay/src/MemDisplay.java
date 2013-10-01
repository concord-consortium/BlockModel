import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class MemDisplay extends Frame
{
    static String ipAddress;
    static int port = 4401;
    static Socket s;
    InputStream sockin;    
    static boolean verbose = false;
    
    public MemDisplay()
    {
        setTitle("TINI Memory Reporter");
        setSize(420,400);
        setLayout(new BorderLayout());
        
        s = null;
            
        String title;

        try
        {
            s = new Socket(InetAddress.getByName(ipAddress), port);
            sockin = s.getInputStream();
            title = "TINI Memory: " + ipAddress;
        }
        catch(IOException e)
        {
            title = "Connection Failed!";
            sockin = new FakedInputStream();
        }

        ScrollingGraph graph = new ScrollingGraph(title, sockin, 1000, 360, verbose);
        add(graph);
        
        addWindowListener(new WindowAdapter()
            {
                public void windowClosing(WindowEvent e)
                {
                    try
                    {
                        if(s != null)
                        {
                            sockin.close();
                            s.close();
                        }
                    }
                    catch(IOException ioe)
                    {
                        ioe.printStackTrace();
                    }
                    System.exit(0);
                }
            });
    }
    
    public static void main(String[] args)
    {
        parseCommandLine(args);
        new MemDisplay().setVisible(true);
    }
    
    class FakedInputStream extends InputStream
    {
        public int read(byte[] ba)
        {
            return -1;
        }
        
        public int read()
        {
            return -1;
        }
    }

    public static void usage()
    {
        System.out.println("usage: MemDisplay -a <remote system> <options>");
        System.out.println();
        System.out.println("Options include:");
        System.out.println("      -p <port number>    Connects on the given port of the");
        System.out.println("                          remote system.  (Default: 4401)");
        System.out.println("      -v                  Turns on verbose mode.  This will");
        System.out.println("                          print out each value received from");
        System.out.println("                          the remote system to the local console.");
        System.exit(1);
    }
  
    public static void parseCommandLine(String[] args)
    {
        if(args != null)
        {
            if(args.length == 0)
                usage();
                
            for(int i = 0; i < args.length; i++)
            {
                if((i%2)== 0)
                {
                    if(args[i].charAt(0) != '-')
                    {
                        usage();
                    }
              
                    switch(args[i].charAt(1))
                    {
                        case 'a':
                            ipAddress = args[++i];
                            break;
                        case 'p':
                            port = Integer.parseInt(args[++i]);
                            break;
                        case 'v':
                            verbose = true;
                            break;
                        default:
                            usage();
                            break;  
                    }
                }                
            }
        }   
        else
            usage();
          
        if(ipAddress == null)
            usage();
    }

    public void setVisible(boolean b)
    {
        if(b)
        {
            Dimension bounds = Toolkit.getDefaultToolkit().getScreenSize();
            Rectangle abounds = getBounds();
            
            setLocation((bounds.width - abounds.width)/ 2,
                        (bounds.height - abounds.height)/ 2);
        }
        
        super.setVisible(b);
    }
}