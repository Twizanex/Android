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

import android.app.AlertDialog;
import android.content.Context;
import android.net.*;

public class Utilities {
	public static Boolean NetworkCheck(Context context)
	{
		final ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo info = cm.getActiveNetworkInfo();
		Boolean mIsConnected = false;
		if (info != null) {
			mIsConnected = (info.getState() == NetworkInfo.State.CONNECTED);
		}
		return mIsConnected;
	}
	
	public static void showAlertView(Context context, int title, int message)
	{
		AlertDialog.Builder ad = new AlertDialog.Builder(context);
		ad.setTitle(title);
		ad.setMessage(message);
		ad.setPositiveButton(R.string.OK, null);
		ad.show();
	}
}