package graph;

public abstract class Graph2D
{
    boolean redraw = true;

    public abstract void resize(int w, int h);

    public abstract void setYRange(float min, float range);

    public abstract void setXRange(float min, float range);

    public abstract Object addBin(int location, String label);

    public abstract boolean removeBin(Object id);

    public abstract void draw(JGraphics g, int x, int y);

    public abstract int plot(JGraphics g);

    public abstract void reset();

    public abstract boolean addPoint(float x, float values[]);

    public abstract boolean addPoint(Object binID, float x, float value);

}
