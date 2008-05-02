package org.paxle.p2p.services.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import net.jxta.document.AdvertisementFactory;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.document.XMLDocument;
import net.jxta.endpoint.Message;
import net.jxta.endpoint.MessageElement;
import net.jxta.endpoint.StringMessageElement;
import net.jxta.pipe.InputPipe;
import net.jxta.pipe.OutputPipe;
import net.jxta.platform.ModuleClassID;
import net.jxta.protocol.ModuleClassAdvertisement;
import net.jxta.protocol.ModuleSpecAdvertisement;
import net.jxta.protocol.PipeAdvertisement;

import org.paxle.p2p.impl.P2PManager;
import org.paxle.p2p.services.IServiceServer;
import org.paxle.p2p.services.search.impl.SearchClientImpl;
import org.paxle.p2p.services.search.impl.SearchServiceConstants;

public abstract class AServiceServer extends AService implements IServiceServer {
	/* ===================================================================
	 * Default Response message properties
	 * =================================================================== */	
	public static final String RESP_REQ_ID = AServiceClient.REQ_ID;
	
	/**
	 * @param p2pManager required to get jxta peer-group services (e.g. pipe- and discovery-service)
	 */
	protected AServiceServer(P2PManager p2pManager) {
		super(p2pManager);				
	}
	
	@Override
	public ArrayList<String> getExportedInterfaces() {
		ArrayList<String> interfaces = super.getExportedInterfaces();
		interfaces.add(IServiceServer.class.getName());
		return interfaces;		
	}
	
	/**
	 * Initialize this p2p service and publish it to the {@link #pg p2p-group}.
	 * This function
	 * <ul>
	 * 	<li>waits for a rendezvous connection</li>
	 *  <li>creates a {@link ModuleClassAdvertisement module-class-advertisement}. 
	 *      See {@link #createModClassAdv()}</li>
	 *  <li>publishes the {@link ModuleClassAdvertisement module-class-advertisement}</li>
	 *  <li>creates a {@link ModuleSpecAdvertisement module-specification-advertisement}. 
	 *      See {@link #createModSpecAdv(ModuleClassID)}</li>
	 *  <li>publishes the {@link ModuleSpecAdvertisement module-specification-advertisement}
	 *  <li>creates a {@link InputPipe input-pipe}.
	 *      See {@link #createPipeAdvertisement()}</li>
	 *  <li>wait for new incoming requests. 
	 *      See {@link #run()}
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
			if (!(e instanceof InterruptedException)) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Function to create the advertisement for an {@link #serviceInputPipe input-pipe} 
	 * @return the advertisement for an {@link #serviceInputPipe input-pipe} 
	 */
	@Override
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
	 * TODO: the requests are processes sequential. We need to span up multiple threads here ...
	 */
	@Override
	protected void process(Message msg) {
		this.processRequest(msg);
	}
	
	/**
	 * Process the next incoming request. 
	 * @see #process(Message)
	 */
	protected abstract void processRequest(Message reqMsg);
		
	/**
	 * Create a new pipe message that can be used to send
	 * a response to a remote peer
	 * @return a newly generated {@link Message response-message} containing
	 * <ul>
	 * 	<li>the {@link #REQ_ID request-id} of the received request message</li>
	 *  <li>...</li>
	 * </ul>
	 */
	protected Message createResponseMessage(Message reqMessage) {
		// create an empty message
		Message respMessage = this.createMessage();
		
		// getting the request ID
		MessageElement queryID = reqMessage.getMessageElement(SearchClientImpl.REQ_ID);
		String reqMsgNr = null;
		try {
			reqMsgNr = new String(queryID.getBytes(false),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			assert(true) : "Unexpected exception: " + e.getMessage();
		}
		
		// add the request ID to the response message
		respMessage.addMessageElement(null, new StringMessageElement(RESP_REQ_ID, reqMsgNr, null));
		return respMessage;
	}
	
	/**
	 * @param reqMessage the received request-message containing the {@link #REQ_PIPE_ADV pipe-advertisement} 
	 *        required to create a new {@link OutputPipe}
	 * @return the newly created {@link OutputPipe} to the remote peer, which has sent the request message
	 * @throws IOException if the pipe could not be created successfully.
	 * 
	 * @see #createResponsePipe(Message, long)
	 */
	protected OutputPipe createResponsePipe(Message reqMessage) throws IOException {
		return this.createResponsePipe(reqMessage, 10000l);
	}
		
	/**
	 * @param reqMessage the received request-message containing the {@link #REQ_PIPE_ADV pipe-advertisement} 
	 *        required to create a new {@link OutputPipe}
	 * @param connectTimeout the timeout to use (in milliseconds)
	 * @return the newly created {@link OutputPipe} to the remote peer, which has sent the request message
	 * @throws IOException if the pipe could not be created successfully.
	 */
	protected OutputPipe createResponsePipe(Message reqMessage, long connectTimeout) throws IOException {
		if (reqMessage == null) throw new NullPointerException("Request message is null");

		// getting the advertisement for the pipe where the response should be sent to
		MessageElement pipeAdv = reqMessage.getMessageElement(null, SearchServiceConstants.REQ_PIPE_ADV);
		if (pipeAdv == null) throw new IllegalArgumentException("The request message contains no pipe advertisement.");
		
		PipeAdvertisement adv = (PipeAdvertisement) AdvertisementFactory.newAdvertisement(
				(XMLDocument<?>) StructuredDocumentFactory.newStructuredDocument(pipeAdv)
		);

		// create an output pipe to send the request
		return this.pgPipeService.createOutputPipe(adv, connectTimeout);
	}
	
	/**
	 * This function ...
	 * <ul>
	 * 	<li>creates a new {@link OutputPipe output-pipe} to the client. 
	 *      See {@link #createResponsePipe(Message)}</li>
	 *  <li>sends the response-message</li>
	 *  <li>closes the {@link OutputPipe output-pipe}</li>
	 * </ul>
	 * @param reqMessage the received request-message containing the pipe-advertisement of the senders response-queue
	 * @param respMessage the response-message to send
	 */
	protected void sendResponseMessage(Message reqMessage, Message respMessage) {		
		if (reqMessage == null) throw new NullPointerException("Request message is null");
		if (respMessage == null) throw new NullPointerException("Response message is null");

		OutputPipe outputPipe = null;
		try {
			// create an output pipe to send the request
			outputPipe = this.createResponsePipe(reqMessage);

			// send the response back to the client
			boolean success = outputPipe.send(respMessage);
			if (!success) {
				throw new IOException("Unable to send response message.");
			}			
			this.sentMsgCount++;
			this.sentBytes += respMessage.getByteLength();

			// close pipe
			outputPipe.close();		
		} catch (IOException e) {
			this.logger.warn(String.format("Error while sending a response-message for request %s.",
					reqMessage.getMessageElement(SearchServiceConstants.REQ_ID)
			),e);
		}
	}
}
