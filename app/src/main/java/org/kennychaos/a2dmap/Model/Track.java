package org.kennychaos.a2dmap.Model;

import android.annotation.TargetApi;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kenny on 18-2-5.
 */

public class Track {
    private int history_id = -1;
    private int index_in_whole_map = -1;
    private int index_begin = -1;
    private int index_end = -1;
    private int area_cleaned = -1;
    private List<MapPoint> mapPointList = new ArrayList<>();

    Track(){}

    Track(int history_id,int index_in_whole_map,int index_begin,int index_end,int area_cleaned,List<MapPoint> mapPointList)
    {
        this.history_id = history_id;
        this.index_in_whole_map = index_in_whole_map;
        this.index_begin = index_begin;
        this.index_end = index_end;
        this.area_cleaned = area_cleaned;
        this.mapPointList = mapPointList;
    }

    public int getHistory_id() {
        return history_id;
    }

    public void setHistory_id(int history_id) {
        this.history_id = history_id;
    }

    public int getIndex_in_whole_map() {
        return index_in_whole_map;
    }

    public void setIndex_in_whole_map(int index_in_whole_map) {
        this.index_in_whole_map = index_in_whole_map;
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
        this.mapPointList = mapPointList;
    }
}
