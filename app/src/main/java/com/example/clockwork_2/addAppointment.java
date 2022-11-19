package com.example.clockwork_2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.clockwork_2.ui.classes.ClassesFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class addAppointment extends AppCompatActivity {
    ArrayList<String> getClasses=new ArrayList<>(), getStudents=new ArrayList<>();
    ArrayAdapter<String> adapterStudents;
    Spinner spinnerClass, spinnerStudents;
    String selectedDate;
    String studentEmail;
    String operation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_appointment);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
        actionBar.setElevation(0);
        actionBar.setTitle("");

        spinnerClass = (Spinner) findViewById(R.id.spinnerClass);
        spinnerStudents = (Spinner) findViewById(R.id.spinnerStudent);
        Button btnAppointmentNextPage=findViewById(R.id.btnNextPageAppointment);
        ImageView btnImageAppointmentNextPage=findViewById(R.id.btnImageNextPageAppointment);

        operation=getIntent().getStringExtra("Operation");
        final String className=getIntent().getStringExtra("className");
        final String target=getIntent().getStringExtra("target");
        if (operation.equals("Reschedule")){
            TextView textView=findViewById(R.id.textView2);
            textView.setText("Reschedule Appointment");
            getStudentEmail(target,className);
            getClasses.add(className);
            getStudents.add(target);
            spinnerStudents.setEnabled(false);
            spinnerClass.setEnabled(false);
            ArrayAdapter<String> adapterClasses = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, getClasses);
            ArrayAdapter<String> adapterStudent = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, getStudents);
            adapterClasses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerClass.setAdapter(adapterClasses);
            spinnerStudents.setAdapter(adapterStudent);

            btnAppointmentNextPage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent=new Intent(addAppointment.this, addAppointment2.class);
                    intent.putExtra("Operation","Reschedule");
                    intent.putExtra("class", className);
                    intent.putExtra("student", target);
                    intent.putExtra("date", selectedDate);
                    intent.putExtra("studentEmail",studentEmail);
                    intent.putExtra("time",getIntent().getStringExtra("time"));
                    startActivity(intent);

                }
            });


            btnImageAppointmentNextPage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent=new Intent(addAppointment.this, addAppointment2.class);
                    intent.putExtra("Operation","Reschedule");
                    intent.putExtra("class", className);
                    intent.putExtra("student", target);
                    intent.putExtra("date", selectedDate);
                    intent.putExtra("studentEmail",studentEmail);
                    intent.putExtra("time",getIntent().getStringExtra("time"));
                    startActivity(intent);

                }
            });
        }
        else{
            populateArrayLists();
            getClasses.add("Select a class...");
            getStudents.add("Pick a student...");

            ArrayAdapter<String> adapterClasses = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, getClasses);
            adapterClasses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerClass.setAdapter(adapterClasses);
            try {
                Field popup = Spinner.class.getDeclaredField("mPopup");
                popup.setAccessible(true);
                android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(spinnerClass);
                popupWindow.setHeight(500);
            }
            catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
            }
            spinnerClass.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    SpinnerStudent(spinnerClass.getSelectedItem().toString());
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            spinnerStudents.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    getStudentEmail(spinnerStudents.getSelectedItem().toString(), spinnerClass.getSelectedItem().toString());
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            btnAppointmentNextPage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (spinnerClass.getSelectedItem().equals("Pick a class...") || spinnerStudents.getSelectedItem().equals("Pick a student...")){
                        Toast.makeText(addAppointment.this, "Pick a class and student!", Toast.LENGTH_SHORT).show();
                    }
                    else if (studentEmail==null){
                        Toast.makeText(addAppointment.this, "Please try again!", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Intent intent=new Intent(addAppointment.this, addAppointment2.class);
                        intent.putExtra("Operation","Normal");
                        intent.putExtra("time","");
                        intent.putExtra("class", spinnerClass.getSelectedItem().toString());
                        intent.putExtra("student", spinnerStudents.getSelectedItem().toString());
                        intent.putExtra("date", selectedDate);
                        intent.putExtra("studentEmail",studentEmail);
                        startActivity(intent);
                    }
                }
            });


            btnImageAppointmentNextPage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (spinnerClass.getSelectedItem().equals("Pick a class...") || spinnerStudents.getSelectedItem().equals("Pick a student...")){
                        Toast.makeText(addAppointment.this, "Pick a class and student!", Toast.LENGTH_SHORT).show();
                    }
                    else if (studentEmail==null){
                        Toast.makeText(addAppointment.this, "Please try again!", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Intent intent=new Intent(addAppointment.this, addAppointment2.class);
                        intent.putExtra("Operation","Normal");
                        intent.putExtra("time","");
                        intent.putExtra("class", spinnerClass.getSelectedItem().toString());
                        intent.putExtra("student", spinnerStudents.getSelectedItem().toString());
                        intent.putExtra("studentEmail",studentEmail);
                        intent.putExtra("date", selectedDate);
                        startActivity(intent);
                    }
                }
            });
        }


        final CalendarView calendarView=findViewById(R.id.calendarSetAppointment);

        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
        selectedDate = sdf.format(new Date(calendarView.getDate()));

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int dayOfMonth) {
                selectedDate = String.format("%02d",month+1)+"-"+String.format("%02d",dayOfMonth)+"-"+String.valueOf(year);
            }
        });

    }
    public void SpinnerStudent(final String className){
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(readFileCurrentUser()).collection("classes").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                List<String> list = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    list.add(document.getId());
                    if (document.getString("className").equals(className)){
                        db.collection("users").document(readFileCurrentUser()).collection("classes").document(document.getId()).collection("Students").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                getStudents.clear();
                                getStudents.add("Pick a student...");
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    getStudents.add(document.getId());
                                }
                                adapterStudents.notifyDataSetChanged();
                            }
                        });
                        break;
                    }
                }

            }
        });
        adapterStudents = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, getStudents);
        adapterStudents.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStudents.setAdapter(adapterStudents);
        try {
            Field popup = Spinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);
            android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(spinnerStudents);
            popupWindow.setHeight(500);
        }
        catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
            // silently fail...
        }
    }

    public void getStudentEmail(final String studentName, final String className){
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(readFileCurrentUser()).collection("classes").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                List<String> list = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    list.add(document.getId());
                    if (document.getString("className").equals(className)){
                        db.collection("users").document(readFileCurrentUser()).collection("classes").document(document.getId()).collection("Students").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    if (document.getId().toString().equals(studentName)){
                                        studentEmail =document.getString("studentEmail");
                                        Log.d("Firebase",studentEmail);
                                        break;
                                    }
                                }
                            }
                        });
                    }
                }

            }
        });
    }

    public void populateArrayLists(){
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(readFileCurrentUser()).collection("classes").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                List<String> list = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    list.add(document.getId());
                    getClasses.add(document.getString("className"));
                }

            }
        });
    }
    private String readFileCurrentUser()
    {
        String myData = "";
        File path = addAppointment.this.getExternalFilesDir(null);
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