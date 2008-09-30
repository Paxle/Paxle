
package org.paxle.desktop.impl;

import java.lang.reflect.Array;
import java.util.Hashtable;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.paxle.core.prefs.IPropertiesStore;
import org.paxle.core.prefs.Properties;

public class ServiceManager {
	
	public static final int FRAMEWORK_BUNDLE_ID = 0;
	
	private final BundleContext context;
	
	public ServiceManager(BundleContext context) {
		this.context = context;
	}
	
	public void shutdownFramework() throws BundleException {
		String osgiFrameworkVendor = context.getProperty(Constants.FRAMEWORK_VENDOR);
		if (osgiFrameworkVendor.equalsIgnoreCase("Eclipse")) {
			// try to find an application launcher and shutdown
			Object app = this.getService("org.eclipse.osgi.service.runnable.ApplicationLauncher");
			if (app != null) {
				try {
					app.getClass().getMethod("shutdown", (Class[])null).invoke(app, (Object[])null);
					return;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}		
		
		final Bundle framework = this.context.getBundle(FRAMEWORK_BUNDLE_ID);
		if (framework != null) {
			framework.stop();
		}
	}
	
	public void restartFramework() throws BundleException {
		final Bundle framework = this.context.getBundle(FRAMEWORK_BUNDLE_ID);
		if (framework != null) {
			framework.update();
		}
	}
	
	public ServiceRegistration registerService(final Object service, final Hashtable<String,?> properties, final String cname) {
		return context.registerService(cname, service, properties);
	}
	
	public <E> ServiceRegistration registerService(final E service, final Hashtable<String,?> properties, final Class<? super E>... clazzes) {
		final String[] cnames = new String[clazzes.length];
		for (int i=0; i<clazzes.length; i++)
			cnames[i] = clazzes[i].getName();
		return context.registerService(cnames, service, properties);
	}
	
	public void addServiceListener(final ServiceListener listener) {
		try { addServiceListener(listener, null); } catch (InvalidSyntaxException e) { e.printStackTrace(); }
	}
	
	public void addServiceListener(final ServiceListener listener, final String filter) throws InvalidSyntaxException {
		context.addServiceListener(listener, filter);
	}
	
	public void removeServiceListener(final ServiceListener listener) {
		context.removeServiceListener(listener);
	}
	
	public Properties getServiceProperties() {
		final IPropertiesStore propstore = getService(IPropertiesStore.class);
		if (propstore == null)
			return null;
		return propstore.getProperties(this.context);
	}
	
	public String getProperty(String key) {
		return this.context.getProperty(key);
	}
	
	public Object getService(String name) {
		return getService(name, Object.class);
	}
	
	public <E> E getService(Class<E> service) {
		return getService(service.getName(), service);
	}
	
	public <E> E getService(String name, Class<E> service) {
		ServiceReference reference = this.context.getServiceReference(name);
		return (reference == null) ? null : getService(reference, service);
	}
	
	/**
	 * This invocation handler simply redirects all method-calls to the corresponding method-calls on
	 * the object given at instantiation. It therefore tries to solve the issues with different
	 * incompatible class-loaders occuring specifically during interaction with the DI-bundle which
	 * uses an own class-loader to handle JDIC-specifics.
	 * 
	 * @see org.paxle.desktop.impl.HelperClassLoader
	 * @see DIJdicBundle for details regarding JDIC
	 * @see ServiceManager#getService(ServiceReference, Class) for an example of how this class is used
	 */
	/* this stub is necessary because this class as well as the DIComponent-interface are loaded
	 * by the HelperClassLoader which is incompatible to OSGi's default bundle class-loader. *//*
	private final class RedirectingInvocationHandler implements InvocationHandler {
		
		private final Object s;
		private final Class<?> clazz;
		
		public RedirectingInvocationHandler(final Object s) {
			this.s = s;
			clazz = s.getClass();
		}
		
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			// method represents the Method-object from the interface given at instantiation of our
			// proxy object. This interface has been loaded by a classloader different from the one
			// the object's class has been loaded and is therefore not compatible to it. That's why
			// we have to retrieve and invoke the correct Method-object from the object's class here
			return clazz.getMethod(method.getName(), method.getParameterTypes()).invoke(s, args);
		}
	}*/
	
	public <E> E getService(final ServiceReference ref, final Class<E> clazz) {
		final Object service = context.getService(ref);
		try {
			// if (clazz.isInstance(service)) {
				return clazz.cast(service);
			/*} else {
				return clazz.cast(Proxy.newProxyInstance(
						Thread.currentThread().getContextClassLoader(),
						new Class<?>[] { clazz },
						new RedirectingInvocationHandler(service)));
			}*/
		} catch (Throwable t) { t.printStackTrace(); return null; }
	}
	
	public void ungetService(final ServiceReference ref) {
		context.ungetService(ref);
	}
	
	public boolean hasService(Class<?> service) {
		return hasService(service.getName());
	}
	
	public boolean hasService(Class<?> service, String query) throws InvalidSyntaxException {
		return hasService(service.getName(), query);
	}
	
	public boolean hasService(String name, String query) throws InvalidSyntaxException {
		try {
			return this.context.getServiceReferences(name, query) != null;
		} catch (Exception e) {
			if (e instanceof InvalidSyntaxException)
				throw (InvalidSyntaxException)e;
			return false;
		}
	}
	
	public boolean hasService(String name) {
		try {
			return this.context.getServiceReference(name) != null;
		} catch (Exception e) {
			return false;
		}
	}
	
	public <E> E[] getServices(final Class<E> service) {
		try {
			return getServices(service, null);
		} catch (InvalidSyntaxException e) { e.printStackTrace(); }
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public <E> E[] getServices(Class<E> service, String query) throws InvalidSyntaxException {
		final ServiceReference[] references = this.context.getServiceReferences(service.getName(), query);
		if (references == null) return null;
		
		final E[] services = (E[])Array.newInstance(service, references.length);
		for (int i=0; i<references.length; i++)
			services[i] = getService(references[i], service);
		return services;
	}
	
	public ServiceReference[] getServiceReferences(final String clazz, final String filter) throws InvalidSyntaxException {
		return context.getServiceReferences(clazz, filter);
	}
	
    public Bundle[] getBundles() {
        return this.context.getBundles();
    }
    
    public Bundle getBundle() {
    	return context.getBundle();
    }
    
    public Bundle getBundle(long bundleID) {
        return this.context.getBundle(bundleID);
    }

	public void addBundleListener(final BundleListener bundleListener) {
		this.context.addBundleListener(bundleListener);
	}
	
	public void removeBundleListener(final BundleListener bundleListener) {
		this.context.removeBundleListener(bundleListener);
	}
}
