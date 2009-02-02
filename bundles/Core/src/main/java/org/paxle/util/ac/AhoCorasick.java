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
package org.paxle.util.ac;

import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

import org.paxle.util.buffer.ArrayByteBuffer;

public class AhoCorasick<E> {

    //This array contains codes (see http://mindprod.com/jgloss/unicode.html for details) and
    //patterns that will be replaced. To add new codes or patterns, just put them at the end
    //of the list. Codes or patterns in this list can not be escaped with [= or <pre>
    public static final String[] htmlentities={
    	// named entities
    	" "     ,"&nbsp;",     //space
        "\u00A1","&iexcl;",    //inverted (spanish) exclamation mark
        "\u00A2","&cent;",     //cent
        "\u00A3","&pound;",    //pound
        "\u00A4","&curren;",   //currency
        "\u00A5","&yen;",      //yen
        "\u00A6","&brvbar;",   //broken vertical bar
        "\u00A7","&sect;",     //section sign
        "\u00A8","&uml;",      //diaeresis (umlaut)
        "\u00A9","&copy;",     //copyright sign
        "\u00AA","&ordf;",     //feminine ordinal indicator
        "\u00AB","&laquo;",    //left-pointing double angle quotation mark
        "\u00AC","&not;",      //not sign
        "\u00AD","&shy;",      //soft hyphen
        "\u00AE","&reg;",      //registered sign
        "\u00AF","&macr;",     //macron
        "\u00B0","&deg;",      //degree sign
        "\u00B1","&plusmn;",   //plus-minus sign
        "\u00B2","&sup2;",     //superscript two
        "\u00B3","&sup3;",     //superscript three
        "\u00B4","&acute;",    //acute accent
        "\u00B5","&micro;",    //micro sign
        "\u00B6","&para;",     //paragraph sign
        "\u00B7","&middot;",   //middle dot
        "\u00B8","&cedil;",    //cedilla
        "\u00B9","&sup1;",     //superscript one
        "\u00BA","&ordm;",     //masculine ordinal indicator
        "\u00BB","&raquo;",    //right-pointing double angle quotation mark
        "\u00BC","&frac14;",   //fraction 1/4
        "\u00BD","&frac12;",   //fraction 1/2
        "\u00BE","&frac34;",   //fraction 3/4
        "\u00BF","&iquest;",   //inverted (spanish) questionmark
        "\u00C0","&Agrave;",
        "\u00C1","&Aacute;",
        "\u00C2","&Acirc;",
        "\u00C3","&Atilde;",
        "\u00C4","&Auml;",
        "\u00C5","&Aring;",
        "\u00C6","&AElig;",
        "\u00C7","&Ccedil;",
        "\u00C8","&Egrave;",
        "\u00C9","&Eacute;",
        "\u00CA","&Ecirc;",
        "\u00CB","&Euml;",
        "\u00CC","&Igrave;",
        "\u00CD","&Iacute;",
        "\u00CE","&Icirc;",
        "\u00CF","&Iuml;",
        "\u00D0","&ETH;",
        "\u00D1","&Ntilde;",
        "\u00D2","&Ograve;",
        "\u00D3","&Oacute;",
        "\u00D4","&Ocirc;",
        "\u00D5","&Otilde;",
        "\u00D6","&Ouml;",
        "\u00D7","&times;",
        "\u00D8","&Oslash;",
        "\u00D9","&Ugrave;",
        "\u00DA","&Uacute;",
        "\u00DB","&Ucirc;",
        "\u00DC","&Uuml;",
        "\u00DD","&Yacute;",
        "\u00DE","&THORN;",
        "\u00DF","&szlig;",
        "\u00E0","&agrave;",
        "\u00E1","&aacute;",
        "\u00E2","&acirc;",
        "\u00E3","&atilde;",
        "\u00E4","&auml;",
        "\u00E5","&aring;",
        "\u00E6","&aelig;",
        "\u00E7","&ccedil;",
        "\u00E8","&egrave;",
        "\u00E9","&eacute;",
        "\u00EA","&ecirc;",
        "\u00EB","&euml;",
        "\u00EC","&igrave;",
        "\u00ED","&iacute;",
        "\u00EE","&icirc;",
        "\u00EF","&iuml;",
        "\u00F0","&eth;",
        "\u00F1","&ntilde;",
        "\u00F2","&ograve;",
        "\u00F3","&oacute;",
        "\u00F4","&ocirc;",
        "\u00F5","&otilde;",
        "\u00F6","&ouml;",
        "\u00F7","&divide;",
        "\u00F8","&oslash;",
        "\u00F9","&ugrave;",
        "\u00FA","&uacute;",
        "\u00FB","&ucirc;",
        "\u00FC","&uuml;",
        "\u00FD","&yacute;",
        "\u00FE","&thorn;",
        "\u00FF","&yuml;"
    };
	
