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

    void receiveSingleBlockMap(BlockMap blockMap);

    void receiveBlockMapList(List<BlockMap> blockMapList,int updateBlockMapCounts,List<Integer> updateBlockMapIndexList);

    void receiveTrack(Track track,int indexBegin,int indexEnd,int cleanedArea);

    void onError(int errorCode,String errorMessage);
}
