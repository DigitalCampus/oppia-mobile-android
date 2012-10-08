package org.digitalcampus.mobile.learning.model;

import java.io.Serializable;

public class Lang implements Serializable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8960131611429444591L;
	private String lang;
	private String content;
	
	public Lang(String lang, String content){
		this.setLang(lang);
		this.setContent(content);
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	
	
}
