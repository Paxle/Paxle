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

	public Object fromXMLString(String xmlValue) {
		// TODO Auto-generated method stub
		return null;
	}

	public String toXMLString(Object value) {
		// TODO Auto-generated method stub
		return null;
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
		return (Field) value;
	}

	public boolean equals(Object x, Object y) throws HibernateException {
        if (x == y) return true; 
        if ((x == null) || (y == null)) return false; 
        return ((Field) x).equals((Field) y); 
	}

	public int hashCode(Object x) throws HibernateException {
		return x.hashCode();
	}

	public boolean isMutable() {
		return false;
	}

	public Object nullSafeGet(ResultSet rs, String[] names, Object owner)throws HibernateException, SQLException {
	      String name = rs.getString(names[0]);
	      return rs.wasNull() ? null : Field.valueOf(name);
	}

	public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
		if (value == null) {
			st.setNull(index, Types.VARCHAR);
		} else {
			st.setString(index, ((Field) value).toString());
		}
	}

	public Object replace(Object original, Object target, Object owner) throws HibernateException {
		return original;
	}

	public Class returnedClass() {
		return Field.class;
	}

	public int[] sqlTypes() {
		return new int[] { Types.VARCHAR };
	}

}
