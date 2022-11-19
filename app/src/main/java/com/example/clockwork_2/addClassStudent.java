package com.example.clockwork_2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class addClassStudent extends AppCompatActivity {
    EditText classCode;
    Button addClass;
    String className,teacherEmail,teacherName, collegeName, classDay, classTime;
    String currentUser, currentUserName;
    FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_class_student);
        classCode=findViewById(R.id.txtBoxClassCode);
        addClass=findViewById(R.id.btnAddClass);
        currentUser=readFileCurrentUser();

        ActionBar actionBar=getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
        actionBar.setElevation(0);
        actionBar.setTitle("");

        addClass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!classCode.getText().toString().isEmpty()){
                    db = FirebaseFirestore.getInstance();
                    DocumentReference docRef =  db.collection("classes").document(classCode.getText().toString());
                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()){
                                    className = document.getString("className");
                                    teacherEmail=document.getString("classOwner");
                                    classDay=document.getString("classDay");
                                    classTime=document.getString("time");
                                    final DocumentReference docRefTeacher =  db.collection("users").document(teacherEmail);
                                    docRefTeacher.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task2) {
                                            if (task2.isSuccessful()) {
                                                DocumentSnapshot document2 = task2.getResult();
                                                teacherName=document2.getString("name");
                                                collegeName=document2.getString("schoolName");
                                                addData();
                                            }
                                            else{
                                                Toast.makeText(addClassStudent.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                                else{
                                    Toast.makeText(addClassStudent.this, "Class doesn't exist!", Toast.LENGTH_SHORT).show();
                                }
                            }

                            else{
                                Toast.makeText(addClassStudent.this, "Class doesn't exist!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

            }
        });
    }
    public void addData(){
        AlertDialog.Builder builder = new AlertDialog.Builder(addClassStudent.this);
        builder.setMessage("Add "+className+" class by "+teacherName+" from "+collegeName+"?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                db = FirebaseFirestore.getInstance();
                final DocumentReference getName =  db.collection("users").document(currentUser);
                getName.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            currentUserName=document.getString("name");

                            final Map<String, Object> classDataUser = new HashMap<>();
                            classDataUser.put("className", className);
                            classDataUser.put("classDay", classDay);
                            classDataUser.put("time", classTime);
                            classDataUser.put("classOwner", teacherEmail);
                            DocumentReference addToStudent = db.collection("users").document(readFileCurrentUser())
                                    .collection("classes").document(classCode.getText().toString());
                            addToStudent.set(classDataUser)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d("Firebase: ","Written class data to student user.");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(addClassStudent.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                            final Map<String, Object> classDataTeacher = new HashMap<>();
                            classDataTeacher.put("studentEmail", currentUser);
                            DocumentReference addToTeacher = db.collection("users").document(teacherEmail)
                                    .collection("classes").document(classCode.getText().toString())
                                    .collection("Students").document(currentUserName);
                            addToTeacher.set(classDataTeacher)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(addClassStudent.this, "Class Added!", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(addClassStudent.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                            startActivity(new Intent(addClassStudent.this, landingPage.class));
                        }
                    }
                });

            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    private String readFileCurrentUser()
    {
        String myData = "";
        File path = addClassStudent.this.getExternalFilesDir(null);
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