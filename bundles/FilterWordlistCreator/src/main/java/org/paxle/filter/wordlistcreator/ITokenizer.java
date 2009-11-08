package org.paxle.filter.wordlistcreator;

import java.io.Reader;

public interface ITokenizer {
	public void setContent(Reader content);
	public boolean hasNext();
	public String next();
}
