package org.kennychaos.a2dmap;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;
import org.kennychaos.a2dmap.Listener.MapListener;
import org.kennychaos.a2dmap.Listener.TCPListener;
import org.kennychaos.a2dmap.Model.BlockMap;
import org.kennychaos.a2dmap.Model.Track;
import org.kennychaos.a2dmap.Utils.Base64Util;
import org.kennychaos.a2dmap.Utils.LogUtil;
import org.kennychaos.a2dmap.Utils.MapUtil;
import org.kennychaos.a2dmap.Utils.TCPUtil;
import org.xutils.view.annotation.Event;
import org.xutils.x;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements TCPListener, MapListener {

    private String local_ip = "";
    private TCPUtil tcpUtil = null;
    private MapUtil mapUtil = null;
    private LogUtil logUtil = null;
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        x.view().inject(this);
        logUtil = new LogUtil(this);

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        try {
            local_ip = String.valueOf(InetAddress.getByName(__formatString(wifiInfo.getIpAddress()))).substring(1);
            logUtil.show("local_ip = " + local_ip , LogUtil.LOG_LEVEL_INFO);
            tcpUtil = new TCPUtil(8088,local_ip);
            tcpUtil.registerListener(this);
            mapUtil = new MapUtil();
            mapUtil.registerListener(this);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }


    @Event(value = R.id.btn_tcp,type = Button.OnClickListener.class)
    private void start_tcp(View view){
//        tcpUtil.setRoomba_ip("192.168.1.102");
        tcpUtil.conn();
    }

    @Event(value = R.id.btn_first,type = Button.OnClickListener.class)
    private void first(View view) {
        String _ = Base64Util.encode(__intToByteArray(0));
        JSONObject jo = new JSONObject();
        try {
            jo.put("map",_);
            jo.put("trak",_);
            tcpUtil.send(jo.toString().getBytes());
            Log.e("--",jo.toString() + " \n " + bytesToHexString(jo.toString().getBytes()));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void receive(byte[] bytes, int length) {
        logUtil.show("receive = " + Arrays.toString(bytes) +" \nsource = " + bytesToHexString(bytes) + " \nlength = " + length,LogUtil.LOG_LEVEL_INFO);
        mapUtil.analysis(new String(bytes));
    }

    @Override
    public void sent(byte[] bytes, int length) {
        logUtil.show("sent = " + Arrays.toString(bytes) +" \nsource = " + bytesToHexString(bytes) + " \nlength = " + length,LogUtil.LOG_LEVEL_INFO);
    }

    @Override
    public void state(String roomba_ip, int port, boolean isConnected, boolean isRec, boolean isCanSend) {
        logUtil.show("roomba_ip = " + roomba_ip + " port = " + port + " isConnected = " + isConnected,LogUtil.LOG_LEVEL_INFO);
    }

    private String bytesToHexString(byte[] src){
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            stringBuilder.append("0x");
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv).append(",");
        }
        return stringBuilder.toString();
    }

    private String __formatString(int value) {
        String strValue = "";
        byte[] ary = __intToByteArray(value);
        for (int i = ary.length - 1; i >= 0; i--) {
            strValue += (ary[i] & 0xFF);
            if (i > 0) {
                strValue += ".";
            }
        }
        return strValue;
    }

    private byte[] __intToByteArray(int value) {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            int offset = (b.length - 1 - i) * 8;
            b[i] = (byte) ((value >>> offset) & 0xFF);
        }
        return b;
    }

    @Override
    public void receiveBlockMap(BlockMap blockMap) {

    }

    @Override
    public void receiveBlockMapInfo(int history_id, int index_in_whole_map, int length) {

    }

    @Override
    public void receiveTrack(Track track) {

    }

    @Override
    public void receiveTrackInfo(int index_begin, int index_end, int area_cleaned) {

    }
}
