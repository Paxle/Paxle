package org.paxle.icon.impl;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.spi.IIORegistry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ctreber.aclib.image.ico.ICOFile;
import com.ctreber.aclib.image.ico.spi.ICOImageReaderSPI;

/**
 * A class to read favicons for a given url or from byte-array using <b>AC.lib ICO</b>.
 * 
 * @author theli
 */
public class FaviconReader {
	private static Log logger = LogFactory.getLog(FaviconReader.class);

	static {
		// registering the ICO lib as new provider
		IIORegistry.getDefaultInstance().registerServiceProvider(new ICOImageReaderSPI());		
	}

	public static BufferedImage readIcoImage(URL theIconURL) {
		BufferedImage image = null;
		try {
			ICOFile lICOFile = new ICOFile(theIconURL);
			ArrayList<BufferedImage> images = (ArrayList<BufferedImage>) lICOFile.getImages();
			if (images != null && images.size() > 0) {
				return selectBest(images);
			} 
		}  catch (Exception e)  {
			logger.warn(String.format("Unable to load favicon from URL '%s'. %s", theIconURL, e.getMessage()));
		}		
		return image;
	}
	
	public static BufferedImage readIcoImage(byte[] content) {
		BufferedImage image = null;
		try {
			ICOFile lICOFile = new ICOFile(content);
			ArrayList<BufferedImage> images = (ArrayList<BufferedImage>) lICOFile.getImages();
			if (images != null && images.size() > 0) {
				return selectBest(images);
			} 
		}  catch (Exception e)  {
			logger.warn(String.format("Unable to load favicon from byte array. %s", e.getMessage()));
		}		
		return image;
	}
	
	private static BufferedImage selectBest(ArrayList<BufferedImage> images) {
		if (images == null) return null;
		// TODO: select image that fits best in size and quality
		return images.get(0);
	}
}