	public static void main(String[] args) throws Exception {
		switch (0) {
			case 0: {
				final Runtime runtime = Runtime.getRuntime();
				final long mem, mem1, mem2, mem3;
				
				final NodeFactoryFactory nff = new NodeFactoryFactory();
				for (int i=1; i<htmlentities.length; i+=2)
					nff.addPattern(htmlentities[i].getBytes());
				final INodeFactory<String> nf = nff.toNodeFactory(3, String.class);
				
				runtime.gc(); runtime.gc(); runtime.gc(); runtime.gc(); runtime.gc(); runtime.gc();
				runtime.gc(); runtime.gc(); runtime.gc(); runtime.gc(); runtime.gc(); runtime.gc();
				runtime.gc(); runtime.gc(); runtime.gc(); runtime.gc(); runtime.gc(); runtime.gc();
				runtime.gc(); runtime.gc(); runtime.gc(); runtime.gc(); runtime.gc(); runtime.gc();
				mem = runtime.freeMemory();
				
				final AhoCorasick<String> ac = new AhoCorasick<String>(nf);
				
				runtime.gc(); runtime.gc(); runtime.gc(); runtime.gc(); runtime.gc(); runtime.gc();
				runtime.gc(); runtime.gc(); runtime.gc(); runtime.gc(); runtime.gc(); runtime.gc();
				runtime.gc(); runtime.gc(); runtime.gc(); runtime.gc(); runtime.gc(); runtime.gc();
				runtime.gc(); runtime.gc(); runtime.gc(); runtime.gc(); runtime.gc(); runtime.gc();
				mem1 = runtime.freeMemory();
				System.out.println("ac instantiated: " + (mem - mem1));
				
				for (int i=0; i<htmlentities.length; i+=2)
					ac.addPattern(htmlentities[i+1].getBytes(), htmlentities[i]);
				
				runtime.gc(); runtime.gc(); runtime.gc(); runtime.gc(); runtime.gc(); runtime.gc();
				runtime.gc(); runtime.gc(); runtime.gc(); runtime.gc(); runtime.gc(); runtime.gc();
				runtime.gc(); runtime.gc(); runtime.gc(); runtime.gc(); runtime.gc(); runtime.gc();
				runtime.gc(); runtime.gc(); runtime.gc(); runtime.gc(); runtime.gc(); runtime.gc();
				mem2 = runtime.freeMemory();
				
				System.out.println("creating fail transisitions");
				ac.createFailTransitions();
				runtime.gc(); runtime.gc(); runtime.gc(); runtime.gc(); runtime.gc(); runtime.gc();
				runtime.gc(); runtime.gc(); runtime.gc(); runtime.gc(); runtime.gc(); runtime.gc();
				runtime.gc(); runtime.gc(); runtime.gc(); runtime.gc(); runtime.gc(); runtime.gc();
				runtime.gc(); runtime.gc(); runtime.gc(); runtime.gc(); runtime.gc(); runtime.gc();
				mem3 = runtime.freeMemory();
				
				System.out.println(
						(htmlentities.length >> 1) + " entities added: " + (mem1 - mem2) +
						", fail-transitions: " + (mem2 - mem3) +
						", total: " + (mem - mem3));
				
				System.out.println();
				System.out.println(ac.root.treeToString());
			} break;
			
			case 1: {
				final NodeFactoryFactory nff = new NodeFactoryFactory();
				for (int i=1; i<htmlentities.length; i+=2)
					nff.addPattern(htmlentities[i].getBytes());
				final INodeFactory<String> nf = nff.toNodeFactory(3, String.class);
				// final INodeFactory<String> nf = new NodeFactoryFactory.LinkedNodeFactory<String>();
				
				final AhoCorasick<String> ac = new AhoCorasick<String>(nf);
				for (int i=0; i<htmlentities.length; i+=2)
					ac.addPattern(htmlentities[i+1].getBytes(), htmlentities[i]);
				
				final File file = new File("/home/kane/bla");
				final ArrayByteBuffer abb = new ArrayByteBuffer((int)file.length());
				final FileInputStream fis = new FileInputStream(file);
				final byte[] buf = new byte[1024];
				int read;
				while ((read = fis.read(buf, 0, 1024)) > 0)
					abb.write(buf, 0, read);
				fis.close();
				final byte[] data = abb.toByteArray();
				abb.clear();
				int c = 0;
				ac.createFailTransitions();
				final long start = System.currentTimeMillis();
				
				int last = 0;
				for (final SearchResult<String> r : ac.search(data)) {
					abb.append(data, last, r.getMatchBegin() - last).append(r.getValue().getBytes());
					/*
					System.out.println(
							"replacing '" + new String(data, r.getMatchBegin(), r.getMatchEnd() - r.getMatchBegin()) +
							"' with '" + r.getValue() + "' @" + r.getMatchBegin());
							*/
					last = r.getMatchEnd() + 1;
					c++;
				}
				abb.append(data, last, data.length - last);
				
				final long end = System.currentTimeMillis();
				System.out.println("time: " + (end - start) + " ms + (" + c + " entities replaced)");
				System.out.println();
				// System.out.println(new String(abb.toByteArray()));
			} break;
			
			case 2: {
				final File file = new File("/home/kane/bla");
				ArrayByteBuffer abb = new ArrayByteBuffer((int)file.length());
				final FileInputStream fis = new FileInputStream(file);
				final byte[] buf = new byte[1024];
				int read;
				while ((read = fis.read(buf, 0, 1024)) > 0)
					abb.write(buf, 0, read);
				fis.close();
				String text = new String(abb.toByteArray());
				abb = null;
				final String[] entities = htmlentities;
				
				int c = 0;
				final long start = System.currentTimeMillis();
				
		        final StringBuffer sb = new StringBuffer(text);
		        for (int i=entities.length-1; i>0; i-=2) {
		        	// System.out.println(entities[i]);
		            int p = 0;
		            while ((p = sb.indexOf(entities[i])) >= 0) {
		                // text = text.substring(0, p) + entities[i - 1] + text.substring(p + entities[i].length());
		            	sb.replace(p, p + entities[i].length(), entities[i - 1]);
		                p += entities[i - 1].length();
		                c++;
		            }
		        }
				
				final long end = System.currentTimeMillis();
				System.out.println("time: " + (end - start) + " ms + (" + c + " entities replaced)");
				System.out.println();
			} break;
		}
	}
	
