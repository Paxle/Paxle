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
