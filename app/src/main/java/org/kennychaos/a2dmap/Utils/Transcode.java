package org.kennychaos.a2dmap.Utils;

/**
 * Created by Kenny on 18-3-15.
 */

public class Transcode {

    /**
     *
     * @param src
     * @return
     */
    public static String __bytesToHexString(byte[] src){
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (byte aSrc : src) {
            int v = aSrc & 0xFF;
            stringBuilder.append("0x");
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv).append(",");
        }
        return stringBuilder.toString();
    }

    /**
     *
     * @param value
     * @return
     */
    public static String __formatString(int value) {
        StringBuilder strValue = new StringBuilder();
        byte[] ary = __intToByteArray(value);
        for (int i = ary.length - 1; i >= 0; i--) {
            strValue.append(ary[i] & 0xFF);
            if (i > 0) {
                strValue.append(".");
            }
        }
        return strValue.toString();
    }

    /**
     *
     * @param value
     * @return
     */
    public static byte[] __intToByteArray(int value) {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            int offset = (b.length - 1 - i) * 8;
            b[i] = (byte) ((value >>> offset) & 0xFF);
        }
        return b;
    }


//    private int __outputToFile(String fileName,String path)
//    {
//
//    }

}
