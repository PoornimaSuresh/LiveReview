package com.mobile.project.livereview;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mobile.project.livereview.entity.UserProfile;

/*
 Created by Kshitij Chhatwani
 */

public class EditUserProfile extends AppCompatActivity {

    private static final String TAG ="New Password " ;
    TextView StaticEmail;
    //EditText change_Email;
    EditText CurrentPassword;
    EditText ChangePassword;
    Button EditData;

    String email_data;
    String data_current;
    String data_newPass;

    Button Change_Data;

    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_profile);

        //Call Firebase to display User Profile of User
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        //Log.d("Current User",user.toString());

        database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference();

        Intent calledFromUserProfile = getIntent();

        StaticEmail = (TextView) findViewById(R.id.Email_Display);
        StaticEmail.setText(UserProfile.email);


        CurrentPassword = (EditText) findViewById(R.id.Existing_Password);
        data_current =  CurrentPassword.getText().toString();

        ChangePassword =(EditText) findViewById(R.id.New_Password);
        data_newPass = ChangePassword.getText().toString();

        //Log.d("New password: ",data_newPass);

        final AuthCredential credential = EmailAuthProvider
                .getCredential(UserProfile.email,UserProfile.UserPassword);

        EditData = (Button) findViewById(R.id.ChangeData);
        EditData.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {

                /*
                Since I am checking the inout Password with the stored password, The re-authentication function is called here itself.
                Hence, no neccessity to sign out and again sign-in. If the user enters a invalid current password, the update failed to take place.
                 */
                if (UserProfile.UserPassword.equals(CurrentPassword.getText().toString()))
                {
                    user.reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        user.updatePassword(ChangePassword.getText().toString())
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            //Log.d(TAG,"User Password changed");
                                                            Toast.makeText(EditUserProfile.this, "Password is Updated", Toast.LENGTH_SHORT).show();

                                                        } else {
                                                            //Log.d(TAG,"Password Not Updated");
                                                            Toast.makeText(EditUserProfile.this, "Password Failed to Update", Toast.LENGTH_SHORT).show();
                                                        }

                                                    }
                                                });

                                    } else {
                                        //Log.d("User Authentication", "Auth Failed");
                                        Toast.makeText(EditUserProfile.this," Authentication Failed",Toast.LENGTH_SHORT);
                                    }
                                }
                            });

                }
                else
                {
                    Toast.makeText(EditUserProfile.this," Invalid Current Password. Operation Failed",Toast.LENGTH_SHORT);
                }
            }
        });

    }
}
