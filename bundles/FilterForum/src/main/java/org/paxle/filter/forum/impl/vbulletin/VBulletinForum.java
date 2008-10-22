/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2008 the original author or authors.
 * 
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 * 
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

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
