package com.mobile.project.livereview;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
import com.mobile.project.livereview.entity.MarkerLocation;
import com.mobile.project.livereview.entity.UserLocation;
import com.mobile.project.livereview.entity.UserProfile;
import com.mobile.project.livereview.login.LoginActivity;

import java.util.LinkedList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private static int user_range = 30;

    List<UserLocation> userLocations = new LinkedList<>();
    List<MarkerLocation> markerLocations = new LinkedList<>();
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private GoogleMap mMap;
    private Marker touchMarker;

    SharedPreferences share;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        UserProfile.getInstance(); //init user profile
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


        /*
        On map click, allow user to broadcast a message to that location
         */
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(final LatLng latLng) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                builder.setTitle("Enter Broadcast Message");

                // Set up the input
                final EditText input = new EditText(MapsActivity.this);

                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String description;
                        description = input.getText().toString();


                        //Adding Rputation Points
                        UserProfile usr = new UserProfile();
                        //usr.addRepu(usr.email, 2);
                        usr.addRepu(2);
                        int r= usr.setReputation();

                        //Using Shared Preferences. Save the reputation points.
                        share = getSharedPreferences("reputation", Context.MODE_PRIVATE);
                        editor = share.edit();
                        editor.putString("repu",Integer.toString(r));
                        Log.d("MapSharedPref:",String.valueOf(r));
                        editor.apply();

                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .snippet(description)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).draggable(false));
                        DatabaseReference db = database.getReference();
                        String uid = db.child("marker_location").push().getKey();
                        db.child("marker_locations").child(uid).child("lat").setValue(latLng.latitude);
                        db.child("marker_locations").child(uid).child("user").setValue(auth.getCurrentUser().getUid());
                        db.child("marker_locations").child(uid).child("lng").setValue(latLng.longitude);
                        db.child("marker_locations").child(uid).child("message").setValue(description);
                        db.push();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                //dialog.getWindow().setBackgroundDrawableResource(R.color.colorPrimaryDark);
            }
        });
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(final LatLng latLng) {
                Log.i("map clicked", "");
                List<UserLocation> userInDistance = new LinkedList<>();
                Location click = new Location("Click");
                click.setLatitude(latLng.latitude);
                click.setLongitude(latLng.longitude);

                //set marker
                if(touchMarker != null)
                    touchMarker.remove();
                touchMarker = mMap.addMarker(new MarkerOptions().position(latLng));

                //get users in distance
                for(UserLocation user : userLocations) {
                    Location location = new Location(user.getUid());
                    location.setLatitude(user.getLat());
                    location.setLongitude(user.getLng());
                    float distance = location.distanceTo(click); // click.distanceTo(location);
                    if(distance <= 2*user_range) {
                        userInDistance.add(user);
                    }
                }

                //notify users
                notifyUsers(userInDistance);
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Log.e("Maps", "marker clicked");
                marker.setTitle(" ");
                marker.showInfoWindow();
                return true;
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

        DatabaseReference marker_db = database.getReference().child("marker_locations");
        marker_db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChildren()) {
                    Log.i("marker location ", "no data");
                    return;
                }

                markerLocations.clear();
                for(DataSnapshot entry : dataSnapshot.getChildren()) {
                    MarkerLocation markerLocation = entry.getValue(MarkerLocation.class);
                    markerLocations.add(markerLocation);
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
                    .fillColor(getColor(R.color.colorAccent60))
                    .strokeWidth(0)
            );
        }
        for(MarkerLocation location : markerLocations) {
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(location.getLat(), location.getLng()))
                    .snippet(location.getMessage())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).draggable(false));
        }

    }

    public void UserProfile(View w)
    {
        Intent callUser = new Intent(MapsActivity.this,displayuser.class);
        startActivity(callUser);
    }
    public void onSignOut(View v) {
        signOut();
    }

    public void onShowMessaging(View v) {
        Intent intent = new Intent(MapsActivity.this, MessagingInbox.class);
        startActivity(intent);
    }

    public void onDiscover(View v) {
        Intent intent = new Intent(MapsActivity.this, DiscoverActivity.class);
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
