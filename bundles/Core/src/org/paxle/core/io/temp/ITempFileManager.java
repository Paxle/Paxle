
package org.paxle.core.io.temp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public interface ITempFileManager {
	
	public abstract File createTempFile() throws IOException;
	public abstract void releaseTempFile(File file) throws FileNotFoundException, IOException;
	
	public abstract void setTempDirFor(ITempDir dir, String... classNames);
	public abstract void removeTempDirFor(String... classNames);
}
