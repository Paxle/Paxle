package org.paxle.se.index.mg4j.impl;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.mg4j.document.AbstractDocumentFactory;
import it.unimi.dsi.mg4j.document.Document;
import it.unimi.dsi.mg4j.document.DocumentFactory;

import java.io.IOException;
import java.io.InputStream;

import org.paxle.core.doc.IIndexerDocument;
import org.paxle.se.index.IFieldManager;

public class PaxleDocumentFactory extends AbstractDocumentFactory implements DocumentFactory {
	private static final long serialVersionUID = 1L;
	
	private IFieldManager fieldManager;
	
	public PaxleDocumentFactory(IFieldManager fieldManager) {
		if (fieldManager == null) throw new NullPointerException("The field-manager is null");
		this.fieldManager = fieldManager;
	}
	
	public DocumentFactory copy() {
		return new PaxleDocumentFactory(this.fieldManager);
	}

	public int fieldIndex(String fieldName) {
		if (!this.fieldManager.isKnown(fieldName)) return -1;
		if (fieldName.equals(IIndexerDocument.TEXT.getName())) return 0;
		if (fieldName.equals(IIndexerDocument.AUTHOR.getName())) return 1;
		return -1;
	}

	public String fieldName(int field) {
		ensureFieldIndex( field );
		if (field == 0) return IIndexerDocument.TEXT.getName();
		if (field == 1) return IIndexerDocument.AUTHOR.getName();
		return null;
	}

	public FieldType fieldType(int field) {
		ensureFieldIndex( field );
		return FieldType.TEXT;
	}

	public Document getDocument(InputStream arg0, Reference2ObjectMap<Enum<?>, Object> arg1) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public int numberOfFields() {
		// TODO Auto-generated method stub
		return 2;
	}

}
