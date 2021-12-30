package xtvapps.core.cache;

import xtvapps.core.LocalContext;

@SuppressWarnings("unused")
public class BytesDiskCache extends DiskCache<byte[]> {

	public BytesDiskCache(LocalContext context, String location, int size, float memSize, CacheSizeDescriptor<byte[]> cacheSizeDescriptor) {
		super(context, location, size, memSize, cacheSizeDescriptor);
	}
	
	public BytesDiskCache(LocalContext context, String location, int size, int memSize) {
		super(context, location, size, memSize);
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
