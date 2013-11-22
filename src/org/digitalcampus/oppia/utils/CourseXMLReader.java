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

package org.digitalcampus.oppia.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.exception.InvalidXMLException;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.model.Media;
import org.digitalcampus.oppia.model.CourseMetaPage;
import org.digitalcampus.oppia.model.Section;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.Context;

public class CourseXMLReader {

	public static final String TAG = CourseXMLReader.class.getSimpleName();
	private Document document;

	public CourseXMLReader(String filename) throws InvalidXMLException {
		File courseXML = new File(filename);
		if (courseXML.exists()) {

			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder;
			try {
				builder = factory.newDocumentBuilder();
				document = builder.parse(courseXML);

			} catch (ParserConfigurationException e) {
				throw new InvalidXMLException(e);
			} catch (SAXException e) {
				throw new InvalidXMLException(e);
			} catch (IOException e) {
				throw new InvalidXMLException(e);
			}
		}
	}
	
	public ArrayList<Lang> getTitles(){
		ArrayList<Lang> titles = new ArrayList<Lang>();
		Node m = null;
		m = document.getFirstChild().getFirstChild();
		NodeList meta = m.getChildNodes();
		for (int j=0; j<meta.getLength(); j++) {
			if(meta.item(j).getNodeName().equals("title")){
				NamedNodeMap attrs = meta.item(j).getAttributes();
				if(attrs.getNamedItem("lang") != null){
					String lang = attrs.getNamedItem("lang").getTextContent();
					titles.add(new Lang(lang, meta.item(j).getTextContent()));
				} else {
					titles.add(new Lang(MobileLearning.DEFAULT_LANG, meta.item(j).getTextContent()));
				}
			}
		}
		return titles;
	}
	
	public ArrayList<Lang> getLangs(){
		ArrayList<Lang> langs = new ArrayList<Lang>();
		NodeList ls = document.getElementsByTagName("langs").item(0).getChildNodes();
		for (int j=0; j<ls.getLength(); j++) {
			Lang l = new Lang(ls.item(j).getTextContent(),"");
			langs.add(l);
		}
		return langs;
	}
	
	public double getVersionId(){
		Node m = document.getFirstChild().getFirstChild();
		NodeList meta = m.getChildNodes();
		for (int j=0; j<meta.getLength(); j++) {
			if(meta.item(j).getNodeName().equals("versionid")){
				return Double.valueOf(meta.item(j).getTextContent());
			}
		}
		return 0;
	}
	
	public ArrayList<CourseMetaPage> getMetaPages(){
		ArrayList<CourseMetaPage> ammp = new ArrayList<CourseMetaPage>();
		Node m = document.getFirstChild().getFirstChild();
		NodeList meta = m.getChildNodes();
		for (int j=0; j<meta.getLength(); j++) {
			if(meta.item(j).getNodeName().toLowerCase(Locale.US).equals("page")){
				CourseMetaPage mmp = new CourseMetaPage();
				
				NamedNodeMap pageAttrs = meta.item(j).getAttributes();
				NodeList pages = meta.item(j).getChildNodes();
				String key = pageAttrs.getNamedItem("id").getTextContent();
				mmp.setId(Integer.parseInt(key));
				
				// get all the langs
				ArrayList<String> langList = new ArrayList<String>();
				for(int p=0; p<pages.getLength(); p++){
					NamedNodeMap nodeAttrs = pages.item(p).getAttributes();
					try {
						String lang = nodeAttrs.getNamedItem("lang").getTextContent();
						if(!langList.contains(lang)){
							langList.add(lang);
						}
					} catch (NullPointerException npe){
						// do nothing
					}
				}
				
				// loop through the langs and set the titles/filenames
				for(String lang: langList){
					String title = "";
					String location = "";
					for(int p=0; p<pages.getLength(); p++){
						NamedNodeMap nodeAttrs = pages.item(p).getAttributes();
						if(pages.item(p).getNodeName().toLowerCase(Locale.US).equals("title") && nodeAttrs.getNamedItem("lang").getTextContent().equals(lang)){
							title = pages.item(p).getTextContent();
						} 
						if(pages.item(p).getNodeName().toLowerCase(Locale.US).equals("location") && nodeAttrs.getNamedItem("lang").getTextContent().equals(lang)){
							location = pages.item(p).getTextContent();
						} 
					}
					Lang l = new Lang(lang,title);
					l.setLocation(location);
					mmp.addLang(l);
					
				}
				ammp.add(mmp);
			}
		}
		return ammp;
	}
	
