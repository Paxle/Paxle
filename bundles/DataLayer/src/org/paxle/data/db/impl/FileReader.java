package org.paxle.data.db.impl;

import java.io.InputStream;
import java.net.URL;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.xmlrules.DigesterLoader;
import org.paxle.core.data.IDataProvider;
import org.paxle.core.data.IDataSink;
import org.paxle.core.queue.Command;

public class FileReader extends Thread implements IDataProvider {
	private InputStream sourceFile = null;
	private IDataSink sink = null;
	
	public FileReader(String name) {
		this.sourceFile = this.getClass().getResourceAsStream(name);
		this.start();
	}
	
	public void setDataSink(IDataSink dataSink) {
		if (dataSink == null) throw new NullPointerException("The data-sink is null-");
		if (this.sink != null) throw new IllegalStateException("The data-sink was already set.");
		this.sink = dataSink;
		this.notify();
	}
	
	@Override
	public void run() {
		try {
			Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
			
			synchronized (this) {
				while (this.sink == null) this.wait();
			}

			System.out.println("Start reading commands from inputstream ...");
			this.parse(this.sourceFile);
			System.out.println("Reading commands from inputstream finished");
		} catch (Exception e) {
			e.getStackTrace();
		}		
	}
	
	/**
	 * Parsing the {@link InputStream} using Apache Disgester 
	 * @param inputStream the input-stream to read
	 */
    private void parse(InputStream inputStream) {
        if (inputStream == null) 
            throw new NullPointerException("The inpustream is null");
        
        try {           
            URL rules = this.getClass().getResource("/resources/rules.xml");
            Digester digester = DigesterLoader.createDigester(rules);
            digester.setNamespaceAware(false);
            digester.setValidating(false);
            digester.setUseContextClassLoader(true);
            
            digester.push(this);
            digester.parse(inputStream);       
            
        } catch (Exception e) {
            e.printStackTrace();
        }    
    }	
    
    public void addCommand(Command nextCommand) throws Exception {
    	this.sink.putData(nextCommand);
    }
}
