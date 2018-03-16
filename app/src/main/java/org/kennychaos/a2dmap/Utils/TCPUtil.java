package org.kennychaos.a2dmap.Utils;

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
    private final static int REFLEX_SENT = 0x21;
    private final static int REFLEX_REC = 0x22;
    private final static int REFLEX_STATE = 0x23;
    private static final String command_search_ip = "version:[1],?\n";

    private int port = -1;
    private ExecutorService S = Executors.newSingleThreadExecutor();
    private ExecutorService R = Executors.newSingleThreadExecutor();
    private Runnable searchRunnable = null;
    private String roomba_ip = "";
    private Socket client = null;
    private InputStream client_im = null;
    private OutputStream client_om = null;

    private List<TCPListener> tcpListenerList = Collections.synchronizedList(new Vector<TCPListener>());

    public TCPUtil(final int port, final String local_ip) {
        this.port = port;
        if (port != -1) {
            final byte[] command = command_search_ip.getBytes();
            searchRunnable = new Runnable() {
                @Override
                public void run() {
                    DatagramSocket socket  = null;
                    try {
                        socket = new DatagramSocket(port);
                        String _ip = local_ip.substring(0, local_ip.lastIndexOf(".")) + ".255";
                        socket.send(new DatagramPacket(command,command.length, InetAddress.getByName(_ip),12002));
                        byte[] bytes_packet = new byte[1024];
                        DatagramPacket packet = new DatagramPacket(bytes_packet,bytes_packet.length);
                        socket.receive(packet);
                        if (packet.getData().length > 0)
                            roomba_ip = packet.getAddress().getHostAddress();
                        Log.e(TAG,"roomba_ip " + roomba_ip);
                        socket.close();
                    } catch (SocketException e) {
                        e.printStackTrace();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
        }
    }

    public void search()
    {
        S.execute(searchRunnable);
    }

    public void conn()
    {
        Log.e(TAG,"conn");
        if (Objects.equals(roomba_ip, ""))
            throw new NullPointerException("roomba ip is null");
        else {
            Executors.newSingleThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        client = new Socket(roomba_ip,port);
                        client_im = client.getInputStream();
                        client_om = client.getOutputStream();
                        // TODO reflexToListener to listener client'S state
                        reflexToListener("",REFLEX_STATE);
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
        if (Objects.equals(roomba_ip, ""))
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
                        reflexToListener(data,REFLEX_SENT);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public void start_rec()
    {
        Log.e(TAG,"start_rec");
        if (!Objects.equals(roomba_ip, "")) {
            R.execute(new Runnable() {
                @Override
                public void run() {
                    byte[] buffers_whole_data_length = new byte[4];
                    int l = 0, _l= -1;
                    try {
                        if (client != null && client.isConnected()) {
                            while (client_im.read(buffers_whole_data_length) != -1) {
                                int d_l = (buffers_whole_data_length[3] & 0xFF) + ((buffers_whole_data_length[2] & 0xFF) << 8) + ((buffers_whole_data_length[1] & 0xFF) << 16) + ((buffers_whole_data_length[0] & 0xFF) << 24);
                                byte[] d = new byte[4096];
                                byte[] bytes = new byte[d_l];
                                while ((_l = client_im.read(d)) != -1) {
                                    System.arraycopy(d, 0, bytes, l, _l);
                                    l += _l;
                                    if (l == d_l)
                                        break;
                                    if (d_l - l < 4096)
                                        d = new byte[d_l - l];
                                }
                                // TODO reflexToListener to listener
                                reflexToListener(bytes, REFLEX_REC);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public void schedule()
    {

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

    private void reflexToListener(Object o, int reflex_type)
    {
        byte[] b;
        List<TCPListener> list = tcpListenerList;
        synchronized (tcpListenerList)
        {
            for (TCPListener listener : list) {
                switch (reflex_type)
                {
                    case REFLEX_REC:
                        b = (byte[]) o;
                        listener.receive(b,b.length);
                        break;
                    case REFLEX_SENT:
                        b = (byte[]) o;
                        listener.sent(b,b.length);
                        break;
                    case REFLEX_STATE:
                        listener.state(roomba_ip,port, client != null && client.isConnected(),!R.isShutdown() && !R.isTerminated(),client != null && client.isConnected());
                        break;
                }
            }
        }
    }

    public void setRoomba_ip(String roomba_ip) {
        this.roomba_ip = roomba_ip;
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