	public ArrayList<Activity> getBaselineActivities(long modId, Context ctx){
		ArrayList<Activity>  acts = new ArrayList<Activity>();
		//NodeList actlist = this.getChildNodeByName(document.getFirstChild().getFirstChild(),"activity").getChildNodes();
		Node docMeta = document.getFirstChild().getFirstChild();
		NodeList meta = docMeta.getChildNodes();
		DbHelper db = new DbHelper(ctx);
		for (int i=0; i<meta.getLength(); i++) {
			if(meta.item(i).getNodeName().toLowerCase(Locale.US).equals("activity")){
				Activity a = new Activity();
				NamedNodeMap activityAttrs = meta.item(i).getAttributes();
				a.setActId(Integer.parseInt(activityAttrs.getNamedItem("order").getTextContent()));
				NamedNodeMap nnm = meta.item(i).getAttributes();
				String actType = nnm.getNamedItem("type").getTextContent();
				String digest = nnm.getNamedItem("digest").getTextContent();
				a.setActType(actType);
				a.setModId(modId);
				a.setSectionId(0);
				a.setAttempted(db.activityAttempted(modId, digest));				
				
				ArrayList<Lang> actTitles = new ArrayList<Lang>();
				ArrayList<Lang> actLocations = new ArrayList<Lang>();
				ArrayList<Lang> actContents = new ArrayList<Lang>();
				ArrayList<Lang> actDescriptions = new ArrayList<Lang>();
				ArrayList<Media> actMedia = new ArrayList<Media>();
				String actMimeType = null;
				NodeList act = meta.item(i).getChildNodes();
				for (int k=0; k<act.getLength(); k++) {
					NamedNodeMap attrs = act.item(k).getAttributes();
					if(act.item(k).getNodeName().equals("title")){
						String lang = attrs.getNamedItem("lang").getTextContent();
						actTitles.add(new Lang(lang, act.item(k).getTextContent()));
					} else if(act.item(k).getNodeName().equals("location")){
						String lang = attrs.getNamedItem("lang").getTextContent();
						actLocations.add(new Lang(lang, act.item(k).getTextContent()));
						try {
							String mimeType = attrs.getNamedItem("type").getTextContent();
							actMimeType = mimeType;
						} catch (NullPointerException npe){
							//do nothing
						}
					} else if(act.item(k).getNodeName().equals("content")){
						String lang = attrs.getNamedItem("lang").getTextContent();
						actContents.add(new Lang(lang, act.item(k).getTextContent()));
					} else if(act.item(k).getNodeName().equals("image")){
						a.setImageFile(attrs.getNamedItem("filename").getTextContent());
					} else if (act.item(k).getNodeName().equals("media")){
						// add media
						NodeList files = act.item(k).getChildNodes();
						for (int m=0; m<files.getLength(); m++) {
							if (files.item(m).getNodeName().equals("file")){
								NamedNodeMap fileAttrs = files.item(m).getAttributes();
								Media mObj = new Media();
								mObj.setFilename(fileAttrs.getNamedItem("filename").getTextContent());
								mObj.setDigest(fileAttrs.getNamedItem("digest").getTextContent());
								mObj.setDownloadUrl(fileAttrs.getNamedItem("download_url").getTextContent());
								if(fileAttrs.getNamedItem("length") != null){
									mObj.setLength(Integer.parseInt(fileAttrs.getNamedItem("length").getTextContent()));
								} else {
									mObj.setLength(0);
								}
								actMedia.add(mObj);
							}
						}
					} else if (act.item(k).getNodeName().equals("description")){
						String lang = attrs.getNamedItem("lang").getTextContent();
						actDescriptions.add(new Lang(lang, act.item(k).getTextContent()));
					}
				}
				a.setTitles(actTitles);
				a.setDescriptions(actDescriptions);
				a.setLocations(actLocations);
				a.setContents(actContents);
				a.setDigest(digest);
				a.setMedia(actMedia);
				a.setMimeType(actMimeType);
				
				acts.add(a);
			}
		}
		db.close();
		return acts;
	}
	
