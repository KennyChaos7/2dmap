package org.kennychaos.a2dmap.Utils;

import org.kennychaos.a2dmap.Listener.TCPListener;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Kenny on 18-2-5.
 */

public class TCPUtil {
    private final String TAG = "=====" + getClass().getSimpleName().toLowerCase() + "===== ";
    public final static int REFLEX_SENT = 0x21;
    public final static int REFLEX_REC = 0x22;
    public final static int REFLEX_STATE = 0x23;
    private final String command_search_ip = "version:[1],?\n";

    private int port = -1;
    private ExecutorService S = Executors.newSingleThreadExecutor();
    private ExecutorService R = Executors.newSingleThreadExecutor();
    private Runnable runnable = null;
    private String roomba_ip = "";
    private DatagramSocket client = null;

    private List<TCPListener> tcpListenerList = Collections.synchronizedList(new Vector<TCPListener>());

    TCPUtil(final int port)
    {
        this.port = port;
        if (port != -1)
        {
            final byte[] command = command_search_ip.getBytes();
            runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        DatagramSocket socket  = new DatagramSocket(port);
                        socket.send(new DatagramPacket(command,command.length));
                        byte[] bytes_packet = new byte[1024];
                        DatagramPacket packet = new DatagramPacket(bytes_packet,bytes_packet.length);
                        socket.receive(packet);
                        roomba_ip = packet.getAddress().getHostAddress();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            S.execute(runnable);
        }
    }

    public synchronized void conn()
    {
        if (Objects.equals(roomba_ip, ""))
            throw new NullPointerException("roomba ip is null");
        else {
            try {
                client = new DatagramSocket();
                client.connect(new InetSocketAddress(roomba_ip,port));
                // TODO reflex to listener client'S state
                reflex("",REFLEX_STATE);
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void send(final byte[] data)
    {
        if (Objects.equals(roomba_ip, ""))
            throw new NullPointerException("roomba ip is null");
        else if (client == null)
            throw new NullPointerException("client is null");
        else if (client.isClosed() || !client.isConnected())
            throw new NullPointerException("client has been close");
        else if (client.isConnected())
        {
            S.shutdown();
            S.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        client.send(new DatagramPacket(data,data.length));
                        // TODO reflex to listener
                        reflex(data,REFLEX_SENT);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public synchronized void start_rec()
    {
        if (!Objects.equals(roomba_ip, "") && (R.isShutdown() || R.isTerminated()))
        {
            R.execute(new Runnable() {
                @Override
                public void run() {
                    while (client != null && client.isConnected())
                    {
                        byte[] bytes = new byte[1024];
                        DatagramPacket rec_packet = new DatagramPacket(bytes,bytes.length);
                        try {
                            client.receive(rec_packet);
                            // TODO reflex to listener
                            reflex(rec_packet.getData(),REFLEX_REC);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    public void registerListener(TCPListener tcpListener)
    {
        List<TCPListener> list = tcpListenerList;
        synchronized (tcpListenerList)
        {
            for (TCPListener listener : tcpListenerList) {
                if (listener.hashCode() != tcpListener.hashCode()) {
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

    private synchronized void reflex(Object o,int reflex_type)
    {
        byte[] b;
        List<TCPListener> list = tcpListenerList;
        synchronized (tcpListenerList)
        {
            Iterator<TCPListener> iterator = tcpListenerList.iterator();
            for (TCPListener listener : tcpListenerList) {
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

}
