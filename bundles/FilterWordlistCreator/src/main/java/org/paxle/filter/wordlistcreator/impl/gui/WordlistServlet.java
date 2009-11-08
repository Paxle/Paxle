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
package org.paxle.filter.wordlistcreator.impl.gui;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.paxle.filter.wordlistcreator.ITokenManager;
import org.paxle.gui.ALayoutServlet;

@Component(metatype=false, immediate=true,
		label="Wordlist Servlet",
		description="A Servlet to manage your wordlists"
)
@Service(Servlet.class)
@Properties({
	@Property(name="org.paxle.servlet.path", value="/wordlist"),
	@Property(name="org.paxle.servlet.doUserAuth", boolValue=false),
	@Property(name="org.paxle.servlet.menu", value="%menu.administration/%menu.bundles/%wordlistServlet.menu"), 
	@Property(name="org.paxle.servlet.menu.icon", value="/resources/images/book_link.png")
})
public class WordlistServlet extends ALayoutServlet implements Servlet {

	private static final long serialVersionUID = 1L;

	@Reference
	protected ITokenManager tokenManager = null;
	
	public Template handleRequest( HttpServletRequest request,
			HttpServletResponse response,
			Context context ) {
		Template template = null;
		try {
			template = this.getTemplate("/resources/templates/Wordlist.vm");
		} catch( Exception e ) {
			//logger.warn("Unexpected Error:", e);
		}
		context.put("tokenManager", tokenManager);
		return template;
	}
}

