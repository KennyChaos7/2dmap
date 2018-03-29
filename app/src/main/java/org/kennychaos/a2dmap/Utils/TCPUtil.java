package org.kennychaos.a2dmap.Utils;

import android.support.annotation.NonNull;
import android.util.Log;

import org.kennychaos.a2dmap.Listener.TCPListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Kenny on 18-2-5.
 */

public class TCPUtil {
    private final String TAG = "=====" + getClass().getSimpleName().toLowerCase() + "===== ";
    private static final String COMMAND_SEARCH_IP = "version:[1],?\n";

    private int port = -1;
    private ExecutorService S = Executors.newSingleThreadExecutor();
    private ExecutorService R = Executors.newSingleThreadExecutor();
    private Runnable searchRunnable = null;
    private String roombaIP = "";
    private String localhost = "";
    private Socket client = null;
    private InputStream client_im = null;
    private OutputStream client_om = null;
    private HashMap<String,Object> hashMap = new HashMap<>();
    private List<TCPListener> tcpListenerList = Collections.synchronizedList(new Vector<TCPListener>());

    public TCPUtil(@NonNull final int port, @NonNull final String localhost) {
        this.port = port;
        this.localhost = localhost;
        searchRunnable = new Runnable() {
            @Override
            public void run() {
                synchronized (roombaIP) {
                    DatagramSocket socket = null;
                    try {
                        socket = new DatagramSocket(port);
                        String _ip = localhost.substring(0, localhost.lastIndexOf(".")) + ".255";
                        socket.send(new DatagramPacket(COMMAND_SEARCH_IP.getBytes(), COMMAND_SEARCH_IP.getBytes().length, InetAddress.getByName(_ip), 12002));
                        byte[] bytes_packet = new byte[1024];
                        DatagramPacket packet = new DatagramPacket(bytes_packet, bytes_packet.length);
                        socket.receive(packet);
                        if (packet.getData().length > 0 && Objects.equals(roombaIP, ""))
                            roombaIP = packet.getAddress().getHostAddress();
                        Log.e(TAG, "roombaIP " + roombaIP);
                        socket.close();
                    } catch (SocketException e) {
                        e.printStackTrace();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    public void search()
    {
        S.execute(searchRunnable);
    }

    public void conn()
    {
        Log.e(TAG,"conn");
        if (Objects.equals(roombaIP, ""))
            throw new NullPointerException("roomba ip is null");
        else {
            Executors.newSingleThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        client = new Socket(roombaIP,port);
                        client_im = client.getInputStream();
                        client_om = client.getOutputStream();
                        // TODO reflexToListener to listener client'S state
                        hashMap.clear();
                        hashMap.put("stateMessage","client is ready. ");
                        reflexToListener(TCPListener.REFLEX_ON_STATE,hashMap);
                        start_rec();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

        }
    }

    public void send(final byte[] data)
    {
        if (Objects.equals(roombaIP, ""))
            throw new NullPointerException("roomba ip is null");
        else if (client == null)
            throw new NullPointerException("client is null");
        else if (client.isClosed() || !client.isConnected())
            throw new NullPointerException("client has been close");
        else if (client.isConnected())
        {
            S.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        client_om.write(data);
                        client_om.flush();
                        // TODO reflexToListener to listener
                        hashMap.clear();
                        hashMap.put("data",data);
                        reflexToListener(TCPListener.REFLEX_ON_SEND,hashMap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        System.gc();
    }

    public void start_rec()
    {
        Log.e(TAG,"start_rec");
        if (!Objects.equals(roombaIP, "")) {
            R.execute(new Runnable() {
                @Override
                public void run() {
                    byte[] buffers_whole_data_length = new byte[4];
                    int should_receive_data_length = 0, receive_data_length= -1;
                    try {
                        if (client != null && client.isConnected()) {
                            while (client_im.read(buffers_whole_data_length) != -1) {
                                int whole_data_length = (buffers_whole_data_length[3] & 0xFF) + ((buffers_whole_data_length[2] & 0xFF) << 8) + ((buffers_whole_data_length[1] & 0xFF) << 16) + ((buffers_whole_data_length[0] & 0xFF) << 24);
                                byte[] packet = new byte[4096];
                                byte[] bytes = new byte[whole_data_length];
                                while ((receive_data_length = client_im.read(packet)) != -1) {
                                    System.arraycopy(packet, 0, bytes, should_receive_data_length, receive_data_length);
                                    should_receive_data_length += receive_data_length;
                                    if (should_receive_data_length == whole_data_length)
                                    {
                                        should_receive_data_length = 0;
                                        break;
                                    }
                                    if (whole_data_length - should_receive_data_length < 4096)
                                        packet = new byte[whole_data_length - should_receive_data_length];
                                }
                                hashMap.clear();
                                hashMap.put("data",bytes);
                                hashMap.put("dataLength",whole_data_length);
                                // TODO reflexToListener to listener
                                reflexToListener(TCPListener.REFLEX_ON_RECEIVE,hashMap);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public void stop_rec()
    {
        Log.e(TAG,"stop_rec");
        if (client != null && client.isConnected()) {
            R.shutdown();
        }
    }


    public void registerListener(TCPListener tcpListener)
    {
        boolean canInsert = true;
        if (tcpListener != null)
        {
            List<TCPListener> list = tcpListenerList;
            synchronized (tcpListenerList) {
                for (TCPListener listener : tcpListenerList) {
                    if (listener.hashCode() == tcpListener.hashCode()) {
                        canInsert = false;
                        break;
                    }else
                        canInsert = true;
                }
                if (canInsert)
                {
                    list.add(tcpListener);
                }
            }
        }
    }

    public void unregisterListener(TCPListener tcpListener)
    {
        List<TCPListener> list = tcpListenerList;
        synchronized (tcpListenerList)
        {
            for (TCPListener listener : tcpListenerList) {
                if (listener.hashCode() == tcpListener.hashCode()) {
                    list.remove(listener);
                }
            }
        }
    }

    private void reflexToListener(int reflex_type, HashMap<String,Object> hashMap)
    {
        List<TCPListener> list = tcpListenerList;
        synchronized (tcpListenerList)
        {
            byte[] bytes = null;
            for (TCPListener listener : list) {
                switch (reflex_type)
                {
                    case TCPListener.REFLEX_ON_ERROR:
                        int errorCode = (int) hashMap.get("errorCode");
                        if (errorCode != 0)
                        {
                            String errorMessage = (String) hashMap.get("errorMessage");
                            listener.onError(errorCode,errorMessage);
                        }
                        break;

                    case TCPListener.REFLEX_ON_RECEIVE:
                        bytes = hashMap.get("data") != null ?  (byte[])hashMap.get("data") : null ;
                        int length = hashMap.get("dataLength") != null ? (int) hashMap.get("dataLength") : 0;
                        listener.onReceive(bytes,length);
                        bytes = null;
                        break;

                    case TCPListener.REFLEX_ON_SEND:
                        bytes = hashMap.get("data") != null ?  (byte[])hashMap.get("data") : null ;
                        listener.onSend(bytes);
                        bytes = null;
                        break;

                    case TCPListener.REFLEX_ON_STATE:
                        String stateMessage = (String) hashMap.get("stateMessage");
                        listener.onState(stateMessage);
                        break;
                }
            }
        }
        System.gc();
    }

    public void setRoombaIP(String roombaIP) {
        this.roombaIP = roombaIP;
    }


    class ScheduleCallable implements Callable<Object>
    {
        String command = "0";
        public ScheduleCallable(String command)
        {
            this.command = command;
        }
        @Override
        public Object call() throws Exception {

            return null;
        }
    }
}
