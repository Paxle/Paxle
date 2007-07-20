package org.paxle.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class ParserTools {
	
	public static String whitespaces2Space(String text) {
		if (text == null) return null;
		return text.replaceAll("\\s", " ").trim();
	}
	
	public static InputStream getInputStream(File file) throws FileNotFoundException {
		return new FileInputStream(file);
	}
	
	/* TODO:
	public static IParserDocument parseSubDoc(File content) {
		
	}
	
	public static File createTempFile(Class clazz) {
		
	}
	*/
}
