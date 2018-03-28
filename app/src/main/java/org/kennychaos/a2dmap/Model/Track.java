package org.kennychaos.a2dmap.Model;

import android.annotation.TargetApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

/**
 * Created by Kenny on 18-2-5.
 */

public class Track {
    private int index_begin = 0;
    private int index_end = 0;
    private int area_cleaned = 0;
    private List<MapPoint> mapPointList = Collections.synchronizedList(new Vector<MapPoint>());

    public Track(){}

    public Track(int index_begin,int index_end,int area_cleaned,List<MapPoint> mapPointList)
    {
        this.index_begin = index_begin;
        this.index_end = index_end;
        this.area_cleaned = area_cleaned;
        this.mapPointList = mapPointList;
    }

    public void add(Track newTrack)
    {
        if (newTrack != null)
        {
            if (newTrack.getIndex_begin() == this.getIndex_end())
            {

            }
        }
    }

    @Override
    public String toString()
    {
        return "index_begin = " + index_begin + " index_end = " + index_end + " area_cleaned = " + area_cleaned + " mapPointList.size = " + mapPointList.size();
    }

    public String getDetails()
    {
        return toString() + "\nmapPointList = " + Arrays.toString(mapPointList.toArray());
    }

    public int getIndex_begin() {
        return index_begin;
    }

    public void setIndex_begin(int index_begin) {
        this.index_begin = index_begin;
    }

    public int getIndex_end() {
        return index_end;
    }

    public void setIndex_end(int index_end) {
        this.index_end = index_end;
    }

    public int getArea_cleaned() {
        return area_cleaned;
    }

    public void setArea_cleaned(int area_cleaned) {
        this.area_cleaned = area_cleaned;
    }

    public List<MapPoint> getMapPointList() {
        return mapPointList;
    }

    public void setMapPointList(List<MapPoint> mapPointList) {
        this.mapPointList.addAll(mapPointList);
    }
}
