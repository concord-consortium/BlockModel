package extra.io.gps;



import waba.ui.*;

import waba.fx.*;

import waba.io.*;

import waba.sys.*;



public class GPS extends Control

{

  SerialPort sp;

  byte[] buf=new byte[100];

  int bufCount=0;

  int bufPos=0; 

  String text;

  float[] location=new float[2];

  Time lastFix=new Time();

  boolean dataValid;



  public GPS()

  {

    sp=new SerialPort(0,9600);

    addTimer(100);

    nextMessage();

  }



  public GPS(SerialPort sp,int readInterval)

  {    

    this.sp=sp;

    addTimer(readInterval);

    if (!sp.isOpen())

      text="Can't open port()";

    else

      nextMessage();

  }



  public void onEvent(Event e)

  {

    //System.out.println("Event "+e);

    if (e.type==ControlEvent.TIMER)

      processInput();

  }



  public void onPaint(Graphics g)

  {

    g.setColor(0,0,0);

    if (text!=null)

      g.drawText(text,x,y);

  }



  private void processInput()

  {

    //System.out.println("prin");

    //if (!sp.isOpen())

    //  System.out.println("!open");

      

    String message;

    if ((message=nextMessage())!=null)

    {

      //text=message;

      //System.out.println("<"+message+">");

      String[] sp=split(message);

      //for(int i=0;i<sp.length;i++)

      //  System.out.print("("+sp[i]+")");

      //System.out.println();

      //repaint();

      if (sp[0].equals("$GPGLL"))

      {

        location[0]=toCoordinate(sp[1],sp[2]);

        location[1]=toCoordinate(sp[3],sp[4]);

        if (sp[5]!=null)

        {

          lastFix.hour=Convert.toInt(sp[5].substring(0,2));

          lastFix.minute=Convert.toInt(sp[5].substring(2,4));

          lastFix.second=Convert.toInt(sp[5].substring(4,6));

          lastFix.millis=0;

        }

        dataValid=(sp[6]!=null&&sp[6].equals("A"));

        text="lat:"+location[0]+" lon:"+location[1]+" fix:"+lastFix.hour+":"+lastFix.minute+":"+lastFix.second+" valid:"+dataValid;

        repaint();

      }

    }

    else

    {

      text="No message "+(cnt++);

      repaint();

    }  

    //System.out.println("prout");

  }



  int cnt;



  public float[] getLocation()

  {

    return location;

  }



  public Time getLastFix()

  {

    return lastFix;

  }



  int maxAvailable=0;



  private String nextMessage()

  {

    StringBuffer sb=null;

    while (true)

    {

      while (bufCount-->0)

      {

        char c=(char)buf[bufPos++];

        if ((c==13||c==10)&&sb!=null)

          return sb.toString();

        if (c=='$'&&sb==null)

          sb=new StringBuffer();

        if (sb!=null)

          sb.append(c);

      }

      int available=sp.readCheck();

      if (available==0)

        return "0 available";

      if (available!=-1)

      {

        maxAvailable=available;

        //if (buf.length<available)

        //  buf=new byte[available];

      }

      else

        sp.readBytes(buf,0,1);

      int toRead=available>buf.length||available==-1?buf.length:available;

      bufCount=sp.readBytes(buf,0,toRead);

      bufPos=0;

      if (bufCount==-1)

        return ">negative one";

      if (bufCount==0)

        return "0 read ("+toRead+" to read ("+available+" available ("+maxAvailable+")))";

    }

  }



  private String[] split(String s)

  {

    if (s==null)

      return null;

    char[] c=s.toCharArray();



    int size=c.length;

    if (c[size-3]=='*');

      size-=3;

    int cnt=1;

    for(int i=0;i<size;i++)

      if (c[i]==',')

        cnt++;

    String[] ret=new String[cnt];

    int last=0;

    int pos=0;

    for(int i=0;i<=size;i++)

    {

      if (i==size||c[i]==',')

      {

        if (i>last)

          ret[pos]=new String(c,last,i-last);

        pos++;

        last=i+1;

      }

    }

    return ret;  

  }



  public float toFloat(String s)

  {

    char[] cha=s.toCharArray();

    int i;

    int size=cha.length;

    for(i=0;i<size&&cha[i]!='.';i++);

    if (i<size)

    {

      int divider=1;

      for(int d=0;d<size-i-1;d++)

        divider*=10;

      return (float)Convert.toInt(s.substring(0,i))+

             (float)Convert.toInt(s.substring(i+1,size))/divider;

    }

    else

      return (float)Convert.toInt(s);

  }



  public float toCoordinate(String s,String dir)

  {

    if (s==null||dir==null)

      return 0f;

    float deg=0f;

    char[] cha=s.toCharArray();

    int i;

    int size=cha.length;

    for(i=size-1;i>=0&&cha[i]!='.';i--);

    if (i>=0)

    {

      int divider=1;

      for(int d=0;d<size-i-1;d++)

        divider*=10;

      deg=(float)Convert.toInt(s.substring(0,i-2))+

          ((float)Convert.toInt(s.substring(i-2,i))+

           (float)Convert.toInt(s.substring(i+1,size))/divider)/60;

      if (dir.equals("S")||dir.equals("W"))

        deg=-deg;

    }

    return deg;

  }

}