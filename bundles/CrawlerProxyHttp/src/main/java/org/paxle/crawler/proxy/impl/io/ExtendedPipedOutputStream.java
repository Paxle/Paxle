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

package org.paxle.crawler.proxy.impl.io;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class ExtendedPipedOutputStream extends PipedOutputStream {
    private PipedInputStream mySinc;
    
    public ExtendedPipedOutputStream(PipedInputStream in) throws IOException{
      super(in);
      mySinc = in;
    }
    public void write(byte b[], int off, int len) throws IOException {
        if (mySinc instanceof ExtenedPipedInputStream) {
            for (int i = 0; i < len; i++) {
                ((ExtenedPipedInputStream) mySinc).receive(b[off + i]);
            }
        } else {
            super.write(b, off, len);
        }
    }

}