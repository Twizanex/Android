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

import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.*; // Menu, MenuInflater, MenuItem and View
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.itnoles.flavored.*;
import com.itnoles.flavored.activities.RostersDetailActivity;
import com.itnoles.flavored.model.Rosters;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

public class RostersFragment extends ListFragment implements LoaderManager.LoaderCallbacks<List<Rosters>>, SearchView.OnQueryTextListener {
    private static final String LOG_TAG = "RostersFragment";

    private boolean mDualPane;
    private int mShownCheckPosition = -1;
    private SectionedListAdapter mAdapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setListShownNoAnimation(true);

        // We have a menu item to show in action bar.
        setHasOptionsMenu(true);

        // The SectionedListAdapter has a header to group players and staff
        mAdapter = new SectionedListAdapter(getActivity());

        // Determine whether we are in single-pane or dual-pane mode by testing the visibility
        // of the detail view.
        View detailsFrame = getActivity().findViewById(R.id.fragment_details);
        if (detailsFrame != null && detailsFrame.getVisibility() != View.VISIBLE) {
            detailsFrame.setVisibility(View.VISIBLE);
        }
        mDualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;

        if (mDualPane) {
            // In dual-pane mode, the list view highlights the selected item.
            getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        }

        // Prepare the loader. Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(20, getArguments(), this);
    }

    @Override
    public Loader<List<Rosters>> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.
        return new RostersLoader(getActivity(), args.getString("url"));
    }

    @Override
    public void onLoadFinished(Loader<List<Rosters>> loader, List<Rosters> data) {
        // Set the new data in the adapter.
        List<Rosters> playerRosters = new ArrayList<Rosters>();
        List<Rosters> staffRosters = new ArrayList<Rosters>();

        for (Rosters roster : data) {
            if (roster.isThisStaff()) {
                staffRosters.add(roster);
            } else {
                playerRosters.add(roster);
            }
        }

        mAdapter.addSection("2012 Athlete Roster", new RostersListAdapter(getActivity(), playerRosters));
        mAdapter.addSection("2012 Coaches and Staff", new RostersListAdapter(getActivity(), staffRosters));
        setListAdapter(mAdapter);
    }

    @Override
    public void onLoaderReset(Loader<List<Rosters>> loader) {
        // Clear the data in the adapter.
        mAdapter.clear();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // inflate the menu
        inflater.inflate(R.menu.roster_fragment, menu);

        // find the search item
        MenuItem searchViewMenuItem = menu.findItem(R.id.menu_search);

        // Retrieve the Search View
        SearchView searchView = (SearchView) searchViewMenuItem.getActionView();
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        // Called when the action bar search text has changed. Since this
        // is a simple array adapter, we can just have it do the filtering.
        ((RostersListAdapter) mAdapter.getListAdapter(0)).getFilter().filter(newText);
        ((RostersListAdapter) mAdapter.getListAdapter(1)).getFilter().filter(newText);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        // Don't care about this.
        return true;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Rosters item = (Rosters) getListAdapter().getItem(position);
        if (mDualPane) {
            // We can display everything in-place with fragments, so update
            // the list to highlight the selected item and show the data.
            getListView().setItemChecked(position, true);

            if (mShownCheckPosition != position) {
                // If we are not currently showing a fragment for the new
                // position, we need to create and install a new one.
                RostersDetailFragment rdf = RostersDetailFragment.newInstance(item.getFullURL());

                // Execute a transaction, replacing any existing fragment
                // with this one inside the frame.
                getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_details, rdf)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
                mShownCheckPosition = position;
            }
        } else {
            Intent intent = new Intent(getActivity(), RostersDetailActivity.class);
            intent.putExtra("url", item.getFullURL());
            intent.putExtra("title", item.getFirstAndLastName());
            startActivity(intent);
        }
    }

    private class RostersLoader extends AbstractContentListLoader<Rosters> {
        public RostersLoader(Context context, String url) {
            super(context, url);
        }

        /**
         * This is where the bulk of our work is done. This function is
         * called in a background thread and should generate a new set of
         * data to be published by the loader.
         */
        @Override
        public List<Rosters> loadInBackground() {
            InputStreamReader reader = null;
            try {
                reader = Utils.openUrlConnection(mURL);
                // create a XMLReader from SAXParser
                XMLReader xmlReader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
                // Create a RostersHandler
                RostersHandler handler = new RostersHandler();
                // Store handler in XMLReader
                xmlReader.setContentHandler(handler);
                // the process starts with a character stream
                xmlReader.parse(new InputSource(reader));
                // Get Results
                return handler.getResults();
            } catch (IOException ioe) {
                Log.w(LOG_TAG, "Problem on I/O", ioe);
            } catch (SAXException saxe) {
                Log.w(LOG_TAG, "Problem on SAX parsing", saxe);
            } catch (ParserConfigurationException pce) {
                // Ignore
            } finally {
                Utils.closeQuietly(reader);
            }
            return null;
        }
    }

    private class RostersListAdapter extends ArrayAdapter<Rosters> {
        public RostersListAdapter(Context context, List<Rosters> data) {
            super(context, 0, data);
        }

        /**
         * Populate new items in the list.
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.rosters_item, parent, false);
            }

            Rosters item = getItem(position);

            RostersTextView fullname = ViewHolder.get(convertView, R.id.fullName);
            fullname.setText(item.getFirstName(), item.getLastName());

            TextView rosterPos = ViewHolder.get(convertView, R.id.position);
            rosterPos.setText(item.getPosition());

            return convertView;
        }
    }
}