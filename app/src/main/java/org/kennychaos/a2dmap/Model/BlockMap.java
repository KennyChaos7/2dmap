package org.kennychaos.a2dmap.Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

/**
 * Created by Kenny on 18-2-5.
 */

public class BlockMap {

    private int history_id = -1;
    private int index_in_whole_map = -1;
    private int length = -1;
    private List<MapPoint> mapPointList = new ArrayList<>();

    BlockMap(){}

    BlockMap(int history_id,int index_in_whole_map,int length,List<MapPoint> mapPointList)
    {
         this.history_id = history_id;
         this.index_in_whole_map = index_in_whole_map;
         this.length = length;
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

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public List<MapPoint> getMapPointList() {
        return mapPointList;
    }

    public void setMapPointList(List<MapPoint> mapPointList) {
        this.mapPointList = mapPointList;
    }
}
