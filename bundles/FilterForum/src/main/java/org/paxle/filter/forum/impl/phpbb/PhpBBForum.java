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
