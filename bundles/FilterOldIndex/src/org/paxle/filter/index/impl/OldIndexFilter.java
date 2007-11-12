package org.paxle.filter.index.impl;

import org.apache.lucene.index.Term;
import org.paxle.core.doc.Field;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.core.filter.IFilterContext;
import org.paxle.core.queue.ICommand;
import org.paxle.filter.index.IOldIndexFilter;
import org.paxle.se.index.lucene.impl.Activator;


public class OldIndexFilter implements IOldIndexFilter {
    
    private Term term;
    
    public void filter(ICommand command, IFilterContext filterContext) {
        long time = System.currentTimeMillis();
            try {
                Field<String> temp2 = IIndexerDocument.LOCATION;
                temp2.getName();
                term = new Term(temp2.getName(), command.getLocation());
                //term = term.createTerm(command.getLocation());
                Activator.indexWriterThread.delete(term);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            //IndexWriter.delete(term.createTerm(command.getLocation()));
        time = System.currentTimeMillis() - time;
        System.out.println("Duration in 'isListed()' for oldIndexDeletion: "+ time + " ms . URL: " + command.getLocation());
    }

}
