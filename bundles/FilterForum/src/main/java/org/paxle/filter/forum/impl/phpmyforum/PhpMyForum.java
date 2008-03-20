package org.paxle.filter.forum.impl.phpmyforum;

import org.paxle.filter.forum.impl.AForum;
import org.paxle.filter.forum.impl.ForumPage;

public class PhpMyForum extends AForum {
	public PhpMyForum() {
		/* ======================================================
		 * pages to ignore
		 * ====================================================== */
		this.ignoreForumPages(
				"calendar.php",
				"login.php",
				"mailpass.php",
				"online.php",
				"search.php",
				"signup.php"
		);
		
		/* ======================================================
		 * pages to crawl
		 * ====================================================== */
		this.addForumPage(new ForumPage("index.php", true));
		this.addForumPage(new ForumPage("profile.php", true, arrayToMap("id")));
		this.addForumPage(new ForumPage("board.php", true, arrayToMap("id","page")));
		this.addForumPage(new ForumPage("topic.php", true, arrayToMap("id","page")));
		this.addForumPage(new ForumPage("members.php", true, arrayToMap("page")));
		this.addForumPage(new ForumPage("team.php", true, arrayToMap("page")));
	}
}
