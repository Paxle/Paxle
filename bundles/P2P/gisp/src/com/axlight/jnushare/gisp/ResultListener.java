/*
 * Copyright (C) 2002-2003 Daishi Kato <daishi@axlight.com>
 * All rights reserved.
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the Sun Project JXTA
 * License Version 1.1 (the "License"); you may not use this
 * file except in compliance with the License. A copy of the
 * License is available at http://www.jxta.org/jxta_license.html.
 * ====================================================================
 *
 * $Id: ResultListener.java,v 1.4 2003/01/31 21:57:02 daishi Exp $
 */

package com.axlight.jnushare.gisp;

/**
 * ResultListener
 * 
 * for GISP.query(...)
 */

public interface ResultListener{
    /**
     * called for String Result
     */
    public void stringResult(String data);

    /**
     * called for XML Result
     */
    public void xmlResult(byte[] data);

    /**
     * called when the query is expired
     */
    public void queryExpired();
}

