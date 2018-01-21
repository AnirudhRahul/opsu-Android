package fluddokt.ex;

import fluddokt.opsu.fake.File;

public class DeviceInfo {
	public static DeviceInfo info = new DeviceInfo();
	public String getInfo() {
		return "";
	}
	public File getDownloadDir() {
		return null;
	}
	public boolean isMusicPlaying(){return false;}
	public void saveName(String name){}
}
