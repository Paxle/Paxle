package org.paxle.data.db.impl;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.type.SerializableType;
import org.hibernate.type.SerializationException;

public class FieldValueUserType extends SerializableType {
	private Log logger = LogFactory.getLog(this.getClass().getName());
	
	public FieldValueUserType() {
		super(Serializable.class);
	}

	@Override
	public Object get(ResultSet rs, String name) throws HibernateException, SQLException {
		try {
			return super.get(rs, name);
		} catch (SerializationException s) {
			this.logger.error(String.format("Error while loading deserializing field value. The SQL statement was:\r\n%s", rs.getStatement()),s);
			return null;
		}
	}
}
