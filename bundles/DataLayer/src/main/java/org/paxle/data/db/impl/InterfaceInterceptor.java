package org.paxle.data.db.impl;

import java.io.Serializable;

import org.hibernate.EmptyInterceptor;
import org.hibernate.EntityMode;
import org.paxle.core.doc.CrawlerDocument;
import org.paxle.core.doc.ICrawlerDocument;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.doc.IndexerDocument;
import org.paxle.core.doc.ParserDocument;
import org.paxle.core.queue.Command;
import org.paxle.core.queue.CommandProfile;
import org.paxle.core.queue.ICommand;
import org.paxle.core.queue.ICommandProfile;

public class InterfaceInterceptor extends EmptyInterceptor {
	private static final long serialVersionUID = 1L;

	public String getEntityName(Object object) {
        if (object instanceof ICommand) {
            return ICommand.class.getName();
        } else if (object instanceof ICrawlerDocument) {
        	return ICrawlerDocument.class.getName();
        } else if (object instanceof IParserDocument) {
        	return IParserDocument.class.getName();
        } else if (object instanceof IIndexerDocument) {
        	return IIndexerDocument.class.getName();
        } else if (object instanceof ICommandProfile) {
        	return ICommandProfile.class.getName();
        }
        return super.getEntityName(object);
    }

    public Object instantiate(String entityName, EntityMode entityMode, Serializable id) {
        if (entityName.equals(ICommand.class.getName())) {
        	Command cmd = new Command();
        	cmd.setOID((Integer)id);
            return cmd;
        } else if (entityName.equals(ICrawlerDocument.class.getName())) {
        	ICrawlerDocument crawlerDocument = new CrawlerDocument();
        	crawlerDocument.setOID((Integer)id);
            return crawlerDocument;
        } else if (entityName.equals(IParserDocument.class.getName())) {
        	IParserDocument parserDocument = new ParserDocument();
        	parserDocument.setOID((Integer)id);
        	return parserDocument;
        } else if (entityName.equals(IIndexerDocument.class.getName())) {
        	IIndexerDocument indexerDocument = new IndexerDocument();
        	indexerDocument.setOID((Integer)id);
        	return indexerDocument;
        } else if (entityName.equals(ICommandProfile.class.getName())) {
        	ICommandProfile profile = new CommandProfile();
        	profile.setOID((Integer)id);
        	return profile;
        }
        return super.instantiate(entityName, entityMode, id);
    }
}
