package fluddokt.ex;


public class DeviceInfo {
	public static DeviceInfo info = new DeviceInfo();
	public String getInfo() {
		return "";
	}
	public String getDownloadDir() {
		return null;
	}
	public boolean shownNotification(String name){return false;}
	public void setShownNotification(String name,boolean val){}
	public boolean isMusicPlaying(){return false;}
	public boolean isSynced(){return false;}
	public void setSynced(boolean in){}
	public boolean hasPhysicalButtons(){return false;}
}
