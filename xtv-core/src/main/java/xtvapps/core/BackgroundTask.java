package xtvapps.core;

import java.util.concurrent.ExecutorService;

@SuppressWarnings("EmptyMethod")
public abstract class BackgroundTask<T> {
    public abstract T onBackground() throws Exception;
    public abstract void onSuccess(T result);

    @SuppressWarnings("unused")
    public void onFailure(Exception e) {}
    public void onFinally() {}

    public void executeMulti(ExecutorService executor) {
        AppContext.asyncExecutor.execute(this, executor);
    }
    public void execute() {
        AppContext.asyncExecutor.execute(this);
    }

    public void execute(LocalContext context, String successMessage, String failureMessage) {
        AppContext.asyncExecutor.execute(this, context, successMessage, failureMessage);
    }
}
