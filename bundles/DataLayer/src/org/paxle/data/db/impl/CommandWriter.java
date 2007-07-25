package org.paxle.data.db.impl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.paxle.core.data.IDataConsumer;
import org.paxle.core.data.IDataSource;
import org.paxle.core.queue.ICommand;
import org.xml.sax.InputSource;

public class CommandWriter extends Thread implements IDataConsumer {

	private File targetFile = null;
	private IDataSource source = null;
	
	public CommandWriter(File targetFile) {
		this.targetFile = targetFile;
		this.start();
	}
	
	public void setDataSource(IDataSource dataSource) {
		if (dataSource == null) throw new NullPointerException("The data-source is null.");
		if (this.source != null) throw new IllegalStateException("The data-source was already set.");
		this.source = dataSource;
		this.notify();
	}
	
	@Override
	public void run() {
		Writer writer = null;
		try {
			Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
			
			synchronized (this) {
				while (this.source == null) this.wait();
			}

			writer = new FileWriter(this.targetFile);
			this.unmarshall(writer);
			
		} catch (Exception e) {
			e.getStackTrace();
		} finally {
			if (writer != null) try { writer.close(); } catch (Exception e) {/* ignore this */}
		}
	}	
	
    private void unmarshall(Writer outputWriter) {
        if (outputWriter == null) throw new NullPointerException("The writer is null");
        
        try {
            outputWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
            outputWriter.write("<CommandList>");        	
        	
    		Mapping mapping = new Mapping();
    		mapping.loadMapping(this.getClass().getResource("/resources/castor/mapping_command.xml"));        	
        	
			Marshaller marshaller = new Marshaller(outputWriter);
			marshaller.setMapping(mapping);
			marshaller.setMarshalAsDocument(false);

			// getting the next command from the datasource
			ICommand command = null;
			while ((command = (ICommand) this.source.getData()) != null) {
				marshaller.marshal(command);
			}
			
			outputWriter.write("</CommandList>");
        } catch (Exception e) {
            e.printStackTrace();
        }    
    }	
}
