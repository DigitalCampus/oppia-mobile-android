package org.digitalcampus.mtrain.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.digitalcampus.mtrain.model.Activity;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.util.Log;

public class ModuleXMLReader {

	public static final String TAG = "ModuleXMLReader";
	private Document document;

	public ModuleXMLReader(String filename) {

		File moduleXML = new File(filename);
		if (moduleXML.exists()) {

			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder;
			try {
				builder = factory.newDocumentBuilder();
				document = builder.parse(moduleXML);

			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public HashMap<String, String> getMeta(){
		HashMap<String, String> hm = new HashMap<String, String>();
		Node m = document.getFirstChild().getFirstChild();
		NodeList meta = m.getChildNodes();
		for (int j=0; j<meta.getLength(); j++) {
			hm.put(meta.item(j).getNodeName(), meta.item(j).getTextContent());
			Log.v(TAG, meta.item(j).getNodeName() + ": " + meta.item(j).getTextContent());
		}
		return hm;
	}
	
	public ArrayList<Activity> getActivities(long modId){
		ArrayList<Activity>  acts = new ArrayList<Activity>();
		Node struct = document.getFirstChild().getFirstChild().getNextSibling();
		Log.d(TAG,struct.getNodeName());
		NodeList s = struct.getChildNodes();
		for (int i=0; i<s.getLength(); i++) {
			// get the id and acts
			int sectionId = Integer.parseInt(s.item(i).getFirstChild().getTextContent());
			Log.v(TAG,String.valueOf(sectionId));
			NodeList activities = s.item(i).getLastChild().getChildNodes();
			for (int j=0; j<activities.getLength(); j++) {
				int actId = Integer.parseInt(activities.item(j).getFirstChild().getTextContent());
				//String actType = activities.item(j).getAttributes();
				NamedNodeMap nnm = activities.item(j).getAttributes();
				String actType = nnm.getNamedItem("type").getTextContent();
				
				Log.v(TAG,String.valueOf(actId));
				Log.v(TAG,actType);
				Activity a = new Activity();
				a.setModId(modId);
				a.setActId(actId);
				a.setSectionId(sectionId);
				a.setActType(actType);
				acts.add(a);
			}
		
		}
		
		return acts;
	}
	
	
}
