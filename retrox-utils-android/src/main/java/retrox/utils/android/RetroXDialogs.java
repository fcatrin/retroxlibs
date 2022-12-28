package retrox.utils.android;

import android.app.Activity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import retrox.utils.android.content.LoginInfo;
import retrox.utils.android.content.SaveStateInfo;
import retrox.utils.android.fileselector.FilesPanel;
import xtvapps.core.Callback;
import xtvapps.core.CoreUtils;
import xtvapps.core.ListOption;
import xtvapps.core.LocalContext;
import xtvapps.core.SimpleBackgroundTask;
import xtvapps.core.SimpleCallback;
import xtvapps.core.android.AndroidCoreUtils;
import xtvapps.core.android.AndroidFonts;
import xtvapps.core.android.AndroidLocalContext;
import xtvapps.core.android.AndroidStandardDialogs;
import xtvapps.core.content.KeyValue;
import xtvapps.vfile.VirtualFile;

public class RetroXDialogs extends AndroidStandardDialogs {
	protected static final String LOGTAG = RetroXDialogs.class.getSimpleName();
	
	private static final int DIALOG_OPENING_THRESHOLD = 800;
	private static SimpleCallback cbListDialogDismiss = null;
	private static SimpleCallback cbGamepadDialog = null;
	private static long openTimeStart = 0;
	
	public static void message(final Activity activity, String message) {
		confirm(activity, null, message, null, null, null, null);
	}
	
	public static void message(final Activity activity, String message, final SimpleCallback callback) {
		confirm(activity, null, message, null, null, callback, null);
	}

	public static void message(final Activity activity, String title, String message) {
		confirm(activity, title, message, null, null, null, null);
	}

	public static void message(final Activity activity, String title, String message, final SimpleCallback callback) {
		confirm(activity, title, message, null, null, callback, null);
	}
	
	public static void confirm(final Activity activity, String message, String optYes, String optNo, final SimpleCallback callback) {
		confirm(activity, null, message, optYes, optNo, callback, null);
	}

	public static void confirm(final Activity activity, String message, String optYes, String optNo, final SimpleCallback callbackYes, final SimpleCallback callbackNo) {
		confirm(activity, null, message, optYes, optNo, callbackYes, callbackNo);
	}

	public static void showException(final Activity activity, Exception e, SimpleCallback callback) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		Throwable cause = e.getCause();
		if (cause!=null) {
			cause.printStackTrace(pw);
		} else {
			e.printStackTrace(pw);
		}
		
		final TextView text = activity.findViewById(R.id.txtDialogAction);
		text.setTextSize(12);
			
