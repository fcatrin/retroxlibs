package retrox.utils.android;

import java.io.File;

import androidx.annotation.NonNull;
import xtvapps.core.CoreUtils;

public class MountPoint {
	File dir;
	String description;
	String filesystem = "unknown";
	boolean isWritable = false;
	
	public MountPoint(String path) {
		this.dir = new File(path);
	}
	
	public MountPoint(File dir) {
		this.dir = dir;
	}

	public MountPoint() {
	}

	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public boolean isValid() {
		return dir!=null;
	}

	public long getFreeSpace() {
		return dir==null?0:dir.getFreeSpace();
	}
	
	public long getTotalSpace() {
		return dir==null?0:dir.getTotalSpace();
	}

	public File getDir() {
		return dir;
	}

	public String getFilesystem() {
		return filesystem;
	}

	public void setFilesystem(String filesystem) {
		this.filesystem = filesystem;
	}

	public String getFriendlyFreeSpace() {
		return CoreUtils.size2humanDetailed(getFreeSpace()) + " free of " + CoreUtils.size2humanDetailed(getTotalSpace());
	}
	
	
	public boolean isWritable() {
		return isWritable;
	}

	public void setWritable(boolean isWritable) {
		this.isWritable = isWritable;
	}

	@NonNull
	@Override
	public String toString() {
		return String.format("path:%s, desc:%s", dir!=null?dir.getAbsolutePath():"null", description);
	}
	
}
