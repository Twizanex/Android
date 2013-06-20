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

package com.itnoles.flavored.fragment;

import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.itnoles.flavored.R;
import com.itnoles.flavored.SectionedListAdapter;
import com.itnoles.flavored.VolleyHelper;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static com.itnoles.flavored.BuildConfig.SCHEDULE_URL;

public class ScheduleFragment extends ListFragment {
    private static final String LOG_TAG = "ScheduleFragment";

    private String header;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // The SectionedListAdapter is going to show schedule header for overall and conference records
        final SectionedListAdapter adapter = new SectionedListAdapter(getActivity(), R.layout.list_section_header);
        setListAdapter(adapter);

        // If this is under tablet, hide detail view.
        View detailsFrame = getActivity().findViewById(R.id.fragment_details);
        if (detailsFrame != null) {
            detailsFrame.setVisibility(View.GONE);
        }

        StringRequest sr = new StringRequest(SCHEDULE_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                List<Schedule> data = getScheduleResult(response);
                adapter.addSection(header, new ScheduleListAdapter(getActivity(), data));
                adapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v(LOG_TAG, "VolleyError", error);
            }
        });
        VolleyHelper.getResultQueue().add(sr);
    }

    private List<Schedule> getScheduleResult(String response) {
        List<Schedule> results = new ArrayList<Schedule>();
        JsonReader reader = new JsonReader(new StringReader(response));
        try {
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if ("header".equals(name)) {
                    header = reader.nextString();
                } else if ("schedule".equals(name)) {
                    reader.beginArray();
                    while (reader.hasNext()) {
                        results.add(readScheduleObject(reader));
                    }
                    reader.endArray();
                }
            }
            reader.endObject();
        } catch (IOException e) {
           Log.w(LOG_TAG, "Problem on reading on file", e);
        } finally {
            try {
                reader.close();
            } catch (IOException ioe) {
                Log.w(LOG_TAG, "Can't close JsonReader", ioe);
            }
        }
        return results;
    }

    private Schedule readScheduleObject(JsonReader reader) throws IOException {
        String date = null;
        String ht = null;
        String hs = null;
        String at = null;
        String as = null;
        String status = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if ("date".equals(name)) {
                date = reader.nextString();
            } else if ("at".equals(name)) {
                at = reader.nextString();
            } else if ("as".equals(name)) {
                as = reader.nextString();
            } else if ("ht".equals(name)) {
                ht = reader.nextString();
            } else if ("hs".equals(name)) {
                hs = reader.nextString();
            } else if ("status".equals(name)) {
                status = reader.nextString();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return new Schedule(date, ht, hs, at, as, status);
    }

    static class Schedule {
        public String date;
        public String ht;
        public String hs;
        public String at;
        public String as;
        public String status;

        Schedule(String date_, String ht_, String hs_, String at_, String as_, String status_) {
            date = date_;
            ht = ht_;
            hs = hs_;
            at = at_;
            as = as_;
            status = status_;
        }
    }

    private class ScheduleListAdapter extends ArrayAdapter<Schedule> {
        public ScheduleListAdapter(Context context, List<Schedule> data) {
            super(context, 0, data);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // A ViewHolder keeps references to children views to avoid unneccessary calls
            // to findViewById() on each row.
            ViewHolder holder;

            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.schedule_item, parent, false);

                holder = new ViewHolder();
                holder.date = (TextView) convertView.findViewById(R.id.date);
                holder.at = (TextView) convertView.findViewById(R.id.away_team);
                holder.as = (TextView) convertView.findViewById(R.id.away_score);
                holder.ht = (TextView) convertView.findViewById(R.id.home_team);
                holder.hs = (TextView) convertView.findViewById(R.id.home_score);
                holder.status = (TextView) convertView.findViewById(R.id.status);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Schedule item = getItem(position);
            holder.date.setText(item.date);
            holder.at.setText(item.at);
            holder.as.setText(item.as);
            holder.ht.setText(item.ht);
            holder.hs.setText(item.hs);
            holder.status.setText(item.status);

            return convertView;
        }
    }

    static class ViewHolder {
        TextView date;
        TextView at;
        TextView as;
        TextView ht;
        TextView hs;
        TextView school;
        TextView status;
    }
}