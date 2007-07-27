package org.paxle.data.db.impl;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.UnmarshalListener;
import org.exolab.castor.xml.Unmarshaller;
import org.paxle.core.data.IDataProvider;
import org.paxle.core.data.IDataSink;
import org.paxle.core.queue.ICommand;

public class CommandReader extends Thread implements IDataProvider, UnmarshalListener {
	/**
	 * An {@link InputStream} to read the XML
	 */
	private InputStream inputStream = null;
	
	/**
	 * A {@link IDataSink data-sink} to write the unmarshalled XML out
	 */
	private IDataSink sink = null;
	
	public CommandReader() {}
	
	public CommandReader(InputStream inputStream) {
		this.inputStream = inputStream;
		this.start();
	}
	
	/**
	 * @see IDataProvider#setDataSink(IDataSink)
	 */
	public void setDataSink(IDataSink dataSink) {
		if (dataSink == null) throw new NullPointerException("The data-sink is null-");
		if (this.sink != null) throw new IllegalStateException("The data-sink was already set.");
		this.sink = dataSink;
		this.notify();
	}
	
	@Override
	public void run() {
		Reader reader = null;
		try {
			Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
			
			synchronized (this) {
				while (this.sink == null) this.wait();
			}

			System.out.println("Start reading commands from inputstream ...");	
			reader = new InputStreamReader(this.inputStream);	
			this.unmarshall(reader);
			System.out.println("Reading commands from inputstream finished");
		} catch (Exception e) {
			e.getStackTrace();
		} finally {
			if (reader != null) try { reader.close(); } catch (Exception e) {/*ignore this*/}
		}
	}
	
	/**
	 * Reads XML from the {@link Reader} and converts it into an {@link ICommand}
	 * @param inputStream the input-stream to read
	 */
    private void unmarshall(Reader reader) {
        if (reader == null) throw new NullPointerException("The reader is null");
        
        try {           
    		Mapping mapping = new Mapping();
    		mapping.loadMapping(this.getClass().getResource("/resources/castor/mapping_command.xml"));        	
        	
			Unmarshaller unmarshaller = new Unmarshaller(ArrayList.class);
			unmarshaller.setUnmarshalListener(this);
			unmarshaller.setMapping(mapping);
			//unmarshaller.setProperty("org.exolab.castor.parser.namespaces", "true");
			unmarshaller.unmarshal(reader);
        } catch (Exception e) {
            e.printStackTrace();
        }    
    }	

    /**
     * @see UnmarshalListener#attributesProcessed(Object)
     */
	public void attributesProcessed(Object object) {
		// nothing todo here
	}

    /**
     * @see UnmarshalListener#fieldAdded(String, Object, Object)
     */	
	public void fieldAdded(String fieldName, Object parent, Object child) {
		// nothing todo here
	}

    /**
     * @see UnmarshalListener#initialized(Object)
     */		
	public void initialized(Object object) {
		// nothing todo here
	}

    /**
     * @see UnmarshalListener#unmarshalled(Object)
     */		
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
