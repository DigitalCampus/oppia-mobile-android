package org.digitalcampus.mobile.learning.utils;

import java.io.File;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageUtils {
	
	public static Bitmap LoadBMPsdcard(String path, Resources res, int defaultImageResource){  
        //creates a 'File' object, to check if the image exists (Thanks to Jbeerdev for the tip)  
        File imageFile = new File(path);  
  
        //if the file exists  
        if(imageFile.exists()) {  
            //load the bitmap from the given path  
            return BitmapFactory.decodeFile(path);  
        } else {  
            //return the standard 'Image not found' bitmap placed on the res folder  
            return BitmapFactory.decodeResource(res, defaultImageResource);  
        }  
    }  

}
