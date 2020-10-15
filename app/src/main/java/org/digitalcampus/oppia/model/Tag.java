/* 
 * This file is part of OppiaMobile - https://digital-campus.org/
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
import java.util.List;

public class Tag implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5144400570961137778L;
	
	public static final String TAG_CLASS = Tag.class.getSimpleName();
	private String name;
	private int count;
	private int id;
	private String description;
	private int orderPriority;
	private String icon;
	private boolean highlight = false;
	
	private ArrayList<Course> courses = new ArrayList<>();
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getCount() {
		// count should not be under 0
		if (this.count < 0){
			return 0;
		}
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public List<Course> getCourses() {
		return courses;
	}
	public void setCourses(List<Course> courses) {
		this.courses = (ArrayList<Course>) courses;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public int getOrderPriority() {
		return orderPriority;
	}
	public void setOrderPriority(int orderPriority) {
		this.orderPriority = orderPriority;
	}
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public boolean isHighlight() {
		return highlight;
	}
	public void setHighlight(boolean highlight) {
		this.highlight = highlight;
	}
	
	
}
