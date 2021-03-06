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

package org.paxle.core.io;

import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.osgi.framework.Bundle;

/**
 * This service can be used to determine the {@link Locale#getDisplayName() names} of all {@link Locale}, 
 * for which a <code>*.properties</code> file is available in the bundle directory <code>OSGI-INF/l10n</code>.
 * <br/>
 * <i>Usage example (for OSGi DS):</i>
 * <pre><code>
 * &#064;Component
 * public class MyService {
 *    &#064;Reference
 *    protected IResourceBundleTool resourceBundleTool;
 *    private String[] locales;
 *  
 *    protected void activate(ComponentContext context) {
 *       // the supported locales
 *       this.locales = this.resourceBundleTool.getLocaleArray(this.getClass().getSimpleName(), Locale.ENGLISH);
 *    }
 * }
 * </code></pre>
 */
public interface IResourceBundleTool {
	public static final String LOCALIZATION_LOCATION_DEFAULT = "OSGI-INF/l10n";
	
	/**
	 * Returns a list of {@link URL URLs} to all {@link ResourceBundle} property-files found for the given base-name, e.g.
	 * for the base-name 'MyService' the following URLs may be returned:
	 * <pre><code> [
	 *    bundleentry://43.fwk24387997/OSGI-INF/l10n/MyService.properties,
	 *    bundleentry://43.fwk24387997/OSGI-INF/l10n/MyService_de.properties
	 * ]</code><pre>
	 * 
	 * @param resourceBundleBase the base-name that is used to file the property files.
	 * @return
	 */
	public @Nonnull List<URL> getLocaleURL(@Nonnull String resourceBundleBase);
	
	public @Nonnull List<URL> getLocaleURL(@Nonnull Bundle osgiBundle, @Nonnull String resourceBundleBase);
	
	/**
	 * Returns the {@link Locale#getDisplayName() names} of all {@link Locale}, for which a properties-file
	 * was found in the OSGi-bundle directory <code>OSGI-INF/l10n</code>.
	 * 
	 * @param resourceBundleBase the {@link ResourceBundle} base-name.
	 * @param defaultLocale the default {@link Locale} returned for files-names without a language-code, e.g.  <code>MyService.properties</code>.
	 * @return the names of all {@link Locale} found, e.g. <code>["en","de"]</code>
	 */
	public @Nonnull List<String> getLocaleList(@Nonnull String resourceBundleBase, @Nullable Locale defaultLocale);
	
	public @Nonnull List<String> getLocaleList(@Nonnull Bundle osgiBundle, @Nonnull String resourceBundleBase, @Nullable Locale defaultLocale);
	
	/**
	 * Returns the {@link Locale#getDisplayName() names} of all {@link Locale}, for which a properties-file
	 * was found in the OSGi-bundle directory <code>OSGI-INF/l10n</code>.
	 * 
	 * @param resourceBundleBase the {@link ResourceBundle} base-name.
	 * @param defaultLocale the default {@link Locale} returned for files-names without a language-code, e.g.  <code>MyService.properties</code>.
	 * @return the names of all {@link Locale} found, e.g. <code>["en","de"]</code>
	 * @see #getLocaleList(String, Locale)
	 */
	public @Nonnull String[] getLocaleArray(@Nonnull String resourceBundleBase, @Nullable Locale defaultLocale);
	
	public @Nonnull String[] getLocaleArray(@Nonnull Bundle osgiBundle, @Nonnull String resourceBundleBase, @Nullable Locale defaultLocale);
	
	/**
	 * A function to return the {@link ResourceBundle} for the given base-name and locale. The data is loaded via a {@link PropertyResourceBundle}
	 * from the directory {@link #LOCALIZATION_LOCATION_DEFAULT}
	 * @since 0.1.39-SNAPSHOT
	 */
	public @CheckForNull ResourceBundle getLocalization(@Nonnull String resourceBundleBase, @Nullable String locale);
	
	public @CheckForNull ResourceBundle getLocalization(@Nonnull Bundle osgiBundle, @Nonnull String resourceBundleBase, @Nullable String localeStr);
	
	/**
	 * A function to return the {@link ResourceBundle} for the given base-name and {@link Locale}. The data is loaded via a {@link PropertyResourceBundle}
	 * from the directory {@link #LOCALIZATION_LOCATION_DEFAULT}
	 * @since 0.1.39-SNAPSHOT
	 */
	public @CheckForNull ResourceBundle getLocalization(@Nonnull String resourceBundleBase, @Nullable Locale locale);
	
	public @CheckForNull ResourceBundle getLocalization(@Nonnull Bundle osgiBundle, @Nonnull String resourceBundleBase, @Nullable Locale locale);
}