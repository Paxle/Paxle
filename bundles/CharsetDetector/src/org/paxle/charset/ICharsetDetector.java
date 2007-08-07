package org.paxle.charset;
import java.io.InputStream;
import java.io.OutputStream;

public interface ICharsetDetector {
	public String[] getSupportedCharsets();
	public ACharsetDetectorOutputStream createOutputStream(OutputStream out);
	public ACharsetDetectorInputStream createInputStream(InputStream in);
}
