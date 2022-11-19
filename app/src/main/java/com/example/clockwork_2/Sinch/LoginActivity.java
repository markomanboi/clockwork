package com.example.clockwork_2.Sinch;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.appcompat.app.ActionBar;

import com.example.clockwork_2.R;
import com.example.clockwork_2.landingPage;
import com.sinch.android.rtc.SinchError;

public class LoginActivity extends BaseActivity implements SinchService.StartFailedListener {

    private Button mLoginButton;
    private EditText mLoginName;
    private ProgressDialog mSpinner;
    String currentEmail,targetEmail,targetName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        currentEmail=getIntent().getStringExtra("currentEmail");
        targetEmail=getIntent().getStringExtra("targetEmail");
        targetName=getIntent().getStringExtra("targetName");
        mLoginName = (EditText) findViewById(R.id.loginName);
        mLoginName.setText(currentEmail);
        mLoginButton = (Button) findViewById(R.id.loginButton);
        mLoginButton.setEnabled(false);
        mLoginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                loginClicked();
            }
        });

        ImageView backbutton=findViewById(R.id.backbutton);
        backbutton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, landingPage.class));
            }
        });
    }


    @Override
    protected void onServiceConnected() {
        mLoginButton.setEnabled(true);
        getSinchServiceInterface().setStartListener(this);
    }

    @Override
    protected void onPause() {
        if (mSpinner != null) {
            mSpinner.dismiss();
        }
        super.onPause();
    }

    @Override
    public void onStartFailed(SinchError error) {
        Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show();
        if (mSpinner != null) {
            mSpinner.dismiss();
        }
    }

    @Override
    public void onStarted() {
        openPlaceCallActivity();
    }

    private void loginClicked() {
        String userName = mLoginName.getText().toString();

        if (userName.isEmpty()) {
            Toast.makeText(this, "Please enter a name", Toast.LENGTH_LONG).show();
            return;
        }

        if (!userName.equals(getSinchServiceInterface().getUserName())) {
            getSinchServiceInterface().stopClient();
        }

        if (!getSinchServiceInterface().isStarted()) {
            getSinchServiceInterface().startClient(userName);
            showSpinner();
        } else {
            openPlaceCallActivity();
        }
    }

    private void openPlaceCallActivity() {

        Intent mainActivity = new Intent(this, PlaceCallActivity.class);
        mainActivity.putExtra("targetEmail",targetEmail);
        mainActivity.putExtra("targetName",targetName);
        startActivity(mainActivity);
    }

    private void showSpinner() {
        mSpinner = new ProgressDialog(this);
        mSpinner.setTitle("Logging in");
        mSpinner.setMessage("Please wait...");
        mSpinner.show();
    }
}
