package org.paxle.core.queue;


public interface ICommandProfileManager {
	public ICommandProfile getProfileByID(int profileID);
	public void storeProfile(ICommandProfile profile);
}
