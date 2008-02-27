package org.paxle.mimetype.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.paxle.core.io.IOTools;
import org.paxle.core.mimetype.IMimeTypeDetector;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class Activator implements BundleActivator {

	/**
	 * A reference to the {@link BundleContext bundle-context}
	 */
	public static BundleContext bc;			
	
	/**
	 * This function is called by the osgi-framework to start the bundle.
	 * @see BundleActivator#start(BundleContext) 
	 */		
	public void start(BundleContext context) throws Exception {
		bc = context;
		
		Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
		System.out.println(this.getClass().getClassLoader().loadClass("org.apache.xerces.parsers.SAXParser"));
		XMLReader parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
		
		URL testFile = bc.getBundle().getResource("test.txt");
		FileOutputStream fout = new FileOutputStream("/tmp/test.txt");
		IOTools.copy(testFile.openStream(), fout);
		fout.flush();
		fout.close();
		
//		this.getClass().getClassLoader().loadClass("org.apache.xerces.parsers.SAXParser");
//		Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
		
		/* ==========================================================
		 * Register Services provided by this bundle
		 * ========================================================== */		
		// register the SubParser-Manager as service
		MimeTypeDetector detector = new MimeTypeDetector(null);
		System.out.println(detector.getMimeType(new File("/tmp/test.txt")));
		
		bc.registerService(IMimeTypeDetector.class.getName(), detector , null);
		
		/* ==========================================================
		 * Register Service Listeners
		 * ========================================================== */		
		bc.addServiceListener(new DetectionHelperListener(detector,bc),DetectionHelperListener.FILTER);	
	}

	/**
	 * This function is called by the osgi-framework to stop the bundle.
	 * @see BundleActivator#stop(BundleContext)
	 */		
	public void stop(BundleContext context) throws Exception {
		// cleanup
		bc = null;		
	}
}