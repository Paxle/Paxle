package org.paxle.iplocator.impl;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Locale;

import net.sf.javainetlocator.InetAddressLocator;
import net.sf.javainetlocator.InetAddressLocatorException;

import org.paxle.core.io.IOTools;

public class LocatorTool {
	public static Locale getLocale(String host) {
		try {
			return InetAddressLocator.getLocale(host);
		} catch (InetAddressLocatorException e) {
			return null;
		}
	}
	
	public static IconData getIcon(String hostNameIp) {
		// trying to determine the locale
		Locale locale = getLocale(hostNameIp);
		if (locale == null) return null;
		
		// get the country code
		String countryCode = locale.getCountry();
		byte[] imageData = readFlagPng(countryCode);
		return (imageData == null) ? null : new IconData(imageData);
	}
	
	public static byte[] readFlagPng(String countryCode) {
		InputStream iconInput = null;
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		try {
			// get icon stream
			iconInput = LocatorTool.class.getResourceAsStream("/resources/flags/" + countryCode.toLowerCase() + ".png");
			if (iconInput == null) return null;
			
			// load data
			IOTools.copy(iconInput, bout);
			return bout.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			try { bout.close(); } catch (Exception e) {/* ignore this */}
			if (iconInput != null) try { iconInput.close(); } catch (Exception e) {/* ignore this */}
		}
	}
}
