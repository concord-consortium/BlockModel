Readme.txt (DC, 09.14.01)

-=---------------=-
TINI Firmware 1.02c
-=---------------=-
--------------------------------------------------------------------
Go to http://www.ibutton.com/TINI/book.html for an online version of 
"The TINI Specification and Developer's Guide".
--------------------------------------------------------------------

-----------------
| TINIWebServer |
-----------------

I. Description
=------------=
  TINIWebserver is a simple web server that demonstrates the use of
  the com.dalsemi.tininet.http.HTTPServer class. This example uses
  three Threads to respond to requests on TCP port 80, poll a DS1920
  temperature iButton and update the index web page. 

  This webserver logs every access attempt to a file called web.log.
  If TINIWebServer is accessed frequently the log can grow large enough
  to consume all free memory in the heap. You can disable logging in the
  WebWorker class or come up with some mechanism to manage the log file
  size. 


II. Files
=------=
  TINIWebserver.java
  WebWorker.java
  TemperatureSensor.java
  TemperatureWorker.java

III. Instructions
=--------------=
  Execute "java TINIWebServer.tini" at a slush prompt.


IV. Revision History
=-------------------=
  alpha4  Created.
  alpha6  Updated for multi-threading.
  beta3.0 Updated for new iButton API.
  1.02    Updated for OWAPI.

