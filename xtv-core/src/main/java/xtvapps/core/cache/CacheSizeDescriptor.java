package xtvapps.core.cache;

public interface CacheSizeDescriptor<T> {
	int getSize(T item);
}
