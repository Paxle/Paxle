package org.paxle.filterlanguageidentification.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

public class TrigramSet {

	/**
	 * Contains the trigrams
	 * String is the trigram
	 * Integer is its rank
	 */
	private HashMap<String, Integer> trigrams = null;

	/**
	 * Defines how many ranks are stored. The actual number of trigrams stored has not much to do with this setting!
	 */
	private int cutofflevel = -1;

	/**
	 * The name of the language as ISO-639-2 code, e.g. "eng", "deu", ...
	 */
	private String language_name = null;

	/**
	 * Initializes the trigrams with the given file.
	 * @param textfile
	 * @param cutoff The number of different ranks stored
	 * @throws IOException
	 */
	public void init(File textfile, int cutoff) throws IOException {
		BufferedReader br = null;
		br = new BufferedReader(new InputStreamReader(new FileInputStream(textfile), "UTF-8"));

		StringBuffer text = new StringBuffer(2048);
		String line;

		while ((line = br.readLine()) != null) {
			text.append(line + ' ');
		}
		init(text.toString(), cutoff);
	}

	/**
	 * Initializes the trigrams with the given test.
	 * @param text
	 * @param cutoff The number of different ranks stored
	 */
	public void init(String text, int cutoff) {

		this.trigrams = new HashMap<String, Integer>();
		this.cutofflevel = cutoff;

		//one or more blanks
		text = text.replaceAll("\\s+", "_");
		//newline
		text = text.replaceAll("\n", "_");
		//numbers
		text = text.replaceAll("\\d", "_");
		//compact underscores
		text = text.replaceAll("_+", "_");

		int i = 0;
		//zerlege den text in trigramme und speichere in der map
		//die <String trigram, int count> zuordnung
		while (i+3 <= text.length()) {
			String trigram = text.substring(i, i+3);
			i++;
			if (trigrams.get(trigram) == null) {
				trigrams.put(trigram,1);
			} else {
				int counter = trigrams.get(trigram);
				trigrams.put(trigram, ++counter);
			}
		}

		//jetzt wird der absolute count in ein rangschema ge�ndert

		//sammeln der unterschiedlichen counts
		Iterator<String> i2 = trigrams.keySet().iterator();
		TreeSet<Integer> counts = new TreeSet<Integer>();
		while (i2.hasNext()) {
			counts.add(trigrams.get(i2.next()));
		}

		//der treeset ist sortiert, also kann man jetzt nacheinander einfach die r�nge in die neue map eintragen
		int rang = counts.size();
		Iterator<Integer> c1 = counts.iterator();
		HashMap<String, Integer> newmap = new HashMap<String, Integer>();
		while (c1.hasNext()) {
			Iterator<String> c2 = trigrams.keySet().iterator();
			int abs_count = c1.next();
			while (c2.hasNext()) {
				String trigram = c2.next();
				int count = trigrams.get(trigram);
				if (count == abs_count) {
					newmap.put(trigram, rang);
				}
			}
			rang--;
		}
		trigrams = newmap;

		if ((this.cutofflevel > -1) && (this.getNumberOfRanks() > this.cutofflevel)) {
			Iterator<String> c4 = trigrams.keySet().iterator();
			while (c4.hasNext()) {
				String trigram = c4.next();
				if (trigrams.get(trigram) > this.cutofflevel) {
					c4.remove();
				}
			}
		}
	}

	/**
	 * Stores the current trigram set in a loadable format 
	 * @param out
	 * @throws IOException
	 */
	public void store(File out) throws IOException {

		int rrank = 1;
		BufferedWriter bw = new BufferedWriter(new FileWriter(out));

		while (rrank <= this.cutofflevel) {
			Iterator<String> i = trigrams.keySet().iterator();
			while (i.hasNext()) {
				String trigram = i.next();
				int crank = trigrams.get(trigram);
				if (crank == rrank) {
					bw.write(trigram + " " + crank + "\n");
				}
			}
			rrank++;
		}
		bw.close();
	}

	/**
	 * For debugging only
	 */
	public void printContent() {
		Iterator<String> i = trigrams.keySet().iterator();
		while (i.hasNext()) {
			String key = i.next();
			System.out.println(key + " " + trigrams.get(key));
		}
	}

