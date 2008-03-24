package org.paxle.core.filter;

import java.net.URI;
import java.util.Properties;

import org.paxle.core.io.temp.ITempFileManager;
import org.paxle.core.norm.IReferenceNormalizer;

public interface IFilterContext {	
	public int getFilterPosition();
	public String getTargetID();
	public IFilter getFilter();
	public Properties getFilterProperties();
	
	/**
	 * @return the {@link ITempFileManager temp-file-manager} that should be used by the filter
	 * to handle temp-files.
	 */
	public ITempFileManager getTempFileManager();
	
	/**
	 * @return a component to normalize {@link URI URIs}.
	 * @see IReferenceNormalizer#normalizeReference(String)
	 */
	public IReferenceNormalizer getReferenceNormalizer();
}
