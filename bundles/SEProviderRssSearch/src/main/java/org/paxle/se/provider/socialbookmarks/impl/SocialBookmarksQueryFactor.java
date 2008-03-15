package org.paxle.se.provider.socialbookmarks.impl;

import org.paxle.core.doc.Field;
import org.paxle.se.query.IQueryFactory;
import org.paxle.se.query.tokens.AToken;

/**
 * SocialBookmarksQueryFactory.
 * Supports:
 * * AND
 * * OR (only first value then)
 * @author allo
 *
 */
//TODO: how much more tokens can be supported with intelligen api-use? 
//Need to split the Factory for different Sites?
public class SocialBookmarksQueryFactor extends IQueryFactory<String> {
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

