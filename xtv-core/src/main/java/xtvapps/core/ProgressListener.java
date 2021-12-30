package xtvapps.core;

@SuppressWarnings({"EmptyMethod", "SameReturnValue"})
public abstract class ProgressListener {
	public abstract boolean update(int progress, int max);
	public void onStart() {}
	public void onEnd() {}
	@SuppressWarnings("unused")
	public int getBufferSize(int max) {
		return 0;
	}
}
