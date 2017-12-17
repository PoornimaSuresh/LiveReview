package com.mobile.project.livereview.entity;

import android.location.Location;

import java.util.Map;

/**
 * Created by raaghav on 12/6/2017.
 */

public class UserProfile
{
    private static final UserProfile ourInstance = new UserProfile();


    public static UserProfile getInstance() {
        return ourInstance;
    }
    public static Location currentLocation;
    public static String email;
    public static int Reputation;
    public static String UserPassword;
    public static Map<String, String> UserData;

   /*
    public void UserProfile()
    {
        Map<String,String>UserData = new HashMap<>();
    }

    //Saving data of new Users.
    public void addUserData(String str)
    {
        if(UserData.containsKey(str))
        {
            Log.d("UserCheck :","User Present");
        }
        else
        {
            UserData.put(str,Integer.toString(0));
            Log.d("UserCheck :","User created "+str);
        }
    }

    public String getReputationForUser(String user)
    {
        String pts = null;
        if(UserData.containsKey(user))
        {
            pts = UserData.get(user);         //Getting the Reputation points for the existing user.
        }
        else
        {
            addUserData(user);
            getReputationForUser(user);
        }

        return pts;
    }
    */

    public void addRepu(int points)
    {
        //Check for valid user
        //String pts = getReputationForUser(user);

        //int data = Integer.parseInt(pts);
        Reputation  += points;
    }

    public int setReputation()
    {
        return Reputation;
    }       // Returns the number of reputation points


}
