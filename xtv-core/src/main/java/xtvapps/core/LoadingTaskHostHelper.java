package xtvapps.core;

public class LoadingTaskHostHelper implements LoadingTaskHost {
	private final static int DETAIL_TIME = 250;
	private int stack = 0;
	private final LoadingTaskHost host;
	private long startTime = 0;
	private String lastInfo = "";

	public LoadingTaskHostHelper(LoadingTaskHost host) {
		this.host = host;
	}

	@Override
	public LocalContext getLocalContext() {
		return host.getLocalContext();
	}

	@Override
	public void showLoadingStart() {
		stack++;
		AppContext.asyncExecutor.post(new Runnable() {
			@Override
			public void run() {
				host.showLoadingStart();
			}
		});
	}

	@Override
	public void showLoadingProgress(final String info, final int progress, final int total) {
		stack = 0;
		
		if (lastInfo.equals(info) || System.currentTimeMillis() - startTime > DETAIL_TIME) {
			startTime = System.currentTimeMillis();
			lastInfo = info;
			AppContext.asyncExecutor.post(new Runnable() {
				@Override
				public void run() {
					host.showLoadingProgress(info, progress, total);
				}
			});
		}
	}

	@Override
	public void showLoadingEnd() {
		if (stack>0) stack--;
		if (stack == 0) {
			AppContext.asyncExecutor.post(new Runnable() {
				@Override
				public void run() {
					host.showLoadingEnd();
				}
			});
		}
	}

	@Override
	public void showLoadingEnd(final Exception e) {
		showLoadingEnd();
		AppContext.asyncExecutor.post(new Runnable() {
			@Override
			public void run() {
				host.showLoadingEnd(e);
			}
		});
	}

	@Override
	public void showLoadingAlert(String title, String message, SimpleCallback callback) {
		host.getLocalContext().showAlert(title, message, callback);
	}
}
