package xtvapps.core;

@SuppressWarnings({"SameReturnValue", "unused"})
public abstract class LoadingTask<T> extends BackgroundTask<T> {
    private static final String LOGTAG = LoadingTask.class.getSimpleName();
    private final String onSuccessMessage;
    private final String onFailureMessage;
    private final ProgressType progressType;
    private Exception exception = null;

    @SuppressWarnings("unused")
    public enum ProgressType {Progressive, Infinite}

    private final LoadingTaskHost host;

    public LoadingTask(LoadingTaskHost loadingTaskHost) {
        this(loadingTaskHost, null, null, null);
    }

    public LoadingTask(LoadingTaskHost loadingTaskHost, String onSuccessMessage, String onFailureMessage) {
        this(loadingTaskHost, onSuccessMessage, onFailureMessage, null);
    }

    public LoadingTask(LoadingTaskHost loadingTaskHost, String onSuccessMessage, String onFailureMessage, ProgressType progressType) {
        this.host = loadingTaskHost;
        this.onSuccessMessage = onSuccessMessage;
        this.onFailureMessage = onFailureMessage;
        this.progressType = progressType;
    }

    @Override
    public final T onBackground() {
        if (progressType == ProgressType.Infinite) host.showLoadingStart();
        try {
            T result = onBackground(host);
            if (progressType == ProgressType.Infinite) host.showLoadingEnd();
            return result;
        } catch (Exception e) {
            exception = e;
            Log.e(LOGTAG, "Error processing background task", e);
            host.showLoadingEnd(onFailureMessage != null ? null : e);
            return null;
        }
    }

    protected abstract T onBackground(LoadingTaskHost host) throws Exception;
    protected abstract void onSuccess(LoadingTaskHost host, T result);

    @Override
    public final void onSuccess(T result) {
        if (exception == null) {
            onSuccess(host, result);
            if (onSuccessMessage!=null) host.getLocalContext().toast(onSuccessMessage);
        } else {
            if (onFailureMessage!=null) {
                String msg = onFailureMessage.replace("{error}", exception.getMessage() + "");
                host.showLoadingAlert("Error", msg, new SimpleCallback(){
                    @Override
                    public void onResult() {
                        onFailure(exception);
                    }
                });
            } else onFailure(exception);
        }
        onFinally();
    }
}
