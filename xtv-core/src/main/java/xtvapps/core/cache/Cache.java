package xtvapps.core.cache;

public interface Cache<T> {
	T get(String s);
	void put(String s, T entry);
	void flush();
}
