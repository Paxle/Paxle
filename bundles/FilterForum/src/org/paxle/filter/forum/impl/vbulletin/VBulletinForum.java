package org.paxle.filter.forum.impl.vbulletin;

import org.paxle.filter.forum.impl.AForum;
import org.paxle.filter.forum.impl.ForumPage;


public class VBulletinForum extends AForum {
	public VBulletinForum() {
		/* ======================================================
		 * pages to ignore
		 * ====================================================== */
		this.ignoreForumPages(
				"calendar.php",
				"register.php",
				"newreply.php",
				"printthread.php", 
				"register.php",				
				"search.php",   	// ignore print only pages (we crawl the full featured version)
				"showpost.php", 	// ignore single posts (we are only interested in whole threads)
				"sendmessage.php" 
		);
		
		/* ======================================================
		 * pages to crawl
		 * ====================================================== */		
		this.addForumPage(new ForumPage("faq.php", true, arrayToMap("faq")));
		this.addForumPage(new ForumPage("index.php", true));
		this.addForumPage(new ForumPage("member.php", true, arrayToMap("u")));
		this.addForumPage(new ForumPage("memberlist.php", true, arrayToMap("page")));
		this.addForumPage(new ForumPage("showthread.php", true, arrayToMap("t","page"), arrayToMap("p")));
		this.addForumPage(new ForumPage("forumdisplay.php", true, arrayToMap("f","page")));
	}
}
