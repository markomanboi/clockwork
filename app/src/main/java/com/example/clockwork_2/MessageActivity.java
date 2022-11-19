package com.example.clockwork_2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.clockwork_2.ui.messages.MessagesFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MessageActivity extends AppCompatActivity {
    List<String> getMessengerNames, getMessages, getTime,getDate;
    ListView listView;
    EditText message;
    String messengerName,messengerEmail,messengerClass, currentUserName;
    Long incrementMessageCount, messageCount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        messengerName=getIntent().getStringExtra("messengerName");
        messengerEmail=getIntent().getStringExtra("messengerEmail");
        messengerClass=getIntent().getStringExtra("messengerClass");
        currentUserName=getIntent().getStringExtra("currentUsername");


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef =  db.collection("users").document(readFileCurrentUser());
        if (currentUserName==null){
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        currentUserName = document.getString("name");

                    }
                }
            });
        }
        //---------------------If no message yet, initialize message in database-----------------------
        initializeMessageDatabase(messengerName, messengerEmail);
        //---------------------------------------------------------------------------------------------
        populateData();
        ActionBar actionBar=getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
        actionBar.setElevation(0);
        setTitle(messengerName);
        listView = findViewById(R.id.listMessageActivity);
        listView.setDivider(null);
        ImageView btnSend=findViewById(R.id.btnSend);
        message=findViewById(R.id.messageText);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
                message.getText().clear();
                message.onEditorAction(EditorInfo.IME_ACTION_DONE);
            }
        });

        db.collection("users").document(readFileCurrentUser())
                .collection("messages")
                .document(messengerName).collection("chatRoom").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                populateData();
            }
        });
    }

    public void sendMessage(){
        final String txtMessage=message.getText().toString();
        Calendar calendar=Calendar.getInstance();
        SimpleDateFormat df1 = new SimpleDateFormat("MM-dd-yyyy");
        SimpleDateFormat df2 = new SimpleDateFormat("HH:mm a");
        final String formattedDate=df1.format(calendar.getTime());
        final String formattedTime=df2.format(calendar.getTime());
        long time= System.currentTimeMillis();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef =   db.collection("users").document(readFileCurrentUser()).collection("messages")
                .document(messengerName);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    messageCount=(Long) document.get("messageCount");
                    incrementMessageCount=messageCount+1;
                    final Map<String, Object> sendMsg = new HashMap<>();
                    sendMsg.put("date", formattedDate);
                    sendMsg.put("message", txtMessage);
                    sendMsg.put("name", currentUserName);
                    sendMsg.put("time", formattedTime);
                    sendMsg.put("index", incrementMessageCount);

                    db.collection("users").document(readFileCurrentUser())
                            .collection("messages").document(messengerName).collection("chatRoom")
                            .document(Long.toString(incrementMessageCount)).set(sendMsg)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("Firebase", "Message saved to user");
                                }
                            });

                    db.collection("users").document(messengerEmail)
                            .collection("messages").document(currentUserName).collection("chatRoom")
                            .document(Long.toString(incrementMessageCount)).set(sendMsg)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("Firebase", "Message saved to other user");
                                }
                            });

                    db.collection("users").document(readFileCurrentUser())
                            .collection("messages").document(messengerName).update("messageCount",incrementMessageCount)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("Firebase", "Updated message count to user");
                                }
                            });

                    db.collection("users").document(messengerEmail)
                            .collection("messages").document(currentUserName).update("messageCount",incrementMessageCount)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("Firebase", "Updated message count to other user");
                                }
                            });

                    final Map<String, Object> random = new HashMap<>();
                    random.put("ree", "ree");
                    db.collection("users").document(readFileCurrentUser()).collection("messages")
                            .document("databaseChanged").collection("random").add(random);

                    db.collection("users").document(messengerEmail).collection("messages")
                            .document("databaseChanged").collection("random").add(random);
                }
            }
        });
    }

    public void initializeMessageDatabase(final String studentName, final String studentEmail){
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef =  db.collection("users").document(readFileCurrentUser()).collection("messages")
                .document(studentName);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (!document.exists()) {
                        DocumentReference docRef2 =  db.collection("users").document(readFileCurrentUser()).collection("messages")
                                .document(studentName);
                        final Map<String, Object> initMsg = new HashMap<>();
                        initMsg.put("email", studentEmail);
                        initMsg.put("messageCount", 0);
                        docRef2.set(initMsg).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                            }
                        });

                        final Map<String, Object> initMsg2 = new HashMap<>();
                        initMsg2.put("email", readFileCurrentUser());
                        initMsg2.put("messageCount", 0);
                        docRef2 =  db.collection("users").document(studentEmail).collection("messages")
                                .document(currentUserName);
                        docRef2.set(initMsg2).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("FIREBASE", "New message instance initialized.");

                        final Map<String, Object> random = new HashMap<>();
                                random.put("ree", "ree");
                                db.collection("users").document(readFileCurrentUser()).collection("messages")
                                        .document("databaseChanged").collection("random").add(random);

                                db.collection("users").document(messengerEmail).collection("messages")
                                        .document("databaseChanged").collection("random").add(random);
                            }
                        });
                    }
                }
            }
        });
    }

    public void populateData(){
        //---------------------------Populate Arraylists-----------------------------------------------
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(readFileCurrentUser()).collection("messages")
                .document(messengerName).collection("chatRoom").orderBy("index").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                getMessengerNames=new ArrayList<>();
                getMessages=new ArrayList<>();
                getDate=new ArrayList<>();
                getTime=new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()){
                    getMessengerNames.add(document.getString("name"));
                    getMessages.add(document.getString("message"));
                    getDate.add(document.getString("date"));
                    getTime.add(document.getString("time"));
                }
                //----------------------ListView---------------------------------------
                if (getMessages.size()!=0) {
                    MyAdapter myAdapter = new MyAdapter();
                    listView.setAdapter(myAdapter);
                }
                //---------------------------------------------------------------------------
            }
        });
    }

    public static String getRandomNumberString() {
        // It will generate 6 digit random Number.
        // from 0 to 999999
        Random rnd = new Random();
        int number = rnd.nextInt(999999);

        // this will convert any number sequence into 6 character.
        return String.format("%06d", number);
    }

    class MyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return getMessengerNames.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View v= LayoutInflater.from(MessageActivity.this).inflate(R.layout.listviewmodelmessageactivity,null);
            TextView messengerName=v.findViewById(R.id.txtName);
            TextView txtMessage=v.findViewById(R.id.txtMessage);

            messengerName.setText(getMessengerNames.get(i)+" • "+getDate.get(i)+" "+getTime.get(i));
            txtMessage.setText(getMessages.get(i));

            return v;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    private String readFileCurrentUser()
    {
        String myData = "";
        File path = MessageActivity.this.getExternalFilesDir(null);
        File myExternalFile = new File(path, "/currentUserClockwork.txt");
        try {
            FileInputStream fis = new FileInputStream(myExternalFile);
            DataInputStream in = new DataInputStream(fis);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            String strLine;
            while ((strLine = br.readLine()) != null) {
                myData = myData + strLine;
            }
            br.close();
            in.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return myData;
    }
}