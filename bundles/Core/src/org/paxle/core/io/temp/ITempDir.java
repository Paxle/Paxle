package org.paxle.core.io.temp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public interface ITempDir {
	
	public File createTempFile(String prefix, String suffix) throws IOException;
	public void releaseTempFile(File file) throws FileNotFoundException, IOException;
}
