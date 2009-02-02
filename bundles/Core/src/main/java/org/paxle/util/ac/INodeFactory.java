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

import java.nio.charset.Charset;

/**
 * A node-factory is a class providing the {@link AhoCorasick}-trie with new nodes
 * which are needed when a new pattern is added. There exist several possibilities
 * of implementing nodes, which also determine the range of possible values for
 * children.
 * <p>
 * I.e. the {@link ArrayNode} (as well as the {@link MappedArrayNode}) only provide
 * support for byte-values from 0 to 127 (so the full ASCII-{@link Charset}) due to
 * their indexed access, whereas {@link LinkedNode}s for example are not limited to
 * positive {@link Byte}-values. Both types are theoretically not bound to
 * <code>byte</code> and maybe support for additional key-types is being added later.
 * Currently <code>byte</code> is the only way to access and operate the trie.
 * <p>
 * The abstract class {@link ANode} is the basis for all accepted nodes, providing
 * the {@link AhoCorasick} class and sub-classes of it with all necessary features
 * needed to traverse the tree, to store values of a given type and some additional
 * methods like visualization, etc.. So the {@link ANode} class has to be sub-classed
 * to provide the methods required for a normal trie-operation.
 * <p>
 * Implementations of nodes may concentrate on memory usage primarily rather than
 * lookup-speed or the other way round or a mixture of both - depending on the types
 * of patterns the trie shall hold and the type of text to be matched.
 * 
 * @param <E> the type of the value
 */
public interface INodeFactory<E> {
	
	/**
	 * Creates a new {@link ANode node}. This method is being called by
	 * {@link AhoCorasick#addPattern(byte[], Object)}.
	 * 
	 * @param depth the depth of the node, a parameter to be evaluated optionally for
	 *        implementation details
	 * @return a new, empty {@link ANode node} containing no value(s) and no key-mappings
	 *         yet
	 */
	public ANode<E> createNode(int depth);
}
