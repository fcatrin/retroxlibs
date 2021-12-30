package xtvapps.core;

@SuppressWarnings("EmptyMethod")
public abstract class Callback<T> {
	public abstract void onResult(T result);
	public void onError(){}
	public void onFinally(){}
}
