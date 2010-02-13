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

package org.paxle.filter.forum.impl;

import java.util.regex.Pattern;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.paxle.core.doc.ICommand;
import org.paxle.core.doc.ICrawlerDocument;
import org.paxle.core.filter.FilterQueuePosition;
import org.paxle.core.filter.FilterTarget;
import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterContext;
import org.paxle.filter.forum.impl.wbb.WBBForum;

@Component(immediate=true, metatype=false)
@Service(IFilter.class)
@FilterTarget(@FilterQueuePosition(
		queueId = FilterQueuePosition.PARSER_OUT)
)
public class ForumFilter implements IFilter<ICommand> {
	private static final Pattern PHPBB_PATTERN = Pattern.compile("Powered\\s+by\\s+<a\\s+href\\s*=\\s*[\"']http://www.phpbb.com[^>]+>.*phpBB");

	
	public void filter(ICommand command, IFilterContext filterContext) {
		// TODO Auto-generated method stub
		try {
			// getting the crawler document
			ICrawlerDocument crawlerDocument = command.getCrawlerDocument();
//
//			// trying to determine which forum software is used
//			RandomAccessFile file = new RandomAccessFile(crawlerDocument.getContent(), "r");
//
//			long offset = (file.length() > 102400)?102400:0;
//			file.seek(offset);
//
//			ByteArrayOutputStream bOut = new ByteArrayOutputStream();
//			int b;
//			while ((b = file.read()) != -1) {
//				bOut.write(b);
//			}
//			file.close();

//			PhpBBForum forum = new PhpBBForum();
			WBBForum forum = new WBBForum();
			
//			// inspect if the document is a phpBB document
//			// Powered by <a href="http://www.phpbb.com/" target="_phpbb" class="copyright">phpBB</a> &copy; 2001, 2005 phpBB Group<br />
//			Matcher matcher = PHPBB_PATTERN.matcher(bOut.toString("UTF-8"));
//			if (!matcher.find()) return;

			// rewrite location
			System.out.println("PHPBB forum found.");
			forum.rewriteLocation(command.getLocation());

			// rewrite parsed links
			forum.rewriteLocation(command.getParserDocument());
			System.out.println("Filtering finished");
		} catch (BlockUrlException e) {
			command.setResult(ICommand.Result.Rejected,e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
