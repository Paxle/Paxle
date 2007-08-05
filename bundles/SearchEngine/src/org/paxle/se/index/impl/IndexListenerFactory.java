package org.paxle.se.index.impl;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;

import org.paxle.se.index.IIndexModifier;
import org.paxle.se.index.IIndexSearcher;
import org.paxle.se.index.IIndexWriter;
import org.paxle.se.query.ITokenFactory;

public class IndexListenerFactory {
	
	public static final String SEARCHER_FILTER = String.format("(objectClass=%s)", IIndexSearcher.class.getName());
	public static final String MODIFIER_FILTER = String.format("(objectClass=%s)", IIndexModifier.class.getName());
	public static final String WRITER_FILTER = String.format("(objectClass=%s)", IIndexWriter.class.getName());
	public static final String TFACTORY_FILTER = String.format("(objectClass=%s)", ITokenFactory.class.getName());
	
	private final BundleContext context;
	private final SEWrapper sewrapper;
	
	public IndexListenerFactory(SEWrapper sewrapper, BundleContext context) {
		this.sewrapper = sewrapper;
		this.context = context;
	}
	
	public ServiceListener getSearcherListener() {
		return new ServiceAdapter<IIndexSearcher>() {
			@Override
			protected void register(IIndexSearcher service) {
				IndexListenerFactory.this.sewrapper.addISearcher(service);
			}
			
			@Override
			protected void unregister() {
				IndexListenerFactory.this.sewrapper.removeISearcher();
			}
		};
	}
	
	public ServiceListener getModifierListener() {
		return new ServiceAdapter<IIndexModifier>() {
			@Override
			protected void register(IIndexModifier service) {
				IndexListenerFactory.this.sewrapper.addIModifier(service);
			}
			
			@Override
			protected void unregister() {
				IndexListenerFactory.this.sewrapper.removeIModifier();
			}
		};
	}
	
	public ServiceListener getWriterListener() {
		return new ServiceAdapter<IIndexWriter>() {
			@Override
			protected void register(IIndexWriter service) {
				IndexListenerFactory.this.sewrapper.addIWriter(service);
			}
			
			@Override
			protected void unregister() {
				IndexListenerFactory.this.sewrapper.removeIWriter();
			}
		};
	}
	
	public ServiceListener getTokenFactoryListener() {
		return new ServiceAdapter<ITokenFactory>() {
			@Override
			protected void register(ITokenFactory service) {
				IndexListenerFactory.this.sewrapper.addTokenFactory(service);
			}
			
			@Override
			protected void unregister() {
				IndexListenerFactory.this.sewrapper.removeTokenFactory();
			}
		};
	}
	
	private class ServiceAdapter<IndexService> implements ServiceListener {
		@SuppressWarnings("unchecked")
		public void serviceChanged(ServiceEvent event) {
			final IndexService service = (IndexService)IndexListenerFactory.this.context.getService(event.getServiceReference());
			switch (event.getType()) {
				case ServiceEvent.REGISTERED: register(service); break;
				case ServiceEvent.UNREGISTERING: unregister(); break;
				case ServiceEvent.MODIFIED: modify(service); break;
			}
		}
		
		protected void register(IndexService service) {  }
		protected void unregister() {  };
		protected void modify(IndexService service) {  }
	}
}
