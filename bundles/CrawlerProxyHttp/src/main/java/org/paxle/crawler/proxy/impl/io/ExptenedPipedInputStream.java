package org.paxle.crawler.proxy.impl.io;

import java.io.IOException;
import java.io.PipedInputStream;

public class ExptenedPipedInputStream extends PipedInputStream {
	
    public synchronized void receive(int b) throws IOException {
        // extend buffer if needed.
        if (in + 1 >= buffer.length) {
            byte[] newBuffer = new byte[(buffer.length * 3)/2];
            System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
            buffer = newBuffer;
        }
        super.receive(b);
    }
}