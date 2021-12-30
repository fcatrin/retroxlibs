package xtvapps.core;

import java.io.File;

@SuppressWarnings({"EmptyMethod", "unused"})
public abstract class DownloadListener {
	public abstract void onDownload(File f);
	public void onFail(Exception e) {}
}
