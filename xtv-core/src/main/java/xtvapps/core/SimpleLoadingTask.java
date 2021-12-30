package xtvapps.core;

@SuppressWarnings({"RedundantThrows", "unused", "RedundantSuppression"})
public abstract class SimpleLoadingTask extends LoadingTask<Void> {

	public SimpleLoadingTask(LoadingTaskHost host, String onSuccessMessage, String onFailureMessage) {
		super(host, onSuccessMessage, onFailureMessage);
	}

	@Override
	protected Void onBackground(LoadingTaskHost host) throws Exception {
		onBackgroundTask();
		return null;
	}

	@Override
	protected void onSuccess(LoadingTaskHost host, Void result) {
		onSuccess();
	}

	public abstract void onBackgroundTask() throws Exception;

	@SuppressWarnings("EmptyMethod")
	public void onSuccess() {}

}
