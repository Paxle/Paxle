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

package org.paxle.util.ac;

public class MappedArrayNode<E> extends ANode<E> {
	
	private final byte mapLength;
	private final byte[] asciiMap;
	private final byte[] mapAscii;
	private final ANode<E>[] paths;
	
	@SuppressWarnings("unchecked")
	public MappedArrayNode(final byte[] asciiMap, final byte[] mapAscii) {
		if (mapAscii.length >= Byte.MAX_VALUE)
			throw new IllegalArgumentException("mapAscii.length >= 127: " + mapAscii.length);
		mapLength = (byte)(mapAscii.length + 1);
		paths = new ANode[mapLength];
		this.asciiMap = asciiMap;
		this.mapAscii = mapAscii;
	}
	
	@Override
	public byte[] getKeys() {
		int num = 0;
		for (int i=1; i<mapLength; i++)
			if (paths[i] != null)
				num++;
		final byte[] r = new byte[num];
		num = 0;
		for (byte i=1; i<mapLength; i++)
			if (paths[i] != null)
				r[num++] = mapAscii[i-1];
		return r;
	}
	
	@Override
	public ANode<E> funcFail() {
		return paths[0];
	}
	
	@Override
	public ANode<E> funcGoto(byte b) {
		if (b < 0)
			return null;
		return paths[asciiMap[b]];
	}
	
	@Override
	public void setFail(ANode<E> node) {
		paths[0] = node;
	}
	
	@Override
	public void setGoto(byte b, ANode<E> node) {
		paths[asciiMap[b]] = node;
	}
}
