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
