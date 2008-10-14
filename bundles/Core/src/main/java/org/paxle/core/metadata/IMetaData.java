
package org.paxle.core.metadata;

import java.io.IOException;
import java.io.InputStream;

public interface IMetaData {
	
	public String getName();
	public String getDescription();
	public String getVersion();
	public InputStream getIcon(int size) throws IOException;
}
