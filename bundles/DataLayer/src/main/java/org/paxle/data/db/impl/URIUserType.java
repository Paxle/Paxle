package org.paxle.data.db.impl;

import java.io.Serializable;
import java.net.URI;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.usertype.EnhancedUserType;

public class URIUserType implements EnhancedUserType {

	public Object fromXMLString(String xmlValue) {
		return URI.create(xmlValue);
	}

	public String objectToSQLString(Object value) {
		return ((URI)value).toASCIIString();
	}

	public String toXMLString(Object value) {
		return ((URI)value).toASCIIString();
	}

	public Object assemble(Serializable cached, Object owner) throws HibernateException {
		return cached;
	}

	public Object deepCopy(Object value) throws HibernateException {
		return value;
	}

	public Serializable disassemble(Object value) throws HibernateException {
		return (URI) value;
	}

	public boolean equals(Object x, Object y) throws HibernateException {
		if (x == y) return true; 
		if ((x == null) || (y == null)) return false; 
		return x.equals(y);
	}

	public int hashCode(Object x) throws HibernateException {
		return x.hashCode();
	}

	public boolean isMutable() {
		return false;
	}

	public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {
		String value = rs.getString(names[0]);
		return (value == null) ? null : URI.create(value);
	}

	public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
		if (value == null) {
			st.setNull(index, Types.VARCHAR);
		} else {
			st.setObject(index, ((URI)value).toASCIIString());
		}
	}

	public Object replace(Object original, Object target, Object owner) throws HibernateException {
		return original;
	}

	public Class<?> returnedClass() {
		return URI.class;
	}

	public int[] sqlTypes() {
		return new int[] { Types.VARCHAR };
	}
	

}
