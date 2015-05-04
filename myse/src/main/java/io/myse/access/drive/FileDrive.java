package io.myse.access.drive;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.ChildList;
import com.google.api.services.drive.model.ChildReference;
import io.myse.access.AccessException;
import io.myse.access.File;
import io.myse.access.Source;
import static io.myse.common.LOG.LOG;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class FileDrive extends File {

	private final String fileId;
	private Boolean dir;
	private final SourceDrive source;

	FileDrive(String fileId, SourceDrive source) {
		this.fileId = fileId;
		this.source = source;
	}

	FileDrive(String fileId, boolean dir, SourceDrive source) {
		this.fileId = fileId;
		this.dir = dir;
		this.source = source;
	}

	FileDrive(com.google.api.services.drive.model.File f, SourceDrive self) {
		this(f.getId(), self);
		this.driveFile = f;
	}

	@Override
	public boolean isDirectory() throws AccessException {
		if (dir == null) {
			dir = TYPE_FOLDER.equals(getFile().getMimeType());
		}
		return dir;
	}

	@Override
	public Date getLastModified() throws AccessException {
		return new Date(getFile().getModifiedDate().getValue());
	}

	static final String TYPE_FOLDER = "application/vnd.google-apps.folder";
	static final String TYPE_EXCEL = "application/vnd.google-apps.spreadsheet";
	static final String TYPE_WORD = "application/vnd.google-apps.document";
	static final String TYPE_GMAP = "application/vnd.google-apps.map";
	static final String TYPE_POWERPOINT = "application/vnd.google-apps.presentation";
	static final String TYPE_FORM = "application/vnd.google-apps.form";
	static final String TYPE_VISIO = "application/vnd.google-apps.drawing";

	/**
	 * List files of a directory. This method can take A LOT of time to execute.
	 * Using it is probably a bad idea. This is why a draft of "DriveExplorer"
	 * exists.
	 *
	 * @return List of files
	 * @throws AccessException
	 */
	@Override
	public List<File> listFiles() throws AccessException {
		ArrayList<File> files = new ArrayList<>();
		try {
			String pageToken = null;
			LOG.info("{}.listFiles();", this);
			do {
				ChildList list = source.getDrive().children().list(fileId).setPageToken(pageToken).execute();
				List<ChildReference> items = list.getItems();
				if (items.isEmpty()) {
					break;
				}
				for (ChildReference r : items) {
					File f = new FileDrive(r.getId(), source);
					files.add(f);
				}
				pageToken = list.getNextPageToken();
			} while (pageToken != null);

		} catch (IOException ex) {
			throw new AccessException(AccessException.AccessState.ERROR, ex);
		}
		return files;
	}

	private com.google.api.services.drive.model.File driveFile;

	private com.google.api.services.drive.model.File getFile() throws AccessException {
		if (driveFile == null) {
			try {
				driveFile = source.getDrive().files().get(fileId).execute();
				if (SourceDrive.DEBUG) {
					LOG.info("{}.getFile(): title = \"{}\"", this, driveFile.getTitle());
				}
			} catch (Exception ex) {
				if (SourceDrive.DEBUG) {
					LOG.info("{}.getFile(): Error", this, ex);
				}
				throw new AccessException(AccessException.AccessState.ERROR, ex);
			}
		}

		return driveFile;
	}

	@Override
	public long getSize() throws AccessException {
		if (getFile().getDownloadUrl() != null) {
			return getFile().getFileSize();
		} else {
			return 0;
		}
	}

	@Override
	public String getPath() {
		return fileId;
	}

	@Override
	public Source getSource() {
		return source;
	}

	private static InputStream downloadFile(Drive service, String exportUrl) throws AccessException {
		try {
			HttpResponse resp
					= service.getRequestFactory()
					.buildGetRequest(new GenericUrl(exportUrl))
					.execute();
			return resp.getContent();
		} catch (IOException e) {
			throw new AccessException(AccessException.AccessState.ERROR, e);
		}
	}

	@Override
	public InputStream getInputStream() throws AccessException {
		try {
			String type = getFile().getMimeType();
			if (isGdriveMime(type)) {
				Map<String, String> exportLinks = getFile().getExportLinks();
				for (Map.Entry<String, String> me : exportLinks.entrySet()) {
					return downloadFile(source.getDrive(), me.getValue());
				}
			} else {
				switch (type) {
					case TYPE_FOLDER:
						return null;
					default:
						source.getDrive().files().get(fileId).executeAsInputStream();
				}
			}
		} catch (IOException ex) {
			LOG.error("getInputStream", ex);
		}
		return null;
	}

	private static boolean isGdriveMime(String mime) {
		switch (mime) {
			case TYPE_EXCEL:
			case TYPE_WORD:
			case TYPE_POWERPOINT:
			case TYPE_GMAP:
			case TYPE_FORM:
			case TYPE_VISIO:
				return true;
			default:
				return false;
		}
	}

	private static String mimeToExtension(String mime) {
		switch (mime) {
			case TYPE_FOLDER:
				return "";
			case TYPE_EXCEL:
				return ".xls";
			case TYPE_WORD:
				return ".doc";
			case TYPE_POWERPOINT:
				return ".ppt";
			case TYPE_GMAP:
				return ".gmap";
			case TYPE_FORM:
				return ".form";
			case TYPE_VISIO:
				return ".vsd";
			default:
				return ".xxx";
		}
	}

	@Override
	public String getName() throws AccessException {
		String name = getFile().getOriginalFilename();
		if (name == null) {
			return getFile().getTitle() + mimeToExtension(getFile().getMimeType());
		}
		return name;
	}

	@Override
	public String toString() {
		return String.format("FileDrive{fileId=\"%s\"}", fileId);
	}

}
