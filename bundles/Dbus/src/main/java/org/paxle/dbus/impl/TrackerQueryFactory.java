package org.paxle.dbus.impl;

import org.paxle.core.doc.Field;
import org.paxle.se.query.IQueryFactory;
import org.paxle.se.query.tokens.AToken;

/**
 * The {@link TrackerQueryFactory} only supports AND operation.
 */
public class TrackerQueryFactory extends IQueryFactory<String> {
		public String and(AToken[] token){
			String result=new String(transformToken(token[0], this));
			for(int i=1;i<token.length;i++) {
				result+=" "+transformToken(token[i], this);
			}
			return result;
		}

		@Override
		public String field(AToken token, Field<?> field) {
			return "";
		}

		@Override
		public String mod(AToken token, String mod) {
			return "";
		}

		@Override
		public String not(AToken token) {
			return "";
		}

		@Override
		public String or(AToken[] token) {
			return transformToken(token[0], this);
		}

		@Override
		public String plain(String str) {
			return str;
		}

		@Override
		public String quote(String str) {
			return str;
		}
}