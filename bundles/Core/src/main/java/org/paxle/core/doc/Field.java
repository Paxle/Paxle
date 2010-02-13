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

package org.paxle.core.doc;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;


public final class Field<Type extends Serializable> implements Comparable<Field<?>>, Serializable {
	private static final long serialVersionUID = 1L;

	private static final Pattern pattern = Pattern.compile("(\\w+)\\s\\(([^)]+)\\)(?:\\s(indexed))?(?:\\s(savedPlain))?(?:\\s(tokenize))?");
	
	private final boolean index;
	private final boolean savePlain;
	private final boolean tokenize;
	
	@Nonnull
	private final Class<Type> clazz;
	
	@Nonnull
	private final String name;
	
	/**
	 * @param index specifies that the value of the field is made <i>searchable</i>
	 * @param savePlain specifies that the value of the field is <i>stored as-is</i> in the index
	 * @param tokenize specifies whether the value of the field is being split up into tokens during indexing
	 * @param name the name of the field
	 * @param clazz the field value type, which must be {@link Serializable serializable}
	 */
	public Field(final boolean index, final boolean savePlain, final boolean tokenize, String name, Class<Type> clazz) {
		if (!Serializable.class.isAssignableFrom(clazz)) throw new IllegalArgumentException("Class must be serializable");
		if (name.length() > 80) throw new IllegalArgumentException("The name is too long. A maximum of 80 chars is allowed.");
		this.index = index;
		this.savePlain = savePlain;
		this.tokenize = tokenize;
		this.name = name;
		this.clazz = clazz; 
	}
	
	public final boolean isIndex() {
		return this.index;
	}
	
	public final boolean isSavePlain() {
		return this.savePlain;
	}
	
	public final boolean isTokenize() {
		return this.tokenize;
	}
	
	@Nonnull
	public final Class<Type> getType() {
		return this.clazz;
	}
	
	@Nonnull
	public final String getName() {
		return this.name;
	}
	
	public int compareTo(Field<?> o) {
		return this.name.compareTo(o.name);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Field) {
			return this.name.equals(((Field<?>)obj).name);
		}
		return super.equals(obj);
	}
	
	@Override
	public int hashCode() {
		return this.name.hashCode();
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(this.name);
		sb.append(" (").append(this.clazz.getName()).append(')');
		if (this.index)
			sb.append(" indexed");
		if (this.savePlain)
			sb.append(" savedPlain");
		if (this.tokenize)
			sb.append(" tokenize");
		return sb.toString();
	}
	
	@SuppressWarnings("unchecked")
	public static Field valueOf(String value) {
		Matcher m = pattern.matcher(value);
		if (!m.matches()) throw new IllegalArgumentException();
		
		String name = m.group(1);
		String clazzName = m.group(2);
		boolean index = m.group(3) != null;
		boolean savePlain = m.group(4) != null;
		boolean tokenize = m.group(5) != null;
		
		try {
			Class<?> clazz = null;
			if (clazzName.startsWith("[L")) {
				clazzName = clazzName.substring(2, clazzName.length()-1);
				clazz = Thread.currentThread().getContextClassLoader().loadClass(clazzName);
				clazz = Array.newInstance(clazz,0).getClass();
			} else {
				clazz = Thread.currentThread().getContextClassLoader().loadClass(clazzName);
			}
			
			return new Field(
					index,
					savePlain,
					tokenize,
					name,
					clazz
			);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
