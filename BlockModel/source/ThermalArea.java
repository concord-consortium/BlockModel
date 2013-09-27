class ThermalArea 
{
    float temp;
    ModelObject mo;

    public ThermalArea(ModelObject o)
    {
	mo = o;
	temp = o.initTemp;
    }

}
