package org.paxle.charset;

public interface ICharsetDetectorStream {
	
	/**
	 * @return the detected charset or <code>null</code> if detection failed
	 */
	public String getCharset();
	
	/**
	 * @return <code>true</code> if charset detection was done successfully.
	 */
	public boolean charsetDetected();
}
