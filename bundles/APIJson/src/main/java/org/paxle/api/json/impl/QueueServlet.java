package org.paxle.api.json.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializable;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializerProvider;
import org.osgi.framework.ServiceReference;
import org.paxle.core.IMWComponent;
import org.paxle.core.doc.ICrawlerDocument;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.queue.ICommand;

/**
 * @scr.component
 * @scr.property name="path" value="/json/queue"
 * 
 * @scr.reference name="component" interface="org.paxle.core.IMWComponent" cardinality="1..n" policy="dynamic" bind="addComponent" unbind="removeComponent"
 */
public class QueueServlet extends AJsonServlet {
	private static final long serialVersionUID = 1L;
	
	Map<String,ServiceReference> components = new HashMap<String, ServiceReference>();
	
	protected void addComponent(ServiceReference ref) {
		components.put((String)ref.getProperty("component.ID"),ref);
	}
			
	protected void removeComponent(ServiceReference ref) {
		components.remove((String)ref.getProperty("component.ID"));
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		ObjectMapper jtm = new ObjectMapper();
		
		if (req.getParameter("debug") == null) {
			resp.setContentType("application/json");
		}
	
		String[] queueIds = req.getParameterValues("queue");
		if (queueIds == null) {
			Map<String, QueueOverview> queues = new HashMap<String, QueueOverview>();
			for (Entry<String, ServiceReference> entry : this.components.entrySet()) {
				queues.put(entry.getKey(), new QueueOverview(entry.getValue()));
			}
			
			// write json out			
			jtm.writeValue(resp.getOutputStream(), queues);
		} else {
			Map<String, QueueView> queues = new HashMap<String, QueueView>();
			
			for (String queueId : queueIds) {
				if (this.components.containsKey(queueId)) {
					queues.put(queueId, new QueueView(this.components.get(queueId)));
				}
			}
			
			// write json out
			jtm.writeValue(resp.getOutputStream(), queues);
		}
	}
	
	class QueueOverview implements JsonSerializable {
		private final ServiceReference ref;
		public QueueOverview(ServiceReference ref) {
			this.ref = ref;
		}
		
		public void serialize(JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
			jgen.writeStartObject();
			
			IMWComponent<?> component = (IMWComponent<?>) ctx.locateService("component", this.ref);
			jgen.writeNumberField("activeCount", new Integer(component.getActiveJobCount()));
			jgen.writeNumberField("enqueuedCount", new Integer(component.getEnqueuedJobCount()));
			jgen.writeNumberField("ppm", new Integer(component.getPPM()));
			
			jgen.writeEndObject();
		}
	}
	
	class QueueView implements JsonSerializable {
		private final ServiceReference ref;
		public QueueView(ServiceReference ref) {
			this.ref = ref;
		}
				
		public void serialize(JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
			jgen.writeStartObject();
			
			IMWComponent<?> component = (IMWComponent<?>) ctx.locateService("component", this.ref);
			
			@SuppressWarnings("unchecked")
			List<ICommand> activeJobs = (List<ICommand>) component.getActiveJobs();
			@SuppressWarnings("unchecked")
			List<ICommand> enqueuedJobs = (List<ICommand>) component.getEnqueuedJobs();
			
			jgen.writeArrayFieldStart("activeJobs");
			this.writeList(activeJobs, jgen);
			jgen.writeEndArray();

			jgen.writeArrayFieldStart("enqueuedJobs");
			this.writeList(enqueuedJobs, jgen);
			jgen.writeEndArray();

			jgen.writeEndObject();
		}
		
		private void writeList(List<ICommand> commands, JsonGenerator jgen) throws JsonGenerationException, IOException {
			if (commands == null || commands.size() == 0) return;
			
			for (ICommand cmd : commands) {
				ICrawlerDocument cDoc = cmd.getCrawlerDocument();
				IParserDocument pDoc = cmd.getParserDocument();
				
				jgen.writeStartObject();
				jgen.writeNumberField("OID", cmd.getOID());
				jgen.writeNumberField("profileID", cmd.getProfileOID());
				jgen.writeStringField("location", cmd.getLocation().toString());
				jgen.writeStringField("mimeType", pDoc==null?"":pDoc.getMimeType());
				jgen.writeNumberField("size", cDoc==null?-1:cDoc.getSize());
				jgen.writeEndObject();
			}
		}
	}
}
