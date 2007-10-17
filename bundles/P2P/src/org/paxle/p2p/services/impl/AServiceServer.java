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
	 * @param p2pManager required to get jxta peer-group services (e.g. pipe- and discovery-service)
	 */
	protected AServiceServer(P2PManager p2pManager) {
		super(p2pManager);				
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
	@Override
	protected void init() {
		try {		
			// wait for rendezvous connection
			this.p2pManager.waitForRdv();
			
			/* ===============================================================
			 * Create module class advertisement 
			 * =============================================================== */
			// create advertisement
			ModuleClassAdvertisement mcadv = this.createModClassAdv();
			this.setName(mcadv.getName() + ":Server");

			// publish advertisement
			this.pgDiscoveryService.publish(mcadv);
			this.pgDiscoveryService.remotePublish(mcadv);
			
			/* ===============================================================
			 * Create module spec advertisement 
			 * =============================================================== */
			// create a new pipe-advertisement
			this.servicePipeAdv = this.createPipeAdvertisement();
			
			// create advertisement
            ModuleSpecAdvertisement mdadv = this.createModSpecAdv(mcadv.getModuleClassID(), this.servicePipeAdv);
			
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
	 * Function to create the advertisement for a {@link #serviceInputPipe input-pipe} 
	 * @return the advertisement for a {@link #serviceInputPipe input-pipe} 
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
	 * @param pipeAdv the {@link PipeAdvertisement} that should be used for the {@link ModuleSpecAdvertisement module-specification-advertisement}
	 * @return the {@link ModuleSpecAdvertisement module-specification-advertisement} of this service
	 */
	protected abstract ModuleSpecAdvertisement createModSpecAdv(ModuleClassID mcID, PipeAdvertisement pipeAdv);
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void process(Message msg) {
		this.processRequest(msg);
	}
	
	/**
	 * Process the next incoming request
	 */
	protected abstract void processRequest(Message reqMsg);
}
