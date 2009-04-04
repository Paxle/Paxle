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

import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LRUMap;
import org.apache.velocity.Template;
import org.paxle.core.filter.IFilter;
import org.paxle.filter.webgraph.impl.GraphFilter;
import org.paxle.gui.ALayoutServlet;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.layout.CollapsedSubtreeLayout;
import prefuse.action.layout.graph.RadialTreeLayout;
import prefuse.activity.Activity;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.io.GraphMLWriter;
import prefuse.data.io.GraphWriter;
import prefuse.render.AbstractShapeRenderer;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.EdgeRenderer;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;


/**
 * @scr.component immediate="true" metatype="false"
 * @scr.service interface="javax.servlet.Servlet"
 * @scr.property name="org.paxle.servlet.path" value="/domaingraph/prefuse"
 * @scr.property name="org.paxle.servlet.menu" value="%menu.administration/%menu.bundles/Webgraph"
 * @scr.property name="org.paxle.servlet.doUserAuth" value="true" type="Boolean"
 * @scr.property name="org.paxle.servlet.menu.icon" value="/resources/images/chart_organisation.png"
 */
public class PrefuseServlet extends ALayoutServlet {
	private static final long serialVersionUID = 1L;
	
	/** 
	 * @scr.reference target="(service.pid=org.paxle.filter.webgraph.impl.GraphFilter)"
	 */
	protected IFilter<?> filter;	
	
	protected LRUMap getRelations() {
		return ((GraphFilter)this.filter).getRelations();
	}
	
	/**
	 * Parts of this code were copied from:
	 * http://bytes.com/topic/java/answers/758481-need-help-prefuse-visulation-graph-save-jpeg-file
	 */	
	@Override
    protected void doRequest(HttpServletRequest request, HttpServletResponse response) {
		String display = request.getParameter("view");
		if (display == null) {
			// using velocity template
			super.doRequest(request, response);
		} else if (display.equalsIgnoreCase("graph") || display.equalsIgnoreCase("graphML")) {
			try {
				Graph graph = this.buildGraph();
				
				OutputStream out = response.getOutputStream();
				if (display.equalsIgnoreCase("graph")) {
					response.setContentType("image/png");
					
					Display d = this.buildDisplay(graph);
			        d.saveImage(out,"png",1);
				} else {
					response.setContentType("text/xml");
					
					GraphWriter writer = new GraphMLWriter();
					writer.writeGraph(graph, out);
				}
				
		        out.flush();
			} catch (Exception e) {
				this.log(e.getMessage(),e);
			}
		} 
	}
	
	private Graph buildGraph() {
		// creating the result structure
		Graph graph = new Graph(true);
		graph.addColumn(VisualItem.LABEL, String.class);

		// getting the relations map
		LRUMap relations= this.getRelations();
		
		// cloning the sourceDomain map
		HashMap<String, Node> domainMap = new HashMap<String, Node>();
		@SuppressWarnings("unchecked")
		HashSet<String> sourceDomains = new HashSet<String>(relations.keySet());

		for (String sourceDomain : sourceDomains) {
			// getting target domains
			@SuppressWarnings("unchecked")
			Set<String> targetDomains = (Set<String>) relations.get(sourceDomain);
			if (targetDomains == null || targetDomains.size() == 0) continue;			
			
			// getting or creating source
			Node sourceNode = domainMap.get(sourceDomain);
			if (sourceNode == null) {
				sourceNode = graph.addNode();
				sourceNode.setString(VisualItem.LABEL, sourceDomain);
				domainMap.put(sourceDomain, sourceNode);
			}
			
			for (String targetDomain : targetDomains) {
				// getting or creating target
				Node targetNode = domainMap.get(targetDomain);
				if (targetNode == null) {
					targetNode = graph.addNode();
					targetNode.setString(VisualItem.LABEL, targetDomain);
					domainMap.put(targetDomain, targetNode);
				}
				
				// adding link
				graph.addEdge(sourceNode, targetNode);
			}
		}
		
		return graph;
	}
	
	private Display buildDisplay(Graph graph) throws InterruptedException {
        // create a new, empty visualization for our data
        final Visualization vis = new Visualization();
        /* VisualGraph vg = */ vis.addGraph("graph", graph);
        
		// -- set up renderers --
		LabelRenderer nodeRenderer = new LabelRenderer(VisualItem.LABEL);
		nodeRenderer.setRenderType(AbstractShapeRenderer.RENDER_TYPE_FILL);
		nodeRenderer.setHorizontalAlignment(Constants.CENTER);
		nodeRenderer.setRoundedCorner(8,8);
		EdgeRenderer edgeRenderer = new EdgeRenderer(
				Constants.EDGE_TYPE_LINE,
				Constants.EDGE_ARROW_FORWARD
		);
		edgeRenderer.setArrowHeadSize(6, 10);
		DefaultRendererFactory rf = new DefaultRendererFactory(nodeRenderer);
		rf.add(new InGroupPredicate("graph.edges"), edgeRenderer);
		vis.setRendererFactory(rf);        
   
        
        // define colors
        int[] palette = new int[] {
                ColorLib.rgba(255,200,200,150),
                ColorLib.rgba(200,255,200,150),
                ColorLib.rgba(200,200,255,150)        		
        };

        DataColorAction fill = new DataColorAction("graph.nodes", VisualItem.LABEL, Constants.NOMINAL, VisualItem.FILLCOLOR, palette);
        ColorAction text = new ColorAction("graph.nodes",VisualItem.TEXTCOLOR, ColorLib.gray(0));
        ColorAction edges = new ColorAction("graph.edges", VisualItem.STROKECOLOR, ColorLib.rgb(200,200,200));          

        ActionList color = new ActionList(Activity.INFINITY);
        color.add(fill);  
        color.add(text);
        color.add(edges);        

        // configuring layout        
//        ForceDirectedLayout fdl = new ForceDirectedLayout("graph", true, true);
//        fdl.setIterations(1);
        
        ActionList layout = new ActionList(0,0);
        RadialTreeLayout treeLayout = new RadialTreeLayout("graph");
        // treeLayout.setAngularBounds(-Math.PI/2, Math.PI);
        layout.add(treeLayout);
        CollapsedSubtreeLayout subLayout = new CollapsedSubtreeLayout("graph");
        layout.add(subLayout);                 
        layout.add(new RepaintAction());

        // add the actions to the visualization
        vis.putAction("recolor", color);
        vis.putAction("color", color);
        vis.putAction("layout", layout);

        
        Display d = new Display(vis);
        d.setSize(640, 480); 
        d.setHighQuality(true);            
        
        vis.run("color");
        vis.run("layout");

        Thread.sleep(200); // XXX: this seems to be necessary
        		
        return d;
	}
	
	/**
	 * Choosing the template to use 
	 */
	@Override
	protected Template getTemplate(HttpServletRequest request, HttpServletResponse response) {
		return this.getTemplate("/resources/templates/graph.vm");
	}	
}
