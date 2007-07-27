/*
 * Copyright (C) 2001-2003 Daishi Kato <daishi@axlight.com>
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
 * $Id: GInfoManager.java,v 1.17 2003/02/13 08:50:42 daishi Exp $
 */

package com.axlight.jnushare.gisp;

/**
 * GInfoManager interface
 *
 */

public interface GInfoManager{
    
    /**
     * start.
     * @param i Initiator of this GInfoManager.
     * @param useThread if this should use thread or not.
     */
    public void start(Initiator i, boolean useThread);

    /**
     * stop.
     */
    public void stop();

    /**
     * manage GInfo.
     *
     * - delete expired data
     * - remote insert data if needed
     * - delete if max num exceeded (TODO)
     * 
     */
    public void manageGInfo();

    /**
     * insert data
     */
    public void insertGInfo(GInfo newInfo);

    /**
     * query GInfo
     * 
     * results will be notified through listener
     */
    public void queryGInfo(String query, Listener listener);

    /**
     * Initiator of this class.
     * GInfoManager must use this interface.
     */
    public interface Initiator{
	public void remoteInsertGInfo(GInfo info);
	public long getCurrentTime();
    }

    /**
     * query result listener.
     * used in queryGInfo.
     */
    public interface Listener{
	public void notifyResult(Object xml);
    }

}

