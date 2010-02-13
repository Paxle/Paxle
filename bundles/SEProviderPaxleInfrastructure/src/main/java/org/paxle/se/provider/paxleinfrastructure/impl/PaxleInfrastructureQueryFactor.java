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

package org.paxle.se.provider.paxleinfrastructure.impl;
import org.paxle.core.doc.Field;
import org.paxle.se.query.IQueryFactory;
import org.paxle.se.query.tokens.AToken;

/**
 * The {@link PaxleInfrastructureQueryFactor} only supports AND operation
 * It is used to get a simple "paxle forum" or "paxle bug #id" query, without any further operations.
 * If you use or, it processes only the first choice. and is simply concatenated.
 * @author allo
 */
public class PaxleInfrastructureQueryFactor extends IQueryFactory<String> {
		@Override
		public String and(AToken[] token){
			String result=transformToken(token[0], this);
			for(int i=1;i<token.length;i++)
				result+=" "+transformToken(token[i], this);
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