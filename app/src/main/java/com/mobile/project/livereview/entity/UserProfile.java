package com.mobile.project.livereview.entity;

import android.location.Location;

import java.util.Map;

/**
 * Created by Raghav & Kshitij
 */

public class UserProfile
{
    private static final UserProfile ourInstance = new UserProfile();


    public static UserProfile getInstance() {
        return ourInstance;
    }
    public static Location currentLocation;
    public static String email;
    public static int Reputation = 0;
    public static String UserPassword;
    public static boolean firstTime = true;



    private void UserProfile()
    {

    }




}
