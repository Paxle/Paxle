package org.paxle.data.db.impl;

import org.exolab.castor.mapping.FieldHandler;
import org.exolab.castor.mapping.ValidityException;
import org.paxle.core.doc.ICrawlerDocument;

public class FileFieldHandler implements FieldHandler {

	/**
	 * Returns the value of the field from the object.
	 *
	 * @param object The object
	 * @return The value of the field
	 * @throws IllegalStateException The Java object has changed and
	 *  is no longer supported by this handler, or the handler is not
	 *  compatiable with the Java object
	 */
	public Object getValue( Object object ) throws IllegalStateException {    
		try {
			if (!(object instanceof ICrawlerDocument)) throw new IllegalStateException("Unknown object type");
			return ((ICrawlerDocument)object).getContent().toURI().toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}


	/**
	 * Sets the value of the field on the object.
	 *
	 * @param object The object
	 * @param value The new value
	 * @throws IllegalStateException The Java object has changed and
	 *  is no longer supported by this handler, or the handler is not
	 *  compatiable with the Java object
	 * @thorws IllegalArgumentException The value passed is not of
	 *  a supported type
	 */
	public void setValue( Object object, Object value ) throws IllegalStateException, IllegalArgumentException {
		System.out.println("setValue");
	}


	/**
	 * Creates a new instance of the object described by this field.
	 *
	 * @param parent The object for which the field is created
	 * @return A new instance of the field's value
	 * @throws IllegalStateException This field is a simple type and
	 *  cannot be instantiated
	 */
	public Object newInstance( Object parent ) throws IllegalStateException
	{
		System.out.println("newInstance");
		return null;
	}


	/**
	 * Sets the value of the field to a default value.
	 *
	 * Reference fields are set to null, primitive fields are set to
	 * their default value, collection fields are emptied of all
	 * elements.
	 *
	 * @param object The object
	 * @throws IllegalStateException The Java object has changed and
	 *  is no longer supported by this handler, or the handler is not
	 *  compatiable with the Java object
	 */
	public void resetValue( Object object )
	throws IllegalStateException, IllegalArgumentException
	{
		System.out.println("resetValue");
	}



	/**
	 * @deprecated No longer supported
	 */
	public void checkValidity( Object object )
	throws ValidityException, IllegalStateException
	{
		// do nothing
	}
}
