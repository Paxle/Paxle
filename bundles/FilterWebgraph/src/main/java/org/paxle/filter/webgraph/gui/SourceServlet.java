/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2009 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package org.paxle.filter.webgraph.gui;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LRUMap;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.paxle.core.filter.IFilter;
import org.paxle.filter.webgraph.impl.GraphFilter;

@Component(metatype=false, immediate=true)
@Service(Servlet.class)
@Properties({
	@Property(name="org.paxle.servlet.path", value="/domaingraph/graphviz"),
	@Property(name="org.paxle.servlet.doUserAuth", boolValue=true)
})
public class SourceServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	@Reference(target="(service.pid=org.paxle.filter.webgraph.impl.GraphFilter)")
	protected IFilter<?> filter;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		/*
		 * Okay, we have a lot of relations here. the filter stores up to 5000 domains with all relations,
		 * if we graph this, the graph gets big and there is no overview.
		 * What we do at the moment is this:
		 * - limit the maximum Number of Nodes(Domains) to maxDomains(=100)
		 * - limit the number of new domains introduced by a parent-domain to count(=5)
		 * this keeps the graph small.
		 */
		LRUMap relations= ((GraphFilter)this.filter).getRelations();
		StringBuffer result=new StringBuffer("digraph domains{\nedge [color=\"#80808080\"]\n");
		Iterator it=(new HashSet(relations.keySet())).iterator();
		int maxDomains=1000;
		int maxChildDomains=5;
		int numDomains=0;
		int minReferences=3;
		HashSet<String> domains=new HashSet<String>();
		while(it.hasNext()){
			String domain1=(String)it.next();
			//skip if the domain is new and we already know maxDomains
			if(!domains.contains(domain1)){
				if(numDomains >= maxDomains) 
					continue;
				domains.add(domain1);
				numDomains++;
			}
			//only nodes with at least minReferences ChildNodes (no lonely nodes)
			if(((Set)relations.get(domain1)).size()<minReferences) continue;
			Iterator<String> it2=((Set)relations.get(domain1)).iterator();
			int count=0; //new domains from this parent-node
			while(it2.hasNext()){
				String domain2=(String) it2.next();
				if(!domains.contains(domain2)){
					if(numDomains >= maxDomains) 
						continue;
					if(count > maxChildDomains)
						continue;
					numDomains++;
					domains.add(domain2);
					count++;
				}
				result.append("\"").append(domain1).append("\"").append("->").append("\"").append(domain2).append("\";\n");
			}
		}
		result.append("}");
		resp.setContentType("text/plain");
		resp.setStatus(200);
		resp.getOutputStream().print(result.toString());
	}

}
