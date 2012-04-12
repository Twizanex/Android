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

package com.itnoles.shared.io;

import android.content.ContentResolver;
import android.content.ContentProviderOperation;
import android.net.Uri;
import android.util.Log;

import com.itnoles.shared.provider.ScheduleContract;
import com.itnoles.shared.provider.ScheduleContract.Schedule;
import com.itnoles.shared.provider.ScheduleContract.Link;
import com.itnoles.shared.provider.ScheduleContract.Staff;
import com.itnoles.shared.util.Utils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static org.xmlpull.v1.XmlPullParser.END_DOCUMENT;
import static org.xmlpull.v1.XmlPullParser.START_TAG;

public class WorksheetsHandler extends XmlHandler {
    private static final String TAG = "WorksheetsHandler";

    private final RemoteExecutor mExecutor;

    public WorksheetsHandler(RemoteExecutor executor) {
        super();
        this.mExecutor = executor;
    }

    @Override
    public ArrayList<ContentProviderOperation> parse(XmlPullParser parser, ContentResolver resolver) throws XmlPullParserException, IOException {
        final HashMap<String, WorksheetEntry> sheets = new HashMap<String, WorksheetEntry>();

        // walk response, collecting all known spreadsheets
        int type;
        while ((type = parser.next()) != END_DOCUMENT) {
            if (type == START_TAG && ENTRY.equals(parser.getName())) {
                final WorksheetEntry entry = WorksheetEntry.fromParser(parser);
                Log.d(TAG, "found worksheet " + entry.toString());
                sheets.put(entry.getTitle(), entry);
            }
        }

        // consider updating each spreadsheet based on update timestamp
        considerUpdate(sheets, ScheduleContract.SCHEDULE, Schedule.CONTENT_URI, resolver);
        considerUpdate(sheets, ScheduleContract.LINK, Link.CONTENT_URI, resolver);
        considerUpdate(sheets, ScheduleContract.STAFF, Staff.CONTENT_URI, resolver);

        return new ArrayList<ContentProviderOperation>();
    }

    private void considerUpdate(HashMap<String, WorksheetEntry> sheets, String sheetName, Uri targetDir, ContentResolver resolver) {
        final WorksheetEntry entry = sheets.get(sheetName);
        if (entry == null) {
            Log.w(TAG, "Missing '" + sheetName + "' worksheet data");
            return;
        }

        final long localUpdated = Utils.queryDirUpdated(targetDir, resolver);
        final long serverUpdated = entry.getUpdated();
        Log.d(TAG, "considerUpdate() for " + entry.getTitle() + " found localUpdated=" + localUpdated + ", server=" + serverUpdated);
        if (localUpdated >= serverUpdated) {
            return;
        }

        final XmlHandler handler = createRemoteHandler(entry);
        if (handler != null) {
            final String request = entry.getListFeed();
            mExecutor.executeWithPullParser(request, handler);
        }
    }

    private XmlHandler createRemoteHandler(WorksheetEntry entry) {
        final String title = entry.getTitle();
        if (ScheduleContract.SCHEDULE.equals(title)) {
            return new ScheduleHandler();
        } else if (ScheduleContract.LINK.equals(title)) {
            return new LinkHandler();
        } else if (ScheduleContract.STAFF.equals(title)) {
            return new StaffHandler();
        }
        return null;
    }
}