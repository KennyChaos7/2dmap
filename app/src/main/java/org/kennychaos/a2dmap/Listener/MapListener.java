package org.kennychaos.a2dmap.Listener;

import org.kennychaos.a2dmap.Model.BlockMap;
import org.kennychaos.a2dmap.Model.MapPoint;
import org.kennychaos.a2dmap.Model.Track;

import java.util.List;

/**
 * Created by Kenny on 18-2-5.
 */

public interface MapListener {

    void receiveBlockMap(BlockMap blockMap);

    void receiveBlockMapInfo(int history_id,int index_in_whole_map,int length);

    void receiveTrack(Track track);

    void receiveTrackInfo(int history_id, int index_in_whole_map, int index_begin, int index_end, int area_cleaned);
}
