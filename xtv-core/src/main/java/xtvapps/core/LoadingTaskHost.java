package xtvapps.core;

public interface LoadingTaskHost {
	LocalContext getLocalContext();
	void showLoadingStart();
	void showLoadingProgress(String info, int progress, int total);
	void showLoadingEnd();
	void showLoadingEnd(Exception e);
	void showLoadingAlert(String title, String message, SimpleCallback callback);
}
