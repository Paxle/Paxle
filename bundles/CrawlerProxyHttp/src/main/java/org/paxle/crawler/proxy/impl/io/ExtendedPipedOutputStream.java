package org.paxle.crawler.proxy.impl.io;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class ExtendedPipedOutputStream extends PipedOutputStream {
    private PipedInputStream mySinc;
    
    public ExtendedPipedOutputStream(PipedInputStream in) throws IOException{
      super(in);
      mySinc = in;
    }
    public void write(byte b[], int off, int len) throws IOException {
        if (mySinc instanceof ExptenedPipedInputStream) {
            for (int i = 0; i < len; i++) {
                ((ExptenedPipedInputStream) mySinc).receive(b[off + i]);
            }
        } else {
            super.write(b, off, len);
        }
    }

}