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
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NGramSet {

	//one or more blanks
	static final Pattern WHITESPACES = Pattern.compile("\\s+");
	//numbers
	static final Pattern NUMBERS = Pattern.compile("\\d");
	//newline
	static final Pattern NEWLINES = Pattern.compile("\n");
	//compact underscores
	static final Pattern UNDERSCORES = Pattern.compile("_+");
	//collection of all replace patterns. Keep UNERSCORES as last one!
	static final Pattern[] REPLACE_PATTERNS = { WHITESPACES, NUMBERS, NEWLINES, UNDERSCORES};
	
	/**
	 * The length of the NGrams in this file
	 */
	private int ngram_length = 3;

	/**
	 * Contains the NGrams
	 * String is the NGram
	 * Integer is its rank
	 */
	private HashMap<String, Integer> ngrams = null;

	/**
	 * Defines how many ranks are stored. The actual number of ngrams stored has not much to do with this setting!
	 */
	private int cutofflevel = -1;

	/**
	 * The name of the language as ISO-639-1 code, e.g. "en", "de", ...
	 */
	private String language_name = null;

	/**
	 * Initializes the ngrams with the given file.
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
		init(text, cutoff);
	}

	/**
	 * Initializes the ngrams with the given test.
	 * @param text
	 * @param cutoff The number of different ranks stored
	 */
	public void init(String text, int cutoff) {
		this.init(new StringBuffer(text), cutoff);
	}

	/**
	 * Initializes the ngrams with the given test.
	 * @param sb
	 * @param cutoff The number of different ranks stored
	 */
	public void init(StringBuffer sb, int cutoff) {

		this.ngrams = new HashMap<String, Integer>();
		this.cutofflevel = cutoff;

		for (final Pattern p : REPLACE_PATTERNS) { 
			final Matcher m = p.matcher(sb); 
			boolean found = m.find(); 
			if (found) { 
				final StringBuffer newSb = new StringBuffer(sb.length()); 
				do { 
					m.appendReplacement(newSb, "_"); 
				} while (found = m.find()); 
				m.appendTail(newSb); sb = newSb; 
			} 
		}

		/*
		 * Cuts the StringBuffer in slices of the NGram length and stores an
		 * <String NGram, int absolute_count> mapping
		 */
		int i = 0;
		while (i+this.ngram_length <= sb.length()) {
			String ngram = sb.substring(i, i+this.ngram_length);
			i++;
			if (ngrams.get(ngram) == null) {
				ngrams.put(ngram,1);
			} else {
				int counter = ngrams.get(ngram);
				ngrams.put(ngram, ++counter);
			}
		}

		//Now we have to change the absolute count for every NGram in a ranking

		//Collect the set of different absolute counts
		Iterator<String> i2 = ngrams.keySet().iterator();
		TreeSet<Integer> counts = new TreeSet<Integer>();
		while (i2.hasNext()) {
			counts.add(ngrams.get(i2.next()));
		}

		//The TreeSet containing the absolute counts is sorted, so we can insert the ranks subsequently in the new map
		int rang = counts.size();
		Iterator<Integer> c1 = counts.iterator();
		HashMap<String, Integer> newmap = new HashMap<String, Integer>();
		while (c1.hasNext()) {
			Iterator<String> c2 = ngrams.keySet().iterator();
			int abs_count = c1.next();
			while (c2.hasNext()) {
				String ngram = c2.next();
				int count = ngrams.get(ngram);
				if (count == abs_count) {
					newmap.put(ngram, rang);
				}
			}
			rang--;
		}
		ngrams = newmap;

		if ((this.cutofflevel > -1) && (this.getNumberOfRanks() > this.cutofflevel)) {
			Iterator<String> c4 = ngrams.keySet().iterator();
			while (c4.hasNext()) {
				String ngram = c4.next();
				if (ngrams.get(ngram) > this.cutofflevel) {
					c4.remove();
				}
			}
		}
	}

	/**
	 * Stores the current NGram set in a loadable format 
	 * @param out
	 * @throws IOException
	 */
	public void store(File out) throws IOException {

		int rrank = 1;
		BufferedWriter bw = new BufferedWriter(new FileWriter(out));

		while (rrank <= this.cutofflevel) {
			Iterator<String> i = ngrams.keySet().iterator();
			while (i.hasNext()) {
				String ngram = i.next();
				int crank = ngrams.get(ngram);
				if (crank == rrank) {
					bw.write(ngram + " " + crank + "\n");
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
		Iterator<String> i = ngrams.keySet().iterator();
		while (i.hasNext()) {
			String key = i.next();
			System.out.println(key + " " + ngrams.get(key));
		}
	}

	/**
	 * Loads a previously stored NGram set
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
	 * Creates a NGram set from a string formed like "xxx rank xxx rank xxx rank [...]"
	 * @param in
	 */
	public void load(String in) {
		this.ngrams = new HashMap<String, Integer>(100);
		StringTokenizer tokenizer = new StringTokenizer(in);
		while (tokenizer.hasMoreTokens()) {
			ngrams.put(tokenizer.nextToken(), Integer.valueOf(tokenizer.nextToken()));
		}
		this.cutofflevel = this.getNumberOfRanks();
	}

	/**
	 * Loads a previously stored NGram set
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
		return this.ngrams;
	}

	/**
	 * Returns the difference between this NGramSet and the given String. Lower values mean higher match.
	 */
	public double getDifference(String text) {
		NGramSet ts = new NGramSet();
		ts.init(new StringBuffer(text), this.cutofflevel);
		return getDifference(ts);
	}

	/**
	 * Returns the difference between this NGramSet and the given NGramSet. Lower values mean higher match.
	 */
	public double getDifference(NGramSet probe) {
		int sum = 0;

		Iterator<String> docit = probe.getSet().keySet().iterator();
		while (docit.hasNext()) {
			String ngram = docit.next();

			if (this.getSet().get(ngram) == null) {
				sum += this.cutofflevel;
			} else {
				int docrank = probe.getSet().get(ngram);
				int refrank = this.getSet().get(ngram);
				if (docrank > refrank) {
					sum += (docrank - refrank);
				} else if (docrank < refrank) {
					sum += (refrank - docrank);
				}
			}
		}
		return (sum / (double) probe.getNumberOfNGrams());
	}

	/**
	 * Returns the difference between this NGramSet and the given text file. Lower values mean higher match.
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
	 * Return the number of NGrams in this object
	 * @see NGramSet#getCutoffLevel()
	 */
	public int getNumberOfNGrams() {
		return this.ngrams.size();
	}

	/**
	 * Get the number of ranks, under which entries are dismissed to save memory/cpu time. This may be more than the result of getNumberOfRanks().
	 * @see NGramSet#getNumberOfNGrams()
	 * @see NGramSet#getNumberOfRanks()
	 */
	public int getCutoffLevel() {
		return this.cutofflevel;
	}

	/**
	 * Return the number of real different counts/ranks in this set. This may be less than the result of getCutoffLevel().
	 * @see NGramSet#getNumberOfNGrams()
	 * @see NGramSet#getCutoffLevel()
	 */
	public int getNumberOfRanks() {
		Iterator<String> i2 = ngrams.keySet().iterator();
		TreeSet<Integer> counts = new TreeSet<Integer>();
		while (i2.hasNext()) {
			counts.add(ngrams.get(i2.next()));
		}
		return counts.size();
	}

	/**
	 * Sets the name for this language profile to the given String.
	 * @see NGramSet#getLanguageName()
	 */
	public void setLanguageName(String name) {
		this.language_name = name;
	}

	/**
	 * Return the name of the language for this set or null, if not set.
	 * @see NGramSet#setLanguageName(String name)
	 */
	public String getLanguageName() {
		return this.language_name;
	}
}