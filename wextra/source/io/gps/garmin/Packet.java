package extra.io.gps.garmin;



import waba.io.*;

import extra.io.*;



public class Packet

{

  private static final int DLE=16;

  private static final int ETX=3;



  public static final int ACK=6;                // 0x06

  public static final int NAK=21;               // 0x15

  public static final int PRODUCT_REQUEST=254;  // 0xFE

  public static final int PRODUCT_DATA=255;     // 0xFF



  public static final int COMMAND=10;           // 0x0A

  public static final int FINISHED=12;          // 0x0C

  public static final int DATE_TIME=14;         // 0x0E

  public static final int POSITION=17;          // 0x11

  public static final int PROXIMITY_WAYPOINT=19;// 0x13

  public static final int NUM_RECORDS=27;       // 0x1B

  public static final int ROUTE_HEADER=29;      // 0x1D

  public static final int ROUTE_WAYPOINT=30;    // 0x1E

  public static final int ALMANAC=31;           // 0x1F

  public static final int TRACK=34;             // 0x22

  public static final int WAYPOINT=35;         // 0x23

  public static final int POS_VEL_TIME=51;      // 0x33



  private int id;

  private byte[] data;

  private BufferStream bs;



  private static byte[] buf=new byte[100];

  private static int bufPos=0;

  private static int numRead=0;



  public static Packet ack=new Packet(ACK,1);



  public Packet()

  {

  }



  public Packet(int id)

  {

    this.id=id;

    data=null;

  }



  public Packet(int id,int size)

  {

    this.id=id;

    data=new byte[size];

  }



  public int getID()

  {

    return id;

  }



  public DataStream getDataStream()

  {

    if (data==null)

      return new DataStream(bs=new BufferStream(),false);

    return new DataStream(new BufferStream(data),false);

  }



  private int calcChecksum()

  {

    int dataSize=data.length;

    int sum=id+dataSize;;

    for(int i=0;i<dataSize;i++)

      sum+=data[i];

    return (-sum) & 0xFF;

  }



  public static Packet readPacket(Stream st)

  {

    Packet p=new Packet();

    p.read(st);

    return p;

  }



  static int totalRead=0;



  public int read(Stream st)

  {

    int dataPos=0;

    boolean waitingForDLE=false;

    int state=0;

    int size=0;

    int checksum=0;

    boolean finished=false;

    int toRead=6;

    while(!finished)

    {

      int fuckup=0;

      while (bufPos>=numRead)

      {

        numRead=st.readBytes(buf,0,toRead);

        totalRead+=numRead;

        toRead-=numRead;

        if (numRead<1)

        {

          if (fuckup++>50)

            return -1;

          waba.sys.Vm.sleep(100);

        }

        bufPos=0;

      }

      byte ch=buf[bufPos];

      switch (state)

      {

        case 0: // 1st DLE

          if (ch==DLE)

            state++;

          break;

        case 1: // packet id

          if (ch==DLE||ch==ETX)

            state=0;

          else

          {

            id=ch;

            state++;

          }

          break;

        case 2: // size

          if (ch==DLE&&!waitingForDLE)

          {

            waitingForDLE=true;

            toRead++;

          }

          else

          if (ch!=DLE&&waitingForDLE)

            state=0;

          else

          {

            waitingForDLE=false;

            size=ch;

            toRead+=size;

            data=new byte[size];

            state++;

          }

          break;

        case 3: // data

          if (ch==DLE&&!waitingForDLE)

          {

            waitingForDLE=true;

            toRead++;

          }

          else

          if (ch!=DLE&&waitingForDLE)

            state=0;

          else

          {

            waitingForDLE=false;

            data[dataPos++]=ch;

            if (dataPos==size)

              state++;

          }

          break;

        case 4: // checksum

          if (ch==DLE&&!waitingForDLE)

          {

            waitingForDLE=true;

            toRead++;

          }

          else

          if (ch!=DLE&&waitingForDLE)

            state=0;

          else

          {

            waitingForDLE=false;

            checksum=ch;

            state++;

          }

          break;

        case 5: // 2nd DLE

          if (ch==DLE)

            state++;

          else

            state=0;

          break;

        case 6: // etx

          if (ch==ETX)

            finished=true;

          state=0;

          break;

      }

      bufPos++;

    }

    return 0;

  }



  public int write(Stream st)

  {

    if (data==null&&bs==null)

      return -1;

    if (data==null)

      data=bs.getBuffer();

    int dataSize=data.length;

    int checksum=id+dataSize;;

    int stuffCnt=(dataSize==DLE?1:0);

    for(int i=0;i<dataSize;i++)

    {

      checksum+=data[i];

      if (data[i]==DLE)

        stuffCnt++;

    }

    checksum= (-checksum) & 0xFF;

    if (checksum==DLE)

      stuffCnt++;

    byte[] outbuf=new byte[dataSize+stuffCnt+6];

    int pos=0;

    outbuf[pos++]=DLE;

    outbuf[pos++]=(byte)id;

    outbuf[pos++]=(byte)dataSize;

    if (dataSize==DLE)

      outbuf[pos++]=DLE;

    for(int i=0;i<dataSize;i++)

    {

      outbuf[pos++]=data[i];

      if (data[i]==DLE)

        outbuf[pos++]=DLE;

    }

    outbuf[pos++]=(byte)checksum;

    if (checksum==DLE)

      outbuf[pos++]=DLE;

    outbuf[pos++]=DLE;

    outbuf[pos++]=ETX;



    int toWrite=outbuf.length;

    int fuckup=0;

    while (toWrite>0)

    {

      int written=st.writeBytes(outbuf,0,toWrite);

      if (written<1)

      {

        if (fuckup++>50)

          return -1;

        waba.sys.Vm.sleep(100);

      }

      toWrite-=written;

    }

    return 0;

  }



  public static void sendNumRecords(Stream st,int num)

  {

    Packet p=new Packet(NUM_RECORDS,2);

    p.getDataStream().writeShort(num);

    p.write(st);

  }



  public static void main(String[] args)

  {

//    Packet p=new Packet(COMMAND,new byte[]{0x01,0x00});

//    System.out.println(p.calcChecksum());

  }

}