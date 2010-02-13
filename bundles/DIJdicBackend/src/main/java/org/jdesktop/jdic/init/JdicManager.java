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

package org.jdesktop.jdic.init;

/*
 * Copyright (C) 2004 Sun Microsystems, Inc. All rights reserved. Use is
 * subject to license terms.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 */
/* =====================================================================
 * MODIFICATIONS BY PAXLE:
 * This file is based on JDic-Version 0.9.5 but contains modifications
 * to run JDic in an OSGi container
 * ===================================================================== */

import java.io.File;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashSet;

import org.jdesktop.jdic.browser.internal.WebBrowserUtil;

/**
 * Initialization manager for JDIC to set the environment variables or
 * initialize the set up for native libraries and executable files.
 * <p>
 * There are 3 modes of operation: WebStart, file system, and .jar file.
 * <p>
 * When using WebStart, please specify a .jar file(jdic-native.jar) with the
 * native libraries for your platform to be loaded by WebStart in your JNPL.
 * This class will find the unjared native libraries and executables, and use
 * them directly.
 * <p>
 * If not in WebStart, the system will expect the native libraries to be located
 * in directory at the root of the classpath or .jar containing this class.
 * 
 * @author Michael Samblanet
 * @author Paul Huang
 * @author George Zhang
 * @author Michael Shan
 * @since July 29, 2004
 */

public class JdicManager {
    /** The path for the JDIC native files (jdic.dll/libjdic.so, etc) */
    static String nativeLibPath = null;

    /**
     * Private constructor to prevent public construction.
     */
    private JdicManager() {}

    /**
     * Returns the canonical name of the platform. This value is derived from the
     * System property os.name.
     * 
     * @return The platform string.
     */
    public static String getPlatform() {
        // See list of os names at: http://lopica.sourceforge.net/os.html
        // or at: http://www.tolstoy.com/samizdat/sysprops.html
        String osname = System.getProperty("os.name");
        if (osname.startsWith("Windows")) {
                return "windows";
        }
        return canonical(osname);
    }

    /**
     * Returns the default DLL extension for platform. 
     * 
     * @return The default DLL extension.
     */
    public static String getPlatformDLLext() 
    {
        if (getPlatform().equals("windows")) {
            return ".dll";
        }
        return ".so";
    }    

    /**
     * Returns the class name suffix for class loader. This value is derived from the
     * System property os.name.
     * 
     * @return The class suffix string.
     */
    public static String getPlatformSuffix()
    {
        // See list of os names at: http://lopica.sourceforge.net/os.html
        // or at: http://www.tolstoy.com/samizdat/sysprops.html
        String osname = System.getProperty("os.name");
        if (osname.startsWith("Windows")) {
            return "windows";
        } 
        return "unix";
    }            
    
    /**
     * Return the name of the architecture. This value is determined by the
     * System property os.arch.
     * 
     * @return The architecture string.
     */
    public static String getArchitecture() {
        String arch = System.getProperty("os.arch");
        if (arch.endsWith("86")) {
                return "x86";
        }
        return canonical(arch);
    }

    /**
     * @param value
     *            The value to be canonicalized.
     * @return The value with all '/', '\' and ' ' replaced with '_', and all
     *         uppercase characters replaced with lower case equivalents.
     */
    private static String canonical(String value) {
        WebBrowserUtil.trace("value:" + value);
        WebBrowserUtil.trace("canonical:"
                        + value.toLowerCase().replaceAll("[\\\\/ ]", "_"));
        return value.toLowerCase().replaceAll("[\\\\/ ]", "_");
    }
    
    /**
     * Initializes the shared native file settings for all the JDIC components/
     * packages. Set necessary environment variables for the shared native
     * library and executable files, including *.dll files on Windows, and *.so
     * files on Unix.
     * 
     * @exception JdicInitException Generic initialization exception
     * 
     * XXX: Function changed by Paxle
     */
    public static void init() throws JdicInitException {
    	return;
    }
    
    private static String getBinaryPath() {
        //WebBrowserUtil.trace("native lib path " + nativeLibPath);
        return nativeLibPath;
    }
    
