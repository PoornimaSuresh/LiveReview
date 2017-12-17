package com.mobile.project.livereview;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mobile.project.livereview.entity.UserProfile;
/*
Created by Kshitij Chhatwani.
 */
public class displayuser extends AppCompatActivity {

    private static final String TAG = "displayuser";
    TextView Name;
    TextView Email;
    TextView Reputation;
    Button edit_btn;
    String EmailIn;
    UserProfile user;
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
        //Log.d(TAG, "displayProfile: "+EmailIn);

        //Name = (TextView) findViewById(R.id.DisplayName);
        Email = (TextView) findViewById(R.id.DisplayEmail);
        Reputation = (TextView) findViewById(R.id.DisplayRepuPoints);
        edit_btn = (Button) findViewById(R.id.EditProfileActivity);
        user = new UserProfile();
        displayProfile();

    }

    public void displayProfile()
    {
        /*
         Obtain the User data from the database and display it here
        */

        Log.d(TAG, "displayProfile: "+UserProfile.email);
        Log.d("userPassword :", UserProfile.UserPassword);

        share = getSharedPreferences("reputation", Context.MODE_PRIVATE);
        String value = share.getString("repu","");
        Log.d(" FromSharedPrefere : ",value);

        Email.setText(UserProfile.email);
        Reputation.setText(value);

        edit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent editActivity = new Intent(displayuser.this, editUserProfile.class);
                startActivity(editActivity);
            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        displayProfile();

        Toast.makeText(displayuser.this,UserProfile.email,Toast.LENGTH_SHORT);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
