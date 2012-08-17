package org.digitalcampus.mtrain.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.digitalcampus.mtrain.application.DbHelper;
import org.digitalcampus.mtrain.model.Activity;
import org.digitalcampus.mtrain.model.Section;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.bugsense.trace.BugSenseHandler;

import android.content.Context;

public class ModuleXMLReader {

	public static final String TAG = "ModuleXMLReader";
	private Document document;
	private String tempFilePath;

	

	public ModuleXMLReader(String filename) {
		// TODO check that it's a valid module xml file else throw error
		File moduleXML = new File(filename);
		if (moduleXML.exists()) {

			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder;
			try {
				builder = factory.newDocumentBuilder();
				document = builder.parse(moduleXML);

			} catch (ParserConfigurationException e) {
				BugSenseHandler.log(TAG, e);
				e.printStackTrace();
			} catch (SAXException e) {
				BugSenseHandler.log(TAG, e);
				e.printStackTrace();
			} catch (IOException e) {
				BugSenseHandler.log(TAG, e);
				e.printStackTrace();
			}
		}
	}
	
	public String getTempFilePath() {
		return tempFilePath;
	}

	public void setTempFilePath(String tempFilePath) {
		this.tempFilePath = tempFilePath;
	}
	
	public HashMap<String, String> getMeta(){
		HashMap<String, String> hm = new HashMap<String, String>();
		Node m = document.getFirstChild().getFirstChild();
		NodeList meta = m.getChildNodes();
		for (int j=0; j<meta.getLength(); j++) {
			hm.put(meta.item(j).getNodeName(), meta.item(j).getTextContent());
		}
		return hm;
	}
	
	public ArrayList<Activity> getActivities(long modId){
		ArrayList<Activity>  acts = new ArrayList<Activity>();
		Node struct = document.getFirstChild().getFirstChild().getNextSibling();
		NodeList s = struct.getChildNodes();
		for (int i=0; i<s.getLength(); i++) {
			// get the id and acts
			NamedNodeMap sectionAttrs = s.item(i).getAttributes();
			//TODO add error checking with conversion to ints
			int sectionId = Integer.parseInt(sectionAttrs.getNamedItem("id").getTextContent());
			NodeList activities = s.item(i).getLastChild().getChildNodes();
			for (int j=0; j<activities.getLength(); j++) {
				
				NamedNodeMap activityAttrs = activities.item(j).getAttributes();
				String actType = activityAttrs.getNamedItem("type").getTextContent();
				//TODO add error checking with conversion to ints
				int actId = Integer.parseInt(activityAttrs.getNamedItem("id").getTextContent());
				String digest = activityAttrs.getNamedItem("digest").getTextContent();
				Activity a = new Activity();
				a.setModId(modId);
				a.setActId(actId);
				a.setSectionId(sectionId);
				a.setActType(actType);
				a.setDigest(digest);
				acts.add(a);
			}
		}
		return acts;
	}
	
	public ArrayList<Section> getSections(int modId, Context ctx){
		ArrayList<Section> sections = new ArrayList<Section>();
		NodeList sects = document.getFirstChild().getFirstChild().getNextSibling().getChildNodes();
		DbHelper db = new DbHelper(ctx);
		for (int i=0; i<sects.getLength(); i++){
			NamedNodeMap sectionAttrs = sects.item(i).getAttributes();
			int sectionId = Integer.parseInt(sectionAttrs.getNamedItem("id").getTextContent());
			String title = this.getChildNodeByName(sects.item(i),"title").getTextContent();
			Section s = new Section();
			s.setSectionId(sectionId);
			s.setTitle(title);
			
			float progress = db.getSectionProgress(modId, sectionId);
			
			s.setProgress(progress);
			//now get activities
			NodeList acts = this.getChildNodeByName(sects.item(i),"activities").getChildNodes();
			for(int j=0; j<acts.getLength();j++){
				Activity a = new Activity();
				NamedNodeMap activityAttrs = acts.item(j).getAttributes();
				a.setActId(Integer.parseInt(activityAttrs.getNamedItem("id").getTextContent()));
				NamedNodeMap nnm = acts.item(j).getAttributes();
				String actType = nnm.getNamedItem("type").getTextContent();
				String digest = nnm.getNamedItem("digest").getTextContent();
				a.setActType(actType);
				a.setModId(modId);
				a.setSectionId(sectionId);
				a.setActivityData(this.nodetoHashMap(acts.item(j)));
				a.setDigest(digest);
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
	
	private HashMap<String,String> nodetoHashMap(Node n){
		HashMap<String,String> hm = new HashMap<String,String>();
		NodeList nl = n.getChildNodes();
		for (int i=0; i<nl.getLength(); i++){
			hm.put(nl.item(i).getNodeName(), nl.item(i).getTextContent());
		}
		return hm;
	}
}
