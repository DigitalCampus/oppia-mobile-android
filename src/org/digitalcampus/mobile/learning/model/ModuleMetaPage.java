package org.digitalcampus.mobile.learning.model;

import java.io.Serializable;
import java.util.ArrayList;

public class ModuleMetaPage implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1597711519611488890L;
	public final static String TAG = ModuleMetaPage.class.getSimpleName();
	private int id;
	private ArrayList<Lang> langs = new ArrayList<Lang>();
	
	public void setId(int id){
		this.id = id;
	}
	
	public int getId(){
		return this.id;
	}
	
	public void addLang(Lang l){
		langs.add(l);
	}
	
	public Lang getLang(String langStr){
		for(Lang l: langs){
			if(l.getLang().toLowerCase().equals(langStr.toLowerCase())){
				return l;
			}
		}
		if(langs.size()> 0){
			return langs.get(0);
		} else {
			return null;
		}
	}
}
