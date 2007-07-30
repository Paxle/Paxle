package org.paxle.p2p;
/*
 * Created on Fri Jul 27 18:27:15 GMT+02:00 2007
 */

import java.io.File;

import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupFactory;
import net.jxta.platform.NetworkManager;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.axlight.jnushare.gisp.GISPImpl;
import com.axlight.jnushare.gisp.ResultListener;

public class Activator implements BundleActivator {
  
	/**
	 * ATTENTION: Set the property value of
	 * <code>org.osgi.framework.system.packages</code>
	 * to <code>javax.security.cert,sun.reflect</code>
	 * to run the following code:	
	 */
  public void start(BundleContext context) throws Exception {
//	  //EXAMPLE FROM: https://gisp.dev.java.net/
//	  PeerGroup group = PeerGroupFactory.newNetPeerGroup();

	  NetworkManager manager = null;
      try {
          manager = new NetworkManager(NetworkManager.ConfigMode.EDGE, "DiscoveryServer"
                  ,
                  new File(new File(".cache"), "DiscoveryServer").toURI());
          manager.startNetwork();
      } catch (Exception e) {
          e.printStackTrace();
          System.exit(-1);
      }
      PeerGroup group = manager.getNetPeerGroup();	  
	  
	  
	  GISPImpl gisp = new GISPImpl();
	  gisp.init(group, null, null);
	  gisp.startApp(null);


	  gisp.insert("tag1", "this is a string");

	  gisp.query("tag1", new ResultListener(){
	      public void stringResult(String data){
	        System.out.println("Got result: " + data);
	      }
	      public void xmlResult(byte[] data){
	      }
	      public void queryExpired(){
	      }
	    });
  }

  /* (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  public void stop(BundleContext context) throws Exception {
  }
}