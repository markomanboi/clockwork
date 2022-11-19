package com.example.clockwork_2.Sinch;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.clockwork_2.R;
import com.example.clockwork_2.landingPage;
import com.sinch.android.rtc.calling.Call;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class PlaceCallActivity extends BaseActivity {

    private Button mCallButton;
    private EditText mCallName;
    String targetEmail,targetName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        targetEmail=getIntent().getStringExtra("targetEmail");
        targetName=getIntent().getStringExtra("targetName");

        mCallName = (EditText) findViewById(R.id.callName);
        mCallButton = (Button) findViewById(R.id.callButton);
        mCallButton.setEnabled(false);
        mCallButton.setOnClickListener(buttonClickListener);

        mCallName.setText(targetName);
        Button stopButton = (Button) findViewById(R.id.stopButton);
        stopButton.setOnClickListener(buttonClickListener);

        if (readFileCurrentUserType().equals("Student")){
            mCallButton.setEnabled(false);
            mCallButton.setText("Please wait for the teacher to call...");
        }


    }

    @Override
    protected void onServiceConnected() {
        TextView userName = (TextView) findViewById(R.id.loggedInName);
        userName.setText(getSinchServiceInterface().getUserName());
        mCallButton.setEnabled(true);
        if (readFileCurrentUserType().equals("Student")){
            mCallButton.setEnabled(false);
        }
    }

    private void stopButtonClicked() {
        if (getSinchServiceInterface() != null) {
            getSinchServiceInterface().stopClient();
        }
        finish();
    }

    private void callButtonClicked() {
        String userName = targetEmail;
        if (userName.isEmpty()) {
            Toast.makeText(this, "Please enter a user to call", Toast.LENGTH_LONG).show();
            return;
        }

        Call call = getSinchServiceInterface().callUserVideo(userName);
        String callId = call.getCallId();

        Intent callScreen = new Intent(this, CallScreenActivity.class);
        callScreen.putExtra(SinchService.CALL_ID, callId);
        startActivity(callScreen);
    }

    private OnClickListener buttonClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.callButton:
                    callButtonClicked();
                    break;

                case R.id.stopButton:
                    stopButtonClicked();
                    break;

            }
        }
    };

    private String readFileCurrentUserType()
    {
        String myData = "";
        File path = PlaceCallActivity.this.getExternalFilesDir(null);
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
