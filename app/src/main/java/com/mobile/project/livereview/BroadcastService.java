package com.mobile.project.livereview;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mobile.project.livereview.entity.UserProfile;

public class BroadcastService extends Service implements LocationListener {
    private LocationManager locationManager;
    private Runnable runnable = null;
    private Handler handler = null;
    private long minute = 5000;

    private FirebaseAuth auth;
    private FirebaseDatabase database;

    public BroadcastService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        auth = FirebaseAuth.getInstance();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        database = FirebaseDatabase.getInstance();

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                updateLocation();
                //rerun every minute
                handler.postDelayed(runnable, minute);
            }
        };

        //execute location broadcast
        updateLocation();

        //rerun every minute
        handler.postDelayed(runnable, minute);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }

    private void updateLocation() {
        Criteria criteria = new Criteria();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        String provider = locationManager.getBestProvider(criteria, true);
        locationManager.removeUpdates(this);
        locationManager.requestSingleUpdate(provider, this, null);
    }

    private void sendLocation(Location location) {
        String uid = "anonymous";

        if(auth.getCurrentUser() != null) {
            uid = auth.getCurrentUser().getUid();
        }

        DatabaseReference db = database.getReference();
        db.child("locations").child(uid).child("lat").setValue(location.getLatitude());
        db.child("locations").child(uid).child("lng").setValue(location.getLongitude());
        db.push();
        Log.e("Broadcast","Received location: " + location.toString());
        UserProfile.currentLocation = location; //update user's current location
    }

    @Override
    public void onLocationChanged(Location location) {
        sendLocation(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
