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

package org.paxle.desktop;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;

/**
 * A Swing- or AWT-component that can be registered at the framework which provides
 * a graphical user-interface. The DesktopIntegration bundle then adds methods to access
 * this component to the SystemTray- and other menus it provides to access to Paxle's
 * GUI-elements.
 */
public interface DIComponent {
	
	/**
	 * Releases all resources associated with this component and prepares to close and
	 * dispose the GUI-elements. This method is called before the frame or parent-component
	 * the {@link #getContainer() container} this {@link DIComponent} provides is disposed.
	 */
	public abstract void close();
	
	/**
	 * Typically a {@link DIComponent} is shown in it's own {@link Frame}. The title of the
	 * frame is set with the value this method returns.
	 * <p><i>Please note: the title should be set on the {@link Frame} when {@link #setFrame(Frame)}
	 * has been invoked by the DesktopBundle. This method shall just provide a name for
	 * referencing this component.</i>
	 * @return a title for this {@link DIComponent} which can be displayed as a frame-title.
	 */
	public abstract String getTitle();
	
	/**
	 * To be able to set an optimal size for the {@link Frame} or component this
	 * {@link DIComponent}'s {@link #getContainer() container} is shown in, this method is
	 * being invoked when the parent component is being created by the DesktopIntegration
	 * bundle.
	 * @return the requested size for the container, this {@link DIComponent} is placed in
	 * @deprecated the size of the parent container should be set when {@link #setFrame(Frame)}
	 *             has been invoked by the DesktopBundle
	 */
	@Deprecated
	public abstract Dimension getWindowSize();
	
	/**
	 * @return the container to display in the parent container or {@link Frame}.
	 */
	public abstract Container getContainer();
	
	/**
	 * Invoked by the DesktopIntegration bundle when this {@link DIComponent} is prepared to
	 * being displayed. The argument is valid until {@link #clone()} has been called on this
	 * component.
	 * @param frame the {@link Frame} this component is being displayed in. 
	 */
	public abstract void setFrame(Frame frame);
}
