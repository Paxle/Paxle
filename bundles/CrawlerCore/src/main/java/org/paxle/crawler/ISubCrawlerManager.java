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

package org.paxle.crawler;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.jcip.annotations.ThreadSafe;

import org.osgi.framework.Constants;

@ThreadSafe
public interface ISubCrawlerManager {
	
	/**
	 * Returns a {@link Map} of installed {@link ISubCrawler sub-crawlers}. The key of this map is the
	 * {@link Constants#SERVICE_PID} of the {@link ISubCrawler}, the value is the {@link ISubCrawler} itself. 
	 * 
	 * @return an unmodifiable collection of all installed {@link ISubCrawler sub-crawlers}, e.g.
	 * <table border="1">
	 *  <tr><td>ftp</td><td>org.paxle.crawler.ftp.impl.FtpCrawler</td></tr>
	 * 	<tr><td>http</td><td>org.paxle.crawler.http.impl.HttpCrawler</td></tr>
	 *  <tr><td>https</td><td>org.paxle.crawler.http.impl.HttpCrawler</td></tr>
	 * </table>
	 */
	public @Nonnull Map<String,ISubCrawler> getSubCrawlers();
	
	/**
	 * @param protocol the crawling-protocol
	 * @return a collection of all enabled {@link ISubCrawler sub-crawlers} for the protocol
	 */
	public @Nonnull Collection<ISubCrawler> getSubCrawlers(String protocol);
	
	/**
	 * Getting an enabled {@link ISubCrawler} which is capable to handle the given network-protocol
	 * @param protocol the crawling-protocol
	 * @return the requested sub-crawler or <code>null</code> if no crawler for
	 *         the specified protocol is available
	 */
	public @Nullable ISubCrawler getSubCrawler(String protocol);
	
	/**
	 * @return a list of known but disabled protocols
	 */
	public @Nonnull Set<String> disabledProtocols();
	
	/**
	 * @return an unmodifiable list of all protocols supported by the registered {@link ISubCrawler sub-crawlers}
	 * 	This list also includes the {@link #disabledProtocols() disabled protocols}.
	 */
	public @Nonnull Set<String> getProtocols();
	
	/**
	 * Determines if a given protocol is supported by one of the registered {@link ISubCrawler sub-crawlers}. 
	 * @param protocol the protocol
	 * @return <code>true</code> if the given protocol is supported or <code>false</code> otherwise
	 */
	public boolean isSupported(String protocol);
}
