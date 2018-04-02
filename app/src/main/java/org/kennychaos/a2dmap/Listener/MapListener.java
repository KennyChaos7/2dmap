package org.kennychaos.a2dmap.Listener;

import org.kennychaos.a2dmap.Model.BlockMap;
import org.kennychaos.a2dmap.Model.MapPoint;
import org.kennychaos.a2dmap.Model.Track;

import java.util.List;

/**
 * Created by Kenny on 18-2-5.
 */

public interface MapListener {
    int REFLEX_RECEIVE_BLOCKMAPLIST = 0x0035;
    int REFLEX_RECEIVE_SINGLE_BLOCKMAP = 0x0036;
    int REFLEX_RECEIVE_TRACK = 0x0037;
    int REFLEX_ON_ERROR = 0x0038;

    /**
     * 理论上单只有一块分块地图更新时才会调用，但多次测试后发现其实并不太可能只更新一块分块地图
     */
    void receiveSingleBlockMap(BlockMap blockMap);

    /**
     * 当更新多块地图时，将会调用到这个监听
     */
    void receiveBlockMapList(List<BlockMap> blockMapList,int updateBlockMapCounts,List<Integer> updateBlockMapIndexList);

    /**
     * 当有任意长度的轨迹都将会调用到这个监听
     */
    void receiveTrack(Track track,int indexBegin,int indexEnd,int cleanedArea);

    @Deprecated
    void onError(int errorCode,String errorMessage);
}
