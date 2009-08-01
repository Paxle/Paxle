/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2009 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package org.paxle.core.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.CharBuffer;

import javax.annotation.Nonnull;
import javax.annotation.WillNotClose;

import net.jcip.annotations.ThreadSafe;

@ThreadSafe
public interface IIOTools {
	
	/**
	 * Copies all data from the given {@link Reader} to the given {@link Appendable}.
	 * <p><i>Note: this method does not close the supplied Reader.</i></p>
	 * 
	 * @see #copy(Reader, Appendable, long) for details
	 * @param in the reader to read from
	 * @param out the appendable to append to
	 * @return the number of copied characters
	 * @throws <b>IOException</b> if an I/O-error occures
	 */
	public long copy(@Nonnull @WillNotClose Reader in, @Nonnull Appendable out) throws IOException;
	
	/**
	 * Copies an amount of data from the given {@link Reader} to the given {@link Appendable}.
	 * <p><i>Note: this method does not close the supplied Reader.</i></p>
	 * 
	 * @see #DEFAULT_BUFFER_SIZE_CHARS for the size of the buffer
	 * @see Reader#read(char[], int, int)
	 * @see Appendable#append(CharSequence)
	 * @see CharBuffer#wrap(char[], int, int)
	 * @param in the reader to read from
	 * @param out the appendable to append to
	 * @param bytes the number of characters to copy
	 * @return the number of copied characters
	 * @throws <b>IOException</b> if an I/O-error occures
	 */
	public long copy(@Nonnull @WillNotClose Reader in, @Nonnull Appendable out, long chars) throws IOException;
	
	/**
	 * Copies all data from the given {@link InputStream} to the given {@link OutputStream}
	 * using the {@link #copy(InputStream, OutputStream, long)}-method.
	 * <p><i>Note: this method does neither close the supplied InputStream nor the OutputStream.</i></p>
	 * 
	 * @see #copy(InputStream, OutputStream, long) for details
	 * @param is the stream to read from
	 * @param os the stream to write to
	 * @return the number of copied bytes
	 * @throws <b>IOException</b> if an I/O-error occures
	 */
	public long copy(@Nonnull @WillNotClose InputStream is, @Nonnull @WillNotClose OutputStream os) throws IOException;
	
	/**
	 * Copies an amount of data from the given {@link InputStream} to the given {@link OutputStream}.
	 * <p><i>Note: this method does neither close the supplied InputStream nor the OutputStream.</i></p>
	 * 
	 * @see #DEFAULT_BUFFER_SIZE_BYTES for the size of the buffer
	 * @see InputStream#read(byte[], int, int)
	 * @see OutputStream#write(byte[], int, int)
	 * @param is the stream to read from
	 * @param os the stream to write to
	 * @param bytes the number of bytes to copy
	 * @return the number of copied bytes
	 * @throws <b>IOException</b> if an I/O-error occures
	 */
	public long copy(@Nonnull @WillNotClose InputStream is, @Nonnull @WillNotClose OutputStream os, long bytes) throws IOException;
	
	public long copy(@Nonnull @WillNotClose  InputStream is, @Nonnull @WillNotClose OutputStream os, final long bytes, final int limitKBps) throws IOException;
}