    /**
     * Initializes the native file settings for the JDIC Browser component 
     * (package <code>org.jdecktop.jdic.browser</code>). Set necessary 
     * environment variables for the Browser specific native library and 
     * executable files, including *.exe files on Windows, and mozembed-<os>-gtk* 
     * files on Unix.
     * 
     * @exception JdicInitException Generic initialization exception
     */
    public static void initBrowserNative() throws JdicInitException {
        // The Browser component is used.
        // If the Browser specific native file setting was already initialized, 
        // just return.
        try {
            // Pre-append the JDIC binary path to PATH(on Windows) or 
            // LD_LIBRARY_PATH(on Unix).         
            
            /** The environment variable for library path setting */
            boolean isWindows = getPlatform().equals("windows");
            String libPathEnv = isWindows ? "PATH" : "LD_LIBRARY_PATH";
            
            String binaryPath = getBinaryPath();            
            InitUtility.preAppendEnv(libPathEnv, binaryPath); 
            WebBrowserUtil.trace("JDIC found BIN path=[" + binaryPath + "]");

            String browserPath = WebBrowserUtil.getBrowserPath();
            if (browserPath == null) {
                throw new JdicInitException(
                    "Can't locate the native browser path!");
            }
            
            if (WebBrowserUtil.isDefaultBrowserMozilla()) {
                // Mozilla is the default/embedded browser.
                // Use the user defined value or the mozilla binary
                // path as the value of MOZILLA_FIVE_HOME env variable.
                String envMFH = InitUtility.getEnv("MOZILLA_FIVE_HOME");
                if (envMFH == null) {
                    File browserFile = new File(browserPath);
                    if (browserFile.isDirectory()) {
                        envMFH = browserFile.getCanonicalPath();
                    } else {
                        envMFH = browserFile.getCanonicalFile().getParent();
                    }                    
                }
                
                if (!isWindows) {
                    // On Unix, add the binary path to PATH.
                    InitUtility.preAppendEnv("PATH", binaryPath);
                } else {               
                    // Mozilla on Windows, reset MOZILLA_FIVE_HOME to the GRE 
                    // directory path:  
                    //   [Common Files]\mozilla.org\GRE\1.x_BUILDID, 
                    // if Mozilla installs from a .exe package.
                    //                
                    String xpcomPath = envMFH + File.separator + "xpcom.dll";                        
                    if (!(new File(xpcomPath).isFile())) {
                        // Mozilla installs from a .exe package. Check the 
                        // installed GRE directory.
                        String mozGreHome 
                            = WebBrowserUtil.getMozillaGreHome();
                        if (mozGreHome == null) {
                            throw new JdicInitException(
                                "Can't locate the GRE directory of the " +
                                "installed Mozilla binary: " + envMFH);
                        }                       
                        envMFH = mozGreHome;
                    }
                }              

                InitUtility.setEnv("MOZILLA_FIVE_HOME", envMFH);
                InitUtility.preAppendEnv(libPathEnv, envMFH);
            } // end - Mozilla is the default/embedded browser.
        } catch (Throwable e) {
            throw new JdicInitException(e);
        }
    }

    private static boolean initNativeLoader = false;        
    private static HashSet<String> loadedLibraries = new HashSet<String>();  
    
    private static String[] jdicLibNames = new String[] {"jdic","jdic-5","jdic-6"};
    
    /**
     * Function changed by Paxle
     */
    public static synchronized void loadLibrary(final String libName) 
            throws PrivilegedActionException 
    {
        try {
            if( !loadedLibraries.contains(libName) ){
                loadedLibraries.add(libName);
                
                // just let OSGi do the job
                if (libName.equals("jdic")) {
                	boolean loaded = false;
                	
                	for (String jdicLibName : jdicLibNames) {
                    	try {
                    		// trying to load the library
                    		System.loadLibrary(jdicLibName);
                    		
                    		// success
                    		loaded = true;
                    		break;
                    	} catch (UnsatisfiedLinkError e) {
                    		// we faild to load the library
                    		// just continue here
                    	} 	
                	}
                	if (!loaded) throw new UnsatisfiedLinkError("Unable to load the native library jdic.");
                } else {
                	System.loadLibrary(libName);
                }
            }
        } catch(Exception e) {
            throw new PrivilegedActionException(e);
        }
    }
    
    private static boolean initBrowserDLLPath = false; 
    
    /**
     * Function changed by Paxle
     */
    public static synchronized Process exec( final String[] args) 
            throws PrivilegedActionException 
    {
        try {        
            if(!initNativeLoader){
                initNativeLoader = true;
                init();
            }
            if(!initBrowserDLLPath){
                initBrowserDLLPath = true;
                initBrowserNative();
            }
            final Process[] res = new Process[] {null};
                AccessController.doPrivileged( new PrivilegedExceptionAction() { 
                    public Object run() throws IOException {
                        res[0] = Runtime.getRuntime().exec( args );
                        return null;
                    }
                });
                return res[0];    
        }catch(PrivilegedActionException e){
            throw e;
        }catch(Exception e){
            throw new PrivilegedActionException(e);
        }
    }        
}