package xtvapps.core.android;

import java.io.IOException;

public interface ContentResolver {
	byte[] resolve(String location) throws IOException;
}
