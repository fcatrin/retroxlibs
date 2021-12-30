package xtvapps.core.tonido;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import xtvapps.core.CoreUtils;
import xtvapps.core.LocalContext;
import xtvapps.core.cache.EntriesCache;
import xtvapps.core.cache.EntriesCache.Type;
import xtvapps.core.xml.ParserException;

public class TonidoFile {
	String user;
	String password;
	String server;
	String resource;
	
	boolean isDirectory;
	long modified;
	long size;
	
	private static final EntriesCache<TonidoFile> lastKnownFiles = new EntriesCache<>(50, Type.LRU);
	
	public TonidoFile(String filespec) {
		filespec = filespec.replace("tonido://", "");
		
		int p = filespec.indexOf("@");
		if (p>=0) {
			String authInfo = filespec.substring(0, p);
			filespec = filespec.substring(p+1);
			
			String[] authParts = authInfo.split(":");
			user = authParts[0];
			if (authParts.length>1) {
				password = authParts[1];
			}
		}
		
		p = filespec.indexOf("/");
		if (p>=0) {
			server = filespec.substring(0, p);
			resource = filespec.substring(p);
		} else {
			server = filespec;
			resource = "";
		}
		isDirectory = CoreUtils.isEmptyString(resource);
		
	}
	
	private String getUrl(boolean isPrivate) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("tonido://");
		if (user!=null && isPrivate) {
			buffer.append(user);
			if (password!=null) {
				buffer.append(":");
				buffer.append(password);
			}
			buffer.append("@");
		}
		buffer.append(getCanonicalPath());
		return buffer.toString();
	}
	
	public String getCanonicalPath() {
		StringBuilder buffer = new StringBuilder();
		if (server!=null) {
			buffer.append(server);
		}
		
		if (resource!=null) {
			buffer.append(resource);
		}
		return buffer.toString();
	}
	
	public String getUrl() {
		return getUrl(false);
	}

	public String getAuthUrl() {
		return getUrl(true);
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public boolean isDirectory() {
		return isDirectory;
	}

	public void setDirectory(boolean isDirectory) {
		this.isDirectory = isDirectory;
	}

	public long getModified() {
		return modified;
	}

	public void setModified(long modified) {
		this.modified = modified;
	}

	
	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public void auth() throws IOException, ParserException {
		TonidoService.auth(server, user, password);
	}
	
	public List<TonidoFile> list(LocalContext context) throws IOException, ParserException {
		return TonidoService.list(context, this);
	}

	public TonidoFile getDirEntry(LocalContext context, String filename) throws IOException, ParserException {
		TonidoFile tonidoFile = lastKnownFiles.get(filename);
		if (tonidoFile!=null) return tonidoFile;
		
		List<TonidoFile> list = TonidoService.list(context,this);
		if (list==null) return null;
		
		for(TonidoFile file : list) {
			if (file.getName().equals(filename)) return file;
		}
		
		return null;
	}

	public InputStream open() throws IOException {
		return TonidoService.open(this);
	}
	
	public String getName() {
		int p = resource.lastIndexOf("/");
		return resource.substring(p+1);
	}
}
