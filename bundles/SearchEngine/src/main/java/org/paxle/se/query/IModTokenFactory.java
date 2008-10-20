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

package org.paxle.se.query;

import org.paxle.se.query.tokens.AToken;
import org.paxle.se.query.tokens.PlainToken;

public interface IModTokenFactory {
	
	public static final String PROP_SUPPORTED_TOKENS = "paxle.query.modtokens";
	
	public abstract boolean isModSupported(String mod);
	public abstract AToken toToken(PlainToken token, String modifier);
}
