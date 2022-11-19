package com.example.clockwork_2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    EditText email, password;
    Button btnLogin;
    String userType="";
    String checkUserType="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        email=findViewById(R.id.email_text_box);
        password=findViewById(R.id.password_textbox);
        btnLogin=findViewById(R.id.btnLogin);


        TextView btnRegister=findViewById(R.id.btnRegister);
        final TextView resetPassword=findViewById(R.id.btnForgotPassword);

        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, resetPassword.class));
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), register.class));
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (email.getText().toString().isEmpty() || password.getText().toString().isEmpty()){
                    Toast.makeText(MainActivity.this, "Username or password incorrect!", Toast.LENGTH_SHORT).show();
                }
                else{
                    login();
                }
            }
        });

    }

    public void login(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentReference docRef =  db.collection("users").document(email.getText().toString());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    checkUserType = document.getString("userType");
                    if (checkUserType==null)
                        checkUserType="No user";

                    if (email.getText().toString().isEmpty() || password.getText().toString().isEmpty()){
                        Toast.makeText(MainActivity.this, "Username or password incorrect!", Toast.LENGTH_SHORT).show();
                    }
                    else if (checkUserType.equals("No user")){
                        Toast.makeText(MainActivity.this, "Incorrect username or password!", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        final FirebaseAuth mAuth=FirebaseAuth.getInstance();
                        mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        //Log.d("TAG", "signInWithEmail:onComplete:" + task.isSuccessful());

                                        // If sign in fails, display a message to the user. If sign in succeeds
                                        // the auth state listener will be notified and logic to handle the
                                        // signed in user can be handled in the listener.
                                        if (!task.isSuccessful()) {
                                            Toast.makeText(MainActivity.this, "User not verified or incorrect username or password!", Toast.LENGTH_SHORT).show();
                                            Log.w("TAG", "signInWithEmail:failed", task.getException());

                                        } else {
                                            checkIfEmailVerified();
                                        }
                                        // ...
                                    }
                                });
                    }
                }
            }
        });

    }
    private void checkIfEmailVerified()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user.isEmailVerified())
        {
            // user is verified, so you can finish this activity or send user to activity which you want.
            Toast.makeText(MainActivity.this, "Successfully logged in", Toast.LENGTH_SHORT).show();
            saveUserLogin(email.getText().toString());
            saveUserType(checkUserType);
            Intent intent=new Intent(this, landingPage.class);
            startActivity(intent);


        }
        else
        {
            // email is not verified, so just prompt the message to the user and restart this activity.
            // NOTE: don't forget to log out the user.
            Toast.makeText(MainActivity.this, "User not verified or incorrect username or password!", Toast.LENGTH_SHORT).show();
            FirebaseAuth.getInstance().signOut();

            //restart this activity

        }
    }

    public void saveUserLogin(String sBody) {
        try {
            File path = MainActivity.this.getExternalFilesDir(null);
            File file = new File(path , "/currentUserClockwork.txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter writer = new FileWriter(file);
            writer.append(sBody);
            writer.flush();
            writer.close();
            Log.d("TextFile","textfile created");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void saveUserType(String sBody) {
        try {
            File path = MainActivity.this.getExternalFilesDir(null);
            File file = new File(path , "/currentUserType.txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter writer = new FileWriter(file);
            writer.append(sBody);
            writer.flush();
            writer.close();
            Log.d("TextFile","textfile created");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
