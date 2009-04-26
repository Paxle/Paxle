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
package org.paxle.data.db.impl;

import java.io.Serializable;

import org.hibernate.EmptyInterceptor;
import org.hibernate.EntityMode;
import org.paxle.core.queue.Command;
import org.paxle.core.queue.CommandProfile;
import org.paxle.core.queue.ICommand;
import org.paxle.core.queue.ICommandProfile;

public class InterfaceInterceptor extends EmptyInterceptor {
	private static final long serialVersionUID = 1L;
	
	@Override
	public String getEntityName(Object object) {
        if (object instanceof ICommand) {
            return ICommand.class.getName();
        } else if (object instanceof ICommandProfile) {
        	return ICommandProfile.class.getName();
        }
        return super.getEntityName(object);
    }
	
	@Override
    public Object instantiate(String entityName, EntityMode entityMode, Serializable id) {
        // if (entityName.equals(ICommand.class.getName())) {
		if (entityName.endsWith("Command")) {
        	Command cmd = new Command();
        	cmd.setOID(((Integer)id).intValue());
            return cmd;
        } else if (entityName.equals(ICommandProfile.class.getName())) {
        	ICommandProfile profile = new CommandProfile();
        	profile.setOID(((Integer)id).intValue());
        	return profile;
        }
        return super.instantiate(entityName, entityMode, id);
    }
}
