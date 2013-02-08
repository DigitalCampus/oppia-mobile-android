package org.digitalcampus.mobile.learning.utils;

import java.io.File;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageUtils {
	
	public static final String TAG = ImageUtils.class.getSimpleName();
	
	public static Bitmap LoadBMPsdcard(String path, Resources res, int defaultImageResource){  
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
