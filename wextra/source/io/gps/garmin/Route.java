package extra.io.gps.garmin;



import waba.io.*;

import extra.io.*;



/**

 * Represents a Garmin route (collection of waypoints).

 * Here's an example of using Route:

 * <pre>

 *   SerialPort sp=new SerialPort(9600,0);

 *   Waypoint way1=new Waypoint

 * </pre>

 */

public class Route

{

  private Waypoint[] waypoints;

  int numPoints;



  int routeNum;

  String comment;



  public Route(int routeNum,String comment)

  {

    this(routeNum,comment,new Waypoint[0]);

  }



  public Route(int routeNum,String comment,Waypoint[] waypoints)

  {

    this.routeNum=routeNum;

    this.comment=comment;

    this.waypoints=waypoints;

    numPoints=waypoints.length;

  }



  public void add(Waypoint way)

  {

    if (numPoints>=waypoints.length)

    {

      Waypoint[] temp=new Waypoint[numPoints+10];

      waba.sys.Vm.copyArray(waypoints,0,temp,0,numPoints);

      waypoints=temp;

    }

    waypoints[numPoints++]=way;

  }



  public void write(Stream st)

  {

    if (numPoints<=0)

      return;

    //Router.raw.setText(numPoints+waypoints[0].getName()+":"+waypoints[1].getName());

    Packet.sendNumRecords(st,numPoints);

    Packet p=Packet.readPacket(st);

    if (p.getID()==Packet.ACK)

    {

      p=new Packet(Packet.ROUTE_HEADER,21);

      DataStream ds=p.getDataStream();

      ds.writeByte(routeNum);

      ds.writeFixedString(comment,20);

      p.write(st);

      p.read(st);

      for(int i=0;i<numPoints;i++)

      {

        //Router.raw.setText(">>"+waypoints[i].getName());

        p=new Packet(Packet.ROUTE_WAYPOINT,60);

        waypoints[i].saveState(p.getDataStream());

        p.write(st);

        p.read(st);

      }

      p=new Packet(Packet.FINISHED,2);

      p.write(st);

    }

    //else

    //  Router.raw.setText("Fuck. Didn't work "+p.getID()+":"+Packet.totalRead);

  }

}