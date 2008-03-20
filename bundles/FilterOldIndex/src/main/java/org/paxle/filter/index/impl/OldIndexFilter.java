package org.paxle.filter.index.impl;

import org.paxle.core.filter.IFilterContext;
import org.paxle.core.queue.ICommand;
import org.paxle.filter.index.IOldIndexFilter;
import org.paxle.se.index.lucene.impl.Activator;


public class OldIndexFilter implements IOldIndexFilter {
    
    public void filter(ICommand command, IFilterContext filterContext) {
        long time = System.currentTimeMillis();
            try {
                Activator.indexWriterThread.delete(command.getLocation());
            } catch (Throwable e) {
                e.printStackTrace();
            }
            //IndexWriter.delete(term.createTerm(command.getLocation()));
        time = System.currentTimeMillis() - time;
        System.out.println("Duration in 'isListed()' for oldIndexDeletion: "+ time + " ms . URL: " + command.getLocation());
    }

}
