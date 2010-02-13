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

package org.paxle.util.ac;

public class ArrayNode<E> extends ANode<E> {
	
	private final ANode<E>[] gotoMap;
	private ANode<E> fail = null;
	
	@SuppressWarnings("unchecked")
	public ArrayNode() {
		gotoMap = new ANode[128];
	}
	
	@Override
	public ANode<E> funcFail() {
		return fail;
	}
	
	@Override
	public ANode<E> funcGoto(byte b) {
		if (b < 0)
			return null;
		return gotoMap[b];
	}
	
	@Override
	public byte[] getKeys() {
		int count = 0;
		for (byte i=0; i>=0; i++)
			if (gotoMap[i] != null)
				count++;
		final byte[] r = new byte[count];
		count = 0;
		for (byte i=0; i>=0; i++)
			if (gotoMap[i] != null)
				r[count++] = i;
		return r;
	}
	
	@Override
	public void setFail(ANode<E> node) {
		fail = node;
	}
	
	@Override
	public void setGoto(byte b, ANode<E> node) {
		gotoMap[b] = node;
	}
}
