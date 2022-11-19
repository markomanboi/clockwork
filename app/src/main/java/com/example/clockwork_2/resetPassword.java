package com.example.clockwork_2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import org.w3c.dom.Text;

public class resetPassword extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
        actionBar.setElevation(0);
        actionBar.setTitle("");

        final EditText email=findViewById(R.id.txtBoxEmail);
        Button btnSendResetMail=findViewById(R.id.btnResetPassword);

        btnSendResetMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (email.getText().toString().equals("")){
                    Toast.makeText(resetPassword.this, "User does not exist!", Toast.LENGTH_SHORT).show();
                }
                else{
                    sendResetMail(email.getText().toString());
                }
            }
        });
    }
    public void sendResetMail(String email){
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(resetPassword.this, "Email Sent!", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                        else{
                            Toast.makeText(resetPassword.this, "User does not exist!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}