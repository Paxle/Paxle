package org.paxle.p2p.services.impl;

import java.io.IOException;
import java.util.ArrayList;

import net.jxta.document.MimeMediaType;
import net.jxta.endpoint.InputStreamMessageElement;
import net.jxta.endpoint.Message;
import net.jxta.endpoint.StringMessageElement;

import org.paxle.p2p.impl.P2PManager;
import org.paxle.p2p.services.IServiceClient;


public abstract class AServiceClient extends AService implements IServiceClient {
	/* ===================================================================
	 * Request message properties
	 * =================================================================== */
	public static final String REQ_ID = "reqID";
	public static final String REQ_PIPE_ADV = "pipeAdv";

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
	
	@Override
	public ArrayList<String> getExportedInterfaces() {
		ArrayList<String> interfaces = super.getExportedInterfaces();
		interfaces.add(IServiceClient.class.getName());
		return interfaces;		
	}
	
	@Override
	protected Message createMessage() {
		return this.createRequestMessage();
	}

	/**
	 * Create a new pipe message that can be used to send
	 * a request to a remote peer
	 * @return a newly generated {@link Message request-message} containing
	 * <ul>
	 * 	<li>a unique {@link #REQ_ID request-id}</li>
	 *  <li>the {@link #REQ_PIPE_ADV pipe-advertisement} of the service {@link AService#serviceInputPipe input-pipe}</li>
	 * </ul>
	 */
	protected Message createRequestMessage() {
		// create a new message
		Message reqMsg = super.createMessage();
		
		// add the unique request message id 
		int reqNr = reqMsg.getMessageNumber();
		reqMsg.addMessageElement(null, new StringMessageElement(REQ_ID, Integer.toString(reqNr), null));
		
		// add the advertisement of the response queue
		try {
			InputStreamMessageElement isme = new InputStreamMessageElement(
					REQ_PIPE_ADV,
					new MimeMediaType("text", "xml"),
					servicePipeAdv.getDocument(new MimeMediaType("text", "xml")).getStream(),
					null);            
			reqMsg.addMessageElement(null, isme, null);
		} catch (IOException e) {
			assert(true) : "Unexpected exception: " + e.getMessage();
		}
		
		return reqMsg;
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
	 * 
	 * @see #process(Message)
	 */
	protected abstract void processResponse(Message respMsg);
}
