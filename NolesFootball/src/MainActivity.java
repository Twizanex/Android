/*
 * Copyright (C) 2012 Jonathan Steele
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

package com.itnoles.nolesfootball;

import android.os.AsyncTask;
import android.os.Bundle;

import com.itnoles.shared.activities.AbstractMainActivity;
import com.itnoles.shared.io.RemoteExecutor;

public class MainActivity extends AbstractMainActivity {
    private static final String WORKSHEET_URL = "https://spreadsheets.google.com/feeds/worksheets/0AvRfIfyMiQAGdDI4dEkwZW9XcDdqUHVOcXpzU0FqcWc/public/basic";

    // Called when the activity is first created.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle headlines = new Bundle();
        headlines.putString("title", "Top Athletics Stories");
        headlines.putString("url", "http://www.seminoles.com/sports/m-footbl/headline-rss.xml");
        onAddTab("News", NolesHeadlinesFragment.class, headlines);

        onAddTab("Schedule", ScheduleFragment.class, null);
        onAddTab("Staff", StaffFragment.class, null);

        // Load and parse the XML worksheet from Google Spreadsheet
        final AsyncTask<Void, Void, Void> doSyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                final RemoteExecutor executor = new RemoteExecutor(MainActivity.this, getContentResolver());
                executor.executeWithPullParser(WORKSHEET_URL, new WorksheetsHandler(executor), 4096);
                return null;
            }
        };
        if (hasHoneycomb()) {
            doSyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            doSyncTask.execute();
        }
    }
}