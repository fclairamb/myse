package io.myse.access.web;

import io.myse.access.AccessException;
import io.myse.access.File;
import io.myse.access.Link;
import io.myse.access.LinkContext;
import io.myse.access.Source;
import static io.myse.common.LOG.LOG;
import io.myse.db.model.DBDescFile;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.tika.io.IOUtils;

public class FileWeb extends File {

	private final String path;
	private final SourceWeb source;

	private DBDescFile desc;

	private DBDescFile getDesc() {
		if (desc == null) {
			desc = DBDescFile.get(this, source.getEntityManager());
		}
		return desc;
	}

	public FileWeb(String path, SourceWeb source) {
		this.path = path;
		this.source = source;
	}

	public FileWeb(DBDescFile desc, SourceWeb source) {
		this.path = desc.getPath();
		this.desc = desc;
		this.source = source;
	}

	@Override
	public boolean exists() throws AccessException {
		return getLinkContent().returnCode == 200;
	}

	@Override
	public boolean isDirectory() throws AccessException {
		// This actually won't be used for this source.
		// TODO: Put it in the super class
		return false;
	}

	@Override
	public Date getLastModified() throws AccessException {
		Date date = getDesc().getLastModified();
		if (date != null) {
			if ((System.currentTimeMillis() - date.getTime()) > 7L * 24 * 3600 * 1000) {
				date = new Date();
			}
		} else {
			date = new Date();
		}
		return date;
	}

	@Override
	public List<File> listFiles() throws AccessException {
		try {
			HTMLLinkExtrator linkExtractor = new HTMLLinkExtrator();
			StringWriter writer = new StringWriter();
			IOUtils.copy(getLinkContent().content, writer);
			List<File> links = new ArrayList<>();
			for (String link : linkExtractor.grabHTMLLinks(writer.toString())) {
				File f = handleLink(link);
				if (f != null) {
					links.add(f);
				}
			}
			return links;
		} catch (IOException ex) {
			LOG.error("Could not access page.", ex);
			throw new AccessException(AccessException.AccessState.ERROR, ex);
		}
	}

	private File handleLink(String link) throws AccessException {
		try {
			String baseUrl = source.getBaseUrl();

			if (link.startsWith("mailto:") || link.startsWith("javascript:")) {
				return null;
			}

			if (link.contains("#")) {
				link = link.substring(0, link.indexOf("#"));
			}

			link = new URL(path).toURI().resolve(link).toString();

			if (!link.startsWith(baseUrl)) {
				LOG.debug("{}: Not taking {} because it doesn't have the base URL !", this, link);
				return null;
			}

			FileWeb f = new FileWeb(link, source);
			f.desc = DBDescFile.getOrCreate(f, source.getEntityManager());
			if (f.getDepth() == 0 || f.getDepth() > getDepth() + 1) {
				f.setDepth(getDepth() + 1);
			}
			return f;
		} catch (Exception ex) {
			LOG.error(String.format("%s: Not taking %s because we couldn't fetch it !", this, link), ex);
			return null;
		}
	}

	@Override
	public long getSize() throws AccessException {
		return getLinkContent().size;
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public Source getSource() {
		return source;
	}

	@Override
	public Link getLink(LinkContext context) throws AccessException {
		return new Link(path);
	}

	@Override
	public InputStream getInputStream() throws AccessException {
		return getLinkContent().content;
	}

	private LinkContent getLinkContent() throws AccessException {
		return FetchCacher.get(path);
	}

	public static class LinkContent {

		public int returnCode;
		public int size;
		public Date lastModified;
		public ByteArrayInputStream content;
	}

	private LinkContent content;

	public int getDepth() {
		return Integer.parseInt(getDesc().getProperties().getOrDefault("depth", "0"));
	}

	public void setDepth(int depth) {
		getDesc().getProperties().put("depth", Integer.toString(depth));
	}

	@Override
	public String toString() {
		return String.format("FileWeb{%s}", path);
	}

}