	public ArrayList<Media> getMedia(){
		ArrayList<Media> media = new ArrayList<Media>();
		NodeList m = document.getFirstChild().getChildNodes();
		for (int i=0; i<m.getLength(); i++) {
			if(m.item(i).getNodeName().equals("media")){
				NodeList files = m.item(i).getChildNodes();
				for (int j=0; j<files.getLength(); j++) {
					if (files.item(j).getNodeName().equals("file")){
						NamedNodeMap fileAttrs = files.item(j).getAttributes();
						Media mObj = new Media();
						mObj.setFilename(fileAttrs.getNamedItem("filename").getTextContent());
						mObj.setDigest(fileAttrs.getNamedItem("digest").getTextContent());
						mObj.setDownloadUrl(fileAttrs.getNamedItem("download_url").getTextContent());
						if(fileAttrs.getNamedItem("length") != null){
							mObj.setLength(Integer.parseInt(fileAttrs.getNamedItem("length").getTextContent()));
						} else {
							mObj.setLength(0);
						}
						if(fileAttrs.getNamedItem("filesize") != null){
							mObj.setFileSize(Double.parseDouble(fileAttrs.getNamedItem("filesize").getTextContent()));
						} else {
							mObj.setFileSize(0);
						}
						media.add(mObj);
					}
				}
			}
		}
		return media;
	}
	public String getCourseImage(){
		String image = null;
		Node m = document.getFirstChild().getFirstChild();
		NodeList meta = m.getChildNodes();
		for (int j=0; j<meta.getLength(); j++) {
			if(meta.item(j).getNodeName().equals("image")){
				NamedNodeMap attrs = meta.item(j).getAttributes();
				image = attrs.getNamedItem("filename").getTextContent();
			}
		}
		return image;
	}
	
	/*
	 * This is used when installing a new course
	 * and so adding all the activities to the db
	 */
	public ArrayList<Activity> getActivities(long modId){
		ArrayList<Activity>  acts = new ArrayList<Activity>();
		Node struct = document.getFirstChild().getFirstChild().getNextSibling();
		NodeList s = struct.getChildNodes();
		for (int i=0; i<s.getLength(); i++) {
			// get the id and acts
			NamedNodeMap sectionAttrs = s.item(i).getAttributes();
			int sectionId = Integer.parseInt(sectionAttrs.getNamedItem("order").getTextContent());
			NodeList activities = s.item(i).getLastChild().getChildNodes();
			for (int j=0; j<activities.getLength(); j++) {
				
				NamedNodeMap activityAttrs = activities.item(j).getAttributes();
				String actType = activityAttrs.getNamedItem("type").getTextContent();
				int actId = Integer.parseInt(activityAttrs.getNamedItem("order").getTextContent());
				String digest = activityAttrs.getNamedItem("digest").getTextContent();
				Activity a = new Activity();				
				a.setModId(modId);
				a.setActId(actId);
				a.setSectionId(sectionId);
				a.setActType(actType);
				a.setDigest(digest);
				// get the titles
				ArrayList<Lang> actTitles = new ArrayList<Lang>();
				ArrayList<Lang> actDescriptions = new ArrayList<Lang>();
				NodeList act = activities.item(j).getChildNodes();
				for (int k=0; k<act.getLength(); k++) {
					NamedNodeMap attrs = act.item(k).getAttributes();
					if(act.item(k).getNodeName().equals("title")){
						String lang = attrs.getNamedItem("lang").getTextContent();
						actTitles.add(new Lang(lang, act.item(k).getTextContent()));
					}
					if(act.item(k).getNodeName().equals("description")){
						String lang = attrs.getNamedItem("lang").getTextContent();
						actDescriptions.add(new Lang(lang, act.item(k).getTextContent()));
					}
				}
				a.setTitles(actTitles);
				a.setDescriptions(actDescriptions);
				
				acts.add(a);
			}
		}
		return acts;
	}
	
	public int getNoActivities(long modId){
		Node struct = document.getFirstChild().getFirstChild().getNextSibling();
		NodeList s = struct.getChildNodes();
		return s.getLength();
	}
	
