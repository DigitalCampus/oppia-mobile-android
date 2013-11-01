/* 
 * This file is part of OppiaMobile - http://oppia-mobile.org/
 * 
 * OppiaMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * OppiaMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with OppiaMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package org.digitalcampus.oppia.model;

import java.io.Serializable;

public class Media implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7381597814535579028L;
	
	public static final String TAG = Media.class.getSimpleName();
	private String filename;
	private String downloadUrl;
	private String digest;
	private int length;
	private double fileSize;
	
	public String getFilename() {
		return filename;
	}
	
	public int getLength() {
		return length;
	}
	
	public void setLength(int length) {
		this.length = length;
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

	public double getFileSize() {
		return fileSize;
	}

	public void setFileSize(double fileSize) {
		this.fileSize = fileSize;
	}
}
