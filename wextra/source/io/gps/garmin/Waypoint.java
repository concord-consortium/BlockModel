package extra.io.gps.garmin;



import waba.io.*;

import extra.io.*;



public class Waypoint implements Storable

{

  public static final int DOT=0;

  public static final int HOUSE=1;

  public static final int GAS=2;

  public static final int CAR=3;

  public static final int FISH=4;

  public static final int BOAT=5;

  public static final int ANCHOR=6;

  public static final int WRECK=7;

  public static final int EXIT=8;

  public static final int SKULL=9;

  public static final int FLAG=10;

  public static final int CAMP=11;

  public static final int DUCK=12;

  public static final int DEER=13;

  public static final int BUOY=14;

  public static final int BACK_TRACK=15;



  public static final int DISPLAY_NAME=0;

  public static final int DISPLAY_NONE=1;

  public static final int DISPLAY_COMMENT=2;



  private static final float semi2deg=180f/2147483647;

  

  private float lat;

  private float lon;

  private String name;

  private String comment;

  private int symbol;

  private int display;



  public Waypoint()

  {

  }



  public Waypoint(Stream st)

  {

    read(st); 

  }



  public Waypoint(String name,float lat,float lon,String comment,int symbol,int display)

  {

    this.name=name;

    this.comment=comment;

    this.symbol=symbol;

    this.lat=lat;

    this.lon=lon;

    this.display=display;

  }



  public void write(Stream st)

  {

    Packet p=new Packet(Packet.WAYPOINT,60);

    saveState(p.getDataStream());

    p.write(st);

  }



  public void read(Stream st)

  {

    loadState(Packet.readPacket(st).getDataStream());

  }



  public byte getID()

  {

    return (byte)255;

  }



  public Storable getInstance()

  {

    return new Waypoint();

  }



  public void loadState(DataStream ds)

  {

    name=ds.readFixedString(6);

    lat=ds.readInt()*semi2deg;

    lon=ds.readInt()*semi2deg;

    ds.readInt(); // unused

    comment=ds.readFixedString(40);

    symbol=ds.readByte();

    display=ds.readByte();

  }



  public void saveState(DataStream ds)

  {

    ds.writeFixedString(ds.toUpperCase(name),6);

    ds.writeInt((int)(lat/semi2deg));

    ds.writeInt((int)(lon/semi2deg));

    ds.writeInt(0); // unused



    ds.writeFixedString(ds.toUpperCase(comment),40);

    ds.writeByte(symbol);

    ds.writeByte(display);

  }



  public String getName()

  {

    return name;

  }



  public String getComment()

  {

    return comment;

  }



  public float[] getPosition()

  {

    return new float[]{lat,lon};

  }



  public int getSymbol()

  {

    return symbol;

  }



  public int getDisplay()

  {

    return display;

  }



  public String toString()

  {

    return "Waypoint("+name+":"+lat+":"+lon+":"+comment+":"+symbol+")";

  }



}