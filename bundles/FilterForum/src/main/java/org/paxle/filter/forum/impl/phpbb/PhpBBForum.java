package org.paxle.filter.forum.impl.phpbb;

import org.paxle.filter.forum.impl.AForum;
import org.paxle.filter.forum.impl.ForumPage;


public class PhpBBForum extends AForum {
	
	public PhpBBForum() {
		/* ======================================================
		 * pages to ignore
		 * ====================================================== */		
		this.ignoreForumPages(
				"groupcp.php",
				"login.php",
				"posting.php",
				"privmsg.php",
				"ucp.php",
				"viewonline.php",
				"search.php"
		);
		
		/* ======================================================
		 * pages to crawl
		 * ====================================================== */
		this.addForumPage(new ForumPage("faq.php", true));
		this.addForumPage(new ForumPage("index.php", true));
		this.addForumPage(new ForumPage("memberlist.php", true,arrayToMap("start")));			
		this.addForumPage(new ForumPage("profile.php", true,arrayToMap("mode=viewprofile","u"),arrayToMap("mode")));
		this.addForumPage(new ForumPage("viewforum.php", true,arrayToMap("f","start")));
		this.addForumPage(new ForumPage("viewtopic.php", true,arrayToMap("t","start"), arrayToMap("p")));
	}

}
