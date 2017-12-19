package com.mobile.project.livereview;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.view.LayoutInflater;
import com.mobile.project.livereview.entity.MarkerLocation;

import java.util.LinkedList;
import java.util.List;
/*
   Created by Raghav
*/

public class DiscoverListAdapter extends ArrayAdapter<MarkerLocation> {


    List<MarkerLocation> data;
    public DiscoverListAdapter(Context context, int layoutId, List<MarkerLocation> locations)
    {
        super(context, layoutId, locations);
        data = locations;
    }

    @Override
    public View getView(int index, View convertView, ViewGroup parent) {
        // Get the data item for this position

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.discover_list_item_layout, parent, false);
        }
        // Lookup view for data population
        TextView title = (TextView) convertView.findViewById(R.id.textViewMessage);
        TextView description = (TextView) convertView.findViewById(R.id.textViewAddress);
        // Populate the data into the template view using the data object
        title.setText(data.get(index).getMessage());
        description.setText(data.get(index).getAddress());
        Log.e("Created View", "Created");
        return convertView;

    }

}
