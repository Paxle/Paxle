package org.paxle.filter.wordlistcreator.impl.tokenizer;

import java.io.Reader;
import java.util.Scanner;

import org.paxle.filter.wordlistcreator.ITokenizer;

public class RegExpTokenizer implements ITokenizer {

	Scanner scanner = null;
	
	public void setContent(Reader content) {
		this.scanner = new Scanner(content).useDelimiter("[^aAbBcCdDeEfFgGhHiIjJkKlLmMnNoOpPqQrRsStTuUvVwWxXyYzZöäüÖÄÜß]");		
	}

	public boolean hasNext() {
		return this.scanner.hasNext();
	}

	public String next() {
		return this.scanner.next();
	}
	
}
