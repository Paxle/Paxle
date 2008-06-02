
package org.paxle.util.ac;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

public class NodeFactoryFactory {
	
	private final byte[] asciiMap = new byte[128];
	private final byte[] mapAscii = new byte[127];
	private byte c = 0;
	
	public NodeFactoryFactory() {
	}
	
	public void addPattern(final byte[] pattern) throws ParseException {
		for (int i=0; i<pattern.length; i++) {
			final byte b = pattern[i];
			if (b < 0)
				throw new ParseException("pattern '" + new String(pattern) + "' contains illegal value '" + b + "'", i);
			if (asciiMap[b] == 0 && c < Byte.MAX_VALUE) {
				mapAscii[c] = b;
				asciiMap[b] = ++c;
			}
		}
	}
	
	public void clear() {
		Arrays.fill(asciiMap, (byte)0);
		Arrays.fill(mapAscii, (byte)0);
		c = 0;
	}
	
	/**
	 * The threshold determines from which depth on to switch from ArrayNodes to LinkedNodes.
	 * <p>
	 * For given set of accepted characters consisting of i.e. 39 elements (<code>a-z, 0-9,
	 * '.', '-'</code> plus one internally used &quot;<code>0</code>&quot;-element needed for
	 * the mapping) applies:<p>
	 * {@link MappedArrayNodes} need 40 * 8 = 320 byte each, independent of the number of
	 * child-nodes added.
	 * {@link LinkedNodes} need 17 bytes per child-node added.<p>
	 * This means: If the anticipated number of children of a node at a given depth is below
	 * 320 / 17 = 18.82, linked nodes use less space than array nodes and, of course, the
	 * other way round. This only holds for the above set-size.<p>
	 * Additionally the very fast lookup-speed with complexity O(1) of array-nodes has to be
	 * taken into account, whereas linked nodes need O(n) time (with n being the number of it's
	 * children). For all strings to match can be said that nodes with a smaller depth are
	 * visited more often, so it makes sense to not choose the threshold-value too small.<p>
	 * <i>
	 *   The actual amount of memory needed by a specific {@link ANode node}-implementation
	 *   is dependant on the JVM used, can not be guaranteed and should be calculated during
	 *   run-time if needed. The example above is just a hint.
	 * </i>
	 * 
	 * @param linkedDepthThreshold the default threshold value from which on to take
	 *        {@link LinkedNode}s instead of {@link MappedArrayNode}s. Values smaller than zero
	 *        cause all nodes (including the root node) to be {@link LinkedNode}s
	 */
	public <E> INodeFactory<E> toNodeFactory(final int linkedDepthThreshold, final Class<E> clazz) {
		final INodeFactory<E> nf;
		if (c == Byte.MAX_VALUE || c < 0) {
			nf = new ArrayPlainNodeFactory<E>();
		} else {
			final byte[] asciiMap = new byte[this.asciiMap.length];
			System.arraycopy(this.asciiMap, 0, asciiMap, 0, this.asciiMap.length);
			final byte[] mapAscii = new byte[c];
			System.arraycopy(this.mapAscii, 0, mapAscii, 0, c);
			return new ArrayMappedNodeFactory<E>(asciiMap, mapAscii);
		}
		return new DepthTSNodeFactory<E>(linkedDepthThreshold, nf, new TreeNodeFactory<E>());
	}
	
	public static <E> INodeFactory<E> toNodeFactory(
			final int linkedDepthThreshold,
			final Class<E> clazz,
			final boolean denyUndefined,
			final Range... ranges) {
		final Range r = Range.combine(denyUndefined, ranges);
		final byte[] asciiMap = new byte[128];
		byte b = 0;
		for (byte idx=0; idx>=0; idx++)
			asciiMap[idx] = (r.isAllowed(idx)) ? ++b : 0;
		return new DepthTSNodeFactory<E>(linkedDepthThreshold,
				new ArrayMappedNodeFactory<E>(asciiMap, b), new TreeNodeFactory<E>());
	}
	
