package org.paxle.se.index.mg4j.impl;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.mg4j.document.Document;
import it.unimi.dsi.mg4j.index.DowncaseTermProcessor;
import it.unimi.dsi.mg4j.index.Index;
import it.unimi.dsi.mg4j.index.IndexIterator;
import it.unimi.dsi.mg4j.index.IndexReader;
import it.unimi.dsi.mg4j.index.TermProcessor;
import it.unimi.dsi.mg4j.index.cluster.IndexCluster;
import it.unimi.dsi.mg4j.query.QueryEngine;
import it.unimi.dsi.mg4j.query.SelectedInterval;
import it.unimi.dsi.mg4j.query.parser.SimpleParser;
import it.unimi.dsi.mg4j.search.DocumentIteratorBuilderVisitor;
import it.unimi.dsi.mg4j.search.score.DocumentScoreInfo;
import it.unimi.dsi.mg4j.search.score.Scorer;
import it.unimi.dsi.mg4j.search.score.VignaScorer;

import java.io.File;


public class Test {
	/** Official index names throughout LSR. */
	public final static String[] INDEX = {"Text","Author"};	
	
	public static void main(String[] args) {
//		testOpenIndex();
		
		try {
			String baseName = "paxle";
			String datadir = "/home/theli/paxle/workspace/runtime-osgi/test/";
			
//			DocumentCollection collection = (DocumentCollection) BinIO.loadObject( new File( datadir, "test.collection" ));
			
			// Load all indices and put them in the name2index map.
			final Object2ReferenceOpenHashMap<String,Index> name2index = new Object2ReferenceOpenHashMap<String,Index>( INDEX.length, .5f );
			
			for( int i = 0; i < INDEX.length; i++ ) {
				Index index = Index.getInstance( new File( datadir, baseName + "-" + INDEX[ i ] ).toString(), true, true );
				name2index.put( INDEX[ i ], index );
			}			
			
			QueryEngine queryEngine = new QueryEngine( 
					new SimpleParser(
							name2index.keySet(), 
							"Text", 
							new Object2ObjectOpenHashMap<String,TermProcessor>( 
									INDEX, 
									new TermProcessor[] { DowncaseTermProcessor.getInstance(), DowncaseTermProcessor.getInstance()} 
							)
					), 
					new DocumentIteratorBuilderVisitor( name2index, name2index.get( INDEX[ 0 ] ), Integer.MAX_VALUE ), 
					name2index 
			);

			queryEngine.score( new Scorer[] { new VignaScorer() },new double[] { 1 } );
			
			String query = "apache";
			int start = 0, maxNumItems = 100;
			
			ObjectArrayList<DocumentScoreInfo<Reference2ObjectMap<Index,SelectedInterval[]>>> results = new ObjectArrayList<DocumentScoreInfo<Reference2ObjectMap<Index,SelectedInterval[]>>>();
			int globNumItems = 0;

			try {
				globNumItems = queryEngine.copy().process( query, start, maxNumItems, results );
			} catch(Exception qe) {
				qe.printStackTrace();
			}
			
			System.out.println("Global item count " + globNumItems);
			for( int i = 0; i < results.size(); i++ ) {
				DocumentScoreInfo<Reference2ObjectMap<Index,SelectedInterval[]>> dsi = results.get( i );
				System.out.println("Doc-Nr " + dsi.document + ", Score " + dsi.score);
				
//				final Document document = collection.document( dsi.document );
//				System.out.println(document.uri());
			}
			
			System.out.println("fertig");
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void testOpenIndex() {
		try {
			Index i = IndexCluster.getInstance("test-text");			
			IndexReader r = i.getReader();
			IndexIterator d = r.documents("you");
			Document doc = null;
			while (d.hasNext()) {
				System.out.println(d.nextDocument());
				System.out.println(d.count());
			}
			r.close();
			
			System.out.println("finished");
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
}
