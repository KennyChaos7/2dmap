package org.kennychaos.a2dmap.Utils;

import android.nfc.Tag;
import android.util.Log;

import com.google.gson.Gson;

import org.kennychaos.a2dmap.Listener.MapListener;
import org.kennychaos.a2dmap.Model.BlockMap;
import org.kennychaos.a2dmap.Model.MapPoint;
import org.kennychaos.a2dmap.Model.Track;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

/**
 * Created by Kenny on 18-2-5.
 */

public class MapUtil {
    private final static byte TYPE_WHOLE_MAP_SMALL = 0;
    private final static byte TYPE_WHOLE_TRACK_SMALL = 1;
    private final static byte TYPE_WHOLE_TRACK_BIG = 2;
    private final static byte TYPE_ADD_TRACK_BIG = 4;
    private final static byte TYPE_ADD_MAP_BIG = 5;
    private final static byte TYPE_ADD_MAP_SMALL = 6;
    private final static byte TYPE_ADD_MAP_ANY = 7;
    private final static int REFLEX_BLOCKMAP_DATA = 0x31;
    private final static int REFLEX_BLOCKMAPLIST_DATA = 0x32;
    private final static int REFLEX_TRACK_DATA = 0x33;
    private final static int REFLEX_BLOCKMAP_INFO = 0x34;
    private final static int REFLEX_TRACK_INFO = 0x35;

    private List<BlockMap> blockMapList = Collections.synchronizedList(new Vector<BlockMap>());
    private Track track = new Track();
    private List<MapListener> mapListenerList = Collections.synchronizedList(new Vector<MapListener>());
    private boolean isInit = false;
    private Gson gson = new Gson();

    public void registerListener(MapListener mapListener)
    {
        List<MapListener> listeners = mapListenerList;
        synchronized (mapListenerList)
        {
            for (MapListener l : listeners)
            {
                if (l.hashCode() != mapListener.hashCode())
                    listeners.add(mapListener);
            }
            if (listeners.size() == 0)
                listeners.add(mapListener);
        }
    }

    public void unregisterListener(MapListener mapListener)
    {
        List<MapListener> listeners = mapListenerList;
        synchronized (mapListenerList)
        {
            for (MapListener l : listeners)
            {
                if (l == mapListener)
                    listeners.remove(l);
            }
        }
    }


    public void analysis(String data)
    {
        initList();
        MapData mapData = gson.fromJson(data,MapData.class);
        Log.e("analysis",data);
        if (mapData.map != null)
        {
            // TODO analysis map
            byte[] map_data_decode = Base64Util.decode(mapData.map);
            setBlockMap(map_data_decode);
        }
        if (mapData.track != null)
        {
            // TODO analysis track
            byte[] track_data_decode = Base64Util.decode(mapData.track);
            setTrack(track_data_decode);
        }
    }

    private void initList()
    {
        if (!isInit)
        {
            for (int i = 0;i < 99 ; i++)
                blockMapList.add(new BlockMap(i));
            isInit = true;
        }
    }

    private void setBlockMap(byte[] map_data_decode) {
        int index_in_whole_map = -1;
        int history_id = -1;
        int data_length = 0;

        byte type = map_data_decode[0];
        int count = to_int(map_data_decode,2,2);

        byte[] array_data = new byte[map_data_decode.length - 4];
        System.arraycopy(map_data_decode,4,array_data,0,array_data.length);

        for (int index = 0; index < count - 1 && array_data.length > 0;index ++ )
        {
            index_in_whole_map = to_int(array_data,2,0);
            history_id = to_int(array_data,2,2);
            data_length = to_int(array_data,2,4);
            if (data_length > 0) {
                byte[] data_compress = new byte[data_length];
                System.arraycopy(array_data, 6 , data_compress, 0, data_length);
                byte[] data_uncompress = uncompress(data_compress, data_length);// 100x100
                int x_begin = (index_in_whole_map - 1) % 10 * 100 ;
                int y_begin = (index_in_whole_map - 1) / 10 * 100 ;
                BlockMap blockMap_new = new BlockMap(history_id, index_in_whole_map, data_length, analysis_bytes(data_uncompress, x_begin, y_begin));

                for (BlockMap blockMap_old : blockMapList) {
                    if (blockMap_old.getIndex_in_whole_map() == index_in_whole_map && blockMap_old.getHistory_id() < history_id) {
                        blockMap_old.setHistory_id(history_id);
                        blockMap_old.setIndex_in_whole_map(index_in_whole_map);
                        blockMap_old.setLength(data_length);
                        blockMap_old.setMapPointList(blockMap_new.getMapPointList());
                        // TODO reflex to listener
                        reflex(blockMap_new, REFLEX_BLOCKMAP_DATA);
                        reflex(blockMap_new, REFLEX_BLOCKMAP_INFO);
                    }
                }

            }
            array_data = bytes_delete(array_data,2 + 2 + 2 + data_length);
        }
        reflex(blockMapList,REFLEX_BLOCKMAPLIST_DATA);
        Log.v("blockmap list ", Arrays.toString(blockMapList.toArray()));
    }

