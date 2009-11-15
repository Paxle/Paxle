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

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.tools.generic.ResourceTool;
import org.apache.velocity.tools.generic.ResourceTool.Key;
import org.apache.velocity.tools.view.CookieTool;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.useradmin.Authorization;
import org.osgi.service.useradmin.Group;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;
import org.osgi.service.useradmin.UserAdmin;
import org.paxle.gui.ALayoutServlet;
import org.paxle.gui.IServiceManager;
import org.paxle.gui.impl.HttpAuthManager;

@Component(metatype=false, immediate=true)
@Service(Servlet.class)
@Properties({
	@Property(name="org.paxle.servlet.path", value="/users"),
	@Property(name="org.paxle.servlet.doUserAuth", boolValue=true),
	@Property(name="org.paxle.servlet.menu", value="%menu.administration/%menu.system/%menu.bundles.userAdmin"), 
	@Property(name="org.paxle.servlet.menu.icon", value="/resources/images/user.png")
})
public class UserView extends ALayoutServlet {
	private static final long serialVersionUID = 1L;
	public static final String USER_LANGUAGE = "user.language";
	
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
			final IServiceManager manager = (IServiceManager) context.get(IServiceManager.SERVICE_MANAGER);
			final UserAdmin userAdmin = (UserAdmin) manager.getService(UserAdmin.class.getName());
			
			// getting the resource-tool for error-message translation
			final ResourceTool rt = (ResourceTool) context.get("resourceTool");
			final Key k = rt.bundle("OSGI-INF/l10n/userview");			
			
			// getting some basic parameters
			final String action = request.getParameter("action");
			final String type = request.getParameter("type");
			final String name = request.getParameter("roleName");
			Role role = (name==null)?null:userAdmin.getRole(name);
			
			if (userAdmin == null) {
				String errorMsg = k.get("error.userAdmin.notFound").toString();
				context.put(ERROR_MSG, errorMsg);
			} else if (action != null && name != null) {						
				boolean redirect = false;
				
				if (!action.equalsIgnoreCase(ACTION_CREATE) && role == null) {
					String errorMsg = k.get("error.roleNotFound").insert(new String[]{name}).toString();
					context.put(ERROR_MSG, errorMsg);
				} else if (action.equalsIgnoreCase(ACTION_CREATE) && role != null) {
					String errorMsg = k.get("error.roleAlreadyExists").insert(new String[]{name}).toString();
					context.put(ERROR_MSG, errorMsg);
				} else if (action.equalsIgnoreCase(ACTION_DELETE)) {
					if (role.getName().equals("Administrator")) {
						String errorMsg = k.get("error.deletingAdminNotAllowed").toString();
						context.put(ERROR_MSG, errorMsg);
					} else {
						userAdmin.removeRole(name);
						redirect = true;
					} 
				} else if (action.equalsIgnoreCase(ACTION_CREATE) && type.equalsIgnoreCase(MODE_USER)) {
					role = userAdmin.createRole(name, Role.USER);
					this.updateUser(userAdmin, (User) role, request, context, k);
					if (!context.containsKey(ERROR_MSG)) {
						redirect = true;
					} else {
						userAdmin.removeRole(name);
						role = null;
					}
				} else if (action.equalsIgnoreCase(ACTION_UPDATE) && type.equalsIgnoreCase(MODE_USER)) {
					this.updateUser(userAdmin, (User) role, request, context, k);
					if (!context.containsKey(ERROR_MSG)) redirect = true;
				}  else if (action.equalsIgnoreCase(ACTION_CREATE) && type.equalsIgnoreCase(MODE_GROUP)) {
					role = userAdmin.createRole(name, Role.GROUP);
				} else if (action.equalsIgnoreCase(ACTION_UPDATE) && type.equalsIgnoreCase(MODE_GROUP)) {
					String errorMsg = k.get("error.notImplemented").toString();
					context.put(ERROR_MSG, errorMsg);
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
	
	
	private void updateUser(UserAdmin userAdmin, User user, HttpServletRequest request, Context context, Key k) throws InvalidSyntaxException, UnsupportedEncodingException {
		if (user == null) return;

		// getting the http.login name
		String loginName = request.getParameter(HttpAuthManager.USER_HTTP_LOGIN);
		
		/* ===========================================================
		 * USERNAME + PWD
		 * =========================================================== */
		// check if the login-name is not empty
		if (loginName == null || loginName.length() == 0) {
			String errorMsg = k.get("error.emptyLoginName").toString();
			this.logger.warn("The http.login name was empty or null.");
			context.put(ERROR_MSG,errorMsg);
			return;
		}
		
		// check if the login name is unique
		Role[] roles = userAdmin.getRoles(String.format("(%s=%s)",HttpAuthManager.USER_HTTP_LOGIN, loginName));
		if (roles != null && (roles.length > 2 || (roles.length == 1 && !roles[0].equals(user)))) {
			String errorMsg = k.get("error.usernameAlreadyKnown").insert(new String[]{loginName}).toString();
			this.logger.warn(String.format("The given login name '%s' is already used by a different user.", loginName));
			context.put(ERROR_MSG,errorMsg);
			return;
		}
		
		// check if the password is typed correctly
		String pwd1 = request.getParameter(HttpAuthManager.USER_HTTP_PASSWORD);
		String pwd2 = request.getParameter(HttpAuthManager.USER_HTTP_PASSWORD + "2");
		if (pwd1 == null || pwd2 == null || !pwd1.equals(pwd2)) {
			String errorMsg = k.get("error.invalidPassword").insert(new String[]{loginName}).toString();
			this.logger.warn(String.format("The password for login name '%s' was not typed correctly.", loginName));
			context.put(ERROR_MSG,errorMsg);
			return;
		}
		
		// configure http-login data
		@SuppressWarnings("unchecked")
		Dictionary<String, Object> props = user.getProperties();
		props.put(HttpAuthManager.USER_HTTP_LOGIN, loginName);
		
		@SuppressWarnings("unchecked")
		Dictionary<String, Object> credentials = user.getCredentials();
		credentials.put(HttpAuthManager.USER_HTTP_PASSWORD, pwd1);
		
		/* ===========================================================
		 * OPEN-ID
		 * =========================================================== */
		String openIdURL = request.getParameter("openid.url");
		if (openIdURL != null && openIdURL.length() > 0) {
			// check if URL is unique
			roles = userAdmin.getRoles(String.format("(openid.url=%s)", openIdURL));
			if (roles != null && (roles.length > 2 || (roles.length == 1 && !roles[0].equals(user)))) {
				String errorMsg = k.get("error.invalidOpenIDURL").insert(new String[]{openIdURL}).toString();
				this.logger.warn(String.format("The given OpenID URL '%s' is already used by a different user.", openIdURL));
				context.put(ERROR_MSG,errorMsg);
				return;
			}
			
			// configure the OpenID URL
			props.put("openid.url", openIdURL);			
		} else {
			// delete old URL
			user.getProperties().remove("openid.url");
		}
		
		/* ===========================================================
		 * LANGUAGE
		 * =========================================================== */
		if (request.getParameter(USER_LANGUAGE) != null) {
			String lang = request.getParameter(USER_LANGUAGE);
			props.put(USER_LANGUAGE,lang);
			CookieTool cookies = (CookieTool) context.get("cookieTool");
			cookies.add("l10n", lang);
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
