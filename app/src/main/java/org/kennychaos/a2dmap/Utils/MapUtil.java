package org.kennychaos.a2dmap.Utils;

import android.util.Log;

import com.google.gson.Gson;

import org.kennychaos.a2dmap.Listener.MapListener;
import org.kennychaos.a2dmap.Model.BlockMap;
import org.kennychaos.a2dmap.Model.MapPoint;
import org.kennychaos.a2dmap.Model.Track;

import java.util.ArrayList;
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


    private List<BlockMap> blockMapList = Collections.synchronizedList(new Vector<BlockMap>());
    private List<Track> trackList = Collections.synchronizedList(new Vector<Track>());
    private List<MapListener> mapListenerList = Collections.synchronizedList(new Vector<MapListener>());

    private Gson gson = new Gson();

    public void analysis(String data)
    {
        MapData mapData = gson.fromJson(data,MapData.class);
        Log.e("analysis",data);
        if (mapData.map != null)
        {
            // TODO analysis map
            byte[] map_data_decode = Base64Util.decode(mapData.map);
            setBlockMap(map_data_decode);
        }
        if (mapData.trak != null)
        {
            // TODO analysis track
            byte[] track_data_decode = Base64Util.decode(mapData.trak);
            setTrack(track_data_decode);
        }
    }

    private void setBlockMap(byte[] map_data_decode) {
        int index_in_whole_map = -1;
        int history_id = -1;
        int data_length = -1;

        byte type = map_data_decode[0];
        int count = to_int(map_data_decode,2,2);

        byte[] array_data = new byte[map_data_decode.length - 4];
        System.arraycopy(map_data_decode,4,array_data,0,array_data.length);

        for (int index = 0; index < count ;index ++ )
        {
            index_in_whole_map = to_int(array_data,2,0 + data_length + 4);
            history_id = to_int(array_data,2,2 + data_length + 4);
            data_length = to_int(array_data,2,4 + data_length + 4);
            byte[] data_compress = new byte[data_length];
            System.arraycopy(array_data,7 + data_length + 4,data_compress,0,data_length);
            byte[] data_uncompress = uncompress(data_compress,data_length);// 100x100

        }
    }

    private List<MapPoint> analysis_bytes(byte[] bytes) {

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
        result += (buffer[0] & MASK) + ((buffer[1] & MASK) << 8);
        return result;
    }

    class MapData
    {
        String map = null;
        String trak = null;
    }
}
