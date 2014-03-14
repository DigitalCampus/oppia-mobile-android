/*
 * From Fedor, 
 * see:
 * http://stackoverflow.com/questions/541966/how-do-i-do-a-lazy-load-of-images-in-listview/3068012#3068012
 * https://github.com/thest1/LazyList
 * 
 *  Released under MIT license
 */

package org.digitalcampus.oppia.utils.lazylist;

import java.io.InputStream;
import java.io.OutputStream;

public class Utils {
    public static void CopyStream(InputStream is, OutputStream os)
    {
        final int buffer_size=1024;
        try
        {
            byte[] bytes=new byte[buffer_size];
            for(;;)
            {
              int count=is.read(bytes, 0, buffer_size);
              if(count==-1)
                  break;
              os.write(bytes, 0, count);
            }
        }
        catch(Exception ex){}
    }
}