	/**
	 * Loads a previously stored trigram set
	 * @param in
	 * @throws IOException
	 */
	public void load(File in) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(in));
		StringBuffer sb = new StringBuffer(2048);
		String line;
		while ((line = br.readLine()) != null) {
			sb.append(line + ' ');
		}
		br.close();
		load(sb.toString());
	}

	/**
	 * Creates a trigram set from a string formed like "xxx rank xxx rank xxx rank [...]"
	 * @param in
	 */
	public void load(String in) {
		StringTokenizer tokenizer = new StringTokenizer(in);
		while (tokenizer.hasMoreTokens()) {
			trigrams.put(tokenizer.nextToken(), Integer.valueOf(tokenizer.nextToken()));
		}
		this.cutofflevel = this.getNumberOfRanks();
	}

	/**
	 * Loads a previously stored trigram set
	 * @param definition_file
	 * @throws IOException
	 */
	public void load(URL definition_file) throws IOException {
		URLConnection connection = definition_file.openConnection();
		InputStream connectionIn = connection.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(connectionIn));
		StringBuffer sb = new StringBuffer(1024);
		String line = null;  
		while ((line = reader.readLine())!=null) {
			sb.append(line + ' ');
		}
		reader.close();
		connectionIn.close();
		load(sb.toString());
	}

	/**
	 * For debugging only
	 */
	public HashMap<String, Integer> getSet() {
		return this.trigrams;
	}

	/**
	 * Returns the difference between this TrigramSet and the given String. Lower values mean higher match.
	 */
	public double getDifference(String text) {
		TrigramSet ts = new TrigramSet();
		ts.init(text, this.cutofflevel);
		return getDifference(ts);
	}

	/**
	 * Returns the difference between this TrigramSet and the given TrigramSet. Lower values mean higher match.
	 */
	public double getDifference(TrigramSet probe) {
		int sum = 0;

		Iterator<String> docit = probe.getSet().keySet().iterator();
		while (docit.hasNext()) {
			String trigram = docit.next();

			if (this.getSet().get(trigram) == null) {
				sum += this.cutofflevel;
			} else {
				int docrank = probe.getSet().get(trigram);
				int refrank = this.getSet().get(trigram);
				if (docrank > refrank) {
					sum += (docrank - refrank);
				} else if (docrank < refrank) {
					sum += (refrank - docrank);
				}
			}
		}
		return (sum / (double) probe.getNumberOfTrigrams());
	}

	/**
	 * Returns the difference between this TrigramSet and the given text file. Lower values mean higher match.
	 * The text file should be UTF-8 encoded.
	 */
	public double getDifference(File textfile) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(textfile), "UTF-8"));
		StringBuffer text = new StringBuffer();
		String line = null;
		while ((line = br.readLine()) != null) {
			text.append(line);
		}
		return getDifference(text.toString());
	}

	/**
	 * Return the number of trigrams in this object
	 * @see TrigramSet#getCutoffLevel()
	 */
	public int getNumberOfTrigrams() {
		return this.trigrams.size();
	}

	/**
	 * Get the number of ranks, under which entries are dismissed to save memory/cpu time. This may be more than the result of getNumberOfRanks().
	 * @see TrigramSet#getNumberOfTrigrams()
	 * @see TrigramSet#getNumberOfRanks()
	 */
	public int getCutoffLevel() {
		return this.cutofflevel;
	}

	/**
	 * Return the number of real different counts/ranks in this set. This may be less than the result of getCutoffLevel().
	 * @see TrigramSet#getNumberOfTrigrams()
	 * @see TrigramSet#getCutoffLevel()
	 */
	public int getNumberOfRanks() {
		Iterator<String> i2 = trigrams.keySet().iterator();
		TreeSet<Integer> counts = new TreeSet<Integer>();
		while (i2.hasNext()) {
			counts.add(trigrams.get(i2.next()));
		}
		return counts.size();
	}

	/**
	 * Sets the name for this language profile to the given String.
	 * @see TrigramSet#getLanguageName()
	 */
	public void setLanguageName(String name) {
		this.language_name = name;
	}

	/**
	 * Return the name of the language for this set or null, if not set.
	 * @see TrigramSet#setLanguageName(String name)
	 */
	public String getLanguageName() {
		return this.language_name;
	}
}