package com.mobile.project.livereview;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mobile.project.livereview.entity.ChatBubble;

import java.util.ArrayList;
import java.util.List;

public class MessagingActivity extends AppCompatActivity {

    private ListView listView;
    private View btnSend;
    private EditText editText;
    boolean myMessage = true;
    private List<ChatBubble> ChatBubbles;
    private ArrayAdapter<ChatBubble> adapter;
    String messageTopic;

    //Firebase
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    //private FirebaseListAdapter<ChatBubble> chatAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        Intent receiveTopicIntent = getIntent();
        if (receiveTopicIntent != null){
            messageTopic = receiveTopicIntent.getStringExtra("topic");
        }

        if (messageTopic == null) {
            messageTopic = "something";
        }

        //Log.v("MessagingAct", messageTopic);

        if (getSupportActionBar() != null){
            getSupportActionBar().setTitle("Chat");
        }

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();


        ChatBubbles = new ArrayList<>();
        listView = (ListView) findViewById(R.id.list_msg);
        btnSend = findViewById(R.id.btn_chat_send);
        editText = (EditText) findViewById(R.id.msg_type);

        //set ListView adapter first
        adapter = new MessageAdapter(this, R.layout.left_message, ChatBubbles);
        listView.setAdapter(adapter);

        final String me = auth.getCurrentUser().getUid();

        // Populate list with messages stored on Firebase:
        DatabaseReference dbRef = database.getReference().child("text_messages").child(messageTopic);
        // Attach a listener to read the data at our posts reference
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e("Mess", "message topic: "+messageTopic);

                //If no messages in DB, return
                if(!dataSnapshot.hasChildren()) {
                    Log.i("messages ", "no data");
                    return;
                }


                /*
                long childIter = dataSnapshot.getChildrenCount();

                while (childIter > 0){
                    String message = dataSnapshot.child("message").getValue(String.class);
                    Log.e("Mess", "message: "+message);
                    boolean isMyMessage = false;
                    if (dataSnapshot.child("user").getValue() == me){
                        isMyMessage = true;
                    }
                    ChatBubble newBubble = new ChatBubble(message, isMyMessage);
                    ChatBubbles.add(newBubble);
                    childIter = childIter - 2;
                }
                */



                //For each message, check if user is me. If so, add it to chatbubble list with myMessage = true
                for(DataSnapshot entry : dataSnapshot.getChildren()) {
                    String message = entry.child("message").getValue(String.class);
                    //Log.e("Mess", "message: "+message);
                    //Log.e("messages", "user  "+entry.child("user").getValue());
                    boolean isMyMessage;
                    if (entry.child("user").getValue() == me){
                        isMyMessage = true;
                        Log.e("messages", me+" wrote message "+message+ " it's ME "+isMyMessage);
                    }else{
                        isMyMessage = false;
                        Log.e("messages", entry.child("user").getValue()+" wrote message "+message+" "+isMyMessage);
                    }
                    ChatBubble newBubble = new ChatBubble(message, isMyMessage);
                    ChatBubbles.add(newBubble);
                    adapter.notifyDataSetChanged();

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //System.out.println("The read failed: " + databaseError.getCode());
            }
        });

        //When SEND is clicked
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getText().toString().trim().equals("")) {
                    Toast.makeText(MessagingActivity.this, "Please type message. ", Toast.LENGTH_SHORT).show();
                } else {
                    //add message to list
                    String message = editText.getText().toString();
                    ChatBubble ChatBubble = new ChatBubble(message, true);
                    ChatBubbles.add(ChatBubble);
                    adapter.notifyDataSetChanged();
                    editText.setText("");


                    DatabaseReference db = database.getReference();
                    String uid = db.child("text_messages").child(messageTopic).push().getKey();
                    //Log.v("MessagingAct","me "+message+" uid "+uid+" topic "+messageTopic);

                    db.child("text_messages").child(messageTopic).child(uid).child("user").setValue(auth.getCurrentUser().getUid());
                    db.child("text_messages").child(messageTopic).child(uid).child("message").setValue(message);
                    //db.child("text_messages").child(messageTopic).child("user").setValue(auth.getCurrentUser().getUid());
                    //db.child("text_messages").child(messageTopic).child("message").setValue(message);
                    db.push();

                    /*
                    if (myMessage) {
                        myMessage = false;
                    } else {
                        myMessage = true;
                    }
                    */
                    //myMessage = !myMessage;
                }
            }
        });

    }
}
