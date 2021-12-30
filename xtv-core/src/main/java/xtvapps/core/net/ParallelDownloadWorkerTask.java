package xtvapps.core.net;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import xtvapps.core.AppContext;
import xtvapps.core.BackgroundTask;

@SuppressWarnings("unused")
public abstract class ParallelDownloadWorkerTask extends BackgroundTask<byte[]> {
	private static final String LOGTAG = ParallelDownloadWorkerTask.class.getSimpleName();
	private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(16);
	private final String location;

	public ParallelDownloadWorkerTask(String location) {
		this.location = location;
	}

	@Override
	public byte[] onBackground() throws Exception {
		return NetworkUtils.httpGet(location);
	}

	@Override
	public void execute() {
		AppContext.asyncExecutor.execute(this, EXECUTOR);
	}
}
