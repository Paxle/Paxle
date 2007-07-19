package org.paxle.data.db.impl;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.xmlrules.DigesterLoader;
import org.paxle.core.data.IDataProvider;
import org.paxle.core.data.IDataSink;

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

			this.parse(this.sourceFile);
		} catch (Exception e) {
			e.getStackTrace();
		}		
	}
	
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
            System.out.println("Done");            
            
        } catch (Exception e) {
            e.printStackTrace();
        }    
    }	
    
    public void addCommand(Command nextCommand) throws Exception {
    	this.sink.putData(nextCommand);
    }
}
