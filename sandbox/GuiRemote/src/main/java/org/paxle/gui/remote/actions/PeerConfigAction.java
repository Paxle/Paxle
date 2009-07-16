package org.paxle.gui.remote.actions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeReference;
import org.paxle.gui.remote.cm.ConfigProperty;
import org.paxle.gui.remote.cm.PropertyValidationResult;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.Preparable;
import com.opensymphony.xwork2.Validateable;

public class PeerConfigAction extends ActionSupport implements Validateable, Preparable  {

	private static final long serialVersionUID = 1L;
	
	private ObjectMapper om = new ObjectMapper();
	
	private String peerUrl = null;
	private Long bundleId = null;
	private String pid =  null;
	
	private List<ConfigProperty> properties = null;
	
	private final HttpClient httpClient;
	
	public PeerConfigAction(HttpClient httpClient) {
		this.httpClient = httpClient;
	}
	
	private String buildConfigURL(boolean validateOnly) {
		// generate URL
		StringBuffer configURL = new StringBuffer(this.peerUrl).append("/configurations")
			.append("/").append(bundleId)
			.append("/").append(pid)
			.append("/properties");		
		if (validateOnly) configURL.append("?validateOnly=true");
		return configURL.toString();
	}
	
	private PostMethod buildRequest(boolean validateOnly) throws JsonGenerationException, IOException {
		// creating the request object
		PostMethod post = new PostMethod(this.buildConfigURL(true));
		post.setRequestHeader("Content-Type","application/json");		
		
		// serializing object
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		this.om.writeValue(bout, this.properties);
		bout.close();
		
		// set request body
		ByteArrayRequestEntity body = new ByteArrayRequestEntity(bout.toByteArray());
		post.setRequestEntity(body);
		
		return post;
	}
	
	
	private void loadProperties() throws JsonParseException, JsonMappingException, MalformedURLException, IOException {
		final String url = this.buildConfigURL(false);
		GetMethod get = null;
		try {
			// prepare method call
			get = new GetMethod(url);
			get.setRequestHeader("Content-Type","application/json");
			
			int status = this.httpClient.executeMethod(get);
			if (status != 200) {
				throw new IOException("Unable to load properties. Server returned: " + get.getStatusLine());
			}
			
			this.properties = om.readValue(get.getResponseBodyAsStream(), new TypeReference<List<ConfigProperty>>(){});	
		} finally {
			if (get != null) get.releaseConnection();
		}
	}
	
	public List<ConfigProperty> getProperties() throws JsonParseException, JsonMappingException, MalformedURLException, IOException {
		return this.properties;
	}
	
	@Override
	public void validate() {
		// nothing todo here
	}
	
	public void validateExecute() {
		// init post request
		PostMethod post = null;
		try {
			// building the request
			post = this.buildRequest(true);
			
			// executing the request
			int status = this.httpClient.executeMethod(post);
			if (status != 200) {
				// getting detailed error messages
				List<PropertyValidationResult> validationResults = om.readValue(post.getResponseBodyAsStream(), new TypeReference<List<PropertyValidationResult>>(){}); 
				if (validationResults != null) {
					for (PropertyValidationResult validationResult : validationResults) {
						String id = validationResult.getId();
						String errorMsg = validationResult.getErrorMsg();
						if (id != null && errorMsg != null) {
							for (int i=0; i < this.properties.size(); i++) {
								ConfigProperty property = this.properties.get(i);
								if (property.getId().equalsIgnoreCase(id)) {
									this.addFieldError("properties[" + i + "].value", errorMsg);
								}
							}
						}
					}
				}
			}	
		} catch (Exception e) {
			this.addActionError(e.getMessage());
		} finally {
			if (post != null) post.releaseConnection();
		}
	}	
	
	public String view() throws Exception {
		return Action.SUCCESS;
	}
	
	@Override
	public String execute() throws Exception {
		PostMethod post = null;
		try {
			// init the request
			post = this.buildRequest(false);

			// execute the request
			int status = this.httpClient.executeMethod(post);
			if (status != 200) {
				this.addActionError(post.getStatusLine().toString());
				return Action.INPUT;
			}
			
			return Action.SUCCESS;
		} finally {
			// close connection
			if (post != null) post.releaseConnection();
		}	
	}

	public String getPeerUrl() {
		return peerUrl;
	}

	public void setPeerUrl(String peerUrl) {
		this.peerUrl = peerUrl;
	}

	public Long getBundleId() {
		return bundleId;
	}

	public void setBundleId(Long bundleId) {
		this.bundleId = bundleId;
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public void prepare() throws Exception {
		if (this.peerUrl != null && this.bundleId != null && this.pid != null && this.properties == null) {
			this.loadProperties();
		}
	}	
}
 