package org.digitalcampus.mtrain.application;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.digitalcampus.mtrain.utils.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

public class MTrain extends Application {

	public static final String TAG = "MTrain";
	
	public static final String MTRAIN_ROOT = Environment.getExternalStorageDirectory() + "/mtrain/";
	public static final String MODULES_PATH = MTRAIN_ROOT + "modules/";
	public static final String MEDIA_PATH = MTRAIN_ROOT + "media/";
	public static final String DOWNLOAD_PATH = MTRAIN_ROOT + "download/";
	public static final String MODULE_XML = "module.xml";
	
	public static void createMTrainDirs() throws RuntimeException {
		String cardstatus = Environment.getExternalStorageState();
        if (cardstatus.equals(Environment.MEDIA_REMOVED)
                || cardstatus.equals(Environment.MEDIA_UNMOUNTABLE)
                || cardstatus.equals(Environment.MEDIA_UNMOUNTED)
                || cardstatus.equals(Environment.MEDIA_MOUNTED_READ_ONLY)
                || cardstatus.equals(Environment.MEDIA_SHARED)) {
            RuntimeException e =
                new RuntimeException("mTrain reports :: SDCard error: "
                        + Environment.getExternalStorageState());
            throw e;
        }
        
        String[] dirs = {
        		MTRAIN_ROOT, MODULES_PATH, MEDIA_PATH, DOWNLOAD_PATH
        };
        
        for (String dirName : dirs) {
            File dir = new File(dirName);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    RuntimeException e =
                        new RuntimeException("mTrain reports :: Cannot create directory: " + dirName);
                    throw e;
                }
            } else {
                if (!dir.isDirectory()) {
                    RuntimeException e =
                        new RuntimeException("mTrain reports :: " + dirName
                                + " exists, but is not a directory");
                    throw e;
                }
            }
        }
	}
	
	//Scan for any newly downloaded modules
	public static boolean installNewDownloads(){
		// get folder
		File dir = new File(MTrain.DOWNLOAD_PATH);

		String[] children = dir.list();
		if (children == null) {
		    // Either dir does not exist or is not a directory
		} else {
		    for (int i=0; i<children.length; i++) {
		        // Get filename of file or directory
		        Log.v(TAG,children[i]);
		        
		        // extract to temp dir and check it's a valid package file
		        File tempdir = new File(MTrain.MTRAIN_ROOT + "temp/");
		        tempdir.mkdirs();
		        FileUtils.unzipFiles(MTrain.DOWNLOAD_PATH, children[i], tempdir.getAbsolutePath());
		        
		        String[] moddirs = tempdir.list(); // use this to get the module name
		        // check a module.xml file exists and is a readable XML file
		        String moduleXMLPath = tempdir + "/" + moddirs[0] + "/" + MTrain.MODULE_XML;
		        Log.v(TAG, moduleXMLPath);
		        File moduleXML = new File(moduleXMLPath);
		       
		        if(moduleXML.exists()){
		        	// if valid xml send it to DB to create the record
		        	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		        	DocumentBuilder builder;
					try {
						builder = factory.newDocumentBuilder();
						Document document = builder.parse(moduleXML);
						Log.v(TAG,"Read module.xml file");
						NodeList nl = document.getElementsByTagName("meta");
						for (int j=0; j<nl.getLength(); j++) {
							Log.v(TAG, nl.item(j).getNodeName());
						}

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
		        	
		        	// move directory from temp to modules dir
		        }
		        // 
		        //finally delete temp directory
		       FileUtils.deleteDir(tempdir);
		        
		    }
		}
		
		return true;
	}
}
