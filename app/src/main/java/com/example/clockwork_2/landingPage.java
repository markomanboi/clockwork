package com.example.clockwork_2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;


public class landingPage extends AppCompatActivity {
    private static final int GALLERY_REQUEST_CODE=123;
    private AppBarConfiguration mAppBarConfiguration;
    String userName;
    TextView toolbarTitle;
    String userType;
    ImageView imageView;
    String imageRef;

    public String getEmail() {
        return email;
    }

    String email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userType=readFileCurrentUserType();
        if (userType.equals("Teacher"))
            setContentView(R.layout.activity_landing_page_teacher);
        else {
            setContentView(R.layout.activity_landing_page_student);
        }
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbarTitle=findViewById(R.id.toolbar_title);
        email=readFileCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentReference docRef =  db.collection("users").document(readFileCurrentUser());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    userName = document.getString("name");
                    toolbarTitle.setText("Welcome, "+userName+"!");
                }
            }
        });

        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_schedule, R.id.nav_classes, R.id.nav_messages)
                .setDrawerLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        getSupportActionBar().setTitle("");
        toolbarTitle.setText("Welcome, "+userName+"!");
        navigationView.getMenu().findItem(R.id.nav_schedule).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                toolbar.setTitle("");
                toolbarTitle.setText("Welcome, "+userName+"!");
                return false;
            }
        });
        navigationView.getMenu().findItem(R.id.nav_classes).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                toolbar.setTitle("");
                toolbarTitle.setText("");
                return false;
            }
        });
        navigationView.getMenu().findItem(R.id.nav_messages).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                toolbar.setTitle("");
                toolbarTitle.setText("");
                return false;
            }
        });

        if (readFileCurrentUserType().equals("Teacher")){
            navigationView.getMenu().findItem(R.id.nav_statistics).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    toolbar.setTitle("");
                    toolbarTitle.setText("");
                    return false;
                }
            });
        }
        navigationView.getMenu().findItem(R.id.nav_logout).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                saveUserLogin();
                saveUserType();
                Intent intent = new Intent(landingPage.this, MainActivity.class);
                startActivity(intent);
                finish();
                return false;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.landing_page_teacher, menu);
        imageView=findViewById(R.id.imageViewPhoto);
        final TextView drawerTextName=findViewById(R.id.drawerTextName);
        final TextView drawerTextSchool=findViewById(R.id.drawerTextSchool);
        TextView drawerTextUserType=findViewById(R.id.drawerTextUserType);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        final DocumentReference docRef =  db.collection("users").document(readFileCurrentUser());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    drawerTextName.setText(document.getString("name"));
                    drawerTextSchool.setText( document.getString("schoolName"));
                }
            }
        });
        drawerTextUserType.setText(readFileCurrentUserType());

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Pick an image"),GALLERY_REQUEST_CODE);
            }
        });
        StorageReference StorageRef = FirebaseStorage.getInstance().getReference();
        final StorageReference imagesRef = StorageRef.child("ClockworkImages" + "/" + readFileCurrentUser()+ ".jpg");
        imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                imageRef = uri.toString();
                Glide.with(getApplicationContext())
                        .load(imageRef)
                        .into(imageView);
            }
        });
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode==GALLERY_REQUEST_CODE && resultCode==RESULT_OK && data!=null){
            StorageReference StorageRef = FirebaseStorage.getInstance().getReference();
            Uri file=data.getData();
            final StorageReference imagesRef = StorageRef.child("ClockworkImages" + "/" + readFileCurrentUser()+ ".jpg");
            imagesRef.putFile(file)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> storageReference = FirebaseStorage.getInstance().getReference().child("ClockworkImages" + "/" + readFileCurrentUser() + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    imageRef = uri.toString();
                                    Glide.with(getApplicationContext())
                                            .load(imageRef)
                                            .into(imageView);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            // ...
                        }
                    });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private String readFileCurrentUser()
    {
        String myData = "";
        File path = landingPage.this.getExternalFilesDir(null);
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
        File path = landingPage.this.getExternalFilesDir(null);
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

    public void saveUserLogin() {
        try {
            File path = landingPage.this.getExternalFilesDir(null);
            File file = new File(path , "/currentUserClockwork.txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter writer = new FileWriter(file);
            writer.append("");
            writer.flush();
            writer.close();
            Log.d("TextFile","User removed");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void saveUserType() {
        try {
            File path = landingPage.this.getExternalFilesDir(null);
            File file = new File(path , "/currentUserType.txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter writer = new FileWriter(file);
            writer.append("");
            writer.flush();
            writer.close();
            Log.d("TextFile","User removed");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}