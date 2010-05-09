/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2010 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.tools.console.impl.desktop;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.eclipse.osgi.framework.console.ConsoleSession;
import org.osgi.service.component.ComponentContext;
import org.paxle.desktop.DIComponent;

@Component(immediate=true)
@Service(DIComponent.class)
public class ConsoleDialogue extends JPanel implements DIComponent {	
	private static final long serialVersionUID = 1L;
	
	private static final Dimension DEFAULT_DIM = new Dimension(700, 500);
	private static final String TITLE = "Console";	
	
	private Frame frame;
	
	protected Console console;
	protected Session session;
	
	protected void activate(ComponentContext ctx) {
		this.console = new Console();
		this.session = new Session(this.console.getOutStream(),this.console.getInStream());
		ctx.getBundleContext().registerService(ConsoleSession.class.getName(), this.session, null);	
	}

	protected void deactivate(ComponentContext ctx) {
		this.console.close();
		this.session.close();
	}

	public Container getContainer() {
		return new JScrollPane(console);
	}

	public String getTitle() {
		return TITLE;
	}

	public Dimension getWindowSize() {
		return DEFAULT_DIM;
	}

	public void setFrame(Frame frame) {
		this.frame = frame;
		this.frame.setTitle(TITLE);
		this.frame.setPreferredSize(DEFAULT_DIM);
//	    this.frame.setIconImage(Toolkit.getDefaultToolkit().createImage(Luke.class.getResource("/img/luke.gif")));		
	}

	public void close() {
	}	
}
