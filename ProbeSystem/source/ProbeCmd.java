public class ProbeCmd 
{
    public final static int START = 0;
    public final static int STOP = 1;
    public final static int READ_INFO = 2;
    public final static int GET_PROBES = 3;

    int cmd;
    ProbeState ps;
    ProbeClient pc;

    boolean completed;

    public ProbeCmd(int c, ProbeClient _pc, ProbeState _ps)
    {
	cmd = c;
	ps = _ps;
	pc = _pc;
	completed = false;
    }

}
