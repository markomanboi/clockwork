package com.example.clockwork_2.ui.messages;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.clockwork_2.MessageActivity;
import com.example.clockwork_2.R;
import com.example.clockwork_2.addMessageStudent;
import com.example.clockwork_2.addMessageTeacher;
import com.example.clockwork_2.landingPage;
import com.example.clockwork_2.ui.schedule.HomeFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MessagesFragment extends Fragment {
    List<String>  getMessengerNames, getClassNames,getMessagePreview, getTime,getEmail,storMessageCount,getMessenger;
    List<Long> getMessageCount;
    private MessagesViewModel messagesViewModel;
    ListView listView;
    View root;
    TextView txtNoMessages;
    String currentUsername;
    FirebaseFirestore db;
    landingPage emailOfLoggedIn;
    String email;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        getMessengerNames=new ArrayList<>();
        getMessenger=new ArrayList<>();
        getEmail=new ArrayList<>();
        getMessageCount=new ArrayList<>();
        getMessagePreview=new ArrayList<>();
        getTime=new ArrayList<>();
        messagesViewModel =
                ViewModelProviders.of(this).get(MessagesViewModel.class);
        root = inflater.inflate(R.layout.fragment_messages, container, false);
        ImageView btnAddMessage=root.findViewById(R.id.btnAddMessage);
        txtNoMessages=root.findViewById(R.id.textNoMessages);
        listView = root.findViewById(R.id.listMessages);
        btnAddMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (readFileCurrentUserType().equals("Teacher"))
                    startActivity(new Intent(getActivity().getApplicationContext(), addMessageTeacher.class));
                else
                    startActivity(new Intent(getActivity().getApplicationContext(), addMessageStudent.class));
            }
        });
        emailOfLoggedIn= (landingPage) getActivity();
        email=emailOfLoggedIn.getEmail();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent=new Intent(getActivity().getApplicationContext(),MessageActivity.class);
                intent.putExtra("messengerName",getMessengerNames.get(i));
                intent.putExtra("messengerEmail",getEmail.get(i));
                startActivity(intent);

            }
        });

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(email).collection("messages")
                .document("databaseChanged").collection("random").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                populateData();
            }
        });
        return root;
    }

    public void populateData(){
        getMessengerNames=new ArrayList<>();
        getMessenger=new ArrayList<>();
        getEmail=new ArrayList<>();
        getMessageCount=new ArrayList<>();
        getMessagePreview=new ArrayList<>();
        getTime=new ArrayList<>();

        //---------------------------Populate Arraylists-----------------------------------------------
        db = FirebaseFirestore.getInstance();
        db.collection("users").document(email).collection("messages").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                getMessengerNames.clear();
                getMessenger.clear();
                getEmail.clear();
                getMessageCount.clear();

                getMessagePreview.clear();
                getTime.clear();
                for (final QueryDocumentSnapshot document : task.getResult()){

                        getMessengerNames.add(document.getId());
                        getEmail.add(document.getString("email"));
                        getMessageCount.add((Long) document.get("messageCount"));


                }
                for (int i=0; i<getMessageCount.size(); i++){
                    DocumentReference docRef =  db.collection("users").document(email)
                            .collection("messages").document(getMessengerNames.get(i)).collection("chatRoom")
                            .document(String.valueOf(getMessageCount.get(i)));
                    final int finalI = i;
                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();

                                if (getMessageCount.get(finalI)==0){
                                    getMessenger.add(document.getString("name"));
                                    getMessagePreview.add(null);
                                    getTime.add(document.getString("time"));
                                }
                                else{
                                    getMessenger.add(document.getString("name"));
                                    getMessagePreview.add(document.getString("message"));
                                    getTime.add(document.getString("time"));
                                }
                                Log.d("LOOP",getMessagePreview.toString());
                                //----------------------ListView---------------------------------------
                                if (getMessagePreview.size()==getMessengerNames.size()) {
                                    MyAdapter myAdapter = new MyAdapter();
                                    listView.setAdapter(myAdapter);

                                    if (listView.getCount() != 0)
                                        txtNoMessages.setVisibility(View.GONE);
                                    else
                                        txtNoMessages.setVisibility(View.VISIBLE);
                                }
                                //---------------------------------------------------------------------------
                            }
                        }
                    });
                }

            }
        });

    }



    private String readFileCurrentUserType()
    {
        String myData = "";
        File path = getActivity().getExternalFilesDir(null);
        File myExternalFile = new File(path, "/currentUserType.txt");
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

          class MyAdapter extends BaseAdapter {

            @Override
            public int getCount() {
                return getMessagePreview.size();
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
                View v= LayoutInflater.from(getActivity().getApplicationContext()).inflate(R.layout.listviewmodelmessage,null);
                TextView messengerName=v.findViewById(R.id.txtName);
                TextView className=v.findViewById(R.id.txtClass);
                TextView messagePreview=v.findViewById(R.id.txtMessagePreview);
                TextView time=v.findViewById(R.id.txtTime);

                messengerName.setText(getMessengerNames.get(i));
                if (readFileCurrentUserType().equals("Teacher"))
                    className.setText("Student");
                else
                    className.setText("Teacher");
                if (getMessagePreview.get(i)==null) {
                    messagePreview.setText("You haven't sent a message to this person!");
                }
                else{
                    if (getMessagePreview.get(i).length() > 40) {
                        messagePreview.setText(getMessenger.get(i) + ":" + getMessagePreview.get(i).substring(0, 40) + "...");
                    } else {
                        messagePreview.setText(getMessenger.get(i) + ":" + getMessagePreview.get(i));
                    }
                }


                time.setText(getTime.get(i));
                return v;
            }


    }



}