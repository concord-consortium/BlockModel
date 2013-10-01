/*
 * TemperatureWorker.java
 */
 
/* Copyright (C) 1999 Dallas Semiconductor Corporation.
 * All rights Reserved. Printed in U.S.A.
 * This software is protected by copyright laws of
 * the United States and of foreign countries.
 * This material may also be protected by patent laws of the United States
 * and of foreign countries.
 * This software is furnished under a license agreement and/or a
 * nondisclosure agreement and may only be used or copied in accordance
 * with the terms of those agreements.
 * The mere transfer of this software does not imply any licenses
 * of trade secrets, proprietary technology, copyrights, patents,
 * trademarks, maskwork rights, or any other form of intellectual
 * property whatsoever. Dallas Semiconductor retains all ownership rights.
 */
/* 
 $Workfile: TemperatureWorker.java $
 $Revision: 6 $
 $Date: 11/29/00 2:18p $
 $Author: Cmclean $
 $Modtime: 11/29/00 11:04a $
 $Log: /TINI/firmware/Lorne/Examples/TINIWebServer/src/TemperatureWorker.java $
* 
* 6     11/29/00 2:18p Cmclean
* 
* 5     7/20/00 5:54p Bryan
* 
* 4     5/20/00 4:33p Cmclean
* 
* 3     9/13/99 10:29a Tomc
* 
* 2     9/10/99 4:18p Tomc
* 
* 1     9/10/99 4:06p Tomc
*/

import java.io.*;
import com.dalsemi.onewire.adapter.*;
import com.dalsemi.onewire.container.*;

/** This class runs an DS1920 iButton server.
  */
public class TemperatureWorker
  implements Runnable
{
  int     currentTemperature;
  
  TINIExternalAdapter adapter;
  OneWireContainer10 tempSensor;
  
  /** Constructor
   */
  public TemperatureWorker()
  {
    adapter = new TINIExternalAdapter();
  }
  /** Return the current temperature
   */
  public int getCurrentTemperature()
  {
    return currentTemperature;
  }
  /** Return buttonFound
   */
  public boolean buttonFound()
  {
    return (tempSensor != null);
  }
  /** Run the Temperature server thread
   */
  public void run()
  {
    byte[] state;
    
    while(true)
    {
      try
      {   
        
        
        if (tempSensor == null)
        {
            adapter.targetFamily(0x10);
            tempSensor = (OneWireContainer10)adapter.getFirstDeviceContainer();
            
        }
        
        if (tempSensor != null)
        {
            if (!tempSensor.isPresent())
                tempSensor = null;
            else
            {
                state = tempSensor.readDevice();
                tempSensor.doTemperatureConvert(state);
                state = tempSensor.readDevice();
                currentTemperature = (int)tempSensor.convertToFahrenheit(tempSensor.getTemperature(state));
            }
        }
        else
        {
          currentTemperature = Integer.MIN_VALUE;
        }
      }      
      catch(Throwable t)
      {
        // why kill the server if the exception is not fatal?
        //System.out.println(t);        
      }      
    }
  }
}
