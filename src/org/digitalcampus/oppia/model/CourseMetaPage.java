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
import java.util.ArrayList;
import java.util.Locale;

public class CourseMetaPage implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1597711519611488890L;
	public final static String TAG = CourseMetaPage.class.getSimpleName();
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
			if(l.getLang().toLowerCase(Locale.US).equals(langStr.toLowerCase(Locale.US))){
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
