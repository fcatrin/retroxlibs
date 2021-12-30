package xtvapps.core.cache;

import xtvapps.core.LocalContext;

public class NetworkCache extends DiskCache<byte[]> {

	public NetworkCache(LocalContext context, String location, int size) {
		super(context, location, size, 10);
	}

	@Override
	protected byte[] fromBytes(byte[] b) {
		return b;
	}

	@Override
	protected byte[] toBytes(byte[] item) {
		return item;
	}

}
