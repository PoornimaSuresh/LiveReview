package com.mobile.project.livereview;
/*
   Created by Raghav
*/
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mobile.project.livereview.entity.MarkerLocation;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class MessagingInbox extends AppCompatActivity {

    SwipeRefreshLayout swipeRefreshLayout;
    ListView listViewDiscover;
    List<MarkerLocation> discoverData = new LinkedList();
    private FirebaseDatabase database;
    DiscoverListAdapter listAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);
        swipeRefreshLayout = findViewById(R.id.swiperefresh);
        listViewDiscover = findViewById(R.id.listDiscover);
        database = FirebaseDatabase.getInstance();

        updateDiscoverList();
        listAdapter = new DiscoverListAdapter(this, R.layout.discover_list_item_layout, discoverData);
        listViewDiscover.setAdapter(listAdapter);
        listViewDiscover.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //TextView tv_topic = (TextView) findViewById(R.id.textViewMessage);
                TextView tv_topic = view.findViewById(R.id.textViewMessage);

                String topic = tv_topic.getText().toString();
                Log.e("discover", topic);
                Intent intent = new Intent(MessagingInbox.this, MessagingActivity.class);
                if (topic != null){
                    intent.putExtra("topic",topic);
                }
                startActivity(intent);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Toast.makeText(MessagingInbox.this, "called refresh", Toast.LENGTH_SHORT).show();
                updateDiscoverList();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void updateDiscoverList() {
        DatabaseReference topics = database.getReference().child("text_messages");
        Log.e("discover ", "start list update");
        topics.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChildren()) {
                    Log.i("marker location ", "no data");
                    return;
                }

                discoverData.clear();

                for (DataSnapshot entry : dataSnapshot.getChildren()) {

                    String topic =entry.getKey(); //entry.getValue().toString();
                    MarkerLocation markerLocation = new MarkerLocation();
                    markerLocation.setMessage(topic);
                    String firstMessage = "";
                    for(DataSnapshot messages : entry.getChildren())
                    {
                        firstMessage = messages.child("message").getValue(String.class);
                        break;
                    }
                    markerLocation.setAddress(firstMessage); //not using address here
                    discoverData.add(markerLocation);
                }

                listAdapter.notifyDataSetChanged();
                Log.e("discover ", " list updated " + discoverData.size());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("db error map", databaseError.getMessage());
            }
        });
    }



}
