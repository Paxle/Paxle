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

package org.paxle.se.index.lucene.impl;

import org.apache.lucene.document.NumberTools;

public class PaxleNumberTools extends NumberTools {
	
	public static byte[] toBytes(long num) {
		final int size = 8 - Long.numberOfLeadingZeros(num) / 8;
		final byte[] buf = new byte[size];
		int pos = 0;
		while (pos < size) {
		    buf[pos++] = (byte)(num & 0xFF);
		    num >>= 8;
		}
		return buf;
	}
	
	public static byte[] toBytes(double num) {
		return toBytes(Double.doubleToLongBits(num));
	}
	
	public static long toLong(byte[] data) {
		long x = 0;
		for (int i=0; i<data.length; i++)
			x |= (data[i] & 0xFFl) << (i * 8);
		return x;
	}
	
	public static double toDouble(byte[] data) {
		return Double.longBitsToDouble(toLong(data));
	}
	
	public static String toBinaryString(byte[] data, boolean sepBytes) {
		final StringBuilder sb = new StringBuilder();
		for (int i=data.length-1; i>=0; i--) {
			for (int j=7; j>=0; j--)
				sb.append(((data[i] & (1 << j)) == 0) ? '0' : '1');
			if (sepBytes)
				sb.append(' ');
		}
		if (sepBytes && sb.length() > 0)
			sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}
}
