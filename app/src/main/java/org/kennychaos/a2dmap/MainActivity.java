package org.kennychaos.a2dmap;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import org.json.JSONException;
import org.json.JSONObject;
import org.kennychaos.a2dmap.Listener.MapListener;
import org.kennychaos.a2dmap.Listener.TCPListener;
import org.kennychaos.a2dmap.Model.BlockMap;
import org.kennychaos.a2dmap.Model.Track;
import org.kennychaos.a2dmap.Utils.LogUtil;
import org.kennychaos.a2dmap.Utils.MapUtil;
import org.kennychaos.a2dmap.Utils.TCPUtil;
import org.kennychaos.a2dmap.View.MapView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.kennychaos.a2dmap.Utils.Transcode.__bytesToHexString;
import static org.kennychaos.a2dmap.Utils.Transcode.__formatString;
import static org.kennychaos.a2dmap.Utils.Transcode.__intToByteArray;

public class MainActivity extends AppCompatActivity implements TCPListener, MapListener {

    private TCPUtil tcpUtil = null;
    private MapUtil mapUtil = null;
    private LogUtil logUtil = null;
    private MapView mapView = null;
    private ExecutorService singleThreadPool = Executors.newSingleThreadExecutor();
    private boolean isRefresh = true;
    private List<BlockMap> blockMapList = new ArrayList<>();
    private Track track = new Track();


    @ViewInject(R.id.linear_mapview)
    private LinearLayout linear_mapview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        x.view().inject(this);
        logUtil = new LogUtil(this);

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager != null ? wifiManager.getConnectionInfo() : null;
        try {
            String local_ip = String.valueOf(InetAddress.getByName(__formatString(wifiInfo != null ? wifiInfo.getIpAddress() : 0))).substring(1);
            logUtil.show("local_ip = " + local_ip, LogUtil.LOG_LEVEL_INFO);
            tcpUtil = new TCPUtil(8088, local_ip);
            tcpUtil.registerListener(this);
            mapUtil = new MapUtil();
            mapUtil.registerListener(this);
            mapView = new MapView(this);
            mapView.init(1f,"#D8B0B0B0",1000,1000);
            linear_mapview.addView(mapView);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Event(value = R.id.btn_search, type = Button.OnClickListener.class)
    private void search(View view)
    {
        mapView.clear();
        tcpUtil.search();
    }

    @Event(value = R.id.btn_tcp,type = Button.OnClickListener.class)
    private void start_tcp(View view){
        mapView.clear();
        tcpUtil.conn();
    }

    @Event(value = R.id.btn_first,type = Button.OnClickListener.class)
    private void first(View view) {
        mapView.clear();
        String _ = Base64.encodeToString(__intToByteArray(0,4),Base64.NO_WRAP);
        JSONObject jo = new JSONObject();
        try {
            jo.put("map",_);
            jo.put("track",_);
            int jo_length = jo.toString().getBytes().length;
            byte[] bytes_send = new byte[jo_length + 4];
            byte[] temp = __intToByteArray(jo_length,4);
            System.arraycopy(temp,0,bytes_send,0,4);
            System.arraycopy(jo.toString().getBytes(),0,bytes_send,4,jo_length);
            tcpUtil.send(bytes_send);
            bytes_send = null;
            temp = null;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Event(value = R.id.btn_update_map,type = Button.OnClickListener.class)
    private void updateMap(View v)
    {
        mapView.clear();
        byte[] historyIdsToBytes = new byte[100 * 2];
        for (int index = 0; index < 100; index++)
        {
            int historyId = this.blockMapList.get(index).getHistory_id();
            byte[] temp = __intToByteArray(historyId,2);
            System.arraycopy(temp,0,historyIdsToBytes,index * 2 , 2);
        }
        String _map = Base64.encodeToString(historyIdsToBytes,Base64.NO_WRAP);
        String _track = Base64.encodeToString(__intToByteArray(this.track.getIndex_end(),2),Base64.NO_WRAP);
        JSONObject jo = new JSONObject();
        try {
            jo.put("map",_map);
            jo.put("track",_track);
            int jo_length = jo.toString().getBytes().length;
            byte[] bytes_send = new byte[jo_length + 4];
            byte[] temp = __intToByteArray(jo_length,4);
            System.arraycopy(temp,0,bytes_send,0,4);
            System.arraycopy(jo.toString().getBytes(),0,bytes_send,4,jo_length);
            tcpUtil.send(bytes_send);
            bytes_send = null;
            temp = null;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReceive(byte[] bytes, int length) {
        logUtil.show("receive = " + Arrays.toString(bytes) +" \nsource = " + __bytesToHexString(bytes) + " \nlength = " + length,LogUtil.LOG_LEVEL_INFO);
        mapUtil.analysis(new String(bytes));
    }

    @Override
    public void onSend(byte[] bytes) {
        logUtil.show("sent = " + Arrays.toString(bytes) +" \nsource = " + __bytesToHexString(bytes) + " \nlength = " + bytes.length,LogUtil.LOG_LEVEL_INFO);

    }

    @Override
    public void onError(int errorCode, String errorMessage) {

    }

    @Override
    public void onState(String stateMessage) {
        logUtil.show(stateMessage,LogUtil.LOG_LEVEL_INFO);
    }

    @Override
    public void receiveSingleBlockMap(BlockMap blockMap) {
        logUtil.show("receiveSingleBlockMap : " + blockMap.getDetails(),LogUtil.LOG_LEVEL_INFO);
        int index = blockMap.getIndex_in_whole_map();
        this.blockMapList.set(index,blockMap);
        mapView.setBlockMapList(mapUtil.getBlockMapList());
        mapView.refresh();
    }

    @Override
    public void receiveBlockMapList(List<BlockMap> blockMapList, int updateBlockMapCounts, List<Integer> updateBlockMapIndexList) {
        logUtil.show("receiveBlockMapList : " + blockMapList.size(),LogUtil.LOG_LEVEL_INFO);
        this.blockMapList = blockMapList;
        mapView.setBlockMapList(mapUtil.getBlockMapList());
        mapView.refresh();
    }

    @Override
    public void receiveTrack(Track track, int indexBegin, int indexEnd, int cleanedArea) {
        logUtil.show("track : " + track.getDetails(),LogUtil.LOG_LEVEL_INFO);
        this.track.add(track);
        mapView.setTrack(mapUtil.getTrack());
        mapView.refresh();
    }

}
