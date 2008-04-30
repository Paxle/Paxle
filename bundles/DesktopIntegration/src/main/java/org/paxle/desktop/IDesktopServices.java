
package org.paxle.desktop;

public interface IDesktopServices {
	
	public boolean isBrowserOpenable();
	public boolean browseUrl(String url);
	public String getPaxleUrl(String... path);
	public void openSettingsDialog();
	public void closeSettingsDialog();
	public void setTrayMenuVisible(boolean yes);
	public boolean isTrayMenuVisible();
}