	public static final class ArrayMappedNodeFactory<E> implements INodeFactory<E> {
		
		private final byte[] asciiMap;
		private final byte[] mapAscii;
		
		public ArrayMappedNodeFactory(final byte[] asciiMap, final byte[] mapAscii) {
			this.asciiMap = asciiMap;
			this.mapAscii = mapAscii;
		}
		
		public ArrayMappedNodeFactory(final byte[] asciiMap, final byte mapLength) {
			this.asciiMap = asciiMap;
			mapAscii = new byte[mapLength];
			int c = 0;
			for (byte i=0; i>=0; i++)
				if (asciiMap[i] > 0) {
					mapAscii[asciiMap[i]-1] = i;
					c++;
				}
			if (c != mapLength)
				throw new RuntimeException("c: " + c + ", map-length: " + mapLength);
		}
		
		public ANode<E> createNode(int depth) {
			return new MappedArrayNode<E>(asciiMap, mapAscii);
		}
	}
	
	public static final class LinkedNodeFactory<E> implements INodeFactory<E> {
		public ANode<E> createNode(int depth) {
			return new LinkedNode<E>();
		}
	}
	
	public static final class ArrayPlainNodeFactory<E> implements INodeFactory<E> {
		public ANode<E> createNode(int depth) {
			return new ArrayNode<E>();
		}
	}
	
	public static final class TreeNodeFactory<E> implements INodeFactory<E> {
		public ANode<E> createNode(int depth) {
			return new TreeNode<E>();
		}
	}
	
	public static final class DepthTSNodeFactory<E> implements INodeFactory<E> {
		
		private final INodeFactory<E> lower, higher;
		private final int threshold;
		
		public DepthTSNodeFactory(final int threshold, final INodeFactory<E> lower, final INodeFactory<E> higher) {
			this.threshold = threshold;
			this.lower = lower;
			this.higher = higher;
		}
		
		public ANode<E> createNode(int depth) {
			return (depth < threshold) ? lower.createNode(depth) : higher.createNode(depth);
		}
	}
	
	public static class Range implements Comparable<Range> {
		
		public final byte from, to;
		public final boolean allowed;
		
		private Range() {
			from = (byte)-1;
			to = (byte)-1;
			allowed = false;
		}
		
		protected Range(final byte from, final byte to, final boolean allowed) {
			this.from = from;
			this.to = to;
			this.allowed = allowed;
		}
		
		@Override
		public int hashCode() {
			final int hash = ((from & 0xFF) << 8) | (to & 0xFF);
			return (allowed) ? -hash : hash;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Range))
				return super.equals(obj);
			return compareTo((Range)obj) == 0;
		}
		
		public int compareTo(Range o) {
			if (allowed ^ o.allowed)
				return (allowed) ? 1 : -1;
			final int c = from - o.from;
			return (c != 0) ? c : to - o.to;
		}
		
		public boolean isAllowed(final byte b) {
			return allowed ^ (b < from || b > to);
		}
		
		public static Range singleAllowed(final byte b) {
			return new Range(b, b, true);
		}
		
		public static Range rangeAllowed(final byte from, final byte to) {
			return new Range(from, to, true);
		}
		
		public static Range singleDenied(final byte b) {
			return new Range(b, b, false);
		}
		
		public static Range rangeDenied(final byte from, final byte to) {
			return new Range(from, to, false);
		}
		
		public static Range combine(final boolean denyUndefined, final Range... ranges) {
			Set<Range> rset = new TreeSet<Range>(Arrays.asList(ranges));		// filter doubles, sort (disallowing ranges first)
			final Range[] ra = rset.toArray(new Range[rset.size()]);
			rset = null;
			
			return new Range() {
				@Override
				public boolean isAllowed(byte b) {
					boolean allowed = denyUndefined;
					for (int i=0; i<ra.length; i++) {
						final Range r = ra[i];
						if (r.from > b)
							break;
						if (r.to >= b)
							allowed = r.allowed;
					}
					return allowed;
				}
			};
		}
	}
}
