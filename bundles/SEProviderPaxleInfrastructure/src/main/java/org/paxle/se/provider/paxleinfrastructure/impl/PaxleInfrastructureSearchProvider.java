/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2010 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.se.provider.paxleinfrastructure.impl;

import java.io.IOException;
import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.se.search.ISearchProvider;
import org.paxle.se.search.ISearchProviderContext;
import org.paxle.se.search.ISearchRequest;
import org.paxle.se.search.SearchProviderContext;

@Component(metatype=false)
@Service(ISearchProvider.class)
@Properties({
	@Property(name="org.paxle.metadata", boolValue=true),
	@Property(name="org.paxle.metadata.localization", value="/OSGI-INF/l10n/SEProviderPaxleInfrastructure")
})
public class PaxleInfrastructureSearchProvider implements ISearchProvider {
	
	public void search(ISearchRequest searchRequest, List<IIndexerDocument> results) throws IOException, InterruptedException {
		try {
			final ISearchProviderContext context = SearchProviderContext.getCurrentContext();
			String request = new PaxleInfrastructureQueryFactor().transformToken(searchRequest.getSearchQuery());
			IIndexerDocument indexerDoc = context.createDocument();
			System.out.println(request);
			if(request.toLowerCase().equals("paxle wiki")){
				indexerDoc.set(IIndexerDocument.LOCATION, "http://wiki.paxle.net/");
				indexerDoc.set(IIndexerDocument.TITLE, "Paxle Wiki");
			}else if(request.toLowerCase().equals("paxle homepage")){
				indexerDoc.set(IIndexerDocument.LOCATION, "http://www.paxle.net/");
				indexerDoc.set(IIndexerDocument.TITLE, "Paxle Homepage");
			}else if(request.toLowerCase().equals("paxle bts")){
				indexerDoc.set(IIndexerDocument.LOCATION, "http://bugs.pxl.li/");
				indexerDoc.set(IIndexerDocument.TITLE, "Paxle Bugtracker");
			}else if(request.toLowerCase().startsWith("paxle bug #")){
				String bugNum=request.substring(11);
				indexerDoc.set(IIndexerDocument.LOCATION, "https://bugs.pxl.li/view.php?id="+bugNum);
				indexerDoc.set(IIndexerDocument.TITLE, "Paxle Bug #"+bugNum);
			}else{
				indexerDoc=null;
			}
			if(indexerDoc!=null)
				results.add(indexerDoc);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
