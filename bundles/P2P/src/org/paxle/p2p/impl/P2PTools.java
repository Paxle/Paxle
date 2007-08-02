package org.paxle.p2p.impl;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.jxta.id.IDFactory;
import net.jxta.peergroup.PeerGroupID;

public class P2PTools {
    /**
     * Returns a SHA1 hash of string.
     * This function was copied from https://jxta-guide.dev.java.net/source/browse/jxta-guide/trunk/src/guide_v2.5/jxse-tutorials-src-20070620.zip
     *
     * @param expression to hash
     * @return a SHA1 hash of string or {@code null} if the expression could
     *         not be hashed.
     */
    public static byte[] hash(final String expression) {
        byte[] result;
        MessageDigest digest;

        if (expression == null) {
            throw new IllegalArgumentException("Invalid null expression");
        }

        try {
            digest = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException failed) {
            failed.printStackTrace(System.err);
            RuntimeException failure = new IllegalStateException("Could not get SHA-1 Message");

            failure.initCause(failed);
            throw failure;
        }

        try {
            byte[] expressionBytes = expression.getBytes("UTF-8");

            result = digest.digest(expressionBytes);
        } catch (UnsupportedEncodingException impossible) {
            RuntimeException failure = new IllegalStateException("Could not encode expression as UTF8");

            failure.initCause(impossible);
            throw failure;
        }
        return result;
    }
    
    /**
     * Given a group name generates a Peer Group ID who's value is chosen based upon that name.
     * This function was copied from https://jxta-guide.dev.java.net/source/browse/jxta-guide/trunk/src/guide_v2.5/jxse-tutorials-src-20070620.zip
     *
     * @param groupName group name encoding value
     * @return The PeerGroupID value
     */
    public static PeerGroupID createPeerGroupID(final String groupName) {
        // Use lower case to avoid any locale conversion inconsistencies
        return IDFactory.newPeerGroupID(PeerGroupID.defaultNetPeerGroupID, hash(groupName.toLowerCase()));
    }    
}
