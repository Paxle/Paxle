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

package org.paxle.crypt.impl;

import java.io.InputStream;
import java.io.OutputStream;

import org.paxle.core.crypt.ACryptInputStream;
import org.paxle.core.crypt.ACryptOutputStream;
import org.paxle.core.crypt.ICrypt;
import org.paxle.crypt.impl.md5.MD5WrapperInStream;
import org.paxle.crypt.impl.md5.MD5WrapperOutStream;

public enum Impls implements ICrypt {
	MD5("md5") {
		public ACryptInputStream createInputStream(InputStream stream) {
			return new MD5WrapperInStream(stream);
		}
		
		public ACryptOutputStream createOutputStream(OutputStream stream) {
			return new MD5WrapperOutStream(stream);
		}
	}
	
	;
	
	public final String name;
	
	private Impls(String name) {
		this.name = name;
	}
}
