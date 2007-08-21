package org.paxle.data.db.impl;

import java.net.URL;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.paxle.core.data.IDataConsumer;
import org.paxle.core.data.IDataProvider;
import org.paxle.core.data.IDataSink;
import org.paxle.core.data.IDataSource;
import org.paxle.core.queue.ICommand;

public class CommandDB implements IDataProvider, IDataConsumer {
	private static final int MAX_IDLE_SLEEP = 60000;

	/**
	 * A {@link IDataSink data-sink} to write the loaded {@link ICommand commands} out
	 */
	private IDataSink<ICommand> sink = null;	
	
	/**
	 * A {@link IDataSource data-source} to fetch {@link ICommand commands} from
	 */
	private IDataSource<ICommand> source = null;	
	
	/**
	 * A {@link Thread thread} to read {@link ICommand commands} from the {@link #source data-source}
	 * and write it into the {@link #db database}. 
	 */
	private Reader readerThread = null;
	
	/**
	 * A {@link Thread thread} to read {@link ICommand commands} from the {@link #db database}
	 * and write it into the {@link #sink data-sink}.
	 */
	private Writer writerThread = null;
	
	/**
	 * The logger
	 */
	private Log logger = LogFactory.getLog(this.getClass());
	
	/**
	 * The hibernate {@link SessionFactory}
	 */
	private SessionFactory sessionFactory;
	
	public CommandDB(URL configURL, List<URL> mappings) {
		try {
			/* ===========================================================================
			 * Init Hibernate
			 * =========================================================================== */
			try {
	        	Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
				
				// Read the hibernate configuration from *.cfg.xml
				Configuration config = new Configuration().configure(configURL);
				
				// register an interceptor (required to support our interface-based command model)
				config.setInterceptor(new InterfaceInterceptor());
				
				// load the various mapping files
				for (URL mapping : mappings) {
					config.addURL(mapping);
				}
				
				String[] sql = config.generateSchemaCreationScript(new org.hibernate.dialect.MySQLDialect());
				
				// create the session factory
				sessionFactory = config.buildSessionFactory();
			} catch (Throwable ex) {
				// Make sure you log the exception, as it might be swallowed
				this.logger.error("Initial SessionFactory creation failed.",ex);
				throw new ExceptionInInitializerError(ex);
			}
		    			
			System.out.println(this.size());
			
			/* ===========================================================================
			 * Init Reader/Writer Threads
			 * =========================================================================== */
		    this.writerThread = new Writer();
		    this.writerThread.start();
		    
		    this.readerThread = new Reader();
		    this.readerThread.start();
		    
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}	
	
	/**
	 * @see IDataConsumer#setDataSource(IDataSource)
	 */
	@SuppressWarnings("unchecked")
	public void setDataSource(IDataSource dataSource) {
		if (dataSource == null) throw new NullPointerException("The data-source is null-");
		if (this.source != null) throw new IllegalStateException("The data-source was already set.");
		synchronized (this.readerThread) {
			this.source = dataSource;
			this.readerThread.notify();			
		}
	}

	/**
	 * @see IDataProvider#setDataSink(IDataSink)
	 */
	@SuppressWarnings("unchecked")
	public void setDataSink(IDataSink dataSink) {
		if (dataSink == null) throw new NullPointerException("The data-sink is null-");
		if (this.sink != null) throw new IllegalStateException("The data-sink was already set.");
		synchronized (this.writerThread) {
			this.sink = dataSink;
			this.writerThread.notify();			
		}
	}
	
	public void close() throws InterruptedException {
		// interrupt reader and writer
		this.readerThread.interrupt();
		this.writerThread.interrupt();
		
		// wait for the threads to shutdown
		this.readerThread.join();
		this.writerThread.join();
		
		// close the DB
		this.sessionFactory.close();
		
		// shutdown the database
		try {
			DriverManager.getConnection("jdbc:derby:;shutdown=true");
		} catch (SQLException e) {
			this.logger.error("Unable to shutdown database.",e);
		}
	}
	
	public boolean isKnown(String location) {
		return false;
	}

	/**
	 * TODO: does not work at the moment
	 */
	private void commandToXML(ICommand cmd) {
		Session session = sessionFactory.getCurrentSession();		
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();	
			Session dom4jSession = session.getSession(EntityMode.DOM4J);
			
			dom4jSession.saveOrUpdate("org.paxle.core.queue.ICommand", cmd);

			transaction.commit();
		} catch (HibernateException e) {
			if (transaction != null && transaction.isActive()) transaction.rollback(); 
			this.logger.error("Error while converting command to XML",e);
		}	
	}
	
