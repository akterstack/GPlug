package com.jroadie.gplug.util

class Base64DataInputStream extends InputStream {
    private static final String base64code = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
    private static final byte[] decodeMap = new byte[128];
    static {
        for(int i = 0; i < 64; i++) {
            decodeMap[(int) base64code.charAt(i)] = (byte) i;
        }
    }

    private InputStream innerStream;
    private int pos = 0;
    private int onhand = 0;

    public Base64DataInputStream(InputStream stream) {
        innerStream = stream;
    }

    @Override
    public int available() throws IOException {
        //TODO: have to set proper available value
        return super.available();
    }

    @Override
    public int read() throws IOException {
        switch(pos) {
            case 0:
                int i0 = innerStream.read();
                if(i0 == -1) {
                    return -1;
                }
                if(i0 > 127) {
                    throw new IllegalArgumentException("Illegal character in Base64 encoded data.");
                }
                int b0 = decodeMap[i0];
                if(b0 < 0) {
                    throw new IllegalArgumentException("Illegal character in Base64 encoded data.");
                }
                int i1 = innerStream.read();
                if(i1 > 127 || i1 == '=') {
                    throw new IllegalArgumentException("Illegal character in Base64 encoded data.");
                }
                int b1 = decodeMap[i1];
                if(b1 < 0) {
                    throw new IllegalArgumentException("Illegal character in Base64 encoded data.");
                }
                int o0 = (b0 << 2) | (b1 >>> 4);
                onhand = b1;
                pos++;
                return o0;
            case 1:
                int i2 = innerStream.read();
                if(i2 == '=' || i2 == -1) {
                    onhand = 0;
                    pos = 0;
                    return -1;
                }
                if(i2 > 127) {
                    throw new IllegalArgumentException("Illegal character in Base64 encoded data.");
                }
                int b2 = decodeMap[i2];
                if(b2 < 0) {
                    throw new IllegalArgumentException("Illegal character in Base64 encoded data.");
                }
                int o1 = ((onhand & 0xf) << 4) | (b2 >>> 2);
                onhand = b2;
                pos++;
                return o1;
            case 2:
                int i3 = innerStream.read();
                if(i3 == '=' || i3 == -1) {
                    onhand = 0;
                    pos = 0;
                    return -1;
                }
                if(i3 > 127) {
                    throw new IllegalArgumentException("Illegal character in Base64 encoded data.");
                }
                int b3 = decodeMap[i3];
                if(b3 < 0) {
                    throw new IllegalArgumentException("Illegal character in Base64 encoded data.");
                }
                int o2 = ((onhand & 3) << 6) | b3;
                onhand = 0;
                pos = 0;
                return o2;
        }
    }
}
