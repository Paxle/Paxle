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

package org.paxle.crawler.impl;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.output.ThresholdingOutputStream;
import org.paxle.crawler.ContentLengthLimitExceededException;

public class ContentLengthLimitOutputStream extends ThresholdingOutputStream {
	private final OutputStream out;
	
	public ContentLengthLimitOutputStream(int limit, OutputStream out) {
		super(limit);
		this.out = out;
	}

	@Override
	protected OutputStream getStream() throws IOException {
		return this.out;
	}

	@Override
	protected void thresholdReached() throws IOException {
		throw new ContentLengthLimitExceededException(String.format(
				"Content-length of resource is larger than the max. allowed size of '%d' bytes.",
				Integer.valueOf(super.getThreshold())
		));
	}

}
