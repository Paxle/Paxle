package org.paxle.se.query;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.paxle.se.query.tokens.AToken;
import org.paxle.se.query.tokens.ModToken;
import org.paxle.se.query.tokens.MultiToken;

public class TokenTools {
	
	public static <Token extends AToken> List<Token> getFromTopLevel(MultiToken parent, Class<Token> clazz) {
		List<Token> tokens = new LinkedList<Token>();
		for (AToken token : parent)
			if (clazz.isAssignableFrom(token.getClass()))
				tokens.add(clazz.cast(token));
		return tokens;
	}
	
	public static <Token extends AToken> List<Token> getDeep(MultiToken parent, Class<Token> clazz) {
		List<Token> tokens = new LinkedList<Token>();
		for (AToken token : parent) {
			if (clazz.isAssignableFrom(token.getClass()))
				tokens.add(clazz.cast(token));
			if (token instanceof MultiToken)
				tokens.addAll(getDeep((MultiToken)token, clazz));
		}
		return tokens;
	}
	
	public static List<ModToken> getModTokens(List<ModToken> tokens, Set<String> modFilters) {
		final List<ModToken> ret = new LinkedList<ModToken>();
		for (final ModToken token : tokens)
			if (modFilters == null || modFilters.contains(token.getMod()))
				ret.add(token);
		return ret;
	}
}