		String msg =  e.toString() + "\n" + e.getMessage() + "\n" + sw.toString();
		message(activity, "Error", msg, callback);
	}
	
	public static void confirm(final Activity activity, String title, String message, String optYes, String optNo, final SimpleCallback callback, final SimpleCallback callbackNo) {

		activity.findViewById(R.id.modal_dialog_actions).setOnClickListener(v -> closeDialog(activity, R.id.modal_dialog_actions, new SimpleCallback(){
			@Override
			public void onResult() {
				if (callback!=null) {
					callback.onError();
					callback.onFinally();
				}
			}
		}));

		
		TextView txtMessage = activity.findViewById(R.id.txtDialogAction);
		txtMessage.setText(message);
		
		TextView txtTitle = activity.findViewById(R.id.txtDialogActionTitle);
		if (CoreUtils.isEmptyString(title)) {
			txtTitle.setVisibility(View.GONE);
		} else {
			txtTitle.setText(title);
			txtTitle.setVisibility(View.VISIBLE);
		}

		final Button btnYes = activity.findViewById(R.id.btnDialogActionPositive);
		final Button btnNo = activity.findViewById(R.id.btnDialogActionNegative);
		
		if (CoreUtils.isEmptyString(optYes)) optYes = "OK";
		
		btnYes.setText(optYes);
		
		btnYes.setOnClickListener(v -> closeDialog(activity, R.id.modal_dialog_actions, callback));
		
		final boolean hasNoButton = !CoreUtils.isEmptyString(optNo);
		
		if (hasNoButton) {
			btnNo.setVisibility(View.VISIBLE);
			btnNo.setText(optNo);
			btnNo.setOnClickListener(v -> closeDialog(activity, R.id.modal_dialog_actions, new SimpleCallback() {
				@Override
				public void onResult() {
					if (callbackNo!=null) callbackNo.onResult();
					if (callback!=null) callback.onError();

					if (callbackNo!=null) callbackNo.onFinally();
					if (callback!=null) callback.onFinally();
				}
			}));
		} else {
			btnNo.setVisibility(View.GONE);
		}
		
		openDialog(activity, R.id.modal_dialog_actions, new SimpleCallback(){
			@Override
			public void onResult() {
				Button activeButton = hasNoButton?btnNo:btnYes; 
				activeButton.setFocusable(true);
				activeButton.setFocusableInTouchMode(true);
				activeButton.requestFocus();
			}
		});
	}

	public static void custom(final Activity activity, View view,
							  String optYes, String optNo,
							  final SimpleCallback callback) {

		activity.findViewById(R.id.modal_dialog_custom).setOnClickListener(v -> closeDialog(activity, R.id.modal_dialog_custom, new SimpleCallback() {
			@Override
			public void onResult() {
				if (callback != null) {
					callback.onError();
					callback.onFinally();
				}
			}
		}));

		ViewGroup container = activity.findViewById(R.id.modal_dialog_custom_container);
		container.removeAllViews();
		container.addView(view);

		final Button btnYes = activity.findViewById(R.id.btnDialogCustomPositive);
		final Button btnNo = activity.findViewById(R.id.btnDialogCustomNegative);

		final boolean hasNoButton = !CoreUtils.isEmptyString(optNo);
		final boolean hasButtons = optYes != null || optNo != null;

		View actions = activity.findViewById(R.id.modal_dialog_custom_buttons);
		if (!hasButtons) {
			actions.setVisibility(View.GONE);
		} else {
			actions.setVisibility(View.VISIBLE);

			if (CoreUtils.isEmptyString(optYes)) optYes = "OK";

			btnYes.setText(optYes);

			btnYes.setOnClickListener(v -> closeDialog(activity, R.id.modal_dialog_custom, new SimpleCallback() {
				@Override
				public void onResult() {
					if (callback != null) {
						callback.onResult();
						callback.onFinally();
					}
				}
			}));

			if (hasNoButton) {
				btnNo.setVisibility(View.VISIBLE);
				btnNo.setText(optNo);
				btnNo.setOnClickListener(v -> closeDialog(activity, R.id.modal_dialog_custom, new SimpleCallback() {
					@Override
					public void onResult() {
						if (callback != null) callback.onError();
						if (callback != null) callback.onFinally();
					}
				}));
			} else {
				btnNo.setVisibility(View.GONE);
			}
		}

		openDialog(activity, R.id.modal_dialog_custom, new SimpleCallback() {
			@Override
			public void onResult() {
				if (hasButtons) {
					Button activeButton = hasNoButton ? btnNo : btnYes;
					activeButton.setFocusable(true);
					activeButton.setFocusableInTouchMode(true);
					activeButton.requestFocus();
				}
			}
		});
	}

	public static void custom(final Activity activity, int viewResourceId,
			Callback<View> customViewCallback, 
			final Callback<View> customViewFocusCallback, 
			String optYes, String optNo, 
			final SimpleCallback callback, 
			final SimpleCallback callbackNo) {
		
		activity.findViewById(R.id.modal_dialog_custom).setOnClickListener(v -> closeDialog(activity, R.id.modal_dialog_custom, new SimpleCallback(){
			@Override
			public void onResult() {
				if (callback!=null) {
					callback.onError();
					callback.onFinally();
				}
			}
		}));
		
		ViewGroup container = activity.findViewById(R.id.modal_dialog_custom_container);
		container.removeAllViews();
		
		LayoutInflater layoutInflater = activity.getLayoutInflater();
		View customView = layoutInflater.inflate(viewResourceId, container);
		if (customViewCallback!=null) customViewCallback.onResult(customView);

		
		final Button btnYes = activity.findViewById(R.id.btnDialogCustomPositive);
		final Button btnNo = activity.findViewById(R.id.btnDialogCustomNegative);
		
		final boolean hasNoButton = !CoreUtils.isEmptyString(optNo);
		final boolean hasButtons = optYes != null || optNo != null;
		
		View actions = activity.findViewById(R.id.modal_dialog_custom_buttons);
		if (!hasButtons) {
			actions.setVisibility(View.GONE);
		} else {
			actions.setVisibility(View.VISIBLE);
		
			if (CoreUtils.isEmptyString(optYes)) optYes = "OK";
			
			btnYes.setText(optYes);
			
			btnYes.setOnClickListener(v -> closeDialog(activity, R.id.modal_dialog_custom, new SimpleCallback() {
				@Override
				public void onResult() {
					if (callback!=null) {
						callback.onResult();
						callback.onFinally();
					}
				}
			}));
			
			if (hasNoButton) {
				btnNo.setVisibility(View.VISIBLE);
				btnNo.setText(optNo);
				btnNo.setOnClickListener(v -> closeDialog(activity, R.id.modal_dialog_custom, new SimpleCallback() {
					@Override
					public void onResult() {
						if (callbackNo!=null) callbackNo.onResult();
						if (callback!=null) callback.onError();

						if (callbackNo!=null) callbackNo.onFinally();
						if (callback!=null) callback.onFinally();
					}
				}));
			} else {
				btnNo.setVisibility(View.GONE);
			}
		}
		
		openDialog(activity, R.id.modal_dialog_custom, new SimpleCallback(){
			@Override
			public void onResult() {
				if (customViewFocusCallback!=null) {
					customViewFocusCallback.onResult(activity.findViewById(R.id.modal_dialog_custom));
				} else {
					if (hasButtons) {
						Button activeButton = hasNoButton?btnNo:btnYes; 
						activeButton.setFocusable(true);
						activeButton.setFocusableInTouchMode(true);
						activeButton.requestFocus();
					}
				}
			}
		});

	}

	
	public static void showLogin(final Activity activity, String title, String user, String password, String optLogin, String optCancel, final Callback<LoginInfo> callback) {
		
		activity.findViewById(R.id.modal_dialog_login).setOnClickListener(v -> {
			AndroidCoreUtils.hideSoftKeyboard(activity, v);
			closeDialog(activity, R.id.modal_dialog_login, new SimpleCallback(){
				@Override
				public void onResult() {
					if (callback!=null) {
						callback.onError();
						callback.onFinally();
					}
				}
			});
		});
		
		final TextView txtTitle = activity.findViewById(R.id.txtDialogLogin);
		if (CoreUtils.isEmptyString(title)) {
			txtTitle.setVisibility(View.GONE);
		} else {
			txtTitle.setText(title);
			txtTitle.setVisibility(View.VISIBLE);
		}

		final SimpleCallback wrapCallback = new SimpleCallback() {

			@Override
			public void onResult() {
				EditText txtUser = activity.findViewById(R.id.txtDialogLoginUser);
				EditText txtPass = activity.findViewById(R.id.txtDialogLoginPassword);
				
				LoginInfo loginInfo = new LoginInfo();
				loginInfo.user     = txtUser.getText().toString();
				loginInfo.password = txtPass.getText().toString();
				callback.onResult(loginInfo);
			}

			@Override
			public void onError() {
				callback.onError();
			}

			@Override
			public void onFinally() {
				AndroidCoreUtils.hideSoftKeyboard(activity, txtTitle);
				callback.onFinally();
			}

			
		};

		final Button btnYes = activity.findViewById(R.id.btnDialogLoginPositive);
		final Button btnNo = activity.findViewById(R.id.btnDialogLoginNegative);
		
		if (!CoreUtils.isEmptyString(optLogin)) {
			btnYes.setText(optLogin);
		}

		if (!CoreUtils.isEmptyString(optCancel)) {
			btnNo.setText(optCancel);
		}

		btnYes.setOnClickListener(v -> closeDialog(activity, R.id.modal_dialog_login, wrapCallback));
		
		btnNo.setOnClickListener(v -> closeDialog(activity, R.id.modal_dialog_login, new SimpleCallback() {
			@Override
			public void onResult() {
				if (callback!=null) callback.onError();
				if (callback!=null) callback.onFinally();
			}
		}));
		
		openDialog(activity, R.id.modal_dialog_login, new SimpleCallback(){
			@Override
			public void onResult() {
				activity.findViewById(R.id.txtDialogLoginUser).requestFocus();
			}
		});

	}
	
	private static void dismissGamepadDialog(Activity activity, SimpleCallback callback) {
		closeDialog(activity, R.id.modal_dialog_gamepad, callback);
	}
	
	// ingame gamepad dialog has fixed labels, and alwas calls callback on close
	public static void showGamepadDialogIngame(final Activity activity, GamepadInfoDialog dialog, final SimpleCallback callback) {
		dialog.updateGamepadVisible(activity);
		activity.findViewById(R.id.modal_dialog_gamepad).setOnClickListener(v -> dismissGamepadDialog(activity, callback));
		
		openDialog(activity, R.id.modal_dialog_gamepad, null);
	}
	
	// this is called by retrobox client:
	// * it can be cancelled
	// * if it has a callback, call it and then hide the dialog (called when starting a game)
	public static void showGamepadDialog(final Activity activity, GamepadInfoDialog dialog, String[] labels, String textTop, String textBottom, boolean hasGamepad, SimpleCallback callback) {
		cbGamepadDialog = callback;
		
		activity.findViewById(R.id.modal_dialog_gamepad).setOnClickListener(v -> {
			if (cbGamepadDialog!=null) {
				cbGamepadDialog.onResult();
				cbGamepadDialog = null;
				setDialogVisible(activity, R.id.modal_dialog_gamepad, false);
			} else {
				dismissGamepadDialog(activity, null);
			}
		});
		
				
		dialog.setLabels(labels);
		dialog.updateGamepadVisible(activity);
		
		dialog.setInfo(textTop, textBottom);
		openDialog(activity, R.id.modal_dialog_gamepad, new SimpleCallback() {
			
			@Override
			public void onResult() {
				activity.findViewById(R.id.modal_dialog_gamepad).requestFocus();
			}
		});
	}
	
	public static void select(final Activity activity, String title, List<ListOption> options, Callback<String> callback) {
		select(activity, title, new ListOptionAdapter(options), callback, null, 0);
	}

	public static void select(final Activity activity, String title, List<ListOption> options, Callback<String> callback, SimpleCallback dismissCallback) {
		select(activity, title, new ListOptionAdapter(options), callback, dismissCallback, 0);
	}

	public static void select(final Activity activity, String title, List<ListOption> options, int preSelectedItem, Callback<String> callback) {
		select(activity, title, new ListOptionAdapter(options), callback, null, preSelectedItem);
	}

	public static void select(final Activity activity, String title, final BaseAdapter adapter, Callback<String> callback) {
		select(activity, title, adapter, callback, null, 0);
	}

	public static void select(final Activity activity, String title, final BaseAdapter adapter, final Callback<String> callback, SimpleCallback callbackDismiss, final int preSelectedItem) {
		activity.findViewById(R.id.modal_dialog_list).setOnClickListener(v -> closeDialog(activity, R.id.modal_dialog_list, new SimpleCallback(){
			@Override
			public void onResult() {
				callback.onError();
				callback.onFinally();
			}
		}));

		TextView txtTitle = activity.findViewById(R.id.txtDialogListTitle);
		if (CoreUtils.isEmptyString(title)) {
			txtTitle.setVisibility(View.GONE);
		} else {
			txtTitle.setText(title);
			txtTitle.setVisibility(View.VISIBLE);
		}
		
		TextView txtInfo = activity.findViewById(R.id.txtDialogListInfo);
		txtInfo.setVisibility(View.GONE);
		
		cbListDialogDismiss = callbackDismiss;
		
		final ListView lv = activity.findViewById(R.id.lstDialogList);
		lv.setAdapter(adapter);
		
		lv.setOnItemClickListener((vadapter, v, position, id) -> {
			final KeyValue result = (KeyValue)adapter.getItem(position);
			closeDialog(activity, R.id.modal_dialog_list, new SimpleCallback() {
				@Override
				public void onResult() {
					if (callback!=null) {
						callback.onResult(result.getKey());
						callback.onFinally();
					}
				}
			});
		});

		openDialog(activity, R.id.modal_dialog_list, new SimpleCallback() {
			@Override
			public void onResult() {
		        lv.setSelection(preSelectedItem);
		        
				lv.setFocusable(true);
				lv.setFocusableInTouchMode(true);
				lv.requestFocus();
			}
		});
		
	}
	
	public static void dismissListDialog(Activity activity) {
		closeDialog(activity, R.id.modal_dialog_list, new SimpleCallback() {
			@Override
			public void onResult() {
				if (cbListDialogDismiss!=null) cbListDialogDismiss.onResult();
				cbListDialogDismiss = null;
			}
		});
	}
	
	public static boolean cancelDialog(Activity activity) {
		View dialog = getVisibleDialog(activity);
		
		if (dialog == null) return false;
		
		dialog.performClick();
		return true;
	}
	
	private static boolean isVisible(View v) {
		return v!=null && v.getVisibility() == View.VISIBLE;
	}

	private static boolean isVisible(Activity activity, int dialogResourceId) {
		View dialog = activity.findViewById(dialogResourceId);
		return dialog !=null && dialog.getVisibility() == View.VISIBLE;
	}

	private static View getVisibleDialog(Activity activity) {
		View sideBar = activity.findViewById(R.id.modal_sidebar);
		View dialogActions = activity.findViewById(R.id.modal_dialog_actions);
		View dialogList = activity.findViewById(R.id.modal_dialog_list);
		View dialogChooser = activity.findViewById(R.id.modal_dialog_chooser);
		View gamepadDialog = activity.findViewById(R.id.modal_dialog_gamepad);
		View saveStateDialog = activity.findViewById(R.id.modal_dialog_savestates);
		View loginDialog = activity.findViewById(R.id.modal_dialog_login);
		View customDialog = activity.findViewById(R.id.modal_dialog_custom);
		
		return
			isVisible(gamepadDialog)? gamepadDialog :
			isVisible(dialogActions)? dialogActions :
			isVisible(dialogList)   ? dialogList :
			isVisible(dialogChooser)   ? dialogChooser :
			isVisible(saveStateDialog) ? saveStateDialog :
			isVisible(loginDialog) ? loginDialog : 
			isVisible(customDialog) ? customDialog :
			isVisible(sideBar)      ? sideBar : null;
	}
	
	public static boolean isDialogVisible(Activity activity) {
		return getVisibleDialog(activity) != null;
	}
	
	public static boolean onKeyDown(Activity activity, int keyCode, final KeyEvent event) {
		return isVisible(activity, R.id.modal_dialog_gamepad);
	}
	
	public static boolean onKeyUp(Activity activity, int keyCode, final KeyEvent event) {
		// TODO use gamepad mappings
		//Log.v("RetroBoxDialog", "UP keyCode: " + keyCode + " elapsed " + (System.currentTimeMillis() - openTimeStart));
		if (keyCode == KeyEvent.KEYCODE_BACK && isDialogVisible(activity)) {
			
			if ((System.currentTimeMillis() - openTimeStart) < DIALOG_OPENING_THRESHOLD) {
				// ignore if this is a key up from a tap on the BACK/SELECT key
				return true;
			}
			
			if (cbGamepadDialog!=null) {
				cbGamepadDialog.onError();
			}
			
			cbGamepadDialog = null;
			cancelDialog(activity);
			return true;
		}
		
		if (isVisible(activity, R.id.modal_dialog_gamepad)) {
			cancelDialog(activity);
			return true;
		}
		return false;
	}

	
	private static void openDialog(Activity activity, int dialogResourceId, final SimpleCallback callback) {
		Animation fadeIn = new AlphaAnimation(0, 1);
		fadeIn.setInterpolator(new DecelerateInterpolator());
		fadeIn.setDuration(400);
		
		final View view = activity.findViewById(dialogResourceId);
		fadeIn.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationEnd(Animation animation) {
				view.clearAnimation();
				if (callback!=null) callback.onResult();
			}

			@Override
			public void onAnimationRepeat(Animation animation) {}

			@Override
			public void onAnimationStart(Animation animation) {}
		});
		
		openTimeStart = System.currentTimeMillis();
		view.setVisibility(View.VISIBLE);
		view.clearAnimation();
		view.startAnimation(fadeIn);
	}

	private static void closeDialog(Activity activity, int dialogResourceId, final SimpleCallback callback) {
		Animation fadeOut = new AlphaAnimation(1, 0);
		fadeOut.setInterpolator(new DecelerateInterpolator());
		fadeOut.setDuration(300);
		
		final View view = activity.findViewById(dialogResourceId);
		fadeOut.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationEnd(Animation animation) {
				view.setVisibility(View.GONE);
				if (callback!=null) {
					callback.onResult();
					callback.onFinally();
				}
			}

			@Override
			public void onAnimationRepeat(Animation animation) {}

			@Override
			public void onAnimationStart(Animation animation) {}
		});
		
		view.clearAnimation();
		view.startAnimation(fadeOut);
	}
	
	
	public static void showSaveStatesDialog(final Activity activity, String title, final SaveStateSelectorAdapter adapter, final Callback<Integer> callback) {
		OnClickListener closingClickListener = v -> closeDialog(activity, R.id.modal_dialog_savestates, new SimpleCallback(){
			@Override
			public void onResult() {
				adapter.releaseImages();
				callback.onError();
				callback.onFinally();
			}
		});
		
		activity.findViewById(R.id.modal_dialog_savestates).setOnClickListener(closingClickListener);
		activity.findViewById(R.id.btnSaveStateCancel).setOnClickListener(closingClickListener);

		AndroidFonts.setViewFont(activity.findViewById(R.id.txtDialogSaveStatesTitle), RetroXUtils.FONT_DEFAULT_B);
		AndroidFonts.setViewFont(activity.findViewById(R.id.txtDialogSaveStatesInfo), RetroXUtils.FONT_DEFAULT_M);
		AndroidFonts.setViewFont(activity.findViewById(R.id.txtDialogSaveStatesSlot), RetroXUtils.FONT_DEFAULT_M);
		AndroidFonts.setViewFont(activity.findViewById(R.id.btnSaveStateCancel), RetroXUtils.FONT_DEFAULT_M);
		
		TextView txtTitle = activity.findViewById(R.id.txtDialogSaveStatesTitle);
		if (CoreUtils.isEmptyString(title)) {
			txtTitle.setVisibility(View.GONE);
		} else {
			txtTitle.setText(title);
			txtTitle.setVisibility(View.VISIBLE);
		}
		
		final TextView txtSlot = activity.findViewById(R.id.txtDialogSaveStatesSlot);
		final TextView txtInfo = activity.findViewById(R.id.txtDialogSaveStatesInfo);
		final Callback<Integer> onSelectCallback = new Callback<Integer>(){

			@Override
			public void onResult(Integer index) {
				System.out.println("show info " + index);
				if (index >= 0 && index < adapter.getCount()) {
					SaveStateInfo info = (SaveStateInfo)adapter.getItem(index);
					txtSlot.setText(info.getSlotInfo());
					txtInfo.setText(info.getInfo());
				} else {
					txtInfo.setText("");
				}
			}};

		final GridView grid = activity.findViewById(R.id.savestates_grid);
		grid.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View view, int index, long arg3) {
				System.out.println("on item selected " + index);
				onSelectCallback.onResult(index);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				System.out.println("on nothing selected ");
				onSelectCallback.onResult(-1);
			}
		});
		
		grid.setOnFocusChangeListener((v, hasFocus) -> {
			System.out.println("on onFocusChange " + hasFocus);
			if (!hasFocus) {
				onSelectCallback.onResult(-1);
			} else {
				int selected = grid.getSelectedItemPosition();
				onSelectCallback.onResult(selected);
			}
		});
		
		grid.setOnItemClickListener((arg0, arg1, index, arg3) -> {
			System.out.println("on click listener");
			callback.onResult(index);
		});
		
		grid.setOnItemLongClickListener((arg0, arg1, index, arg3) -> {
			System.out.println("on long click listener");
			onSelectCallback.onResult(index);
			return true;
		});
		
		SimpleBackgroundTask task = new SimpleBackgroundTask() {
			@Override
			public void onBackgroundTask() throws Exception {
				adapter.loadImages();
			}

			@Override
			public void onSuccess() {
				final GridView grid = activity.findViewById(R.id.savestates_grid);
				grid.setAdapter(adapter);
				grid.setSelection(getSelectedSaveState(adapter));

				openDialog(activity, R.id.modal_dialog_savestates, new SimpleCallback() {
					@Override
					public void onResult() {
						grid.requestFocus();
					}
				});

			}
		};

		task.execute();
	}
	
	private static int getSelectedSaveState(SaveStateSelectorAdapter adapter) {
		for(int i=0; i<adapter.getCount(); i++) {
			SaveStateInfo info = (SaveStateInfo)adapter.getItem(i);
			if (info.isSelected()) {
				return i;
			}
		}
		return 0;
	}

	

	public static void showSidebar(final Activity activity, final BaseAdapter mainAdapter, final BaseAdapter secondaryAdapter, final Callback<KeyValue> callback) {
		
		final ListView lv = activity.findViewById(R.id.lstSidebarList);
		lv.setAdapter(mainAdapter);
		
		lv.setOnItemClickListener((vadapter, v, position, id) -> {
			dismissSidebar(activity);

			KeyValue result = (KeyValue)mainAdapter.getItem(position);
			callback.onResult(result);
		});
		
		final ListView lvBottom = activity.findViewById(R.id.lstSidebarBottom);
		lvBottom.setAdapter(secondaryAdapter);

		lvBottom.setOnItemClickListener((vadapter, v, position, id) -> {
			dismissSidebar(activity);

			KeyValue result = (KeyValue)secondaryAdapter.getItem(position);
			callback.onResult(result);
		});
		
		activity.findViewById(R.id.modal_sidebar).setOnClickListener(v -> dismissSidebar(activity));

		
		lv.setOnFocusChangeListener((v, hasFocus) -> {
			if (!hasFocus) {
				lv.setItemChecked(-1, false);
			}
		});

		lvBottom.setOnFocusChangeListener((v, hasFocus) -> {
			if (!hasFocus) {
				lvBottom.setItemChecked(-1, false);
			}
		});
		
		openSidebar(activity, lv);
		
	}
	
	private static void openSidebar(Activity activity, final ListView lv) {
		setDialogVisible(activity, R.id.modal_sidebar, true);
		View sidebar = activity.findViewById(R.id.sidebar);
		
		AnimationSet animationSet = new AnimationSet(true);
		animationSet.setInterpolator(new DecelerateInterpolator());
		animationSet.setDuration(500);
		
		Animation fadeIn = new AlphaAnimation(0.25f, 1);

		TranslateAnimation animation = new TranslateAnimation(-sidebar.getWidth(), 0, 0, 0);
		
		animationSet.addAnimation(fadeIn);
		animationSet.addAnimation(animation);
		
		sidebar.setAnimation(animationSet);
		
		animationSet.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {}
			
			@Override
			public void onAnimationRepeat(Animation animation) {}
			
			@Override
			public void onAnimationEnd(Animation animation) {
		        lv.setSelection(0);
		        
				lv.setFocusable(true);
				lv.setFocusableInTouchMode(true);
				lv.requestFocus();
			}
		});
		
		sidebar.startAnimation(animationSet);
		
	}

	public static void dismissSidebar(final Activity activity) {
		View sidebar = activity.findViewById(R.id.sidebar);
		
		AnimationSet animationSet = new AnimationSet(true);
		animationSet.setInterpolator(new DecelerateInterpolator());
		animationSet.setDuration(300);
		
		Animation fadeIn = new AlphaAnimation(1, 0);

		TranslateAnimation animation = new TranslateAnimation(0, -sidebar.getWidth(), 0, 0);
		
		animationSet.addAnimation(fadeIn);
		animationSet.addAnimation(animation);
		
		animationSet.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {}
			
			@Override
			public void onAnimationRepeat(Animation animation) {}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				setDialogVisible(activity, R.id.modal_sidebar, false);
			}
		});
		
		sidebar.startAnimation(animationSet);
	}

	private static void setDialogVisible(Activity activity, int id, boolean visible) {
		activity.findViewById(id).setVisibility(visible?View.VISIBLE:View.INVISIBLE);
	}

	@Override
	public void confirm(LocalContext context, String title, String message, String optYes, String optNo, SimpleCallback callback) {
		if (context instanceof AndroidLocalContext) {
			Activity activity = ((AndroidLocalContext)context).getActivity();
			RetroXDialogs.confirm(activity, title, message, optYes, optNo, callback, null);
		}
	}

	@Override
	public void message(LocalContext context, String title, String message, SimpleCallback callback) {
		if (context instanceof AndroidLocalContext) {
			Activity activity = ((AndroidLocalContext)context).getActivity();
			RetroXDialogs.message(activity, title, message, callback);
		}
	}

	@Override
	public void select(LocalContext context, String title, List<ListOption> options, Callback<String> callback, SimpleCallback dismissCallback) {
		if (context instanceof AndroidLocalContext) {
			Activity activity = ((AndroidLocalContext)context).getActivity();
			RetroXDialogs.select(activity, title, options, callback, dismissCallback);
		}
	}

	public static class FileChooserConfig {
		public String title;
		public VirtualFile initialDir;
		public List<String> matchList;
		public Callback<VirtualFile> callback;
		public Callback<VirtualFile> browseCallback;
		public boolean isDirOnly;
		public boolean isDirOptional;
	}
	
	public static void showFileChooserDialog(final Activity activity, final VirtualFile sysRoot, final FileChooserConfig config) {
		final Callback<VirtualFile> listCallback = new Callback<VirtualFile>() {
			@Override
			public void onResult(final VirtualFile result) {
				closeDialog(activity, R.id.modal_dialog_chooser, new SimpleCallback() {
					@Override
					public void onResult() {
						config.callback.onResult(result);
						config.callback.onFinally();
					}
				});
			}
			@Override
			public void onError() {
				closeDialog(activity, R.id.modal_dialog_chooser, new SimpleCallback(){
					@Override
					public void onResult() {
						config.callback.onError();
						config.callback.onFinally();
					}
				});
			}
		};
		
		activity.findViewById(R.id.modal_dialog_chooser).setOnClickListener(v -> listCallback.onError());

		TextView txtTitle = activity.findViewById(R.id.txtDialogChooserTitle);
		if (CoreUtils.isEmptyString(config.title)) {
			txtTitle.setVisibility(View.GONE);
		} else {
			txtTitle.setText(config.title);
			txtTitle.setVisibility(View.VISIBLE);
		}

		SimpleBackgroundTask task = new SimpleBackgroundTask() {
			@Override
			public void onBackgroundTask() throws Exception {
				if (config.initialDir == null) return;
				try {
					if (config.initialDir.exists()) return;
				} catch (IOException e) {
					e.printStackTrace();
				}
				config.initialDir = null;
			}

			@Override
			public void onSuccess() {
				final ListView lv = activity.findViewById(R.id.lstDialogChooser);

				TextView txtStatus1 = activity.findViewById(R.id.txtPanelStatus1);
				TextView txtStatus2 = activity.findViewById(R.id.txtPanelStatus2);

				TextView  txtStorage = activity.findViewById(R.id.txtStorage);
				ImageView imgStorage = activity.findViewById(R.id.imgStorage);

				FilesPanel filesPanel = new FilesPanel(activity, sysRoot, lv, txtStorage, imgStorage,
						txtStatus1, txtStatus2, listCallback, config);
				filesPanel.refresh();

				openDialog(activity, R.id.modal_dialog_chooser, new SimpleCallback() {
					@Override
					public void onResult() {
						if (lv.getChildCount()>0) {
							lv.setSelection(0);
						}

						lv.setFocusable(true);
						lv.setFocusableInTouchMode(true);
						lv.requestFocus();
					}
				});
			}
		};
		task.execute();
	}

}