	// package private for Map-implementation and Searcher
	class SearchNodeResult {
		
		final ANode<E> node;
		final int idx;
		
		private SearchNodeResult(final ANode<E> match, final int idx) {
			this.node = match;
			this.idx = idx;
		}
		
		SearchResult<E> result(final int index) {
			if (node.values == null || index >= node.values.size())
				return null;
			final ANode.ValueEntry<E> entry = node.values.get(index);
			return new SearchResult<E>(idx - entry.length + 1, idx, entry.value);
		}
		
		int size() {
			return (node.values == null) ? 0 : node.values.size();
		}
	}
	
	// package private for Map-implementation and Searcher
	/** the root node of this trie, it has the depth <code>-1</code> */
	final ANode<E> root;
	
	/** factory to use to create new nodes when {@link #addPattern(byte[], Object)} is called */
	private final INodeFactory<E> factory;
	
	/** number of keys this trie contains */
	private int size = 0;
	
	/** determines whether the fail-transitions between the nodes have to be rebuilt or not @see #createFailTransitions() */
	private boolean dirty = false;
	
	public AhoCorasick() {
		this(new NodeFactoryFactory.LinkedNodeFactory<E>());
	}
	
	public AhoCorasick(final int threshold) {
		this(new NodeFactoryFactory.DepthTSNodeFactory<E>(threshold,
				new NodeFactoryFactory.ArrayPlainNodeFactory<E>(),
				new NodeFactoryFactory.LinkedNodeFactory<E>()));
	}
	
