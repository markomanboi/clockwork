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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.clockwork_2.ui.classes.ClassesFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class addClassTeacher extends AppCompatActivity {
    EditText textBoxName;
    Spinner spinnerDay, spinnerHour1, spinnerHour2, spinnerMinute1,spinnerMinute2;
    String classCode="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_class);
        //-----Declaration------------------------'
        textBoxName=findViewById(R.id.txtBoxClassName);
        spinnerDay=findViewById(R.id.spinnerDay);
        spinnerHour1=findViewById(R.id.spinnerTimeHours1);
        spinnerHour2=findViewById(R.id.spinnerTimeHours2);
        spinnerMinute1=findViewById(R.id.spinnerTimeMinutes1);
        spinnerMinute2=findViewById(R.id.spinnerTimeMinutes2);

        //----------------------------------------

        ActionBar actionBar=getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
        actionBar.setElevation(0);
        actionBar.setTitle("");

        Spinner spinner = (Spinner) findViewById(R.id.spinnerDay);
    // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.week_days, android.R.layout.simple_spinner_item);
    // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        Spinner spinnerHours1 = (Spinner) findViewById(R.id.spinnerTimeHours1);

        ArrayAdapter<CharSequence> adapterHours = ArrayAdapter.createFromResource(this,
                R.array.time_hours, android.R.layout.simple_spinner_item);
        adapterHours.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHours1.setAdapter(adapterHours);
        try {
            Field popup = Spinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);

            // Get private mPopup member variable and try cast to ListPopupWindow
            android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(spinnerHours1);

            // Set popupWindow height to 500px
            popupWindow.setHeight(500);
        }
        catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
            // silently fail...
        }

        Spinner spinnerHours2 = (Spinner) findViewById(R.id.spinnerTimeHours2);
        spinnerHours2.setAdapter(adapterHours);
        try {
            Field popup = Spinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);

            // Get private mPopup member variable and try cast to ListPopupWindow
            android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(spinnerHours2);

            // Set popupWindow height to 500px
            popupWindow.setHeight(500);
        }
        catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
            // silently fail...
        }

        Spinner spinnerMinutes1 = (Spinner) findViewById(R.id.spinnerTimeMinutes1);
        ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this,
                R.array.time_minutes, android.R.layout.simple_spinner_item);
        adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMinutes1.setAdapter(adapterMinutes);
        try {
            Field popup = Spinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);

            // Get private mPopup member variable and try cast to ListPopupWindow
            android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(spinnerMinutes1);

            // Set popupWindow height to 500px
            popupWindow.setHeight(500);
        }
        catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
            // silently fail...
        }

        Spinner spinnerMinutes2 = (Spinner) findViewById(R.id.spinnerTimeMinutes2);
        spinnerMinutes2.setAdapter(adapterMinutes);
        try {
            Field popup = Spinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);

            // Get private mPopup member variable and try cast to ListPopupWindow
            android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(spinnerMinutes2);

            // Set popupWindow height to 500px
            popupWindow.setHeight(500);
        }
        catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
            // silently fail...
        }


        Button btnAddClass=findViewById(R.id.btnAddClass);
        btnAddClass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addData();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    public void addData() {
        String className = textBoxName.getText().toString();
        String classDay = spinnerDay.getSelectedItem().toString();
        String time1 = spinnerHour1.getSelectedItem().toString() + ":" + spinnerMinute1.getSelectedItem().toString();
        String time2 = spinnerHour2.getSelectedItem().toString() + ":" + spinnerMinute2.getSelectedItem().toString();

        if (className.isEmpty()) {
            Toast.makeText(addClassTeacher.this, "Please fill out required fields!", Toast.LENGTH_SHORT).show();
        } else if (time1.equals(time2)) {
            Toast.makeText(addClassTeacher.this, "Class can't have the same start and end time!", Toast.LENGTH_SHORT).show();
        } else {
            classCode=getRandomNumberString();
            final FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("classes").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    List<String> list = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        list.add(document.getId());
                    }
                    while (list.contains(classCode)==false){
                        classCode=getRandomNumberString();
                    }
                }
            });

            final Map<String, Object> classDataEmpirical = new HashMap<>();
            classDataEmpirical.put("classOwner", readFileCurrentUser());
            classDataEmpirical.put("className", className);
            classDataEmpirical.put("classDay", classDay);
            classDataEmpirical.put("time", time1 + "-" + time2);

            DocumentReference docRef = db.collection("classes").document(classCode);
            docRef.set(classDataEmpirical)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("Firebase: ","Written class data to empirical.");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(addClassTeacher.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                        }
                    });

            final Map<String, Object> classDataUser = new HashMap<>();
            classDataUser.put("className", className);
            classDataUser.put("classDay", classDay);
            classDataUser.put("time", time1 + "-" + time2);
            classDataUser.put("classOwner", readFileCurrentUser());
            DocumentReference docRef2 = db.collection("users").document(readFileCurrentUser())
                    .collection("classes").document(classCode);
            docRef2.set(classDataUser)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(addClassTeacher.this, "Class added!", Toast.LENGTH_SHORT).show();
                            Log.d("Firebase: ","Written class data to user.");
                            AlertDialog.Builder builder = new AlertDialog.Builder(addClassTeacher.this);
                                builder.setMessage("The code for this class is:"+classCode);
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        AlertDialog.Builder builder2 = new AlertDialog.Builder(addClassTeacher.this);
                                        builder2.setMessage("Add another class?");
                                        builder2.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                finish();
                                                startActivity(new Intent(addClassTeacher.this, addClassTeacher.class));
                                            }
                                        });
                                        builder2.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                Toast.makeText(addClassTeacher.this, "Class added!", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(addClassTeacher.this, landingPage.class));
                                            }
                                        });
                                        builder2.show();
                                    }
                                });
                            builder.show();


                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(addClassTeacher.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
    private String readFileCurrentUser()
    {
        String myData = "";
        File path = addClassTeacher.this.getExternalFilesDir(null);
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
    public static String getRandomNumberString() {
        // It will generate 6 digit random Number.
        // from 0 to 999999
        Random rnd = new Random();
        int number = rnd.nextInt(999999);

        // this will convert any number sequence into 6 character.
        return String.format("%06d", number);
    }
}