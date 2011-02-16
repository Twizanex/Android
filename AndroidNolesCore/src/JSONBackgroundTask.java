//  Copyright 2010 Jonathan Steele
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.itnoles.shared;

import org.apache.http.*; // HttpEntity, HttpResponse, HttpStatus
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;

import android.os.AsyncTask;
import android.net.http.AndroidHttpClient;
import android.util.Log;

import java.io.*; // BufferedInputStream, InputStream and IOException

/**
 * JSONBackgroundTask
 * it is the class has a delegate for AsyncTask class which is for json parsing
 * @author Jonathan Steele
 */

public class JSONBackgroundTask extends AsyncTask<String, Void, JSONArray> {
	private static final String LOG_TAG = "JSONHelper";
	private JSONAsyncTaskCompleteListener callback;
	
	// Constructor
	public JSONBackgroundTask(JSONAsyncTaskCompleteListener callback) {
		this.callback = callback;
	}
	
	// perform a computation on a background thread
	@Override
	protected JSONArray doInBackground(String... params)
	{
		JSONArray json = null;
		InputStream inputStream = null;
		byte[] buffer = new byte[1024];
		final AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
		final HttpGet getRequest = new HttpGet(params[0]);
		try {
			HttpResponse response = client.execute(getRequest);
			final HttpEntity entity = response.getEntity();
			inputStream = new BufferedInputStream(entity.getContent());
			int bytesRead = 0;
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				json = new JSONArray(new String(buffer, 0, bytesRead));
			}
		} catch (Exception e) {
			getRequest.abort();
			Log.w(LOG_TAG, "bad json array", e);
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				Log.w(LOG_TAG, "can't close input stream", e);
			}
			
			if (client != null)
				client.close();
		}
		return json;
	}
	
	// Runs on the UI thread after doInBackground
	@Override
	protected void onPostExecute(JSONArray result)
	{
		callback.onTaskComplete(result);
	}
}