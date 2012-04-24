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

package com.itnoles.nolesfootball;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.itnoles.shared.fragments.AbstractHeadlinesFragment;

public class HeadlinesFragment extends AbstractHeadlinesFragment {
    @Override
    protected String getNewsURL() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return prefs.getString("newsurl_preference", "http://www.seminoles.com/sports/m-footbl/headline-rss.xml");
    }
}