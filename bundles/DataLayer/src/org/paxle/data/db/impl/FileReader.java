package org.paxle.data.db.impl;

import java.io.InputStream;

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;
import org.paxle.core.data.IDataProvider;
import org.paxle.core.data.IDataSink;
import org.paxle.core.queue.Command;
import org.xml.sax.InputSource;

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
	 * Parsing the {@link InputStream} using Apache Castor
	 * @param inputStream the input-stream to read
	 */
    private void parse(InputStream inputStream) {
        if (inputStream == null) 
            throw new NullPointerException("The inpustream is null");
        
        try {           
    		Mapping mapping = new Mapping();
    		mapping.loadMapping(this.getClass().getResource("/resources/castor/mapping_command.xml"));        	
        	
			Unmarshaller unmarshaller = new Unmarshaller(Command.class);
			unmarshaller.setMapping(mapping);
			Command command = (Command) unmarshaller.unmarshal(new InputSource(inputStream));
						
			// TODO: support for multiple commands required
			
            this.addCommand(command);
        } catch (Exception e) {
            e.printStackTrace();
        }    
    }	
    
    public void addCommand(Command nextCommand) throws Exception {
    	this.sink.putData(nextCommand);
    }
}
