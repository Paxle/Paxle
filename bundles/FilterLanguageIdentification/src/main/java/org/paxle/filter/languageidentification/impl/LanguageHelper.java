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

package org.paxle.filter.languageidentification.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

public class LanguageHelper {

	public static void main(String[] args) {
		
		String korpus = "/home/roland/Desktop/langident/korpus-es.txt";
		String def = "/home/roland/Desktop/langident/es.txt";
		makeTrigramSet(new File(korpus), 100, new File(def));
		
	}
	
	public static void makeTrigramSet(File reference, int cutoff, File out) {
		TrigramSet ts = null;
		try {
			ts = new TrigramSet();
			ts.init(reference, cutoff);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			ts.store(out);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	@SuppressWarnings("unused")
	private void storeForGnuplot(TrigramSet tg, File outfile) throws IOException {
		int[] tsa = new int[tg.getSet().size()];
		FileWriter fw = null;
		fw = new FileWriter(outfile);
		Iterator<String> i = tg.getSet().keySet().iterator();
		int x = 0;
		while (i.hasNext()) {
			String trigram = i.next();
			tsa[x] = tg.getSet().get(trigram);
			x++;
		}
		Arrays.sort(tsa);

		x = 0;
		while (x < tg.getSet().size()) {
			fw.write(new Integer(tsa[x]).toString());
			fw.write("\n");
			x++;
		}
		fw.close();
	}
	
}
