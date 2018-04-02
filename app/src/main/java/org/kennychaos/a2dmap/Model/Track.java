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
    
    /**
     * 该轨迹的启始序号
     */
    private int index_begin = 0;

    /**
     * 该轨迹的末序号
     */
    private int index_end = 0;

    /**
     * 清扫面积
     */
    private int area_cleaned = 0;

    /**
     * 该轨迹的数据
     */
    private List<MapPoint> mapPointList = new ArrayList<>();

    public Track(){}

    public Track(int index_begin,int index_end,int area_cleaned,List<MapPoint> mapPointList)
    {
        this.index_begin = index_begin;
        this.index_end = index_end;
        this.area_cleaned = area_cleaned;
        this.mapPointList = mapPointList;
    }

    /**
     * 增加轨迹
     */
    public void add(Track newTrack)
    {
        if (newTrack != null)
        {
            /**
             * 只有当前上一次轨迹数据的末点序号跟新接受到轨迹的启始序号一样才能证明是上一段轨迹的延续
             * 否则只能从新获取
             * 
             * ps: 此app轨迹都是使用从头获取的方式来
             */
            if (newTrack.getIndex_begin() == this.getIndex_end())
            {
                this.index_begin = this.index_end;
                this.index_end = newTrack.getIndex_end();
                this.mapPointList.addAll(newTrack.getMapPointList());
                this.area_cleaned = newTrack.getArea_cleaned();
            }
            else
                this.index_end = 0;
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
