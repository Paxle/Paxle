/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2009 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package org.paxle.desktop.impl.dialogues.cconsole;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Vector;

import org.osgi.service.event.Event;
import org.paxle.core.doc.CommandEvent;
import org.paxle.core.doc.ICommand;
import org.paxle.core.doc.ICrawlerDocument;
import org.paxle.core.doc.IParserDocument;
import org.paxle.desktop.impl.Messages;
import org.paxle.desktop.impl.ServiceManager.MWComponents;
import org.paxle.desktop.impl.dialogues.cconsole.CrawlingConsole.TableColumnSpecs;

final class TableColumns implements TableColumnSpecs {
	
	private static final char[] HEX = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
	
	static final int DEFAULT = (
			(1 << Columns.COMPONENT.ordinal()) |
			(1 << Columns.LOCATION.ordinal()) |
			(1 << Columns.DEPTH.ordinal()));
	
	private final String unknown = Messages.getString("crawlingConsole.unknown");
	
	int showCmd;
	
	public TableColumns() {
		this(DEFAULT);
	}
	
	public TableColumns(final int showCmd) {
		this.showCmd = showCmd;
	}
	
	public Object[] getColumnHeaders() {
		final Object[] r = new Object[Integer.bitCount(showCmd)];
		int i = 0;
		for (final Columns col : Columns.values())
			if ((showCmd & (1 << col.ordinal())) != 0)
				r[i++] = col.getL10n();
		return r;
	}
	
	public String name() {
		return Integer.toString(showCmd);
	}
	
	private String fromCollection(final Object[] o) {
		return (o == null) ? "null" : fromCollection(Arrays.asList(o));
	}
	
	private String fromCollection(final Map<?,?> m) {
		// TODO
		return (m == null) ? "null" : m.toString();
	}
	
	private String fromCollection(final Collection<?> c) {
		// TODO
		return (c == null) ? "null" : c.toString();
	}
	
