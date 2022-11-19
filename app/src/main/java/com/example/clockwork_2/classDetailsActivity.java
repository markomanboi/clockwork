package com.example.clockwork_2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class classDetailsActivity extends AppCompatActivity {
    List<String> getNames, getEmail;
    ListView listView;
    TextView noOneHere;
    String className,classCode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_details);

        className=getIntent().getStringExtra("className");
        classCode=getIntent().getStringExtra("classCode");
        ActionBar actionBar=getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
        actionBar.setElevation(0);
        actionBar.setTitle("");
        noOneHere=findViewById(R.id.textNoStudents);
        listView=findViewById(R.id.listView);
        TextView txtClassName=findViewById(R.id.textClassName);
        txtClassName.setText(className+" Class");
        populateData(classCode);

    }

    public void populateData(final String classCode){
        final String[] classOwnerEmail = {""};

        final FirebaseFirestore db=FirebaseFirestore.getInstance();

        db.collection("classes").document(classCode).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                getNames=new ArrayList<>();
                getEmail=new ArrayList<>();
                DocumentSnapshot documentSnapshot=task.getResult();
                if (documentSnapshot.exists()){
                    classOwnerEmail[0] =documentSnapshot.getString("classOwner");
                    db.collection("users").document(classOwnerEmail[0]).collection("classes")
                            .document(classCode).collection("Students").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            for (QueryDocumentSnapshot document: task.getResult()){
                                getNames.add(document.getId());
                                getEmail.add(document.getString("studentEmail"));
                                Log.d("TESTING", getNames.toString());
                                //----------------------ListView---------------------------------------
                                if (getNames.size()!=0){
                                    MyAdapter myAdapter = new MyAdapter();
                                    listView.setAdapter(myAdapter);

                                    if (listView.getCount()!=0)
                                        noOneHere.setVisibility(View.GONE);
                                    else
                                        noOneHere.setVisibility(View.VISIBLE);
                                }
                                //---------------------------------------------------------------------------
                            }
                        }
                    });
                }
            }
        });
    }
    class MyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return getNames.size();
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
            View v= LayoutInflater.from(classDetailsActivity.this).inflate(R.layout.listviewmodelclassdetails,null);
            TextView name=v.findViewById(R.id.txtName);
            TextView email=v.findViewById(R.id.txtEmail);
            name.setText(getNames.get(i));
            email.setText(getEmail.get(i));

            return v;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}