package com.webingenia.cifstest.db.model;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "file")
public class DBDescFile implements Serializable {

	@Id
	private long id;

	/**
	 * Path of the file
	 */
	@Column
	private String filePath;

	/**
	 * If the file is a directory.
	 */
	@Column
	private boolean directory;

	/**
	 * Modification date
	 */
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateMod;

	/**
	 * Previous modification date
	 */
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date datePreviousMod;

	/**
	 * Next planned analysis. This is not an absolute date, it is used to
	 * prioritize which analysis should be performed first.
	 */
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date nextAnalysis;

	@Column
	private int nbErrors;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public boolean isDirectory() {
		return directory;
	}

	public void setDirectory(boolean directory) {
		this.directory = directory;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public Date getLastModification() {
		return dateMod;
	}

	public void setLastModification(Date lastModification) {
		this.dateMod = lastModification;
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

	public Date getDatePreviousMod() {
		return datePreviousMod;
	}

	public void setDatePreviousMod(Date datePreviousMod) {
		this.datePreviousMod = datePreviousMod;
	}

	public int getNbErrors() {
		return nbErrors;
	}

	public void setNbErrors(int nbErrors) {
		this.nbErrors = nbErrors;
	}

}
