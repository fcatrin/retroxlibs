package xtvapps.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncExecutor {
    private static final String LOGTAG = AsyncExecutor.class.getSimpleName();
    private final ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
    private final UIThreadExecutor uiThreadExecutor;

    public AsyncExecutor(UIThreadExecutor uiThreadExecutor) {
        this.uiThreadExecutor = uiThreadExecutor;
    }

    public <T> void execute(final BackgroundTask<T> task) {
        execute(task, null, null, null);
    }

    public <T> void execute(BackgroundTask<T> task, LocalContext context, String successMessage, String failureMessage) {
        execute(task, context, successMessage, failureMessage, null);
    }

    public <T> void execute(final BackgroundTask<T> task, ExecutorService executorService) {
        execute(task, null, null, null, executorService);
    }

    private <T> void execute(final BackgroundTask<T> task, final LocalContext context, final String successMessage, final String failureMessage, ExecutorService executorService) {
        if (executorService == null) executorService = singleThreadExecutor;

        executorService.execute(new Runnable() {
            T result = null;
            Exception exception = null;

            @Override
            public void run() {
                try {
                    result = task.onBackground();
                } catch (Exception e) {
                    exception = e;
                    Log.e(LOGTAG, "Error processing background task", e);
                }

                post(new Runnable() {
                    @Override
                    public void run() {
                        if (exception == null) {
                            task.onSuccess(result);
                            if (successMessage != null) context.toast(successMessage);
                        } else {
                            task.onFailure(exception);
                            if (failureMessage != null) context.showAlert(failureMessage);
                        }
                        task.onFinally();
                    }
                });
            }
        });
    }

    public void post(Runnable runnable) {
        uiThreadExecutor.post(runnable);
    }

    public void post(Runnable runnable, long ms) {
        uiThreadExecutor.post(runnable, ms);
    }

}
