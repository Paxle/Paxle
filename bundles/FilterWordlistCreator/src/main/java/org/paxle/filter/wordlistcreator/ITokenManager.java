package org.paxle.filter.wordlistcreator;

import java.io.IOException;

import org.paxle.core.doc.ICommand;

public interface ITokenManager {

	public void registerContent(ICommand command) throws IOException;
	
}
