package xtvapps.vfile;

import java.io.IOException;
import java.util.List;

@SuppressWarnings("RedundantThrows")
public interface VirtualFileHandler {
	List<VirtualFile> list(VirtualFile virtualFile) throws IOException;
	void get(VirtualFile srcFile, VirtualFile dstFolder, VirtualFileOperationProgressListener listener) throws IOException;
	void put(VirtualFile srcFile, VirtualFile dstFolder, VirtualFileOperationProgressListener listener) throws IOException;
	void mkdir(VirtualFile virtualFile) throws IOException;
	void delete(VirtualFile virtualFile) throws IOException;
	boolean canSort(VirtualFile virtualFile);
	VirtualFile getParentStorage(VirtualFile virtualFile);
	long getFreeSpace(VirtualFile virtualFile);
	long getTotalSpace(VirtualFile virtualFile) throws IOException;
	VirtualFile getStorage(VirtualFile virtualFile);
	boolean supportsInnerCopy();
	boolean hasElements();
	void copyOrMove(VirtualFile srcFile, VirtualFile dstFolder, VirtualFileOperationProgressListener progressListener, boolean move) throws IOException;
	boolean exists(VirtualFile virtualFile) throws IOException;
	void rename(VirtualFile virtualFile, String newName) throws IOException;
	List<VirtualFile> listTree(VirtualFile virtualFile) throws IOException;
	void stat(VirtualFile virtualFile) throws IOException;
}
