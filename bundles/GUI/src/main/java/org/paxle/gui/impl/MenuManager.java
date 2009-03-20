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
package org.paxle.gui.impl;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;

import javax.imageio.ImageIO;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.paxle.gui.IMenuManager;

/**
 * @scr.component immediate="true" metatype="false"
 * @scr.service interface="org.paxle.gui.IMenuManager"
 * @scr.service interface="javax.servlet.Servlet"
 * @scr.property name="path" value="/menu"
 */
public class MenuManager extends HttpServlet implements IMenuManager, Servlet {
	private static final long serialVersionUID = 1L;
	
	private MenuItem rootItem;
	
	public MenuManager() {
		this.rootItem = new MenuItem(null, null);
	}
	public void addItem(String url, String name, String resourceBundleBaseName, ClassLoader loader, int pos, URL iconURL) {
		this.rootItem.addItem(url, name, resourceBundleBaseName, loader, pos, iconURL);
	}
	public Collection<MenuItem> getMenuItemList() {
		return this.rootItem.getMenuItemList();
	}
	public void removeItem(String name) {
		this.rootItem.removeItem(name);
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {
		if (req.getParameter("icon") != null) {
			String iconURL = req.getParameter("icon");
			BufferedImage img = ImageIO.read(new URL(iconURL));
			response.setHeader("Content-Type","image/png");
			ImageIO.write(img, "png", response.getOutputStream());
		}
	}
}
