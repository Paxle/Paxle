/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2008 the original author or authors.
 * 
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 * 
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.gui.impl.servlets;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.paxle.gui.ALayoutServlet;

public class TheaddumpView extends ALayoutServlet {
	
	private static final long serialVersionUID = 1L;
	
	@Override
	public Template handleRequest( 
			HttpServletRequest request,
			HttpServletResponse response,
			Context context 
	) {
		
		Template template = null;
		
		try {
			// get the current dump
			Map<Thread,StackTraceElement[]> dumps = Thread.getAllStackTraces();
			context.put("dumps", dumps);
			
			// specify the template to use
			template = this.getTemplate("/resources/templates/ThreaddumpView.vm");            
		} catch (Exception e) {
			// TODO Auto-generated catch block
			this.logger.error("Error",e);
		}
		return template;
	}
}
