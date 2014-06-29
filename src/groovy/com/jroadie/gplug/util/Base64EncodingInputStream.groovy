package com.jroadie.gplug.util

public class Base64EncodingInputStream extends InputStream {

    private static final String base64code = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

    private InputStream innerStream;
    private int pos = 0;
    private int onhand = 0;
    private boolean terminated = false;

    public Base64EncodingInputStream(InputStream stream) {
        innerStream = stream;
    }

    @Override
    public int available() throws IOException {
        //TODO: have to set proper available value
        return super.available();
    }

    @Override
    public int read() throws IOException {
        int read = terminated ? -1 : (pos == 3 ? onhand : innerStream.read());
        if(read == -1) {
            terminated = true;
            if(pos == 0 || pos == 4) {
                return -1;
            } else {
                pos++;
                if(onhand == -1) {
                    return '=';
                } else {
                    int m = pos == 2 ? onhand << 4 : (pos == 3 ? onhand << 2 : onhand);
                    onhand = -1;
                    return base64code.charAt(m);
                }
            }
        } else {
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
                pos = 3;
            } else {
                m = onhand;
                onhand = 0;
                pos = 0;
            }
            return base64code.charAt(m);
        }
    }
}
