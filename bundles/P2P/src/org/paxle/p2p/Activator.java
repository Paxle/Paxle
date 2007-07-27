package org.paxle.p2p;
/*
 * Created on Fri Jul 27 18:27:15 GMT+02:00 2007
 */

import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupFactory;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.axlight.jnushare.gisp.GISPImpl;
import com.axlight.jnushare.gisp.ResultListener;

public class Activator implements BundleActivator {
  
  /* (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  public void start(BundleContext context) throws Exception {
	  //EXAMPLE FROM: https://gisp.dev.java.net/
	  PeerGroup group = PeerGroupFactory.newNetPeerGroup();

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