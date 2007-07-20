package org.paxle.parser.html.impl.tags;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

import org.htmlparser.tags.MetaTag;

public class MetaTagManager {
	
	private static interface Prefixed {
		public boolean isPrefixed();
		public String getPrefix();
	}
	/**
	 * @see <a href="http://www.metatab.de/index.html">Dublin Core META-tags</a>
	 */
	public static enum Names implements Prefixed {
		// "name"-attributes
		Title("DC"),
			Title_Alternative("DC"),
		Creator("DC"),
		Subject("DC"),
		Description("DC"),
			Description_TableOfContents("DC"), Description_Abstract("DC"),
		Publisher("DC"),
		Contributor("DC"),
		Date("DC"),
			Date_Created("DC"), Date_Valid("DC"), Date_Available("DC"), Date_Issued("DC"), Date_Modified("DC"),
		Type("DC"),
		Format("DC"),
			Format_Extent("DC"), Format_Medium("DC"),
		Source("DC"),
		Language("DC"),
		Relation("DC"),
			Relation_IsVersionOf("DC"), Relation_HasVersion("DC"), Relation_IsReplacedBy("DC"), Relation_Replaces("DC"),
			Relation_IsRequiredBy("DC"), Relation_Requires("DC"), Relation_IsPartOf("DC"), Relation_HasPart("DC"),
			Relation_IsReferencedBy("DC"), Relation_References("DC"), Relation_IsFormatOf("DC"), Relation_HasFormat("DC"),
		Coverage("DC"), Coverage_Spatial("DC"), Coverage_Temporal("DC"),
		Rights("DC"),
		Keywords,
		Robots,
		Author,
		Copyright,
		Generator,
		Audience,
		Abstract,
		Page_Type,
		Page_Topic,
		Revisit_After,
		
		// "http-equiv"-attributes
		Content_Type,
		Refresh,
		Expires,
		Content_Language, Content_Script_Style, Content_Style_Type,
		Cache_Control,
		Pragma
		
		;
		
		private final String pref;
		
		private Names() {
			this.pref = null;
		}
		
		private Names(String pref) {
			this.pref = pref;
		}
		
		public String getPrefix() {
			return this.pref;
		}
		
		public boolean isPrefixed() {
			return this.pref != null;
		}
	}
	
	private final Hashtable<Names,Collection<String>> tags = new Hashtable<Names,Collection<String>>();
	
	private void add(Names n, String v) {
		if (v == null || v.length() == 0)
			return;
		Collection<String> col = tags.get(n);
		if (col == null)
			tags.put(n, col = new LinkedList<String>());
		col.add(v);
	}
	
	public void addMetaTag(MetaTag tag) {
		Names n = getName(tag.getAttribute("name"));
		if (n != null) {
			add(n, tag.getMetaContent().replaceAll("\\s", " ").trim());
			return;
		}
		n = getName(tag.getHttpEquiv());
		if (n != null) {
			add(n, tag.getMetaContent().replaceAll("\\s", " ").trim());
			return;
		}
	}
	
	public Collection<String> get(Names n) {
		return this.tags.get(n);
	}
	
	public String getCombined(Names... names) {
		return getCombined(" ", names);
	}
	
	public String getCombined(String glue, Names... names) {
		final StringBuilder sb = new StringBuilder();
		for (Names name : names) {
			if (this.tags.containsKey(name)) {
				final Collection<String> col = get(name);
				if (col.size() == 0) continue;
				final Iterator<String> it = col.iterator();
				while (it.hasNext()) {
					sb.append(it.next()).append(glue);
				}
			}
		}
		return (sb.length() == 0) ? null : sb.delete(sb.length() - glue.length(), sb.length()).toString();
	}
	
	public String getFirst(Names... names) {
		for (Names name : names) {
			if (this.tags.containsKey(name)) {
				final Collection<String> col = get(name);
				if (col.size() == 0) continue;
				return col.iterator().next();
			}
		}
		return null;
	}
	
	public void clear() {
		this.tags.clear();
	}
	
	private static Names getName(String attr) {
		if (attr == null) return null;
		for (final Names n : Names.values()) {
			String ns = n.toString().replace('_', '.').toUpperCase();
			if (n.isPrefixed())
				ns = n.getPrefix() + "." + ns;
			if (attr.toUpperCase().matches(ns))
				return n;
		}
		return null;
	}
}
