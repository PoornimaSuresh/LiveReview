package com.mobile.project.livereview;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mobile.project.livereview.entity.UserLocation;

import java.util.LinkedList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private static int user_range = 15;

    List<UserLocation> userLocations = new LinkedList<>();
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private GoogleMap mMap;
    private Marker touchMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i("places", "Place: " + place.getName());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 18));

            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("places", "An error occurred: " + status);
            }
        });

        startService(new Intent(this, BroadcastService.class));
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (!(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            mMap.setMyLocationEnabled(true);
        }

        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                requestLocations();
            }
        });


        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.i("map clicked", "");

                //set marker
                if(touchMarker != null)
                    touchMarker.remove();

                touchMarker = mMap.addMarker(new MarkerOptions().position(latLng));

                //get users in distance
                List<UserLocation> userInDistance = new LinkedList<>();
                Location click = new Location("Click");
                click.setLatitude(latLng.latitude);
                click.setLongitude(latLng.longitude);

                for(UserLocation user : userLocations) {
                    Location location = new Location(user.getUid());
                    location.setLatitude(user.getLat());
                    location.setLongitude(user.getLng());

                    float distance = click.distanceTo(location);
                    if(distance <= 2*user_range) {
                        userInDistance.add(user);
                    }
                }

                //notify users
                notifyUsers(userInDistance);
            }
        });
    }

    public void requestLocations() {
        String uid = "anonymous";

        if(auth.getCurrentUser() != null) {
            uid = auth.getCurrentUser().getUid();
        }

        DatabaseReference db = database.getReference().child("locations");
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChildren()) {
                    Log.i("location recovery", "no data");
                    return;
                }

                userLocations.clear();
                for(DataSnapshot entry : dataSnapshot.getChildren()) {
                   UserLocation userLocation = entry.getValue(UserLocation.class);
                   userLocations.add(userLocation);
                }

                redrawMap();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("db error map", databaseError.getMessage());
            }
        });
    }

    private void redrawMap() {
        mMap.clear();

        for(UserLocation location : userLocations) {
            mMap.addCircle(new CircleOptions()
                    .center(new LatLng(location.getLat(), location.getLng()))
                    .radius(user_range)
                    .fillColor(Color.BLUE)
            );
        }
    }

//    public void requestLocations() {
//        final String url = "https://us-central1-livereview-b4382.cloudfunctions.net/locations?";
//
//        float zoom = mMap.getCameraPosition().zoom;
//        LatLng curr = mMap.getCameraPosition().target;
//        String query = "lat=" + curr.latitude + "&lng=" + curr.longitude + "&zoom=" + zoom;
//
//        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url+query, null, new Response.Listener<JSONObject>() {
//            @Override
//            public void onResponse(JSONObject response) {
//                try {
//                    JSONArray locations = response.getJSONArray("locations");
//                    mMap.clear();
//
//                    for(int i = 0; i < locations.length(); i++) {
//                        JSONObject entry = locations.getJSONObject(i);
//                        double lat = entry.getDouble("lat");
//                        double lng = entry.getDouble("lng");
//                        mMap.addCircle(new CircleOptions().center(new LatLng(lat, lng)).radius(2).fillColor(Color.BLUE));
//                    }
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                //mTxtDisplay.setText("Response: " + response.toString());
//            }
//        }, new Response.ErrorListener() {
//
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                // TODO Auto-generated method stub
//            }
//        });
//
//        RequestService.getInstance(this).addToRequestQueue(jsObjRequest);
//    }

    public void onSignOut(View v) {
        signOut();
    }

    public void onShowMessaging(View v) {
        Intent intent = new Intent(MapsActivity.this, MessagingActivity.class);
        startActivity(intent);
    }

    //sign out method
    public void signOut() {
        auth.signOut();

        Intent intent = new Intent(MapsActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void notifyUsers(List<UserLocation> userInDistance) {
        //TODO implement
    }
}
