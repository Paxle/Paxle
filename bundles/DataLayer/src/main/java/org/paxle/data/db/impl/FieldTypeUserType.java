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
package org.paxle.data.db.impl;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.usertype.EnhancedUserType;
import org.paxle.core.doc.Field;

public class FieldTypeUserType implements EnhancedUserType {

	private static final String[] PROP_NAMES = new String[] { 
		"name", "clazz", "isIndex", "isSavePlain", "isTokenize"
	};

	private static final int[] PROP_TYPES = new int[] {
		Types.VARCHAR, Types.VARCHAR, Types.BIT, Types.BIT, Types.BIT
	};

	/**
	 *  <column name="name"/>
	 *	<column name="clazz"/>
	 *	<column name="isIndex"/>
	 *	<column name="isSavePlain"/>
	 *  <column name="isTokenize"/>
	 */
	public int[] sqlTypes() {
		return PROP_TYPES;
	}	

	public String objectToSQLString(Object value) {
		if (!(value instanceof Field)) throw new IllegalArgumentException();
		return value.toString();
	}

	public Object assemble(Serializable cached, Object owner) throws HibernateException {
		return cached;
	}

	public Object deepCopy(Object value) throws HibernateException {
		return value;
	}

	public Serializable disassemble(Object value) throws HibernateException {
		return (Field<?>) value;
	}

	public boolean equals(Object x, Object y) throws HibernateException {
		if (x == y) return true; 
		if ((x == null) || (y == null)) return false; 
		return ((Field<?>) x).equals(y); 
	}

	public int hashCode(Object x) throws HibernateException {
		return x.hashCode();
	}

	public boolean isMutable() {
		return false;
	}

	@SuppressWarnings("unchecked")
	public Object nullSafeGet(ResultSet rs, String[] names, Object owner)throws HibernateException, SQLException {
		String name = rs.getString(names[0]);		
		if (rs.wasNull()) return null;
		String clazz = rs.getString(names[1]);
		if (rs.wasNull()) return null;
		boolean isIndex = rs.getBoolean(names[2]);
		if (rs.wasNull()) return null;
		boolean isSavePlain = rs.getBoolean(names[3]);
		if (rs.wasNull()) return null;
		boolean isTokenize = rs.getBoolean(names[4]);
		if (rs.wasNull()) return null;
		
		try {
			return new Field(
					isIndex,
					isSavePlain,
					isTokenize,
					name,
					Thread.currentThread().getContextClassLoader().loadClass(clazz)
			);
		} catch (ClassNotFoundException e) {
			throw new HibernateException(e);
		}
	}

	public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
		if (value == null) {
			st.setNull(index+0, Types.VARCHAR);
			st.setNull(index+1, Types.VARCHAR);
			st.setNull(index+2, Types.BIT);
			st.setNull(index+3, Types.BIT);
		} else {
			st.setString(index+0, ((Field<?>) value).getName());
			st.setString(index+1, ((Field<?>) value).getType().getName());
			st.setBoolean(index+2, ((Field<?>) value).isIndex());
			st.setBoolean(index+3, ((Field<?>) value).isSavePlain());			
		}
	}

	public Object replace(Object original, Object target, Object owner) throws HibernateException {
		return original;
	}

	public Class<?> returnedClass() {
		return Field.class;
	}

	public Object fromXMLString(String xmlValue) {
		// TODO Auto-generated method stub
		return null;
	}

	public String toXMLString(Object value) {
		// TODO Auto-generated method stub
		return null;
	}		
}
