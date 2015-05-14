package io.myse.access.drive;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.ChildList;
import com.google.api.services.drive.model.ChildReference;
import io.myse.access.AccessException;
import io.myse.access.File;
import io.myse.access.Link;
import io.myse.access.LinkContext;
import io.myse.access.Source;
import static io.myse.common.LOG.LOG;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class FileDrive extends File {

	private final String fileId;
	private Boolean dir;
	private final SourceDrive source;
	private boolean notFound;

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
			dir = TYPE_GD_FOLDER.equals(getFile().getMimeType());
		}
		return dir;
	}

	@Override
	public Date getLastModified() throws AccessException {
		return new Date(getFile().getModifiedDate().getValue());
	}

	static final String TYPE_GD_FOLDER = "application/vnd.google-apps.folder",
			TYPE_GD_SPREADSHEET = "application/vnd.google-apps.spreadsheet",
			TYPE_GD_DOCS = "application/vnd.google-apps.document",
			TYPE_GD_SLIDES = "application/vnd.google-apps.presentation",
			TYPE_GD_FORM = "application/vnd.google-apps.form",
			TYPE_GD_DRAWING = "application/vnd.google-apps.drawing",
			TYPE_GMAP = "application/vnd.google-apps.map",
			TYPE_MS_EXCEL = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
			TYPE_MS_POWERPOINT = "application/vnd.openxmlformats-officedocument.presentationml.presentation",
			TYPE_MS_WORD = "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
			TYPE_PDF = "application/pdf";

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
		if (driveFile == null && !notFound) {
			try {
				driveFile = source.getDrive().files().get(fileId).execute();
				if (SourceDrive.DEBUG && driveFile != null) {
					LOG.info("{}.getFile(): title = \"{}\"", this, driveFile.getTitle());
				}
			} catch (com.google.api.client.googleapis.json.GoogleJsonResponseException ex) {
				if (ex.getStatusCode() == 404) {
					notFound = true;
				} else {
					throw new AccessException(AccessException.AccessState.ERROR, ex);
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

	private String getPreferredFormatMime(String type, Map<String, String> exportLinks) {
		String targetType;
		switch (type) {
			case TYPE_GD_SPREADSHEET:
				targetType = TYPE_MS_EXCEL;
				break;
			case TYPE_GD_SLIDES:
				targetType = TYPE_MS_POWERPOINT;
				break;
			case TYPE_GD_DOCS:
				targetType = TYPE_MS_WORD;
				break;
			default:
				targetType = exportLinks.entrySet().iterator().next().getKey();
		}
		return exportLinks.get(targetType);
	}

	@Override
	public InputStream getInputStream() throws AccessException {
		try {
			String type = getFile().getMimeType();
			if (isGdriveMime(type)) {
				Map<String, String> exportLinks = getFile().getExportLinks();
				return downloadFile(source.getDrive(), getPreferredFormatMime(type, exportLinks));
			} else {
				switch (type) {
					case TYPE_GD_FOLDER:
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
			case TYPE_GD_SPREADSHEET:
			case TYPE_GD_DOCS:
			case TYPE_GD_SLIDES:
			case TYPE_GMAP:
			case TYPE_GD_FORM:
			case TYPE_GD_DRAWING:
				return true;
			default:
				return false;
		}
	}

	private static String mimeToExtension(String mime) {
		switch (mime) {
			case TYPE_GD_FOLDER:
				return "";
			case TYPE_GD_SPREADSHEET:
				return ".xls";
			case TYPE_GD_DOCS:
				return ".doc";
			case TYPE_GD_SLIDES:
				return ".ppt";
			case TYPE_GMAP:
				return ".gmap";
			case TYPE_GD_FORM:
				return ".form";
			case TYPE_GD_DRAWING:
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

	@Override
	public boolean exists() throws AccessException {
		getFile();
		return !notFound;
	}

	@Override
	public Link getLink(LinkContext context) throws AccessException {
		return new Link(getFile().getAlternateLink());
	}

}
