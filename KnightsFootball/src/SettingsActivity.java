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

import android.os.Bundle;

import com.itnoles.shared.activities.AbstractSettingsActivity;

public class SettingsActivity extends AbstractSettingsActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNewsPref.setEntries(R.array.listNames);
        mNewsPref.setEntryValues(R.array.listValues);

        final String getValueArray = getResources().getStringArray(R.array.listValues)[0];
        final String getNewsURL = mNewsPref.getSharedPreferences().getString(SP_KEY_NEWS_URL, getValueArray);
        mNewsPref.setValueIndex(mNewsPref.findIndexOfValue(getNewsURL));

        mNewsPref.setSummary(mNewsPref.getEntry());
    }
}