package org.paxle.data.db;

import org.paxle.core.queue.ICommandProfile;

public interface ICommandProfileDB {
	public ICommandProfile getProfileByID(int profileID);
	public void storeProfile(ICommandProfile profile);
}
