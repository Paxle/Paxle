package org.paxle.gui.impl.servlets;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.paxle.core.IMWComponent;
import org.paxle.core.queue.ICommand;
import org.paxle.gui.ALayoutServlet;
import org.paxle.gui.impl.ServiceManager;

public class QueueView extends ALayoutServlet {
	
	private static final long serialVersionUID = 1L;
    
	@Override
	public Template handleRequest( 
    		HttpServletRequest request,
            HttpServletResponse response,
            Context context 
    ) {
        
        Template template = null;

        try {
        	ServiceManager manager = (ServiceManager) context.get(SERVICE_MANAGER);
        	
        	String queue = request.getParameter("queue");
            if (queue != null && queue.length() > 0) {
            	Object[] services = manager.getServices("org.paxle.core.IMWComponent","(component.ID="+queue+")");
                if (services != null && services.length == 1 && services[0] instanceof IMWComponent) {
                	List<ICommand> activeJobs = ((IMWComponent<ICommand>)services[0]).getActiveJobs();
                	context.put("activeJobs", activeJobs);
                	List<ICommand> enqueuedJobs = ((IMWComponent<ICommand>)services[0]).getEnqueuedJobs();
                	context.put("enqueuedJobs", enqueuedJobs);
                }
            }
            
			String reload = request.getParameter("reload");
			if (reload == null) {
				template = this.getTemplate("/resources/templates/QueueView.vm");
			} else if (reload.equals("queueList")) {
				// we don't want full html 
				context.put("layout", "plain.vm");
				
				// just return the activity overview
				template = getTemplate("/resources/templates/QueueViewLists.vm");
			} else if (reload.equals("overview")) {
				// we don't want full html 
				context.put("layout", "plain.vm");
				
				// just return the activity overview
				template = getTemplate("/resources/templates/QueueViewOverview.vm");
			}
            
        } catch (Exception e) {
            // TODO Auto-generated catch block
            this.logger.error("Error",e);
        }
        return template;
    }
}
