package retrox.utils.android;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import xtvapps.core.CoreUtils;
import xtvapps.core.ProgressListener;

public class InfoProgressListener extends ProgressListener {
	private final static int DETAIL_TIME = 250;

	private static final String LOGTAG = InfoProgressListener.class.getSimpleName();
	
	public static final boolean operationIsCancelled = false;
	
	String infoTemplate;
	String lastInfoTemplate = "";
	private final TextView textViewInfo;
	private final TextView textViewProgress;
	private final ProgressBar progressView;
	
	private long lastUpdateTime = 0;
	
	public InfoProgressListener(TextView textViewInfo, TextView textViewProgress, ProgressBar progressView) {
		this.textViewInfo     = textViewInfo;
		this.textViewProgress = textViewProgress;
		this.progressView     = progressView;
	}
	
	public void setInfoTemplate(String infoTemplate) {
		this.infoTemplate = infoTemplate;
	}
	
	private void updateInfo(int progress, int total) {
		String info = infoTemplate;
		if (info.endsWith("{size}")) {
			String sizeInfo  = "";
			info = info.replace("{size}", "");
			if (total != 0) {
				sizeInfo = CoreUtils.size2humanDetailed(progress) + " / " + CoreUtils.size2humanDetailed(total);
				if (textViewProgress == null) {
					info += "    ( " + sizeInfo + " )";
				}
			}
			if (textViewProgress!=null) textViewProgress.setText(sizeInfo);
			progressView.setProgress(progress);
		} else if (info.endsWith("{count}")) {
			String countInfo = "";
			info = info.replace("{count}", "");
			if (total != 0) {
				countInfo = (progress+1) + " / " + total;
				if (textViewProgress == null) {
					info += "  ( " + countInfo + " )";	
				}
			}
			if (textViewProgress!=null) textViewProgress.setText(countInfo);
			progressView.setProgress(progress+1);
		} else {
			if (textViewProgress!=null) textViewProgress.setText("");
			progressView.setProgress(progress);
		}
		
		textViewInfo.setText(info);
		progressView.setMax(total);
		setVisible(View.VISIBLE);
	}
	
	public void reset() {
		progressView.post(() -> {
			progressView.setProgress(0);
			progressView.setMax(0);
			textViewInfo.setText("");
			if (textViewProgress!=null) textViewProgress.setText("");
			setVisible(View.INVISIBLE);
		});
	}
	
	public void setVisible(int visibility) {
		textViewInfo.setVisibility(visibility);
		if (textViewProgress!=null) textViewProgress.setVisibility(visibility);
		
		progressView.setVisibility(visibility);
	}
	
	@Override
	public boolean update(final int progress, final int max) {
		if (!lastInfoTemplate.equals(infoTemplate) || System.currentTimeMillis() - lastUpdateTime > DETAIL_TIME) {
			lastUpdateTime = System.currentTimeMillis();
			lastInfoTemplate = infoTemplate;
			progressView.post(() -> updateInfo(progress, max));
		}		
		return operationIsCancelled;
	}
}
