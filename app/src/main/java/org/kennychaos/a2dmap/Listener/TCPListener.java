package org.kennychaos.a2dmap.Listener;

/**
 * Created by Kenny on 18-2-5.
 */

public interface TCPListener {

    void receive(byte[] bytes,int length);

    void sent(byte[] bytes,int length);

    void state(String roomba_ip, int port ,boolean isConnected , boolean isRec,boolean isCanSend);
}
