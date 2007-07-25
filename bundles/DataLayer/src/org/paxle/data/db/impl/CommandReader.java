package org.paxle.data.db.impl;

import java.io.InputStream;
import java.util.ArrayList;

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.UnmarshalListener;
import org.exolab.castor.xml.Unmarshaller;
import org.paxle.core.data.IDataProvider;
import org.paxle.core.data.IDataSink;
import org.paxle.core.queue.ICommand;
import org.xml.sax.InputSource;

public class CommandReader extends Thread implements IDataProvider, UnmarshalListener {
	private InputStream sourceFile = null;
	private IDataSink sink = null;
	
	public CommandReader(String name) {
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
			this.unmarshall(this.sourceFile);
			System.out.println("Reading commands from inputstream finished");
		} catch (Exception e) {
			e.getStackTrace();
		}		
	}
	
	/**
	 * Parsing the {@link InputStream} using Apache Castor
	 * @param inputStream the input-stream to read
	 */
    private void unmarshall(InputStream inputStream) {
        if (inputStream == null) 
            throw new NullPointerException("The inpustream is null");
        
        try {           
    		Mapping mapping = new Mapping();
    		mapping.loadMapping(this.getClass().getResource("/resources/castor/mapping_command.xml"));        	
        	
			Unmarshaller unmarshaller = new Unmarshaller(ArrayList.class);
			unmarshaller.setUnmarshalListener(this);
			unmarshaller.setMapping(mapping);
			unmarshaller.unmarshal(new InputSource(inputStream));
        } catch (Exception e) {
            e.printStackTrace();
        }    
    }	

	public void attributesProcessed(Object object) {
		// nothing todo here
	}

	public void fieldAdded(String fieldName, Object parent, Object child) {
		// nothing todo here
	}

	public void initialized(Object object) {
		// nothing todo here
	}

	public void unmarshalled(Object object) {
		if (object instanceof ICommand) {
			try {
				this.sink.putData((ICommand)object);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
