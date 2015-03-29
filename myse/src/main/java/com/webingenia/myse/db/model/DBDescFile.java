package com.webingenia.myse.db.model;

import com.webingenia.myse.access.AccessException;
import com.webingenia.myse.access.File;
import com.webingenia.myse.common.Hashing;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
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
			@Index(name = "date_mod", columnList = "date_mod", unique = false)
		}
)
public class DBDescFile implements Serializable {

	@Id
	@Column(name = "file_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@JoinColumn(name = "source_id")
	@ManyToOne
	private DBDescSource source;
	/**
	 * Path of the file
	 */
	@Column(name = "file_path")
	private String filePath;

	@Column(name = "file_size")
	private long fileSize;

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
		String path = getPath();
		int p = path.lastIndexOf(".");
		return path.substring(p + 1).toLowerCase();
	}

	public String getName() {
		String path = getPath();
		int p = path.lastIndexOf(".");
		return path.substring(p + 1).toLowerCase();
	}

	private static int extensionToPriority(String extension) {
		switch (extension) {
			case "doc":
			case "docx":
			case "pdf":
			case "odt":
				return 0;
			case "xls":
			case "xlsx":
				return 10;
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

	public static List<DBDescFile> listFiles(DBDescSource s, boolean dir, int limit, EntityManager em) {
		return em.createQuery("SELECT f FROM DBDescFile f WHERE f.source = :source AND f.directory = :dir ORDER BY f.toAnalyse DESC, f.nextAnalysis ASC", DBDescFile.class)
				.setParameter("source", s)
				.setParameter("dir", dir)
				.setMaxResults(limit)
				.getResultList();
	}

	public static DBDescFile getOrCreate(File file, EntityManager em) throws AccessException {
		DBDescFile df = get(file, em);

		if (df == null) {
			df = new DBDescFile();
			df.setSource(file.getSource().getDesc());
			df.setPath(file.getPath());
			df.setDirectory(file.isDirectory());
		}

		return df;
	}

	@Override
	public String toString() {
		return "DescFile{" + getPath() + "}";
	}

	public void performingAnalysis() {
		nbAnalysis += 1;
		toAnalyse = false;
		lastAnalysis = new Date();
	}

	public String getDocId() {
		String fullId = source.getShortName() + "-" + getPath();
		return Hashing.toSHA1(fullId);
	}
}
