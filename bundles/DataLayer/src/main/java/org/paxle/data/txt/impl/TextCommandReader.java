package org.paxle.data.txt.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.paxle.core.queue.Command;
import org.paxle.data.impl.ACommandReader;

public class TextCommandReader extends ACommandReader {
	
	public TextCommandReader(InputStream inputStream) {
		super(inputStream);
	}
	
    protected void read(Reader reader) throws IOException {
        if (reader == null) throw new NullPointerException("The reader is null");
        
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line = null;
        while((line = bufferedReader.readLine())!=null) {
        	line = line.trim();
        	if (line.length() == 0) continue;
        	else if (line.startsWith("#")) continue;
        	
        	Command cmd = Command.createCommand(line);
        	this.enqueue(cmd);
        }
    }	
}