package xtvapps.vfile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CachedFolder {
	private static final long VALID_TIME = 129000; // 129[s]
	
	final Map<String, VirtualFile> vFilesMap = new HashMap<>();
	final List<VirtualFile> vFiles;
	
	long lastAccessed;

	public CachedFolder(List<VirtualFile> vFiles) {
		this.vFiles = vFiles;
		for(VirtualFile vFile : vFiles) {
			this.vFilesMap.put(vFile.getName(), vFile);
		}
		lastAccessed = System.currentTimeMillis();
	}
	
	public List<VirtualFile> getList() {
		lastAccessed = System.currentTimeMillis();
		return vFiles;
	}
	
	public boolean isValid() {
		return System.currentTimeMillis() - lastAccessed < VALID_TIME;
	}
	
	public VirtualFile get(String name) {
		lastAccessed = System.currentTimeMillis();
		return vFilesMap.get(name);
	}

}
