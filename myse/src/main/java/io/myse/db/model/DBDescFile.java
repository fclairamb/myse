package io.myse.db.model;

import io.myse.access.AccessException;
import io.myse.access.File;
import io.myse.common.Hashing;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(
		name = "file",
		indexes = {
			@Index(name = "path", columnList = "source_id,file_path", unique = true),
			@Index(name = "next", columnList = "source_id,directory,to_analyse,date_mod", unique = false),
			@Index(name = "date_mod", columnList = "date_mod", unique = false),
			@Index(name = "docId", columnList = "docId", unique = true)
		}
)
public class DBDescFile implements Serializable {

	@Id
	@Column(name = "file_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@JoinColumn(name = "source_id")
	@ManyToOne(cascade = CascadeType.REMOVE)
	private DBDescSource source;
	/**
	 * Path of the file
	 */
	@Column(name = "file_path")
	private String filePath;

	@Column(name = "file_size")
	private long fileSize;

	@Column(name = "file_name")
	private String fileName;

	/**
	 * If the file is a directory.
	 */
	@Column(name = "directory")
	private boolean directory;

	/**
	 * Modification date
	 */
	@Column(name = "date_mod")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateMod;

	/**
	 * Next planned analysis. This is used to prioritize when the next analysis
	 * will happen
	 */
	@Column(name = "next_analysis")
	private int nextAnalysis;

	@Column(name = "last_analysis")
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastAnalysis;

	@Column(name = "to_analyse")
	private boolean toAnalyse;

	@Column(name = "nb_analysis")
	private int nbAnalysis;

	@Column(name = "nb_errors")
	private int nbErrors;

	@Column(name = "doc_id")
	private String docId;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public DBDescSource getSource() {
		return source;
	}

	public void setSource(DBDescSource source) {
		this.source = source;
	}

	public String getPath() {
		return filePath;
	}

	public void setPath(String filePath) {
		this.filePath = filePath;
	}

	public long getSize() {
		return fileSize;
	}

	public void setSize(long fileSize) {
		this.fileSize = fileSize;
	}

	/**
	 * Get the name of the file.
	 * 
	 * The name of the file is the name you would have on a POSIX OS. It is most
	 * probably the last part of the path but this is not mandatory. The obvious
	 * exception is Google Drive, where the path is an ID.
	 * 
	 * @return Name of the file
	 */
	public String getName() {
		return fileName;
	}

	public void setName(String fileName) {
		this.fileName = fileName;
	}

	public boolean isDirectory() {
		return directory;
	}

	public void setDirectory(boolean directory) {
		this.directory = directory;
	}

	public Date getLastModified() {
		return dateMod;
	}

	public void setLastModified(Date date) {

		if (dateMod == null || date.compareTo(dateMod) > 0) {
			this.toAnalyse = true;
			this.dateMod = date;
		}
	}

	public String getExtension() {
		String path = getName();
		int p = path.lastIndexOf(".");
		return path.substring(p + 1).toLowerCase();
	}

	private static int extensionToPriority(String extension) {
		//TODO: Find a more generic/elegant way to do this
		switch (extension) {
			case "doc":
			case "docx":
			case "pdf":
			case "odt":
				return 0;
			case "ppt":
			case "pptx":
			case "odp":
				return 10;
			case "xls":
			case "xlsx":
			case "ods":
				return 20;
			case "html":
			case "txt":
				return 30;
			default:
				return 50;
		}
	}

	public void updateNextAnalysis() {
		{ // We increment it by its age
			long elapsed = (System.currentTimeMillis() - dateMod.getTime()) + nbErrors * 3600 * 1000;
			nextAnalysis += Math.log(elapsed);
		}

		{ // And by its size
			nextAnalysis += Math.log(fileSize);
		}

		{ // And by its extension
			int priority = extensionToPriority(getExtension());
			nextAnalysis += priority;
		}
	}

	public Date getDateMod() {
		return dateMod;
	}

	public void setDateMod(Date dateMod) {
		this.dateMod = dateMod;
	}

	public int getNbErrors() {
		return nbErrors;
	}

	public void setNbErrors(int nbErrors) {
		this.nbErrors = nbErrors;
	}

	public boolean isToAnalyze() {
		return toAnalyse;
	}

	public void setToAnalyze(boolean toAnalyze) {
		this.toAnalyse = toAnalyze;
	}

	public static DBDescFile get(File file, EntityManager em) {
		for (DBDescFile f : em.createQuery("SELECT f FROM DBDescFile f WHERE f.source = :source AND f.filePath = :path", DBDescFile.class)
				.setParameter("source", file.getSource().getDesc())
				.setParameter("path", file.getPath()).getResultList()) {
			return f;
		}
		return null;
	}

	public static DBDescFile get(String docId, EntityManager em) {
		for (DBDescFile f : em.createQuery("SELECT f FROM DBDescFile f WHERE f.docId = :docId", DBDescFile.class)
				.setParameter("docId", docId)
				.getResultList()) {
			return f;
		}
		return null;
	}

	/**
	 * List all files with the ones to analyse first.
	 *
	 * @param source Source
	 * @param dir Directory
	 * @param nb Number of files to fetch
	 * @param em EntityManager instance (JPA)
	 * @return List of files
	 */
	public static List<DBDescFile> listFilesAll(DBDescSource source, boolean dir, int nb, EntityManager em) {
		return em.createQuery("SELECT f FROM DBDescFile f WHERE f.source = :source AND f.directory = :dir ORDER BY f.toAnalyse DESC, f.nextAnalysis ASC", DBDescFile.class)
				.setParameter("source", source)
				.setParameter("dir", dir)
				.setMaxResults(nb)
				.getResultList();
	}

	/**
	 * List all files that need to be analysed.
	 *
	 * @param s Source
	 * @param nb Number of files to fetch
	 * @param em EntityManager instance (JPA)
	 * @return List of files
	 */
	public static List<DBDescFile> listFilesToAnalyse(DBDescSource s, int nb, EntityManager em) {
		return em.createQuery("SELECT f FROM DBDescFile f WHERE f.source = :source AND f.toAnalyse = true ORDER BY f.nextAnalysis ASC", DBDescFile.class)
				.setParameter("source", s)
				.setMaxResults(nb)
				.getResultList();
	}

	public static DBDescFile getOrCreate(File file, EntityManager em) throws AccessException {
		DBDescFile df = get(file, em);

		if (df == null) {
			df = new DBDescFile();
			df.setSource(file.getSource().getDesc());
			df.setPath(file.getPath());
			df.setName(file.getName());
			df.setDirectory(file.isDirectory());
			df.setDocId(df.generateDocId());
			em.persist(df);
		}

		return df;
	}

	@Override
	public String toString() {
		return String.format("DescFile{path=\"%s\", name=\"%s\"}", getPath(), getName());
	}

	public void performingAnalysis() {
		nbAnalysis += 1;
		toAnalyse = false;
		lastAnalysis = new Date();
	}

	public String generateDocId() {
		String fullId = source.getShortName() + "-" + getPath();
		return Hashing.toSHA1(fullId);
	}

	private void setDocId(String genDocId) {
		docId = genDocId;
	}

	public String getDocId() {
		return docId;
	}
}
