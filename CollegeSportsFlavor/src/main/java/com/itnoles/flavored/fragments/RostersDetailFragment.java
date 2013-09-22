/*
 * Copyright (C) 2013 Jonathan Steele
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.itnoles.flavored.fragments;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.itnoles.flavored.AbstractContentListLoader;
import com.itnoles.flavored.Utils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class RostersDetailFragment extends ListFragment implements LoaderManager.LoaderCallbacks<List<String>> {
    private static final String LOG_TAG = "RostersDetailFragment";

    private ArrayAdapter<String> mAdapter;

    public static RostersDetailFragment newInstance(String urlString) {
        RostersDetailFragment f = new RostersDetailFragment();

        // Supply url input as an argument.
        Bundle args = new Bundle();
        args.putString("url", urlString);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String title = getArguments().getString("title");
        if (title != null) {
            getActivity().getActionBar().setTitle(title);
        }

        mAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);
        setListAdapter(mAdapter);

        // Prepare the loader. Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(21, getArguments(), this);
    }

    @Override
    public Loader<List<String>> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.
        return new RostersDetailLoader(getActivity(), args.getString("url"));
    }

    @Override
    public void onLoadFinished(Loader<List<String>> loader, List<String> data) {
        // Set the new data in the adapter.
        mAdapter.addAll(data);
    }

    @Override
    public void onLoaderReset(Loader<List<String>> loader) {
        // Clear the data in the adapter.
        mAdapter.clear();
    }

    private class RostersDetailLoader extends AbstractContentListLoader<String> {
        public RostersDetailLoader(Context context, String url) {
            super(context, url);
        }

        /**
         * This is where the bulk of our work is done. This function is
         * called in a background thread and should generate a new set of
         * data to be published by the loader.
         */
        @Override
        public List<String> loadInBackground() {
            JsonReader jsonReader = null;
            try {
                InputStreamReader reader = Utils.openUrlConnection(mURL);
                jsonReader = new JsonReader(reader);
                return readRosters(jsonReader);
            } catch (IOException ioe) {
                Log.w(LOG_TAG, "Problem on i/o", ioe);
            } finally {
                Utils.closeQuietly(jsonReader);
            }
            return null;
        }

        private List<String> readRosters(JsonReader reader) throws IOException {
            List<String> results = new ArrayList<String>();
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                boolean notNull = reader.peek() != JsonToken.NULL;
                if ("experience".equals(name) && notNull) {
                    results.add("Experience: " + reader.nextString());
                } else if ("eligibility".equals(name) && notNull) {
                    results.add("Class: " + reader.nextString());
                } else if ("height".equals(name) && notNull) {
                    results.add("Height: " + reader.nextString());
                } else if ("weight".equals(name) && notNull) {
                    results.add("Weight: " + reader.nextString());
                } else if ("hometown".equals(name) && notNull) {
                    results.add("Hometown: " + reader.nextString());
                } else if ("position_event".equals(name)) {
                    results.add(reader.nextString().replace("=>", ": "));
                } else {
                    reader.skipValue();
                }
            }
            reader.endObject();
            return results;
        }
    }
}