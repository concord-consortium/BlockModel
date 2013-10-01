import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class ScrollingGraph extends Panel
{
    private InputStream datain;
    
    private int updateInterval;
    
    private int[] values;
    private int startOffset;
    private int endOffset;
    
    public int maxValue = 300000;
    public int increment = 25000;
    
    private int currentValue;
    
    private boolean updateDisplay = false;
    
    private Object lock = new Object();
    
    private Font titleFont = new Font("Dialog", Font.BOLD, 24);
    private Font textFont = new Font("MonoSpaced", Font.PLAIN, 12);
    
    private Label curr;
    private Label avg;
    
    private int hStart;
    private float vFactor;
    
    private FontMetrics metrics;
    private int halfFontHeight;
    private int hPos;
    private int hWidth;
    
    private GraphPanel graph;
    
    public Color foregroundColor = Color.green;
    public Color backgroundColor = Color.black;
    public Color averageColor = Color.red;
    
    private boolean verbose = false;
    
    public ScrollingGraph(String title, InputStream datain, int updateInterval, int divisions, boolean v)
    {
        verbose = v;
        
        setForeground(foregroundColor);
        setBackground(backgroundColor);
        
        setLayout(new BorderLayout());
        Label top = new Label(title, Label.CENTER);
        top.setFont(titleFont);
        add("North", top);
        
        curr = new Label("CURR: N/A", Label.CENTER);
        curr.setFont(textFont);
        
        avg = new Label("AVG: N/A", Label.CENTER);
        avg.setFont(textFont);

        Panel bottom = new Panel(new GridLayout(1,2));
        bottom.add(curr);
        bottom.add(avg);
        add("South", bottom);
        
        graph = new GraphPanel();
        add("Center", graph);
        
        metrics = getFontMetrics(textFont);
        halfFontHeight = metrics.getMaxAscent() / 2;
        
        this.datain = datain;
        this.updateInterval = updateInterval;
        values = new int[divisions];
        startOffset = 0;
        endOffset = 0;
        setDisplayConstants();
        new PollingThread().start();
    }
    
    private void setDisplayConstants()
    {
        hPos = metrics.stringWidth("" + maxValue) + 10;
        vFactor = (float)(graph.getSize().height - halfFontHeight) / (float)(maxValue);

        hPos += (getSize().width - hPos) % (values.length - 1);
        hWidth = (getSize().width - hPos) / (values.length - 1);
    }
    
    private class PollingThread extends Thread
    {
        public void run()
        {
            byte[] buff = new byte[4];
            int data;
            int bytesread;
            int temp;
        
            while(true)
            {
                bytesread = 0;
                while(bytesread < 4)
                {
                    try
                    {
                        temp = datain.read(buff);
                    }
                    catch(IOException ioe)
                    {
                        temp = -1;
                    }
                
                    if(temp == -1)
                    {
                        updateDisplay = false;
                        return;
                    }
                    else
                    {
                        bytesread += temp;
                    }
                }

                data = ((buff[0] <<  0) & 0x000000ff) +
                       ((buff[1] <<  8) & 0x0000ff00) +
                       ((buff[2] << 16) & 0x00ff0000) +
                       ((buff[3] << 24) & 0xff000000);

                if(data > maxValue)
                {
                    while(maxValue < data)
                    {
                        maxValue += increment;
                    }
                    setDisplayConstants();
                }
            
                synchronized (lock)
                {
                    currentValue = data;
                    if(verbose)
                        System.out.println("" + currentValue);
                }

                if(!updateDisplay)
                {
                    updateDisplay = true;
                    new DisplayThread().start();
                }
            }
        }
    }
    
    private class DisplayThread extends Thread
    {
        boolean firstValue = true;
        public void run()
        {
            while(updateDisplay)
            {
                long currTime = System.currentTimeMillis();
                synchronized (lock)
                {
                    if(firstValue)
                    {
                        values[0] = currentValue;
                        firstValue = false;
                    }
                    else
                    {
                        endOffset = (endOffset + 1) % values.length;
                        values[endOffset] = currentValue;
                        if(endOffset == startOffset)
                        {
                            startOffset = (startOffset + 1) % values.length;
                        }
                    }
                }
                graph.repaint();
                currTime = System.currentTimeMillis() - currTime;
                try
                {
                    if(currTime < updateInterval)
                    {
                        Thread.sleep(updateInterval - currTime);
                    }
                }
                catch(InterruptedException ex)
                {
                }
            }
        }
    }
    
    private class GraphPanel extends Panel
    {
        GraphPanel()
        {
            addComponentListener(new ComponentAdapter()
                {
                    public void componentResized(ComponentEvent ev)
                    {
                        setDisplayConstants();
                    }
                });
        }
        
        public void update(Graphics g)
        {
            paint(g);
        }
        
        public void paint(Graphics g)
        {
            String header;
            int headerWidth;
                
            int height = getSize().height;
            int width = getSize().width;
            int temp;

            Image bufferImage = createImage(width, height);
            Graphics bufferGraphics = bufferImage.getGraphics();

            for(int i = increment; i <= maxValue; i += increment)
            {
                header = "" + i;
                headerWidth = metrics.stringWidth(header);
                temp = height - (int)(i * vFactor);
                bufferGraphics.drawLine(hPos - 3, temp, hPos, temp);
                temp += halfFontHeight;
                bufferGraphics.drawString(header, hPos - 5 - headerWidth, temp);
            }
                
            bufferGraphics.drawLine(hPos, 0, hPos, height);
            bufferGraphics.drawLine(hPos, height - 1, width, height - 1);
                
            curr.setText("CURR: " + currentValue);

            int[] x;
            int[] y;
            int i;
                    
            int average = 0;

            synchronized(lock)
            {
                int start = startOffset;
                x = new int[values.length + 2];
                y = new int[values.length + 2];
                int hLoc = hPos - hWidth;
                i = 0;

                while(start != endOffset)
                {
                    hLoc += hWidth;
                    x[i] = hLoc;
                    y[i] = height - (int)(values[start] * vFactor);
                    i++;
                    average += values[start];
                    start = (start + 1) % values.length;
                }

                hLoc += hWidth;
                x[i] = hLoc;
                y[i] = height - (int)(values[start] * vFactor);
                i++;
                average += values[start];
                start = (start + 1) % values.length;
                    
                x[i] = hLoc;
                y[i++] = height - 1;
                x[i] = hPos;
                y[i++] = height - 1;
            }
                
            bufferGraphics.fillPolygon(x, y, i);
                
            average = average / (i - 2);
            avg.setText("AVG: " + average);
            average = height - (int)(average * vFactor);
            bufferGraphics.setColor(averageColor);
            bufferGraphics.drawLine(hPos, average, width, average);
            
            g.drawImage(bufferImage, 0,0,width,height,null);
        }
    }
}