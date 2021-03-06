package org.kennychaos.a2dmap.Model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

/**
 * Created by Kenny on 18-2-5.
 */

public class BlockMap {

    /**
     * 历史更新标识
     * 默认该表示必须为1
     * 当每获取到一个分块地图的时候必须更新此标识，并且在除开第一次获取地图外，每次获取最新的地图时将必须将100块的分块地图的历史更新标识组成一个byte数组发送出去
     */
    private int history_id = 1;

    /**
     * 该分块地图在100块分块地图中的序号
     */
    private int index_in_whole_map = -1;

    /**
     * 该分块的数据长度
     */
    private int length = -1;

    /**
     * 该分块地图的地图数据
     */
    private List<MapPoint> mapPointList = new ArrayList<>();

    @Deprecated
    public BlockMap(int index_in_whole_map){
        this.index_in_whole_map = index_in_whole_map;
    }

    public BlockMap(int history_id,int index_in_whole_map,int length,List<MapPoint> mapPointList)
    {
         this.history_id = history_id;
         this.index_in_whole_map = index_in_whole_map;
         this.length = length;
         this.mapPointList = mapPointList;
    }

    @Override
    public String toString()
    {
        return "\nhistory_id = " + history_id + " index_in_whole_map = " + index_in_whole_map + " length = " + length + " mapPointList.size = " + mapPointList.size() + "\n";
    }

    public String getDetails()
    {
        return toString() + " mapPointList = " + Arrays.toString(mapPointList.toArray());
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
