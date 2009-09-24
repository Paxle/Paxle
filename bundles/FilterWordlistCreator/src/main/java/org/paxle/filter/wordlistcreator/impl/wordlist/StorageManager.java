/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2009 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package org.paxle.filter.wordlistcreator.impl.wordlist;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.h2.tools.Server;

public class StorageManager {

	/** The H2 webfrontend */
	private Server ws = null;

	private Connection dbConn = null;

	private Log logger = LogFactory.getLog(this.getClass());; 

	public StorageManager (File dataDir) throws IOException {
		/* setup data dir */
		if (!dataDir.exists()) 
			if (!dataDir.mkdirs())
				throw new IOException("Couldn't create data directory '" + dataDir.toString() + "'");
		try {
			this.ws = Server.createWebServer().start();
			Class.forName("org.h2.Driver");
			String jdbcurl = "jdbc:h2:" + dataDir.toURI() + "database.h2";
			logger.info("Using jdbc URL '" + jdbcurl + "'");
			this.dbConn = DriverManager.getConnection(jdbcurl);
			Statement stmt = this.dbConn.createStatement(); 
			stmt.executeUpdate("CREATE TABLE `tokens` (`token` VARCHAR( 255 ) NOT NULL , `count` BIGINT DEFAULT 1 NOT NULL , PRIMARY KEY ( `token` ));");
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void store(TokenCollection tokens) {
		logger.warn("Dumping cache");

		PreparedStatement stmt = null;
		try {
			stmt = this.dbConn.prepareStatement("UPDATE `tokens` SET `count`=`count`+1 WHERE `token`=?;");
			int i = 0;
			int ar = 0;
			while (i < tokens.getTokens().length) {
				stmt.setString(1, tokens.getTokens()[i].getToken());
				ar = stmt.executeUpdate();
				if (ar == 0) {
					Statement st = this.dbConn.createStatement(); 
					st.executeUpdate("INSERT INTO `tokens`(`token`) VALUES('" + tokens.getTokens()[i].getToken() + "');");
					st.close();
				}
				i++;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null)
				try {stmt.close();} catch (SQLException e) {}
		}
	}

	public void close() {
		try {
			this.dbConn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
