/*
 * Copyright (C) 2011 Jonathan Steele
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.itnoles.knightfootball;

import android.content.Intent;
import android.net.NetworkInfo;

import com.itnoles.shared.service.AbstractSyncService;
import com.itnoles.shared.io.WorksheetsHandler;

public class SyncService extends AbstractSyncService {
    private static final String WORKSHEET_URL = "https://spreadsheets.google.com/feeds/worksheets/0AvRfIfyMiQAGdFowOThSZGs5OXpQMnpvdEJSc29TWHc/public/basic";

    @Override
    protected void onHandleIntent(Intent intent) {
        /**
         * Check to see if we are connected to a data or wifi network.
         * if false, return early or execute XML
         */
        final NetworkInfo activeNetwork = mManager.getActiveNetworkInfo();
        if (!(activeNetwork != null && activeNetwork.isConnectedOrConnecting())) {
            return;
        }
        mRemoteExecutor.executeWithPullParser(WORKSHEET_URL, new WorksheetsHandler(mRemoteExecutor));
    }
}