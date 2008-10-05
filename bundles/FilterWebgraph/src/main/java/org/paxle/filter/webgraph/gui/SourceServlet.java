package org.paxle.filter.webgraph.gui;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.paxle.filter.webgraph.impl.GraphFilter;

public class SourceServlet extends HttpServlet{
	private static final long serialVersionUID = 1L;
	private GraphFilter filter;
	public SourceServlet(GraphFilter filter){
		this.filter=filter;
	}
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		Map<String, Set<String>> relations=this.filter.getRelations();
		StringBuffer result=new StringBuffer("digraph domains{\n");
		Iterator<String> it=relations.keySet().iterator();
		while(it.hasNext()){
			String domain1=it.next();
			Iterator it2=relations.get(domain1).iterator();
			while(it2.hasNext()){
				result.append("\"").append(domain1).append("\"").append("->").append("\"").append(it2.next()).append("\";\n");
			}
		}
		result.append("}");
		resp.setContentType("text/plain");
		resp.setStatus(200);
		resp.getOutputStream().print(result.toString());
	}

}
