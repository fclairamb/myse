package com.webingenia.cifstest.db.model;

import com.webingenia.cifstest.access.File;
import com.webingenia.cifstest.access.Source;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "file")
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
	 * Next planned analysis. This is not an absolute date, it is used to
	 * prioritize which analysis should be performed first.
	 */
	@Column(name = "date_next_analysis")
	@Temporal(TemporalType.TIMESTAMP)
	private Date nextAnalysis;

	@Column(name = "to_analyze")
	private boolean toAnalyze;

	@Column
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

	public boolean isDirectory() {
		return directory;
	}

	public void setDirectory(boolean directory) {
		this.directory = directory;
	}

	public Date getLastModification() {
		return dateMod;
	}

	public void setLastModification(Date date) {

		if (dateMod == null || date.compareTo(dateMod) > 0) {
			this.toAnalyze = true;
			this.dateMod = date;
		}

		long elapsed = (System.currentTimeMillis() - dateMod.getTime()) + nbErrors * 3600 * 1000;
		nextAnalysis = new Date(System.currentTimeMillis() + elapsed);
	}

	public Date getNextAnalysis() {
		return nextAnalysis;
	}

	public void setNextAnalysis(Date nextAnalysis) {
		this.nextAnalysis = nextAnalysis;
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
		return toAnalyze;
	}

	public void setToAnalyze(boolean toAnalyze) {
		this.toAnalyze = toAnalyze;
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
		return em.createQuery("SELECT f FROM DBDescFile f WHERE f.source = :source AND f.directory = :dir ORDER BY f.nextAnalysis", DBDescFile.class)
				.setParameter("source", s)
				.setParameter("dir", dir)
				.setMaxResults(limit)
				.getResultList();
	}

	public static DBDescFile getOrCreate(File file, EntityManager em) {
		DBDescFile df = get(file, em);

		if (df == null) {
			df = new DBDescFile();
			df.setSource(file.getSource().getDesc());
			df.setPath(file.getPath());
		}

		return df;
	}

}
