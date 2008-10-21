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

package org.paxle.se.provider.rsssearch.impl;

import org.paxle.core.doc.Field;
import org.paxle.se.query.IQueryFactory;
import org.paxle.se.query.tokens.AToken;

/**
 * RssSearchQueryFactory.
 * Supports:
 * * AND
 * * OR (only first value then)
 * @author allo
 *
 */
//TODO: how much more tokens can be supported with intelligen api-use? 
//Need to split the Factory for different Sites?
public class RssSearchQueryFactor extends IQueryFactory<String> {
	
	@Override
	public String and(AToken[] token){
		String result=new String(transformToken(token[0], this));
		for(int i=1;i<token.length;i++)
			result+=" "+transformToken(token[i], this);
		return result;
	}

	@Override
	public String field(AToken arg0, Field<?> arg1) {
		return "";
	}

	@Override
	public String mod(AToken arg0, String arg1) {
		return "";
	}

	@Override
	public String not(AToken arg0) {
		return ""; //at least del.icio.us does not support NOT :(
		//see http://del.icio.us/help/faq#Is_there_a_way_to_use_not_in_tag
	}

	@Override
	/**
	 * returns the first Value only
	 */
	public String or(AToken[] arg0) {
		return transformToken(arg0[0], this);
	}

	@Override
	public String plain(String arg0) {
		return arg0;
	}

	@Override
	public String quote(String arg0) {
		return arg0;
	}
	
}

