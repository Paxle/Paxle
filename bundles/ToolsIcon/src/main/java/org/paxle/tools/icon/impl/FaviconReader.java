/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2010 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.tools.icon.impl;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Service;
import org.paxle.tools.icon.IFaviconReader;

import com.ctreber.aclib.image.ico.ICOFile;
import com.ctreber.aclib.image.ico.spi.ICOImageReaderSPI;

/**
 * A class to read favicons for a given url or from byte-array using <b>AC.lib ICO</b>.
 * 
 * @author theli
 */
@Component
@Service(IFaviconReader.class)
public class FaviconReader implements IFaviconReader {
	private Log logger = LogFactory.getLog(FaviconReader.class);
	
	protected ImageReaderSpi serviceProvider = null;
	
	@Activate
	void activate() {
		this.serviceProvider = new ICOImageReaderSPI();
		
		// registering the ICO lib as new provider
		IIORegistry.getDefaultInstance().registerServiceProvider(this.serviceProvider);		
	}
	
	@Deactivate
	void deactivate() {
		// unregister service provider
		IIORegistry.getDefaultInstance().deregisterServiceProvider(this.serviceProvider);
	}

	public Image readIcoImage(URL theIconURL) {
		BufferedImage image = null;
		try {
			ICOFile lICOFile = new ICOFile(theIconURL);
			Image[] images = lICOFile.getImages();
			if (images != null && images.length > 0) {
				return this.selectBest(images);
			} 
		}  catch (Exception e)  {
			this.logger.warn(String.format("Unable to load favicon from URL '%s'. %s", theIconURL, e.getMessage()));
		}		
		return image;
	}
	
	public Image readIcoImage(byte[] content) {
		BufferedImage image = null;
		try {
			ICOFile lICOFile = new ICOFile(content);
			Image[] images = lICOFile.getImages();
			if (images != null && images.length > 0) {
				return this.selectBest(images);
			} 
		}  catch (Exception e)  {
			this.logger.warn(String.format("Unable to load favicon from byte array. %s", e.getMessage()));
		}		
		return image;
	}
	
	private Image selectBest(Image[] images) {
		if (images == null) return null;
		// TODO: select image that fits best in size and quality
		return images[0];
	}
}