	public void insertValues(final Vector<String> row, final Event event, final ICommand cmd, final String lastFilter) {
		
		for (final Columns col : Columns.values()) try {
			if ((showCmd & (1 << col.ordinal())) == 0)
				continue;
			
			switch (col) {
				/* ICommand specific */
				case COMPONENT: {
					String compId = (String)event.getProperty(CommandEvent.PROP_COMPONENT_ID);
					if (compId.endsWith(".in")) { //$NON-NLS-1$
						compId = compId.substring(0, compId.length() - ".in".length()); //$NON-NLS-1$
					} else if (compId.endsWith(".out")) { //$NON-NLS-1$
						compId = compId.substring(0, compId.length() - ".out".length()); //$NON-NLS-1$
					}
					final MWComponents mwc = MWComponents.valueOfID(compId);
					row.add((mwc == null) ? compId : mwc.toString());
				} break;
				
				case DEPTH: {
					row.add((cmd == null) ? "-" : Integer.toString(cmd.getDepth()));
				} break;
				
				case LOCATION: {
					final String uri = (String)event.getProperty(CommandEvent.PROP_COMMAND_LOCATION);
					row.add(URLDecoder.decode(uri, Charset.defaultCharset().name()));
				} break;
				
				case RESULT: {
					row.add((cmd == null) ? unknown : cmd.getResult().toString());
				} break;
				
				case RESULTTEXT: {
					row.add((cmd == null) ? unknown : cmd.getResultText());
				} break;
				
				case LAST_FILTER: {
					row.add((lastFilter == null) ? unknown : lastFilter);
				} break;
				
				/* ICrawlerDocument specific */
				case C_CHARSET: {
					final ICrawlerDocument cdoc = (cmd == null) ? null : cmd.getCrawlerDocument();
					row.add((cdoc == null) ? unknown : cdoc.getCharset());
				} break;
				
				case C_DATE: {
					final ICrawlerDocument cdoc = (cmd == null) ? null : cmd.getCrawlerDocument();
					String dateStr = unknown;
					final Date lastCrawled;
					if (cdoc != null && (lastCrawled = cdoc.getCrawlerDate()) != null)
						dateStr = DateFormat.getDateTimeInstance().format(lastCrawled);
					row.add(dateStr);
				} break;
				
				case C_LANGS: {
					final ICrawlerDocument cdoc = (cmd == null) ? null : cmd.getCrawlerDocument();
					row.add((cdoc == null) ? unknown : fromCollection(cdoc.getLanguages()));
				} break;
				
				case C_LASTMOD: {
					final ICrawlerDocument cdoc = (cmd == null) ? null : cmd.getCrawlerDocument();
					String dateStr = unknown;
					final Date lastMod;
					if (cdoc != null && (lastMod = cdoc.getLastModDate()) != null)
						dateStr = DateFormat.getDateTimeInstance().format(lastMod);
					row.add(dateStr);
				} break;
				
				case C_MD5: {
					final ICrawlerDocument cdoc = (cmd == null) ? null : cmd.getCrawlerDocument();
					if (cdoc == null) {
						row.add(unknown);
					} else {
						final byte[] md5 = cdoc.getMD5Sum();
						if (md5 == null) {
							row.add(unknown);
						} else {
							final StringBuilder sb = new StringBuilder(md5.length * 2);
							for (int i=0; i<md5.length; i++)
								sb.append(HEX[(md5[i] & 0xF0) >>> 8]).append(HEX[md5[i] & 0x0F]);
							row.add(sb.toString());
						}
					}
				} break;
				
				case C_MIMETYPE: {
					final ICrawlerDocument cdoc = (cmd == null) ? null : cmd.getCrawlerDocument();
					row.add((cdoc == null) ? unknown : cdoc.getMimeType());
				} break;
				
				case C_SIZE: {
					final ICrawlerDocument cdoc = (cmd == null) ? null : cmd.getCrawlerDocument();
					row.add((cdoc == null) ? unknown : Long.toString(cdoc.getSize()));
				} break;
				
				case C_STATUS: {
					final ICrawlerDocument cdoc = (cmd == null) ? null : cmd.getCrawlerDocument();
					row.add((cdoc == null) ? unknown : cdoc.getStatus().toString());
				} break;
				
				case C_STATUSTEXT: {
					final ICrawlerDocument cdoc = (cmd == null) ? null : cmd.getCrawlerDocument();
					row.add((cdoc == null) ? unknown : cdoc.getStatusText());
				} break;
				
				/* IParserDocument specific */
				case P_AUTHOR: {
					final IParserDocument pdoc = (cmd == null) ? null : cmd.getParserDocument();
					row.add((pdoc == null) ? unknown : pdoc.getAuthor());
				} break;
				
				case P_CHARSET: {
					final IParserDocument pdoc = (cmd == null) ? null : cmd.getParserDocument();
					String csName = unknown;
					final Charset charset;
					if (pdoc != null && (charset = pdoc.getCharset()) != null)
						csName = charset.name();
					row.add(csName);
				} break;
				
				case P_HEADLINES: {
					final IParserDocument pdoc = (cmd == null) ? null : cmd.getParserDocument();
					row.add((pdoc == null) ? unknown : fromCollection(pdoc.getHeadlines()));
				} break;
				
				case P_IMAGES: {
					final IParserDocument pdoc = (cmd == null) ? null : cmd.getParserDocument();
					row.add((pdoc == null) ? unknown : fromCollection(pdoc.getImages()));
				} break;
				
				case P_KEYWORDS: {
					final IParserDocument pdoc = (cmd == null) ? null : cmd.getParserDocument();
					row.add((pdoc == null) ? unknown : fromCollection(pdoc.getKeywords()));
				} break;
				
				case P_LANGUAGES: {
					final IParserDocument pdoc = (cmd == null) ? null : cmd.getParserDocument();
					row.add((pdoc == null) ? unknown : fromCollection(pdoc.getLanguages()));
				} break;
				
				case P_LASTMOD: {
					final IParserDocument pdoc = (cmd == null) ? null : cmd.getParserDocument();
					String dateStr = unknown;
					final Date lastChanged;
					if (pdoc != null && (lastChanged = pdoc.getLastChanged()) != null)
						dateStr = DateFormat.getDateTimeInstance().format(lastChanged);
					row.add(dateStr);
				} break;
				
				case P_LINKS: {
					final IParserDocument pdoc = (cmd == null) ? null : cmd.getParserDocument();
					row.add((pdoc == null) ? unknown : fromCollection(pdoc.getLinks()));
				} break;
				
				case P_MIMETYPE: {
					final IParserDocument pdoc = (cmd == null) ? null : cmd.getParserDocument();
					row.add((pdoc == null) ? unknown : pdoc.getMimeType());
				} break;
				
				case P_STATUS: {
					final IParserDocument pdoc = (cmd == null) ? null : cmd.getParserDocument();
					row.add((pdoc == null) ? unknown : pdoc.getStatus().toString());
				} break;
				
				case P_STATUSTEXT: {
					final IParserDocument pdoc = (cmd == null) ? null : cmd.getParserDocument();
					row.add((pdoc == null) ? unknown : pdoc.getStatusText());
				} break;
				
				case P_SUBDOCS: {
					final IParserDocument pdoc = (cmd == null) ? null : cmd.getParserDocument();
					if (pdoc == null) {
						row.add(unknown);
					} else {
						// TODO
					}
				} break;
				
				case P_SUMMARY: {
					final IParserDocument pdoc = (cmd == null) ? null : cmd.getParserDocument();
					row.add((pdoc == null) ? unknown : pdoc.getSummary());
				} break;
				
				case P_TITLE: {
					final IParserDocument pdoc = (cmd == null) ? null : cmd.getParserDocument();
					row.add((pdoc == null) ? unknown : pdoc.getTitle());
				} break;
			}
		} catch (UnsupportedEncodingException e) { /* ignore, we are using the default charset here */
		} catch (Throwable e) { e.printStackTrace(); }
		
	}
}