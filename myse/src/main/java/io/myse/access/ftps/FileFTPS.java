package io.myse.access.ftps;

import io.myse.access.AccessException;
import io.myse.access.File;
import io.myse.access.Link;
import io.myse.access.LinkContext;
import io.myse.access.Source;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilters;
import org.apache.commons.net.ftp.FTPSClient;

public class FileFTPS extends File {

	private final SourceFTPS source;
	private final FTPFile file;
	private final String path;

	FileFTPS(String path, SourceFTPS source) {
		this.path = path;
		this.source = source;
		this.file = new FTPFile();
		this.file.setName(".");
		this.file.setTimestamp(Calendar.getInstance());
	}

	FileFTPS(String path, FTPFile file, SourceFTPS source) {
		this.path = path;
		this.file = file;
		this.source = source;
	}

	@Override
	public boolean isDirectory() throws AccessException {
		return file.isDirectory() || path.equals("/");
	}

	@Override
	public boolean exists() throws AccessException {
		return file.isFile() || isDirectory();
	}

	@Override
	public Date getLastModified() throws AccessException {
		return file.getTimestamp().getTime();
	}

	@Override
	public List<File> listFiles() throws AccessException {
		try {
			List<File> list = new ArrayList<>();
			FTPSClient client = source.getClient();
			client.changeWorkingDirectory(path);
			for (FTPFile f : client.listFiles(path, FTPFileFilters.ALL)) {
				String newPath = path;
				if (!newPath.endsWith("/")) {
					newPath += "/";
				}
				newPath += f.getName();

				list.add(new FileFTPS(newPath, f, source));
			}
			return list;
		} catch (IOException ex) {
			throw source.convertException(ex);
		}
	}

	@Override
	public Source getSource() {
		return source;
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public InputStream getInputStream() throws AccessException {
		try {
			return source.client.retrieveFileStream(path);
		} catch (IOException ex) {
			throw source.convertException(ex);
		}
	}

	@Override
	public long getSize() throws AccessException {
		return file.getSize();
	}

	@Override
	public Link getLink(LinkContext context) {
		return null;
	}

}
