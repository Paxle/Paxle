
package org.paxle.desktop;

public interface IDesktopServices {
	
	public static enum Dialogues {
		SETTINGS, STATS, CCONSOLE
	}
	
	public void openDialogue(Dialogues d);
	public void closeDialogue(Dialogues d);
	public boolean isBrowserOpenable();
	public boolean browseUrl(String url);
	public String getPaxleUrl(String... path);
	public void setTrayMenuVisible(boolean yes);
	public boolean isTrayMenuVisible();
}
