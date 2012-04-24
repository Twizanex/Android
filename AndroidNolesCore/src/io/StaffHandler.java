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

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.net.Uri;
import android.util.Log;

import com.itnoles.shared.provider.ScheduleProvider;
import com.itnoles.shared.util.SpreadsheetEntry;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

import static com.itnoles.shared.util.ParserUtils.queryItemUpdated;
import static org.xmlpull.v1.XmlPullParser.END_DOCUMENT;
import static org.xmlpull.v1.XmlPullParser.START_TAG;

public class StaffHandler extends XmlHandler {
    private static final String TAG = "StaffHandler";

    public StaffHandler() {
        super(ScheduleProvider.CONTENT_AUTHORITY);
    }

    @Override
    public ArrayList<ContentProviderOperation> parse(XmlPullParser parser, ContentResolver resolver) throws XmlPullParserException, IOException {
        final ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();

        // Walk document, parsing any incoming entries
        int type;
        while ((type = parser.next()) != END_DOCUMENT) {
            if (type == START_TAG && ENTRY.equals(parser.getName())) {
                // Process single spreadsheet row at a time
                final SpreadsheetEntry entry = SpreadsheetEntry.fromParser(parser);
                final Uri staffUri = Uri.withAppendedPath(ScheduleProvider.STAFF_CONTENT_URI, Uri.encode(entry.get("title")));

                // Check for existing details, only update when changed
                final long localUpdated = queryItemUpdated(staffUri, resolver);
                final long serverUpdated = entry.getUpdated();
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log.v(TAG, "found staff localUpdated=" + localUpdated + ", server=" + serverUpdated);
                }
                if (localUpdated >= serverUpdated) {
                    continue;
                }

                // Clear any existing values for this staff, treating the
                // incoming details as authoritative.
                batch.add(ContentProviderOperation.newDelete(staffUri).build());

                final ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(ScheduleProvider.STAFF_CONTENT_URI);
                builder.withValue(ScheduleProvider.UPDATED, serverUpdated);
                builder.withValue(ScheduleProvider.NAME, entry.get("title"));
                builder.withValue(ScheduleProvider.POSITIONS, entry.get(ScheduleProvider.POSITIONS));

                // Normal staff details ready, write to provider
                batch.add(builder.build());
            }
        }
        return batch;
    }
}