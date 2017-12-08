package com.mobile.project.livereview.entity;

import android.location.Location;

/**
 * Created by raaghav on 12/6/2017.
 */

public class UserProfile {
    private static final UserProfile ourInstance = new UserProfile();

    public static UserProfile getInstance() {
        return ourInstance;
    }
    public static Location currentLocation;
    private UserProfile() {

    }
}
