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

package org.paxle.crypt.impl.md5;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.paxle.core.crypt.ACryptInputStream;
import org.paxle.core.crypt.ACryptOutputStream;
import org.paxle.core.crypt.ICrypt;

@Component(metatype=false, immediate=true)
@Service(ICrypt.class)
@Property(name=ICrypt.CRYPT_NAME_PROP, value="md5")
public class MD5Crypt implements ICrypt {

	public ACryptInputStream createInputStream(InputStream stream) {
		return new MD5WrapperInStream(stream);
	}

	public ACryptOutputStream createOutputStream(OutputStream stream) {
		return new MD5WrapperOutStream(stream);
	}

}
