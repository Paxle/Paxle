package org.paxle.p2p.services.impl;

import net.jxta.endpoint.Message;
import net.jxta.pipe.InputPipe;
import net.jxta.platform.ModuleClassID;
import net.jxta.protocol.ModuleClassAdvertisement;
import net.jxta.protocol.ModuleSpecAdvertisement;
import net.jxta.protocol.PipeAdvertisement;

import org.paxle.p2p.impl.P2PManager;

public abstract class AServiceServer extends AService implements Runnable {
	/**
	 * indicates if this thread was terminated
	 * @see #terminate()
	 */
	protected boolean stopped = false;

	/**
	 * the service master thread
	 * @see #run()
	 */
	protected Thread workerThread = null;

	/**
	 * a input-pipe to receive incoming requests
	 */
	private InputPipe serviceInputPipe;
	
	/**
	 * @param p2pManager required to get jxta peer-group services (e.g. pipe- and discovery-service)
	 */
	protected AServiceServer(P2PManager p2pManager) {
		super(p2pManager);
		
		this.workerThread = new Thread(this);
		this.workerThread.start();
	}
	
	/**
	 * Initialize this p2p service and publish it to the {@link #pg p2p-group}.
	 * This function
	 * <ul>
	 * 	<li>waits for a rendezvous connection</li>
	 *  <li>creates a {@link ModuleClassAdvertisement module-class-advertisement}. See {@link #createModClassAdv()}</li>
	 *  <li>publishes the {@link ModuleClassAdvertisement module-class-advertisement}</li>
	 *  <li>creates a {@link ModuleSpecAdvertisement module-specification-advertisement}. See {@link #createModSpecAdv(ModuleClassID)}</li>
	 *  <li>publishes the {@link ModuleSpecAdvertisement module-specification-advertisement}
	 *  <li>creates a {@link InputPipe input-pipe}</li>
	 *  <li>wait for new incoming requests. See {@link #run()}
	 * </ul>
	 */
	private void init() {
		try {		
			// wait for rendezvous connection
			this.p2pManager.waitForRdv();
			
			/* ===============================================================
			 * Create module class advertisement 
			 * =============================================================== */
			// create advertisement
			ModuleClassAdvertisement mcadv = this.createModClassAdv();
			this.workerThread.setName(mcadv.getName());

			// publish advertisement
			this.pgDiscoveryService.publish(mcadv);
			this.pgDiscoveryService.remotePublish(mcadv);
			
			/* ===============================================================
			 * Create module spec advertisement 
			 * =============================================================== */			
			// create advertisement
            ModuleSpecAdvertisement mdadv = this.createModSpecAdv(mcadv.getModuleClassID());
			
            // publish it
            this.pgDiscoveryService.publish(mdadv);
            this.pgDiscoveryService.remotePublish(mdadv);
            
			/* ===============================================================
			 * Create input pipe
			 * =============================================================== */	   
            this.serviceInputPipe = this.pgPipeService.createInputPipe(mdadv.getPipeAdvertisement());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Function to create the advertisement for the {@link #serviceInputPipe input-pipe} 
	 * @return
	 */
	protected abstract PipeAdvertisement createPipeAdvertisement();
	
	/**
	 * Function to create the {@link ModuleClassAdvertisement module-class-advertisement} 
	 * of this service
	 * @return the {@link ModuleClassAdvertisement module-class-advertisement} of this service
	 */
	protected abstract ModuleClassAdvertisement createModClassAdv();
	
	/**
	 * Function to create the {@link ModuleSpecAdvertisement module-specification-advertisement} of this service
	 * @param mcID the ID of the {@link ModuleClassAdvertisement module-class-advertisement}
	 * @return the {@link ModuleSpecAdvertisement module-specification-advertisement} of this service
	 */
	protected abstract ModuleSpecAdvertisement createModSpecAdv(ModuleClassID mcID);
	
	/**
	 * listening for new incoming requests
	 */
	public void run() {
		// init server side of service
		this.init();
		
		while (!this.stopped && !Thread.interrupted()) {
            try {
            	// fetch the next message
            	Message nextMsg = serviceInputPipe.waitForMessage();
            	System.out.println(nextMsg);

            	// process message
            	this.process(nextMsg);
            } catch (InterruptedException e) {
                Thread.interrupted();
                this.stopped = true;
            } catch (Exception e) {
            	e.printStackTrace();
            }
        }
	}
	
	@Override
	public void terminate() {
		this.stopped = true;
		this.workerThread.interrupt();
		try {
			this.workerThread.join(15000);
		} catch (InterruptedException e) {
			/* ignore this */
		}		
	};
	
	/**
	 * Process a request message received via the {@link #serviceInputPipe input-pipe}
	 * @param msg a request message received via the {@link #serviceInputPipe input-pipe}
	 */
	protected abstract void process(Message msg);
}