	public ArrayList<Section> getSections(int modId, Context ctx){
		ArrayList<Section> sections = new ArrayList<Section>();
		NodeList sects = document.getFirstChild().getFirstChild().getNextSibling().getChildNodes();
		DbHelper db = new DbHelper(ctx);
		for (int i=0; i<sects.getLength(); i++){
			NamedNodeMap sectionAttrs = sects.item(i).getAttributes();
			int order = Integer.parseInt(sectionAttrs.getNamedItem("order").getTextContent());
			Section s = new Section();
			s.setOrder(order);
			
			//get section titles
			NodeList nodes = sects.item(i).getChildNodes();
			ArrayList<Lang> sectTitles = new ArrayList<Lang>();
			String image = null;
			for (int j=0; j<nodes.getLength(); j++) {
				NamedNodeMap attrs = nodes.item(j).getAttributes();
				if(nodes.item(j).getNodeName().equals("title")){
					String lang = attrs.getNamedItem("lang").getTextContent();
					sectTitles.add(new Lang(lang, nodes.item(j).getTextContent()));
				} else if(nodes.item(j).getNodeName().equals("image")){
					image = attrs.getNamedItem("filename").getTextContent();
				}
			}
			s.setTitles(sectTitles);
			s.setImageFile(image);
			
			float progress = db.getSectionProgress(modId, order);
			
			s.setProgress(progress);
			//now get activities
			NodeList acts = this.getChildNodeByName(sects.item(i),"activities").getChildNodes();
			for(int j=0; j<acts.getLength();j++){
				Activity a = new Activity();
				NamedNodeMap activityAttrs = acts.item(j).getAttributes();
				a.setActId(Integer.parseInt(activityAttrs.getNamedItem("order").getTextContent()));
				NamedNodeMap nnm = acts.item(j).getAttributes();
				String actType = nnm.getNamedItem("type").getTextContent();
				String digest = nnm.getNamedItem("digest").getTextContent();
				a.setActType(actType);
				a.setModId(modId);
				a.setSectionId(order);
				a.setCompleted(db.activityCompleted(modId, digest));				
				
				ArrayList<Lang> actTitles = new ArrayList<Lang>();
				ArrayList<Lang> actLocations = new ArrayList<Lang>();
				ArrayList<Lang> actContents = new ArrayList<Lang>();
				ArrayList<Lang> actDescriptions = new ArrayList<Lang>();
				ArrayList<Media> actMedia = new ArrayList<Media>();
				String actMimeType = null;
				NodeList act = acts.item(j).getChildNodes();
				for (int k=0; k<act.getLength(); k++) {
					NamedNodeMap attrs = act.item(k).getAttributes();
					if(act.item(k).getNodeName().equals("title")){
						String lang = attrs.getNamedItem("lang").getTextContent();
						actTitles.add(new Lang(lang, act.item(k).getTextContent()));
					} else if(act.item(k).getNodeName().equals("location")){
						String lang = attrs.getNamedItem("lang").getTextContent();
						actLocations.add(new Lang(lang, act.item(k).getTextContent()));
						try {
							String mimeType = attrs.getNamedItem("type").getTextContent();
							actMimeType = mimeType;
						} catch (NullPointerException npe){
							//do nothing
						}
					} else if(act.item(k).getNodeName().equals("content")){
						String lang = attrs.getNamedItem("lang").getTextContent();
						actContents.add(new Lang(lang, act.item(k).getTextContent()));
					} else if(act.item(k).getNodeName().equals("image")){
						a.setImageFile(attrs.getNamedItem("filename").getTextContent());
					} else if (act.item(k).getNodeName().equals("media")){
						// add media
						NodeList files = act.item(k).getChildNodes();
						for (int m=0; m<files.getLength(); m++) {
							if (files.item(m).getNodeName().equals("file")){
								NamedNodeMap fileAttrs = files.item(m).getAttributes();
								Media mObj = new Media();
								mObj.setFilename(fileAttrs.getNamedItem("filename").getTextContent());
								mObj.setDigest(fileAttrs.getNamedItem("digest").getTextContent());
								mObj.setDownloadUrl(fileAttrs.getNamedItem("download_url").getTextContent());
								if(fileAttrs.getNamedItem("length") != null){
									mObj.setLength(Integer.parseInt(fileAttrs.getNamedItem("length").getTextContent()));
								} else {
									mObj.setLength(0);
								}
								actMedia.add(mObj);
							}
						}
					} else if (act.item(k).getNodeName().equals("description")){
						String lang = attrs.getNamedItem("lang").getTextContent();
						actDescriptions.add(new Lang(lang, act.item(k).getTextContent()));
					}
				}
				a.setTitles(actTitles);
				a.setDescriptions(actDescriptions);
				a.setLocations(actLocations);
				a.setContents(actContents);
				a.setDigest(digest);
				a.setMedia(actMedia);
				a.setMimeType(actMimeType);
				
				s.addActivity(a);
			}
			
			sections.add(s);
			
		}
		db.close();
		return sections;
	}
	
	private Node getChildNodeByName(Node parent, String nodeName){
		NodeList nl = parent.getChildNodes();
		for (int i=0; i<nl.getLength(); i++){
			if(nl.item(i).getNodeName().equals(nodeName)){
				return nl.item(i);
			}
		}
		return null;
	}
}
