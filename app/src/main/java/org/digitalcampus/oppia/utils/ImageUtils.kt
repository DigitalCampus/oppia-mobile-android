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
package org.digitalcampus.oppia.utils

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import org.digitalcampus.mobile.learning.R
import java.io.File

object ImageUtils {

    val TAG = ImageUtils::class.simpleName

    @JvmStatic
    fun loadBMPsdcard(path: String, res: Resources, defaultImageResource: Int): BitmapDrawable {
        val imageFile = File(path)
        val size = res.getDimensionPixelSize(R.dimen.course_actionbar_icon_size)

        return try {
            // if the file exists
            if (imageFile.exists()) {
                var bmp = BitmapFactory.decodeFile(path)
                // scale the bitmap to the target size
                bmp = Bitmap.createScaledBitmap(bmp!!, size, size, false)
                BitmapDrawable(res, bmp)
            } else {
                // return the standard 'Image not found' bitmap
                var bmp = BitmapFactory.decodeResource(res, defaultImageResource)
                bmp = Bitmap.createScaledBitmap(bmp!!, size, size, false)
                BitmapDrawable(res, bmp)
            }
        } catch (oome: OutOfMemoryError) {
            val bmp = BitmapFactory.decodeResource(res, defaultImageResource)
            BitmapDrawable(res, bmp)
        }
    }

    init {
        throw IllegalStateException("Utility class")
    }

}