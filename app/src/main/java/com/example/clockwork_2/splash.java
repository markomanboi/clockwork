package com.example.clockwork_2;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class splash extends AppCompatActivity {
    ProgressBar progressBar;
    int progress=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        setContentView(R.layout.activity_splash);

        progressBar=findViewById(R.id.progressBar2);

        setProgressValue(progress);
    }
    private void setProgressValue(final int progress) {

        // set the progress
        progressBar.setProgress(progress);
        // thread is used to change the progress value
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                setProgressValue(progress + 10);
            }
        });
        thread.start();

        if (progress==100) {
            if (!readFileCurrentUser().equals("")){
                startActivity(new Intent(getApplicationContext(), landingPage.class));
                finish();
            }
           else{
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        }
    }

    private String readFileCurrentUser()
    {
        String myData = "";
        File path = splash.this.getExternalFilesDir(null);
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
    private String readFileCurrentUserType()
    {
        String myData = "";
        File path = splash.this.getExternalFilesDir(null);
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
}
