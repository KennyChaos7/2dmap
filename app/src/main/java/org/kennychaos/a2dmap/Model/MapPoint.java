package org.kennychaos.a2dmap.Model;

/**
 * Created by Kenny on 18-2-5.
 */

public class MapPoint {
    public final static int TYPE_OBS = 1;
    public final static int TYPE_EMPTY = 0;
    public final static int TYPE_CLEANED = 10;
    public final static int TYPE_TRACK = 9;

    private int x = 0;
    private int y = 0;
    private int type = -1;

    public MapPoint(int x, int y, int type)
    {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    @Override
    public String toString()
    {
        return "MapPoint x = " + x + " y = " + y + " type = " + type;
    }
}
