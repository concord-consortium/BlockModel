


public class LinDataProbe extends Object implements uCsimmDataProbe implements Runnable{
    float A2Dfactor = (float)((float)(1 << 24) / (float)50000.0);
    int MTcalibrations[] = {-7, -2, -2, 4, -4, -4, 10, 11};

    int count;
    public String units = null;
    public int numValues;
    int id;

    public LinDataProbe()
    {

    }
	
    public void run()
    {
	while(true){
	    values = pm.getData(this);
	    ret = pm.status;

	    if(ret == 0 && values != null && curProbe != null){
		// We got a point print it!
		curProbe.convert(values);
		
		for(i=0; i<values.length; i++){
		    System.out.print(values[i] + " ");
		}
		System.out.println(" ");

	    } else if(ret == -1){

		System.out.println("ErrDP:" + pm.errStr);
		break;
	      
	    } else if(ret == 1){

		System.out.println("Stopped:" + pm.msgStr);
		break;

	    } else if(ret == 2){
		// Try again
		continue;
	    } else {
		System.out.println("Unknown Error");
		break;
	    }

	}



    }

    public void init(int _id, int nv)
    {
	id = _id;
	numValues = nv;
	
    }

    public void convert(float []values)
    {
	int i;

	if(id == 1){
	    values[0] = (float)(values[0] / A2Dfactor);
	} else {
	    // Compute degrees
	    for(i=0; i<8; i++){
		values[i] = (float)((float)(values[i] + (float)MTcalibrations[i] - 
					    (float)819) * 
				    (float)0.06105);
	    }
	}
    }

    public boolean matches(int _nv)
    {
	return (_nv == numValues);
    }
}















