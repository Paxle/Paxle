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
