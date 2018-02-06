package org.kennychaos.a2dmap.Model;

import android.content.Context;
import android.util.Log;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * this class is used to help send UDP data according to length
 * 
 * @author afunx
 * 
 */
public class UDPSocketClient {

	private static final String TAG = "UDPSocketClient";
	private DatagramSocket mSocket;
	private volatile boolean mIsStop;
	private volatile boolean mIsClosed;
    private final static int socketTimeOut = 3*60*1000;
	public static int COUNT = 1;
    private String result = "";

	public UDPSocketClient() {
		try {
			this.mSocket = new DatagramSocket();
			this.mIsStop = false;
			this.mIsClosed = false;
		} catch (SocketException e) {

			e.printStackTrace();
		}
	}

	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}

	public void interrupt() {

		this.mIsStop = true;
	}

	/**
	 * close the UDP socket
	 */
	public synchronized void close() {
		if (!this.mIsClosed && mSocket!= null) {
			this.mSocket.close();
			this.mIsClosed = true;
		}
	}

	/**
	 * send the data by UDP
	 * 
	 * @param data
	 *            the data to be sent
	 * @param targetHost
	 *            the host name of target, e.g. 192.168.1.101
	 * @param targetPort
	 *            the port of target
	 * @param interval
	 *            the milliseconds to between each UDP sent
	 */
	public void sendData(byte[][] data, String targetHostName, int targetPort,
                         long interval) {
		sendData(data, 0, data.length, targetHostName, targetPort, interval);
	}
	
	
	/**
	 * send the data by UDP
	 * 
	 * @param data
	 *            the data to be sent
	 * @param offset
	 * 			  the offset which data to be sent
	 * @param count
	 * 			  the count of the data
	 * @param targetHost
	 *            the host name of target, e.g. 192.168.1.101
	 * @param targetPort
	 *            the port of target
	 * @param interval
	 *            the milliseconds to between each UDP sent
	 */
	public void sendData(byte[][] data, int offset, int count,
                         String targetHostName, int targetPort, long interval) {
		if ((data == null) || (data.length <= 0)) {

			return;
		}
		for (int i = offset; !mIsStop && i < offset + count; i++) {
			if (data[i].length == 0) {
				continue;
			}
			try {
				// Log.i(TAG, "data[" + i + " +].length = " + data[i].length);
				DatagramPacket localDatagramPacket = new DatagramPacket(
						data[i], data[i].length,
						InetAddress.getByName(targetHostName), targetPort);
				this.mSocket.send(localDatagramPacket);
			} catch (UnknownHostException e) {

				e.printStackTrace();
				mIsStop = true;
				break;
			} catch (IOException e) {

			}
			try {
				Thread.sleep(interval);
			} catch (InterruptedException e) {
				e.printStackTrace();

				mIsStop = true;
				break;
			}
		}
		if (mIsStop) {
			close();
		}
	}

    /**
     *
     * @param data
     * @param targetHostName
     * @param targetPort
     */
    public void sendData(byte[] data, String targetHostName, int targetPort , int i){
        if (data != null && data.length > 0 ){
            DatagramPacket localDatagramPacket = null;
            try {
				localDatagramPacket = new DatagramPacket(
						data, data.length,
						InetAddress.getByName(targetHostName), targetPort);
				this.mSocket.send(localDatagramPacket);
				setmSocket(this.mSocket);
			}catch (SocketException e) {
				e.printStackTrace();
				mIsStop = true;
            } catch (UnknownHostException e) {
                e.printStackTrace();
                mIsStop = true;
            } catch (IOException e) {
                e.printStackTrace();
                mIsStop = true;
            }
        }
    }

    /**
     *
     * @param data
     * @param targetHostName
     * @param targetPort
     */
    public void sendData(byte[] data, String targetHostName, int targetPort){
        if (data != null && data.length > 0 ){
            DatagramPacket localDatagramPacket = null;
            try {
                localDatagramPacket = new DatagramPacket(
                        data,data.length,
                        InetAddress.getByName(targetHostName), targetPort);
                this.mSocket.send(localDatagramPacket);
                setmSocket(this.mSocket);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                close();
            }
        }
    }

    /**
     *
     * @param data
     * @param targetHostName
     * @param targetPort
     * @return
     */
    public String receive(byte[] data, String targetHostName, int targetPort){
        String str = null;
        try {
			sendData(data,targetHostName,targetPort,0);
			DatagramSocket datagramSocket = this.getmSocket();
			byte[] datas = new byte[64];
			DatagramPacket datagramPacket = new DatagramPacket(datas,64);
            str = analyze(datagramSocket,datagramPacket,0);
        } catch (SocketException e){
			e.printStackTrace();
		} catch (IOException e) {
            e.printStackTrace();
            mIsStop = true;
            return null;
        } finally {
//            close();
        }
        return str;
    }




    /**
     *
     * @param datagramSocket
     * @param datagramPacket
     * @return
     * @throws IOException
     */
    private String analyze(DatagramSocket datagramSocket, DatagramPacket datagramPacket,int tag) throws IOException {
        String str = null;
        datagramSocket.receive(datagramPacket);
		if (datagramPacket.getData().length>0) {
			byte[] recDatas = Arrays.copyOf(datagramPacket.getData(), datagramPacket.getLength());
			for (int i = 0; i < recDatas.length; i++) {
//				Log.e(TAG, "recDatas[" + i + "]:" + recDatas[i]);
			}
			Log.e(TAG, "receiveSpecLenBytes: " + new String(recDatas));
			str = new String(recDatas) + "/" + datagramPacket.getAddress().toString();
			if (tag > 0) {
				if (str.contains("upgrade_mcu:process,")) {
					int position = str.lastIndexOf(",s");
					String _p = str.substring(position - 2, position);//
					Log.e(TAG, _p);
					tag = 2;
				} else if (str.contains("upgrade_mcu")
						&& !str.contains("upgrade_mcu:start")
						&& !str.contains("upgrade_mcu:downloaded,s")
						|| str.contains("upgrade_mcu:busy")) {
					tag = 1;
					return str;
				} else if (str.toLowerCase().contains("ok")||str.toLowerCase().contains("fail"))
                    return str;
                else if (str.contains("progressing"))
                    tag = 1;
				str = analyze(datagramSocket, datagramPacket, tag--);
			}
		}else
			str = "";
        return str;
    }



    public DatagramSocket getmSocket() {
		return mSocket;
	}

	public void setmSocket(DatagramSocket mSocket) {
		this.mSocket = mSocket;
	}
}
