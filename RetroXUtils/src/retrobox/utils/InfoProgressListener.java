package retrobox.utils;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import xtvapps.core.NetworkProgressListener;
import xtvapps.core.Utils;

public class InfoProgressListener extends NetworkProgressListener {
	private final static int DETAIL_TIME = 250;
	
	public static boolean operationIsCancelled = false;
	
	String infoTemplate;
	String lastInfoTemplate = "";
	private TextView textViewInfo;
	private TextView textViewProgress;
	private ProgressBar progressView;
	
	private long lastUpdateTime = 0;
	
	public InfoProgressListener(TextView textViewInfo, TextView textViewProgress, ProgressBar progressView) {
		this.textViewInfo     = textViewInfo;
		this.textViewProgress = textViewProgress;
		this.progressView      = progressView;
	}
	
	public void setInfoTemplate(String infoTemplate) {
		this.infoTemplate = infoTemplate;
	}
	
	private void updateInfo(int progress, int total) {
		String info = new String(infoTemplate); 
		if (info.endsWith("{size}")) {
			String sizeInfo  = "";
			info = info.replace("{size}", "");
			if (total == 0) {
				sizeInfo = "";
			} else {
				sizeInfo = Utils.size2humanDetailed(progress) + " / " + Utils.size2humanDetailed(total);
				if (textViewProgress == null) {
					info += "    ( " + sizeInfo + " )";
				}
			}
			if (textViewProgress!=null) textViewProgress.setText(sizeInfo);
		} else if (info.endsWith("{count}")) {
			String countInfo = "";
			info = info.replace("{count}", "");
			if (total == 0) {
				countInfo = "";
			} else {
				countInfo = (progress+1) + " / " + total;
				if (textViewProgress == null) {
					info += "  ( " + countInfo + " )";	
				}
			}
			if (textViewProgress!=null) textViewProgress.setText(countInfo);
		}
		textViewInfo.setText(info);
		progressView.setProgress(progress);
		progressView.setMax(total);
		textViewInfo.setVisibility(View.VISIBLE);
		progressView.setVisibility(View.VISIBLE);
	}
	
	@Override
	public boolean updateProgress(final int progress, final int max) {
		if (!lastInfoTemplate.equals(infoTemplate) || System.currentTimeMillis() - lastUpdateTime > DETAIL_TIME) {
			lastUpdateTime = System.currentTimeMillis();
			lastInfoTemplate = infoTemplate;
			progressView.post(new Runnable() {
				@Override
				public void run() {
					updateInfo(progress, max);
				}
			});
		}		
		return operationIsCancelled;
	}
}
