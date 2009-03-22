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
package org.paxle.gui.impl.servlets;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.useradmin.Authorization;
import org.osgi.service.useradmin.Group;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;
import org.osgi.service.useradmin.UserAdmin;
import org.paxle.gui.ALayoutServlet;
import org.paxle.gui.impl.HttpContextAuth;
import org.paxle.gui.impl.ServiceManager;

/**
 * @scr.component immediate="true" metatype="false"
 * @scr.service interface="javax.servlet.Servlet"
 * @scr.property name="org.paxle.servlet.path" value="/users"
 * @scr.property name="org.paxle.servlet.menu" value="%menu.administration/%menu.system/%menu.bundles.userAdmin"
 * @scr.property name="org.paxle.servlet.doUserAuth" value="true" type="Boolean"
 * @scr.property name="org.paxle.servlet.menu.icon" value="/resources/images/user.png"
 */
public class UserView extends ALayoutServlet {
	private static final long serialVersionUID = 1L;
	
	private static final String ERROR_MSG = "errorMsg";
	
	private static final String ACTION_CREATE = "create";
	private static final String ACTION_UPDATE = "update";
	private static final String ACTION_DELETE = "delete";
	private static final String MODE_USER = "user";
	private static final String MODE_GROUP = "group";

	@Override
	public Template handleRequest(HttpServletRequest request, HttpServletResponse response, Context context) throws Exception {
		
		Template template = null;
		try {
			// getting the servicemanager
			final ServiceManager manager = (ServiceManager) context.get(SERVICE_MANAGER);
			final UserAdmin userAdmin = (UserAdmin) manager.getService(UserAdmin.class.getName());
			
			// getting some basic parameters
			final String action = request.getParameter("action");
			final String type = request.getParameter("type");
			final String name = request.getParameter("roleName");
			Role role = (name==null)?null:userAdmin.getRole(name);
			
			if (userAdmin == null) {
				context.put(ERROR_MSG, "Unable to find the user-admin service.");
			} else if (action != null && name != null) {
				boolean redirect = false;
				
				if (!action.equalsIgnoreCase(ACTION_CREATE) && role == null) {
					context.put(ERROR_MSG,String.format("Role with name '%s' can not be found.",name));
				} else if (action.equalsIgnoreCase(ACTION_CREATE) && role != null) {
					context.put(ERROR_MSG, String.format("Role with name '%s' already exists.",name));
				} else if (action.equalsIgnoreCase(ACTION_DELETE)) {
					if (role.getName().equals("Administrator")) {
						context.put(ERROR_MSG,"You are not allowed to delete the Administrator account.");
					} else {
						userAdmin.removeRole(name);
						redirect = true;
					} 
				} else if (action.equalsIgnoreCase(ACTION_CREATE) && type.equalsIgnoreCase(MODE_USER)) {
					role = userAdmin.createRole(name, Role.USER);
					this.updateUser(userAdmin, (User) role, request, context);
					redirect = true;
				} else if (action.equalsIgnoreCase(ACTION_UPDATE) && type.equalsIgnoreCase(MODE_USER)) {
					this.updateUser(userAdmin, (User) role, request, context);
					redirect = true;
				}  else if (action.equalsIgnoreCase(ACTION_CREATE) && type.equalsIgnoreCase(MODE_GROUP)) {
					role = userAdmin.createRole(name, Role.GROUP);
				} else if (action.equalsIgnoreCase(ACTION_UPDATE) && type.equalsIgnoreCase(MODE_GROUP)) {
					context.put(ERROR_MSG, "Not implemented");
				}

				// redirect request if required
				if (redirect) response.sendRedirect(request.getServletPath());
			}
			
			context.put("role", role);
			context.put("userView", this);
			
			template = this.getTemplate( "/resources/templates/UserView.vm");
		} catch (ResourceNotFoundException e) {
			logger.error( "resource: " + e);
			e.printStackTrace();
		} catch (ParseErrorException e) {
			logger.error( "parse : " + e);
			e.printStackTrace();
		} catch (Exception e) {
			logger.error( "exception: " + e);
			e.printStackTrace();
		}

		return template;
	}
	
