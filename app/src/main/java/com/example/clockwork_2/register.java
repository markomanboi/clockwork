package com.example.clockwork_2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class register extends AppCompatActivity {
    EditText Name, Email,SchoolName,Password,ConfirmPassword;
    RadioButton teacher,student;
    String userType="";
    Button register;
    ImageView centerRegister;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
        actionBar.setElevation(0);
        actionBar.setTitle("");

        Name=findViewById(R.id.txtBoxName);
        Email=findViewById(R.id.txtBoxEmail);
        SchoolName=findViewById(R.id.txtBoxSchoolName);
        Password=findViewById(R.id.txtBoxPassword);
        ConfirmPassword=findViewById(R.id.txtBoxPasswordConfirm);

        teacher=findViewById(R.id.rbRegisterTeacher);
        student=findViewById(R.id.rbRegisterStudent);

        register=findViewById(R.id.btnRegister);
        centerRegister=findViewById(R.id.btnImageRegister);
        teacher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (student.isChecked()){
                    student.setChecked(false);
                }
                userType="Teacher";
            }
        });

        student.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (teacher.isChecked()){
                    teacher.setChecked(false);
                }
                userType="Student";
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addData();
            }
        });

        centerRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addData();
            }
        });
    }

    public void addData() {
        String name = Name.getText().toString();
        final String email = Email.getText().toString();
        final String password = Password.getText().toString();
        String schoolName=SchoolName.getText().toString();
        String confirmPassword=ConfirmPassword.getText().toString();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()|| schoolName.isEmpty() || userType.equals("")) {
            Toast.makeText(register.this, "Please fill out required fields!", Toast.LENGTH_SHORT).show();
        }
        else if (!password.equals(confirmPassword)){
            Toast.makeText(register.this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
        }
        else if (password.length()<6){
            Toast.makeText(register.this, "Minimum of 6 characters for pasword!", Toast.LENGTH_SHORT).show();
        }
        else{

            final FirebaseFirestore db = FirebaseFirestore.getInstance();
            final Map<String, Object> user = new HashMap<>();
            user.put("name", name);
            user.put("email", email);
            user.put("schoolName", schoolName);
            user.put("userType", userType);
            DocumentReference docRef = db.collection("users").document(email);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Toast.makeText(register.this, "Email already exists!", Toast.LENGTH_SHORT).show();
                        } else {
                            FirebaseAuth mAuth=FirebaseAuth.getInstance();
                            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(register.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    // If sign in fails, display a message to the user. If sign in succeeds
                                    // the auth state listener will be notified and logic to handle the
                                    // signed in user can be handled in the listener.
                                    if (!task.isSuccessful()) {
                                        // Show the message task.getException()
                                        Log.d("Firebase", "Account failed",task.getException());
                                    }
                                    else
                                    {
                                        Log.d("Firebase", "Account created");
                                        db.collection("users").document(email)
                                                .set(user)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        sendVerificationEmail();
                                                        Toast.makeText(register.this, "Verification Email Sent!", Toast.LENGTH_SHORT).show();
                                                        startActivity(new Intent(register.this, MainActivity.class));
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.d("Testing", "ERROR");
                                                        Toast.makeText(register.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                }
                            });

                        }
                    }
                }
            });


        }
    }
    private void sendVerificationEmail()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // email sent


                            // after email is sent just logout the user and finish this activity
                            FirebaseAuth.getInstance().signOut();
                        }
                        else
                        {
                            // email not sent, so display message and restart the activity or do whatever you wish to do

                            //restart this activity
                            Toast.makeText(register.this, "Something went wrong! Email not sent!", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }



}
