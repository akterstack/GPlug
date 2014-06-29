package com.jroadie.gplug.util

public class Base64Coder {
    private static final String base64code = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

    private static final byte[] decodeMap = new byte[128];
    static {
        for (int i = 0; i < 64; i++) {
            decodeMap[(int) base64code.charAt(i)] = (byte) i;
        }
    }

    public static String encode(String toEncode) {
        return encode(toEncode.getBytes())
    }

    public static String encode(byte[] toEncode) {
        int pos = 0;
        int onhand = 0;

        StringBuffer buffer = new StringBuffer();
        for(byte b in toEncode) {
            int read = b;
            int m;
            if(pos == 0) {
                m = (read >> 2) & 63;
                onhand = read & 3;
                pos = 1;
            } else if(pos == 1) {
                m = (onhand << 4) + ((read >> 4) & 15);
                onhand = read & 15;
                pos = 2;
            } else if(pos == 2) {
                m = ((read >> 6) & 3) + (onhand << 2);
                onhand = read & 63;
                buffer.append(base64code.charAt(m));
                m = onhand;
                onhand = 0;
                pos  = 0;
            }
            buffer.append(base64code.charAt(m));
        }
        while(pos > 0 && pos < 4) {
            pos++;
            if(onhand == -1) {
                buffer.append('=');
            } else {
                int m = pos == 2 ? onhand << 4 : (pos == 3 ? onhand << 2 : onhand);
                onhand = -1;
                buffer.append(base64code.charAt(m));
            }
        }
        return buffer.toString()
    }

    public static byte[] decode(String toDecode) {
        int iLen = toDecode.length();
        if (iLen % 4 != 0) {
            throw new IllegalArgumentException("Length of Base64 encoded input string is not a multiple of 4.");
        }
        while (iLen > 0 && toDecode.charAt(iLen - 1) == '=') iLen--;
        int oLen = (iLen * 3) / 4;
        byte[] out = new byte[oLen];
        int ip = 0;
        int op = 0;
        while (ip < iLen) {
            int i0 = toDecode.charAt(ip++);
            int i1 = toDecode.charAt(ip++);
            int i2 = ip < iLen ? toDecode.charAt(ip++) : 'A';
            int i3 = ip < iLen ? toDecode.charAt(ip++) : 'A';
            if (i0 > 127 || i1 > 127 || i2 > 127 || i3 > 127) {
                throw new IllegalArgumentException("Illegal character in Base64 encoded data.");
            }
            int b0 = decodeMap[i0];
            int b1 = decodeMap[i1];
            int b2 = decodeMap[i2];
            int b3 = decodeMap[i3];
            if (b0 < 0 || b1 < 0 || b2 < 0 || b3 < 0) {
                throw new IllegalArgumentException("Illegal character in Base64 encoded data.");
            }
            int o0 = (b0 << 2) | (b1 >>> 4);
            int o1 = ((b1 & 0xf) << 4) | (b2 >>> 2);
            int o2 = ((b2 & 3) << 6) | b3;
            out[op++] = (byte) o0;
            if (op < oLen) out[op++] = (byte) o1;
            if (op < oLen) out[op++] = (byte) o2;
        }
        return out;
    }
}
