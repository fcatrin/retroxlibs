package retrox.utils.android.vinput;

import android.app.AlertDialog;
import android.content.Context;

import retrox.utils.android.R;

public class QuitHandler {
	@SuppressWarnings("EmptyMethod")
    public static abstract class QuitHandlerCallback {
		public abstract void onQuit();
		public void onCancel() {}
    }
	
	public static void askForQuit(Context context, final QuitHandlerCallback callback) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(context);
    	builder.setTitle(context.getString(R.string.quit_title));
		builder.setMessage(context.getString(R.string.quit_msg));
		
		builder.setPositiveButton(context.getString(R.string.quit_yes), (arg0, arg1) -> {
            if (callback!=null) callback.onQuit();
        });
		builder.setNegativeButton(context.getString(R.string.quit_no), (arg0, arg1) -> {
            if (callback!=null) callback.onCancel();
        });
		builder.create().show();		
	}
}
