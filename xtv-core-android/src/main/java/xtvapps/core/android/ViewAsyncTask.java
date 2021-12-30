package xtvapps.core.android;

import java.lang.ref.WeakReference;

import xtvapps.core.BackgroundTask;

public abstract class ViewAsyncTask<V, T> extends BackgroundTask<T> {
	private final WeakReference<V> viewReference;
	
	public ViewAsyncTask(V v){
		viewReference = new WeakReference<>(v);
	}
	
	protected V getView() {
		return viewReference.get();
	}
	
}
