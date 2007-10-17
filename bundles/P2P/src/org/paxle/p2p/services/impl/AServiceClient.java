package org.paxle.p2p.services.impl;

import net.jxta.endpoint.Message;

import org.paxle.p2p.impl.P2PManager;


abstract class AServiceClient extends AService {

	/**
	 * @param p2pManager required to get jxta peer-group services (e.g. pipe- and discovery-service)
	 */
	protected AServiceClient(P2PManager p2pManager) {
		super(p2pManager);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void init() {
		try {		
			// wait for rendezvous connection
			this.p2pManager.waitForRdv();
			
			// create a pipe advertisement
			this.servicePipeAdv = this.createPipeAdvertisement();
			
			// create a new input pipe
            this.serviceInputPipe = this.pgPipeService.createInputPipe(this.servicePipeAdv);
            
            // TODO: do we need to publish our pipe advertisement?
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void process(Message msg) {
		this.processResponse(msg);
	}
	
	/**
	 * Function to process the received response message.
	 * @param respMsg the response message that was received
	 */
	protected abstract void processResponse(Message respMsg);
}
