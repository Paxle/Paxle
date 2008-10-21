/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2008 the original author or authors.
 * 
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 * 
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.data.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.paxle.core.data.IDataProvider;
import org.paxle.core.data.IDataSink;
import org.paxle.core.queue.ICommand;

public abstract class ACommandReader extends Thread implements IDataProvider<ICommand> {
	/**
	 * An {@link InputStream} to read the XML
	 */
	private InputStream inputStream = null;
	
	/**
	 * A {@link IDataSink data-sink} to write the unmarshalled XML out
	 */
	private IDataSink<ICommand> sink = null;
	
	public ACommandReader() {}
	
	public ACommandReader(InputStream inputStream) {
		this.inputStream = inputStream;
		this.start();
	}
	
	/**
	 * @see IDataProvider#setDataSink(IDataSink)
	 */
	public synchronized void setDataSink(IDataSink<ICommand> dataSink) {
		if (dataSink == null) throw new NullPointerException("The data-sink is null-");
		if (this.sink != null) throw new IllegalStateException("The data-sink was already set.");
		this.sink = dataSink;
		this.notify();
	}
	
	@Override
	public void run() {
		Reader reader = null;
		try {			
			synchronized (this) {
				while (this.sink == null) this.wait();
			}

			System.out.println("Start reading commands from inputstream ...");	
			reader = new InputStreamReader(this.inputStream);	
			this.read(reader);
			System.out.println("Reading commands from inputstream finished");
		} catch (Exception e) {
			e.getStackTrace();
		} finally {
			if (reader != null) try { reader.close(); } catch (Exception e) {/*ignore this*/}
		}
	}

    /**
     * @see UnmarshalListener#unmarshalled(Object)
     */		
	public void enqueue(ICommand cmd) {
		try {
			this.sink.putData(cmd);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
    protected abstract void read(Reader reader) throws IOException;
}