	private synchronized List<ICommand> fetchNextCommands(int offset, int limit)  {		
		List<ICommand> result = null;
		Session session = sessionFactory.getCurrentSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();

			Query query = session.createQuery("from ICommand");
			query.setFirstResult(offset);
			query.setMaxResults(limit);
			result = (List<ICommand>) query.list();
			// TODO: delete object or mark as loaded
			
			transaction.commit();
		} catch (HibernateException e) {
			if (transaction != null && transaction.isActive()) transaction.rollback(); 
			this.logger.error("Error while querying queue size",e);
		}	

		return result;
	}
	
	private synchronized void storeCommand(ICommand cmd) {
		Session session = sessionFactory.getCurrentSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			
	        session.save(cmd);
	        
			transaction.commit();
		} catch (HibernateException e) {
			if (transaction != null && transaction.isActive()) transaction.rollback(); 
			this.logger.error("Error while writing command to db",e);
		}
	}
	
	/**
	 * @return the total size of the command queue
	 */
	public long size() {		
		Long count = -1l;
		Session session = sessionFactory.getCurrentSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			
			count = (Long) session.createQuery("select count(*) from ICommand").uniqueResult();
			
			transaction.commit();
		} catch (HibernateException e) {
			if (transaction != null && transaction.isActive()) transaction.rollback(); 
			this.logger.error("Error while querying queue size",e);
		}		
		
		return count;
	}
	
	/**
	 * Resets the command queue
	 */
	public void reset() {
		Session session = sessionFactory.getCurrentSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			
			session.createQuery("DELETE FROM ICommand").executeUpdate();
			
			transaction.commit();
		} catch (HibernateException e) {
			if (transaction != null && transaction.isActive()) transaction.rollback(); 
			this.logger.error("Error while reseting queue.",e);
		}
	}
	
	/**
	 * A {@link Thread} to read {@link ICommand commands} from the {@link CommandDB#db}
	 * and to write it into the {@link CommandDB#sink data-sink}
	 */
	class Writer extends Thread {

		@Override
		public void run() {
			try {
				synchronized (this) {
					while (CommandDB.this.sink == null) this.wait();
				}			

				List<ICommand> commands = null;
				while(!Thread.currentThread().isInterrupted()) {
					commands = CommandDB.this.fetchNextCommands(0,1);
					if (commands.size() > 0) {
						for (ICommand command : commands) {
							CommandDB.this.sink.putData(command);
//							commandToXML(command);
						} 
					} else {
						// sleep for a while
						synchronized (this) {
							this.wait(MAX_IDLE_SLEEP);	
						}						
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {

			}		
		}
		
		public synchronized void signalNewDbData() {
			this.notify();
		}
	}
	
	/**
	 * A {@link Thread} to read {@link ICommand commands} from the {@link CommandDB#source data-source}
	 * and to write it into the {@link CommandDB#db}.
	 */	
	class Reader extends Thread {
		@Override
		public void run() {
			try {

				synchronized (this) {
					while (CommandDB.this.sink == null) this.wait();
				}		
				
				while(!Thread.currentThread().isInterrupted()) {
					ICommand command = CommandDB.this.source.getData();
					if (command != null) {
						// store data into db
						CommandDB.this.storeCommand(command);
						
						// signal writer that a new URL is available
						CommandDB.this.writerThread.signalNewDbData();
					}
				}				
			} catch (Exception e) {
				e.printStackTrace();
			} finally {

			}
		}
	}
}
