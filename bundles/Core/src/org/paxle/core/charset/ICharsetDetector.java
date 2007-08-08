package org.paxle.core.charset;
import java.io.InputStream;
import java.io.OutputStream;

public interface ICharsetDetector {
	public boolean isInspectable(String mimeType);
	public String[] getInspectableMimeTypes();
	public String[] getSupportedCharsets();
	public ACharsetDetectorOutputStream createOutputStream(OutputStream out);
	public ACharsetDetectorInputStream createInputStream(InputStream in);
}
