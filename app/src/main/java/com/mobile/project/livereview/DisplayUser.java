package com.mobile.project.livereview;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mobile.project.livereview.entity.MarkerLocation;
import com.mobile.project.livereview.entity.UserProfile;
/*
Created by Kshitij Chhatwani.
Modified by Raghav
 */
public class DisplayUser extends AppCompatActivity {

    private static final String TAG = "displayuser";
    TextView Name;
    TextView Email;
    TextView Reputation;
    Button edit_btn;
    String EmailIn;
    String reputation_points = "0";
    SharedPreferences share;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_displayuser);

        // This activity is called when the Profile Button is Clicked.

        Intent calledFromMaps = getIntent();
        EmailIn = calledFromMaps.getStringExtra("emailID");

        Email = (TextView) findViewById(R.id.DisplayEmail);
        Reputation = (TextView) findViewById(R.id.DisplayRepuPoints);
        edit_btn = (Button) findViewById(R.id.EditProfileActivity);
        fetchData();
        displayProfile();

    }

    private void fetchData() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        Log.e("Display", auth.getCurrentUser().getUid());
        DatabaseReference marker_db = database.getReference().child("users").child(auth.getCurrentUser().getUid());

        marker_db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChildren()) {
                    Log.i("Display Profile  ", "no data");
                    return;
                }
                int i=0;
                for (DataSnapshot entry : dataSnapshot.getChildren()) {
                    if(i==0)
                    {
                        i++;
                        continue;
                    }
                    int a = entry.getValue(Integer.class);
                    Log.e("Display User", "Got reputation points " + a);
                    if(UserProfile.firstTime)
                    {
                        UserProfile.Reputation = a;
                        UserProfile.firstTime=false;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("db error map", databaseError.getMessage());
            }
        });
    }

    public void displayProfile()
    {
        /*
         Obtain the User data from the database and display it here
        */

        Reputation.setText(UserProfile.Reputation + "");
        Email.setText(UserProfile.email);


        edit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent editActivity = new Intent(DisplayUser.this, EditUserProfile.class);
                startActivity(editActivity);
            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        displayProfile();

        Toast.makeText(DisplayUser.this,UserProfile.email,Toast.LENGTH_SHORT);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


}
