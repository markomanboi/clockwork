package com.example.clockwork_2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.clockwork_2.ui.messages.MessagesFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class addMessageStudent extends AppCompatActivity {
    List<String> getTeacherEmail, getTeacherName;
    ListView listView;
    TextView noOneToMessage;
    String currentUserName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_message_student);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
        actionBar.setElevation(0);
        actionBar.setTitle("");
        noOneToMessage=findViewById(R.id.textNoOnetoMessage);
        listView=findViewById(R.id.listView);
        populateData();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentReference docRef =  db.collection("users").document(readFileCurrentUser());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    currentUserName = document.getString("name");

                }
            }
        });


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent=new Intent(addMessageStudent.this.getApplicationContext(),MessageActivity.class);
                intent.putExtra("messengerName",getTeacherName.get(i));
                intent.putExtra("messengerEmail",getTeacherEmail.get(i));
                intent.putExtra("currentUsername",currentUserName);
                startActivity(intent);
            }
        });

    }
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    public void populateData(){
        //---------------------------Populate Arraylists-----------------------------------------------
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(readFileCurrentUser()).collection("classes").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                getTeacherName=new ArrayList<>();
                getTeacherEmail=new ArrayList<>();

                for (final QueryDocumentSnapshot document : task.getResult()){
                    if (!getTeacherEmail.contains(document.getString("classOwner"))) {
                        getTeacherEmail.add(document.getString("classOwner"));
                    }
                    db.collection("users").document(document.getString("classOwner")).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (document.exists()){
                                DocumentSnapshot documentSnapshot=task.getResult();
                                getTeacherName.add(documentSnapshot.getString("name"));
                            }
                            //----------------------ListView---------------------------------------
                            if (getTeacherName.size()!=0){
                                MyAdapter myAdapter = new MyAdapter();
                                listView.setAdapter(myAdapter);

                                if (listView.getCount()!=0)
                                    noOneToMessage.setVisibility(View.GONE);
                                else
                                    noOneToMessage.setVisibility(View.VISIBLE);
                            }
                            //---------------------------------------------------------------------------
                        }
                    });
                }
            }
        });

    }
    class MyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return getTeacherEmail.size();
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
            View v= LayoutInflater.from(addMessageStudent.this).inflate(R.layout.listviewmodelmessageactivity,null);
            TextView student=v.findViewById(R.id.txtName);
            TextView email=v.findViewById(R.id.txtMessage);

            student.setText(getTeacherName.get(i)+" • Teacher");
            email.setText(getTeacherEmail.get(i));

            return v;
        }
    }
    private String readFileCurrentUser()
    {
        String myData = "";
        File path = addMessageStudent.this.getExternalFilesDir(null);
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