	private void updateUser(UserAdmin userAdmin, User user, HttpServletRequest request, Context context) throws InvalidSyntaxException, UnsupportedEncodingException {
		if (user == null) return;
		
		String loginName = request.getParameter(HttpContextAuth.USER_HTTP_LOGIN);
		
		/* ===========================================================
		 * USERNAME + PWD
		 * =========================================================== */
		// check if the login-name is not empty
		if (loginName == null || loginName.length() == 0) {
			String errorMsg = String.format("The login name must not be null or empty.");
			this.logger.warn(errorMsg);
			context.put("errorMsg",errorMsg);
			return;
		}
		
		// check if the login name is unique
		Role[] roles = userAdmin.getRoles(String.format("(%s=%s)",HttpContextAuth.USER_HTTP_LOGIN, loginName));
		if (roles != null && (roles.length > 2 || (roles.length == 1 && !roles[0].equals(user)))) {
			String errorMsg = String.format("The given login name '%s' is already used by a different user.", loginName);
			this.logger.warn(errorMsg);
			context.put("errorMsg",errorMsg);
			return;
		}
		
		// check if the password is typed correctly
		String pwd1 = request.getParameter(HttpContextAuth.USER_HTTP_PASSWORD);
		String pwd2 = request.getParameter(HttpContextAuth.USER_HTTP_PASSWORD + "2");
		if (pwd1 == null || pwd2 == null || !pwd1.equals(pwd2)) {
			String errorMsg = String.format("The password for login name '%s' was not typed correctly.", loginName);
			this.logger.warn(errorMsg);
			context.put("errorMsg",errorMsg);
			return;
		}
		
		// configure http-login data
		@SuppressWarnings("unchecked")
		Dictionary<String, Object> props = user.getProperties();
		props.put(HttpContextAuth.USER_HTTP_LOGIN, loginName);
		
		@SuppressWarnings("unchecked")
		Dictionary<String, Object> credentials = user.getCredentials();
		credentials.put(HttpContextAuth.USER_HTTP_PASSWORD, pwd1);
		
		/* ===========================================================
		 * OPEN-ID
		 * =========================================================== */
		String openIdURL = request.getParameter("openid.url");
		if (openIdURL != null && openIdURL.length() > 0) {
			// check if URL is unique
			roles = userAdmin.getRoles(String.format("(openid.url=%s)", openIdURL));
			if (roles != null && (roles.length > 2 || (roles.length == 1 && !roles[0].equals(user)))) {
				String errorMsg = String.format("The given OpenID URL '%s' is already used by a different user.", openIdURL);
				this.logger.warn(errorMsg);
				context.put("errorMsg",errorMsg);
				return;
			}
			
			// configure the OpenID URL
			props = user.getProperties();
			props.put("openid.url", openIdURL);			
		} else {
			// delete old URL
			user.getProperties().remove("openid.url");
		}
		
		/* ===========================================================
		 * MEMBERSHIP
		 * =========================================================== */
		// process membership
		Authorization auth = userAdmin.getAuthorization(user);		
		String[] currentMembership = auth.getRoles();
		if (currentMembership == null) currentMembership = new String[0];
		
		String[] newMembership = request.getParameterValues("membership");
		if (newMembership == null) newMembership = new String[0];
		
		// new memberships
		for (String groupName : newMembership) {
			if (!auth.hasRole(groupName)) {
				Role role = userAdmin.getRole(groupName);
				if (role != null && role.getType() == Role.GROUP) {
					((Group)role).addMember(user);
				}
			}
		}

		// memberships to remove
		ArrayList<String> oldMemberships = new ArrayList<String>(Arrays.asList(currentMembership));
		oldMemberships.removeAll(Arrays.asList(newMembership));
		for (String roleName : oldMemberships) {
			if (auth.hasRole(roleName)) {
				Role role = userAdmin.getRole(roleName);
				if (role != null && role.getType() == Role.GROUP) {
					((Group)role).removeMember(user);
				}
			}
		}
	}
	
	public Group[] getParentGroups(UserAdmin userAdmin, User user) throws InvalidSyntaxException {
		ArrayList<Group> groups = new ArrayList<Group>();		
		
		if (user != null) {
			Authorization auth = userAdmin.getAuthorization(user);
			if (auth != null) {
				String[] currentRoles = auth.getRoles();
				if (currentRoles != null) {
					for (String roleName : currentRoles) {
						Role role = userAdmin.getRole(roleName);
						if (role != null && role.getType() == Role.GROUP) {
							groups.add((Group) role);
						}
					}
				}
			}
		}
		
		return groups.toArray(new Group[groups.size()]);
	}
}
