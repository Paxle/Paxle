package org.paxle.filter.forum.impl.wbb;

import org.paxle.filter.forum.impl.AForum;
import org.paxle.filter.forum.impl.ForumPage;

public class WBBForum extends AForum {
	public WBBForum() {
		/* ======================================================
		 * pages to ignore
		 * ====================================================== */
		this.ignoreForumPages(
				 "addreply.php"				
				,"calendar.php"
				,"editpost.php"				
				,"forgotpw.php"
				,"formmail.php"				
				,"login.php"
				,"map.php"
				,"markread.php"
				,"misc.php"
				,"newthread.php"				
				,"print.php"				
				,"register.php"
				,"report.php"				
				,"search.php"
				,"statsinfo.php"				
				,"usercp.php"
		);		
		
		/* ======================================================
		 * pages to crawl
		 * ====================================================== */
		this.addForumPage(new ForumPage("board.php", true, arrayToMap("boardid","page")));		
		this.addForumPage(new ForumPage("index.php", true, arrayToMap("page=Index|Board", "l"), arrayToMap("form","action")));
		this.addForumPage(new ForumPage("memberslist.php", true, arrayToMap("page")));
		this.addForumPage(new ForumPage("profile.php", true, arrayToMap("userid")));		
		this.addForumPage(new ForumPage("team.php", true, arrayToMap("page")));		
		this.addForumPage(new ForumPage("thread.php", true, arrayToMap("threadid","page"), arrayToMap("goto")));
	}
}
