package org.digitalcampus.mobile.learning.model;

import java.io.Serializable;

public class Media implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7381597814535579028L;
	
	public static final String TAG = "Media";
	private String filename;
	private String downloadUrl;
	private String digest;
	
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public String getDownloadUrl() {
		return downloadUrl;
	}
	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}
	public String getDigest() {
		return digest;
	}
	public void setDigest(String digest) {
		this.digest = digest;
	}
	
}
