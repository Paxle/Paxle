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

package org.paxle.filter.blacklist;

/**
 * This exception should be thrown when a name for a blacklist contains chars that can't be handled by the blacklistStore.
 */
 @SuppressWarnings("serial")
public class InvalidBlacklistnameException extends Exception 
{

	public InvalidBlacklistnameException() 
	{ 
		super();
	} 

	public InvalidBlacklistnameException( final String s ) 
	{ 
		super(s); 
	} 
}