	public AhoCorasick(final INodeFactory<E> factory) {
		this.factory = factory;
		root = factory.createNode(-1);	// root is at depth -1 because normal entries have to begin at 0
		root.setFail(root);
	}
	
	/**
	 * @return the number of elements contained
	 */
	public int size() {
		return size;
	}
	
	/**
	 * Resets this trie to it's original state by removing all added patterns
	 */
	public void clear() {
		for (final byte b : root.getKeys())
			root.setGoto(b, null);
		size = 0;
		dirty = false;
	}
	
	public AhoCorasick<E> compact() {
		final AhoCorasick<E> ac = new AhoCorasick<E>(new INodeFactory<E>() {
			public ANode<E> createNode(int depth) {
				if (depth == -1) {
					final byte[] k = root.getKeys();
					
				} else {
				}
				return null;
			}
		});
		return null;
	}
	
	/**
	 * Fail-transitions between nodes are needed to be able to recognize patterns. Consider an {@link
	 * #AhoCorasick}-trie containing the following entries:
	 * <ul>
	 *   <li><code>abab</code></li>
	 *   <li><code>bad</code></li>
	 * </ul>
	 * The string to be matched on shall be "<code>abad</code>".
	 * <p>
	 * Here a fail-transition between the last character of <code>abab</code> to the last character of
	 * <code>bad</code> is needed, otherwise "<code>bad</code>" in the given string wouldn't be recognized
	 * properly.
	 * <p>
	 * Fail-transitions are recomputed for the whole trie if {@link #dirty} is set to <code>true</code>
	 * and {@link #matchingNode(byte[], ANode, int, int)} is invoked. Recomputing the fail-transitions
	 * directly during removal or while adding an entry is quite complicated and would involve processing
	 * the same nodes multiple times since we don't have any references to parents in nodes, therefore -
	 * and because alterations on the trie are mostly performed in chunks - it is more efficient to
	 * recompute the fail-transitions only when needed.
	 * <p>
	 * This method cleans up all fail-transitions added previously and also clears the added fail-values of
	 * the nodes via {@link ANode#clearFailValues()}, no prior cleanup-step is required.
	 */
	public void createFailTransitions() {
		// breadth-first => nodes nearer to root are already processed
		final Queue<ANode<E>> queue = new LinkedList<ANode<E>>();
		for (final ANode<E> node : root) {
			node.setFail(root);
			node.clearFailValues();
			queue.add(node);
		}
		while (!queue.isEmpty()) {
			final ANode<E> node = queue.remove();
			for (final byte b : node.getKeys()) {
				final ANode<E> u = node.funcGoto(b);
				u.clearFailValues();
				queue.add(u);
				ANode<E> v;
				
				v = node.funcFail();
				while (v.funcGoto(b) == null) {
					v = v.funcFail();
					if (v == root)
						break;
				}
				v = v.funcGoto(b);
				if (v == null) {
					v = root;
				} else {
					u.addValues(v.getValues());
				}
				u.setFail(v);
			}
		}
		dirty = false;
	}
	
	public boolean addPattern(final byte[] p, final E value) {
		return addPattern(p, 0, p.length, value);
	}
	