    private void setTrack(byte[] track_data_decode)
    {
        int type = track_data_decode[0];
        int count_bytes_mappoint = track_data_decode[1];
        int area_cleaned = to_int(track_data_decode,2,2);
        int index_begin = to_int(track_data_decode,2,4);
        int index_end = to_int(track_data_decode,2,6);
        int length = track_data_decode.length - 8;
        byte[] data = new byte[length];
        System.arraycopy(track_data_decode,8,data,0,length);
        if (index_begin != 0 && track.getIndex_end() == index_begin)
        {
            // TODO analysis track data
            track.setMapPointList(analysis_bytes(data,count_bytes_mappoint));
            track.setIndex_begin(index_begin);
            track.setIndex_end(index_end);
            track.setArea_cleaned(area_cleaned);
        }else if (track.getIndex_end() != index_begin && index_begin != 0)
        {
            // TODO abandon this track

        }else if (index_begin == 0)
        {
            // TODO reset track data
            track = new Track(index_begin,index_end,area_cleaned,analysis_bytes(data,count_bytes_mappoint));
        }
        // TODO reflex to listener
        reflex(track,REFLEX_TRACK_DATA);
        reflex(track,REFLEX_TRACK_INFO);
    }

    private List<MapPoint> analysis_bytes(byte[] bytes,int x_begin , int y_begin) {
        List<MapPoint> mapPointList = new ArrayList<>();
        int x = 0;
        int y = 0;
        for (int i = 0; i < bytes.length; i++) {
            if (i > 0 && i % 25 == 0) {
                y++;
                x = 0;
            }
            for (int j = 7; j > 0; j = j - 2) {
                String b1 = String.valueOf((byte) ((bytes[i] >> j) & 0x1));
                String b2 = String.valueOf((byte) ((bytes[i] >> (j - 1)) & 0x1));
                int type = Integer.parseInt(b1 + b2);
                if (type != 0) {
                    MapPoint mapPoint = new MapPoint(x + x_begin ,y + y_begin , type);
                    mapPointList.add(mapPoint);
                }
                x++;
            }
        }
        return mapPointList;
    }

    private List<MapPoint> analysis_bytes(byte[] bytes,int count_bytes_mappoint)
    {
        List<MapPoint> mapPointList = new ArrayList<>();
        int interval = count_bytes_mappoint / 2;
        for (int i = 0; i <= bytes.length - count_bytes_mappoint; i+=count_bytes_mappoint ) {
            int x = to_int(bytes, interval, i);
            int y = to_int(bytes, interval, i + interval);
            MapPoint mapPoint = new MapPoint(x,y,MapPoint.TYPE_TRACK);
            mapPointList.add(mapPoint);
        }
        return mapPointList;
    }

    private byte[] uncompress(byte[] compress, int len) {
        byte[] uncompress_buf = new byte[2500];
        int repeat_len = 0;
        int index = 0;
        for (int i = 0; i < len; i++) {
            if ((compress[i] & (0x3 << 6)) == (0x3 << 6)) {
                repeat_len <<= 6;
                repeat_len += compress[i] & 0x3F;
            } else {
                if (repeat_len != 0) {
                    int j = 0;
                    for (j = 0; j < repeat_len; j++) {
                        uncompress_buf[index++] = compress[i];
                    }
                    repeat_len = 0;
                } else {
                    uncompress_buf[index++] = compress[i];
                }
            }
        }
        assert (index != 2500);
        return uncompress_buf;
    }

    private int to_int(byte[] bytes, int length, int start)
    {
        byte[] buffer = new byte[length];
        System.arraycopy(bytes, start, buffer, 0, length);
        int MASK = 0xFF;
        int result = 0;
        result += (buffer[1] & MASK) + ((buffer[0] & MASK) << 8);
        return result;
    }

    private byte[] bytes_delete(byte[] target , int index)
    {
        int l = target.length - index;
        byte[] r = new byte[l];
        if (index == l)
            return new byte[]{};
        else if (index < l)
        {
            System.arraycopy(target,index,r,0,l);
            return r;
        }
        return new byte[]{};
    }

    private void reflex(Object o,int type)
    {
        List<MapListener> listeners = mapListenerList;
        synchronized (mapListenerList)
        {
            for (MapListener l : listeners) {
                switch (type) {
                    case REFLEX_BLOCKMAP_DATA:
                        l.receiveBlockMap((BlockMap) o);
                        break;
                    case REFLEX_BLOCKMAPLIST_DATA:
                        l.receiveBlockMapList((List<BlockMap>) o);
                        break;
                    case REFLEX_TRACK_DATA:
                        l.receiveTrack((Track) o);
                        break;
                    case REFLEX_BLOCKMAP_INFO:
                        BlockMap blockMap = (BlockMap)o;
                        l.receiveBlockMapInfo(blockMap.getHistory_id(),blockMap.getIndex_in_whole_map(),blockMap.getLength());
                        break;
                    case REFLEX_TRACK_INFO:
                        Track track = (Track)o;
                        l.receiveTrackInfo(track.getIndex_begin(),track.getIndex_end(),track.getArea_cleaned());
                        break;
                }
            }
        }
    }

    public List<BlockMap> getBlockMapList() {
        return blockMapList;
    }

    public Track getTrack() {
        return track;
    }

    class MapData
    {
        String map = null;
        String track = null;
    }
}
