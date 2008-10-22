package org.paxle.filter.forum.impl;

import java.util.regex.Pattern;

import org.paxle.core.doc.ICrawlerDocument;
import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterContext;
import org.paxle.core.queue.ICommand;
import org.paxle.filter.forum.impl.wbb.WBBForum;

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
