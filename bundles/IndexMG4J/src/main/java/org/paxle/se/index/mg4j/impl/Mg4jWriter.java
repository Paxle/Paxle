package org.paxle.se.index.mg4j.impl;

import it.unimi.dsi.fastutil.io.BinIO;
import it.unimi.dsi.mg4j.document.DocumentFactory;
import it.unimi.dsi.mg4j.index.CompressionFlags;
import it.unimi.dsi.mg4j.index.DiskBasedIndex;
import it.unimi.dsi.mg4j.io.FileLinesCollection;
import it.unimi.dsi.mg4j.tool.Concatenate;
import it.unimi.dsi.mg4j.tool.IndexBuilder;
import it.unimi.dsi.mg4j.util.ImmutableExternalTriePrefixDictionary;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.data.IDataConsumer;
import org.paxle.core.data.IDataSource;
import org.paxle.core.queue.ICommand;
import org.paxle.se.index.IFieldManager;

public class Mg4jWriter extends Thread implements IDataConsumer<ICommand>{
	
	/**
	 * A {@link IDataSource data-source} to read {@link ICommand commands}
	 */
	private IDataSource<ICommand> source = null;
	
	/**
	 * The local logger
	 */
	private final Log logger = LogFactory.getLog(this.getClass());
	
	private final IFieldManager fieldManager;
	
	private final DocumentFactory docFactory;
	
	public Mg4jWriter(IFieldManager fieldManager) {
		if (fieldManager == null) throw new NullPointerException("The field-manager is null");
		this.fieldManager = fieldManager;
		this.docFactory = new PaxleDocumentFactory(this.fieldManager);
	}
	
	/**
	 * @see IDataConsumer#setDataSource(IDataSource)
	 */
	public void setDataSource(IDataSource<ICommand> dataSource) {
		if (dataSource == null) throw new NullPointerException("The data-source is null.");
		if (this.source != null) throw new IllegalStateException("The data-source was already set.");
		this.source = dataSource;
		this.notify();
	}
	
	/**
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		try {
			Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
			
			synchronized (this) {
				while (this.source == null) this.wait();
			}

			int count = 0;
			while (!Thread.interrupted()) {
				// fetch the next command from the data-source
				ICommand command = this.source.getData();
				
				// check status
				if (command.getResult() != ICommand.Result.Passed) {
					this.logger.warn("Won't save document " + command.getLocation() + " with result '" + command.getResult() + "' (" + command.getResultText() + ")");
					continue;
				} 
				
				// create a new document sequence
				List<ICommand> commands = new ArrayList<ICommand>(Arrays.asList(command));				
				PaxleDocumentSequence documentSequence = new PaxleDocumentSequence(this.docFactory,commands);				
				
//				// scan the document collection
//				Scan.run(
//						"paxle",						
//						// the document sequence to index
//						documentSequence, 						
//						// just downcase all characters
//						DowncaseTermProcessor.getInstance(),						
//						// no zip collection
//						null,						
//						// using 32MB for buffering
//						32 * 1024,						
//						// use the default batch size
//						Scan.DEFAULT_BATCH_SIZE,						
//						// a list of fields that should be indexed
//						indexedFields.toIntArray(),						
//						// no virtual fields
//						null,						
//						null,						
//						// XXX: no map file so far
//						null,						
//						// default logging interval
//						ProgressLogger.DEFAULT_LOG_INTERVAL,						
//						// no batch dir
//						null
//				);
				
				new File("test" + count).mkdir();
				IndexBuilder indexBuilder = new IndexBuilder("test" + count + "/paxle", documentSequence)
//				.batchDirName("batches")
//				.scanBufferSize(32 * 1024)
//				.combineBufferSize(32 * 1024)
				.skipBufferSize(32 * 1024);
				
				indexBuilder.run();
				
				count++;
				
				if (count == 2) {
					for ( int i = 0; i < this.docFactory.numberOfFields(); i++ ) {
						new Concatenate( "test/paxle-" + this.docFactory.fieldName(i), 
								new String[]{
									"test0/paxle-" + this.docFactory.fieldName(i),
									"test1/paxle-" + this.docFactory.fieldName(i)
								},
								false,
								1048576,
								CompressionFlags.DEFAULT_STANDARD_INDEX,
								false,
								false,
								64,
								8,
								32768,
								10000).run();
						
						BinIO.storeObject(new ImmutableExternalTriePrefixDictionary( new FileLinesCollection( "test/paxle-" + this.docFactory.fieldName(i) + DiskBasedIndex.TERMS_EXTENSION, "UTF-8" ) ), "test/paxle-" + this.docFactory.fieldName(i) + DiskBasedIndex.TERMMAP_EXTENSION );
												
					}
				}
			}
		} catch (InterruptedException e) {
			this.logger.info("MG4J writer was interrupted, quitting...");
		} catch (Exception e) {
			this.logger.error("Internal error in lucene writer thread", e);
			e.printStackTrace();
		} 
	}
}
