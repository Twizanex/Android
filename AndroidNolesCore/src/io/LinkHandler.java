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

import com.itnoles.shared.provider.ScheduleContract.Link;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

import static com.itnoles.shared.util.Utils.UPDATED;
import static com.itnoles.shared.util.Utils.queryItemUpdated;
import static org.xmlpull.v1.XmlPullParser.END_DOCUMENT;
import static org.xmlpull.v1.XmlPullParser.START_TAG;

public class LinkHandler extends XmlHandler {
    private static final String TAG = "LinkHandler";

    @Override
    public ArrayList<ContentProviderOperation> parse(XmlPullParser parser, ContentResolver resolver) throws XmlPullParserException, IOException {
        final ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();

        // Walk document, parsing any incoming entries
        int type;
        while ((type = parser.next()) != END_DOCUMENT) {
            if (type == START_TAG && ENTRY.equals(parser.getName())) {
                // Process single spreadsheet row at a time
                final SpreadsheetEntry entry = SpreadsheetEntry.fromParser(parser);
                final String title = entry.get("title");
                final Uri linkUri = Link.buildLinkUri(title);

                // Check for existing details, only update when changed
                final long localUpdated = queryItemUpdated(linkUri, resolver);
                final long serverUpdated = entry.getUpdated();
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log.v(TAG, "found link " + entry.toString());
                    Log.v(TAG, "found localUpdated=" + localUpdated + ", server=" + serverUpdated);
                }
                if (localUpdated >= serverUpdated) {
                    continue;
                }

                // Clear any existing values for this link, treating the
                // incoming details as authoritative.
                batch.add(ContentProviderOperation.newDelete(linkUri).build());

                final ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(Link.CONTENT_URI);
                builder.withValue(UPDATED, serverUpdated);
                builder.withValue(Link.NAME, title);
                builder.withValue(Link.URL, entry.get("link"));

                // Normal staff details ready, write to provider
                batch.add(builder.build());
            }
        }
        return batch;
    }
}