package retrobox.fileselector;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import retrobox.utils.R;
import retrobox.utils.RetroBoxDialog;
import retrobox.utils.RetroBoxDialog.FileChooserConfig;
import retrobox.utils.RetroBoxUtils;
import retrobox.utils.ThreadedBackgroundTask;
import xtvapps.core.Callback;
import xtvapps.core.UserVisibleException;
import xtvapps.vfile.VirtualFile;

public class FilesPanel {
	
	protected static final String LOGTAG = null;
	ListView lv;
	private boolean busy;
	private TextView txtPanelStatus1;
	private TextView txtPanelStatus2;
	private Activity activity;
	private TextView txtStorage;
	private ImageView iconStorage;
	private VirtualFile currentStorage;
	private VirtualFile currentFolder;
	protected boolean focused = false;
	private VirtualFile currentParent;
	private VirtualFile sysRoot;
	private FileChooserConfig config;
	
	class FolderInfo {
		List<VirtualFile> list;
		long freeSpace;
		long totalSpace;
		public int nElements;
		String error;
		Exception e;
	}
	
	public FilesPanel(Activity activity, VirtualFile sysRoot, ListView listView, TextView txtStorage, ImageView iconStorage, TextView txtPanelStatus1, TextView txtPanelStatus2,
			final Callback<VirtualFile> openCallback, final FileChooserConfig config) {
		this.activity = activity;
		this.lv = listView;
		this.txtStorage = txtStorage;
		this.iconStorage = iconStorage;
		this.txtPanelStatus1 = txtPanelStatus1;
		this.txtPanelStatus2 = txtPanelStatus2;
		this.sysRoot = sysRoot;
		this.config = config;
		this.currentFolder = config.initialDir;
		
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> vadapter, View v, int position, long id) {
				if (busy) return;
				
				VirtualFile vf = (VirtualFile)lv.getAdapter().getItem(position);
				Log.d(LOGTAG, "FileChooser go to " + vf);
				
				if (vf.getName().equals("_select_")) {
					openCallback.onResult(currentFolder);
					return;
				}
				
				if (vf.isDirectory()) {
					browse(vf);
				} else {
					if (!config.isDirOnly) {
						openCallback.onResult(vf);
					}
				}
			}
		});
		

		lv.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				focused = hasFocus;
			}
		});
		
	}
	
	public VirtualFile getCurrentStorage() {
		return currentStorage;
	}
	
	public VirtualFile getCurrentFolder() {
		return currentFolder;
	}

	
	public void refresh() {
		lv.setAdapter(null);
		if (config.browseCallback!=null) config.browseCallback.onResult(currentFolder);
		browse(currentFolder, lv.getSelectedItemPosition());
	}
	
	public void browse(final VirtualFile browseDir) {
		browse(browseDir, 0);
	}
	
	public void browse(final VirtualFile browseDir, final int selectItem) {
		setBusy(true);
		final VirtualFile dir = browseDir == null ? sysRoot : browseDir;
		currentFolder = dir;
		
		String path = dir.getPath();
		if (path.equals("/")) {
			path = "";
		} else {
			path = " - " + path;
		}
		
		currentStorage = dir.getStorage();
		if (currentStorage == null) currentStorage = sysRoot;
		
		txtStorage.setText(currentStorage.getFriendlyName() + path);
		iconStorage.setImageResource(currentStorage.getIconResourceId());

		List<VirtualFile> loadingList = new ArrayList<VirtualFile>();
		VirtualFile loadingFileDummy = new VirtualFile(VirtualFile.ROOT_LOCAL + VirtualFile.TYPE_SEPARATOR);
		loadingFileDummy.setIsLoading(true);
		loadingList.add(loadingFileDummy);
		FileListAdapter adapter = new FileListAdapter(loadingList);
		lv.setAdapter(adapter);
		
		RetroBoxUtils.runOnBackground(activity, new ThreadedBackgroundTask() {
			FolderInfo folderInfo;
			
			@Override
			public void onBackground() {
				folderInfo = loadDir(dir);
			}

			@Override
			public void onUIThread() {
				if (config.browseCallback!=null) config.browseCallback.onResult(dir);
				
                Log.d("FILES", "size " + folderInfo.list.size());
                long size = 0;
                for(VirtualFile file : folderInfo.list) {
                	Log.d("FILES", "file " + file);
                	size += file.getSize();
                }
                
                if (currentFolder.getHandler().hasElements()) {
	                int nElements = folderInfo.nElements;
	                
	                String strElements = nElements == 0 ? activity.getString(R.string.folder_elements_0) :
	                	(nElements == 1 ? 
	                			activity.getString(R.string.folder_elements_1) : 
	                			activity.getString(R.string.folder_elements_n).replace("{n}",String.valueOf(nElements)));
	                
	                if (size>0) {
	                	strElements += " / " + RetroBoxUtils.size2human(size);
	                }
	                
	                txtPanelStatus1.setText(strElements);
                } else {
                	txtPanelStatus1.setText("");
                }
				
                long free = folderInfo.freeSpace;
                long total = folderInfo.totalSpace;
                
                if (total>0) {
                	String freeHuman  = RetroBoxUtils.size2human(free);
                	String totalHuman = RetroBoxUtils.size2human(total);
                	int percentFree = (int)(free * 100.0f / total);
                	txtPanelStatus2.setText(
                			activity.getString(R.string.folder_free)
                			.replace("{free}", freeHuman)
                			.replace("{total}", totalHuman)
                			.replace("{percent}", String.valueOf(percentFree)));
                } else {
                	txtPanelStatus2.setText("");
                }
                
				FileListAdapter adapter = new FileListAdapter(folderInfo.list);
				lv.setAdapter(adapter);
				setBusy(false);
				
				int itemToSelect = selectItem;
				if (itemToSelect >= folderInfo.list.size()) {
					itemToSelect = folderInfo.list.size() - 1;
				}
				if (itemToSelect>=0) {
					lv.setSelection(itemToSelect);
				}
				
				lv.requestFocus();
				
				if (folderInfo.error != null) {
					RetroBoxDialog.showAlert(activity, folderInfo.error);
				} else if (folderInfo.e != null) {
					RetroBoxDialog.showException(activity, folderInfo.e, null);
				}
			}
		});
	}

	
	private FolderInfo loadDir(VirtualFile dir) {
		Log.d(LOGTAG, "Loading dir for " + dir);
		List<VirtualFile> list = new ArrayList<VirtualFile>();

		VirtualFile parent = dir.getParent();
		if (parent!=null) {
			parent.setIsParent(true);
			parent.setFriendlyName(activity.getString(R.string.folder_parent));
			list.add(parent);
		}

		currentParent = parent;

		if ((config.isDirOnly || config.isDirOptional) && !dir.isStorage() && !dir.getPath().equals("/")) {
			VirtualFile vFileSelectFolder = new VirtualFile(dir, "_select_");
			vFileSelectFolder.setFriendlyName(activity.getString(R.string.folder_select));
			vFileSelectFolder.setIconResourceId(R.drawable.ic_label_outline_white_36dp);
			vFileSelectFolder.setIsDirectory(true);
			list.add(vFileSelectFolder);
		}
		
		FolderInfo folderInfo = new FolderInfo();
		try {
			List<VirtualFile> tmpList = dir.list();
			
			if (dir.canSort()) {
				Collections.sort(tmpList, new Comparator<VirtualFile>() {
					@Override
					public int compare(VirtualFile lhs, VirtualFile rhs) {
						if (lhs.isDirectory() && !rhs.isDirectory()) {
							return -1;
						}
						if (!lhs.isDirectory() && rhs.isDirectory()) {
							return 1;
						}
						return lhs.getName().toLowerCase(Locale.US).compareTo(rhs.getName().toLowerCase(Locale.US));
					}
				});
			}
			
			for(int i=0; i<tmpList.size(); i++) {
				VirtualFile file = tmpList.get(i);
				//int icon = file.isDirectory() ? R.drawable.ic_folder_white_36dp : R.drawable.ic_insert_drive_file_grey600_36dp;
				//icon = SystemRootHandler.isMount(file) ? R.drawable.ic_sd_storage_white_36dp : icon;
				//icon = file.canRead() ? icon : R.drawable.ic_block_grey600_36dp;
				//file.setIconResourceId(icon);
				list.add(file);
			}
			folderInfo.freeSpace = dir.getFreeSpace();
			folderInfo.totalSpace = dir.getTotalSpace();
			folderInfo.nElements = tmpList.size();
		} catch (Exception e) {
			if (e instanceof UserVisibleException) {
				folderInfo.error = e.getMessage();
			} else {
				folderInfo.e = e;
			}
		}
		
		folderInfo.list = list;
		return folderInfo;
	}
	
	private void setBusy(boolean busy) {
		this.busy = busy;
	}

	public void requestFocus() {
		lv.requestFocus();
	}

	public boolean isFocused() {
		return focused;
	}

	public boolean onBackPressed() {
		if (busy) return false;
		
		if (lv.getSelectedItemPosition()>0) {
			lv.setSelection(0);
			return true;
		}
		
		if (currentParent!=null) {
			browse(currentParent);
			return true;
		}
		
		return false;
	}
}