	/**
	 * Adds a pattern to this trie. If it is not being removed in the mean-time, any call to
	 * either {@link #search(byte[], int, int)} or {@link #match(byte[], int, int)} will include
	 * this pattern in the matching-process.
	 * @param p the pattern to add
	 * @param value the value to attach to the node representing the final step of this pattern.
	 *        It will be returned when this pattern is recognized in some string.
	 * @return whether the pattern denoted by <code>p</code> has not been in the trie before.
	 */
	public boolean addPattern(final byte[] p, int off, int len, final E value) {
		if (len == 0 || off > len || len > p.length)
			throw new IllegalArgumentException();
		
		ANode<E> last = root, node;
		for (int i=0; i<len; i++) {
			final byte b = p[i];
			node = last.funcGoto(b);
			if (node == null) {
				node = factory.createNode(i);
				last.setGoto(b, node);
			} else {
				node.incUsageCount();
			}
			last = node;
		}
		final boolean r = (last.getFirstValue() == null);
		last.setFirstValue(value, p.length);
		if (r) {
			size++;
			dirty = true;
		}
		return r;
	}
	
	public E removePattern(final byte[] p) {
		return removePattern(p, 0, p.length);
	}
	
	/**
	 * Removes a pattern from this trie.
	 * @param p the pattern to remove
	 * @return the value attached to the pattern <code>p</code>, if it is contained in this trie,
	 *         <code>null</code> otherwise.
	 */
	public E removePattern(final byte[] p, final int off, final int len) {
		if (size == 0)
			throw new NoSuchElementException();
		final E r = match(p, off, len);
		if (r == null)
			throw new NoSuchElementException();
		
		ANode<E> q = root, nq;
		for (int i=0; i<len; i++) {
			final byte b = p[i];
			nq = q.funcGoto(b);
			if (!nq.decUsageCount()) {
				q.setGoto(b, null);
				break;
			}
			q = nq;
		}
		size--;
		dirty = true;
		return r;
	}
	
	// package private
	SearchNodeResult matchingNode(final byte[] p, final ANode<E> node, final int from, final int to) {
		if (dirty)
			createFailTransitions();
		ANode<E> q = node, nq;
		outer: for (int i=from; i<to; i++) {
			final byte b = p[i];
			
			// System.out.print("char '" + (char)b + "': ");
			while ((nq = q.funcGoto(b)) == null) {
				if (q == root)
					continue outer;
				
				nq = q.funcFail();
				// System.out.println("fail    from " + q + " to " + nq);
				q = nq;
			}
			// System.out.println("succeed from " + q + " to " + nq);
			
			q = nq;
			final LinkedList<ANode.ValueEntry<E>> vals = q.getValues();
			if (vals != null)
				return new SearchNodeResult(q, i);
		}
		return null;
	}
	
	public Iterable<SearchResult<E>> search(final byte[] p) {
		return search(p, 0, p.length);
	}
	
	/**
	 * Matches all patterns saved in this trie in the given byte-array and gradually returns
	 * the next match as {@link SearchResult}. Modifying the trie during a search has not been tested
	 * yet, though it should be safe in theory - concurrent access however is not supported.
	 * @param p the byte-array to search in
	 * @param off offset from which on to start searching
	 * @param len number of bytes to search in the array
	 * @return an {@link Iterable} providing {@link Iterator}s over the sequentially discovered
	 *         {@link SearchResult}s
	 */
	public Iterable<SearchResult<E>> search(final byte[] p, final int off, final int len) {
		return new Searcher<E>(p, off, off + len, this);
	}
	
	public E match(final byte[] p) {
		return match(p, 0, p.length);
	}
	
	/**
	 * Searches for all patterns saved in this trie in the given byte-array and returns the
	 * first match.
	 * @param p the byte-array to search in
	 * @param off offset from which on to start searching
	 * @param len number of bytes to search array
	 * @return the first value attached to the pattern which matched the given array first
	 */
	public E match(final byte[] p, final int off, final int len) {
		final SearchNodeResult result = matchingNode(p, root, off, off + len);
		if (result == null)
			return null;
		final SearchResult<E> sr = result.result(0);
		return (sr == null) ? null : sr.getValue();
	}
}
