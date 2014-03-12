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

package org.digitalcampus.oppia.task;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import org.digitalcampus.oppia.listener.GetImageListener;
import org.digitalcampus.oppia.utils.ImageUtils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

public class GetImageTask extends AsyncTask<Payload, Object, Payload> {

	public static final String TAG = GetImageTask.class.getSimpleName();
	protected Context ctx;
	private GetImageListener imageListener;

	public GetImageTask(Context ctx) {
		this.ctx = ctx;
	}

	@Override
	protected Payload doInBackground(Payload... params) {

		Payload payload = params[0];

		try {
			Drawable d = ImageUtils.getDrawableFromUrl(payload.getUrl(), "");
			ArrayList<Object> al = new ArrayList<Object>();
			al.add(d);
			payload.setResponseData(al);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return payload;
	}

	@Override
	protected void onPostExecute(Payload response) {
		synchronized (this) {
			if (imageListener != null) {
				imageListener.downloadComplete(response);
			}
		}
	}

	public void setGetImageListener(GetImageListener gil) {
		synchronized (this) {
			imageListener = gil;
		}
	}
}
