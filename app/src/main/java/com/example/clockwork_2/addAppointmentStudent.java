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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class addAppointmentStudent extends AppCompatActivity {
    ArrayList<String> getClasses=new ArrayList<>(), getTeacher=new ArrayList<>();
    ArrayAdapter<String> adapterTeacher;
    Spinner spinnerClass, spinnerTeacher;
    String selectedDate;
    String teacherEmail;
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
        spinnerTeacher = (Spinner) findViewById(R.id.spinnerStudent);
        Button btnAppointmentNextPage=findViewById(R.id.btnNextPageAppointment);
        ImageView btnImageAppointmentNextPage=findViewById(R.id.btnImageNextPageAppointment);

        operation=getIntent().getStringExtra("Operation");
        final String className=getIntent().getStringExtra("className");
        final String target=getIntent().getStringExtra("target");

        if (operation.equals("Reschedule")){
            TextView textView=findViewById(R.id.textView2);
            textView.setText("Reschedule Appointment");
            getTeacherEmail(className);
            getClasses.add(className);
            getTeacher.add(target);
            spinnerTeacher.setEnabled(false);
            spinnerClass.setEnabled(false);
            ArrayAdapter<String> adapterClasses = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, getClasses);
            ArrayAdapter<String> adapterStudent = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, getTeacher);
            adapterClasses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerClass.setAdapter(adapterClasses);
            spinnerTeacher.setAdapter(adapterStudent);
            btnAppointmentNextPage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent=new Intent(addAppointmentStudent.this, addAppointment2.class);
                    intent.putExtra("Operation","Reschedule");
                    intent.putExtra("class", className);
                    intent.putExtra("student", target);
                    intent.putExtra("date", selectedDate);
                    intent.putExtra("studentEmail",teacherEmail);
                    intent.putExtra("time",getIntent().getStringExtra("time"));
                    startActivity(intent);
                }
            });


            btnImageAppointmentNextPage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent=new Intent(addAppointmentStudent.this, addAppointment2.class);
                    intent.putExtra("Operation","Reschedule");
                    intent.putExtra("class", className);
                    intent.putExtra("student", target);
                    intent.putExtra("date", selectedDate);
                    intent.putExtra("studentEmail",teacherEmail);
                    intent.putExtra("time",getIntent().getStringExtra("time"));
                    startActivity(intent);

                }
            });
        }
        else{
            populateArrayLists();
            getClasses.add("Select a class...");
            spinnerTeacher.setEnabled(false);
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
                    getTeacherEmail(spinnerClass.getSelectedItem().toString());
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });


            btnAppointmentNextPage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (spinnerClass.getSelectedItem().equals("Select a class...")){
                        Toast.makeText(addAppointmentStudent.this, "Pick a class!", Toast.LENGTH_SHORT).show();
                    }
                    else if (getTeacher==null || spinnerTeacher.getSelectedItem()==null){
                        Toast.makeText(addAppointmentStudent.this, "Please try again!", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Intent intent=new Intent(addAppointmentStudent.this, addAppointment2.class);
                        intent.putExtra("class", spinnerClass.getSelectedItem().toString());
                        intent.putExtra("Operation","Normal");
                        intent.putExtra("time",getIntent().getStringExtra(""));
                        intent.putExtra("student", spinnerTeacher.getSelectedItem().toString());
                        intent.putExtra("date", selectedDate);
                        intent.putExtra("studentEmail",teacherEmail);
                        startActivity(intent);
                    }
                }
            });


            btnImageAppointmentNextPage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (spinnerClass.getSelectedItem().equals("Select a class...")){
                        Toast.makeText(addAppointmentStudent.this, "Pick a class!", Toast.LENGTH_SHORT).show();
                    }
                    else if (getTeacher==null || spinnerTeacher.getSelectedItem()==null){
                        Toast.makeText(addAppointmentStudent.this, "Please try again!", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Intent intent=new Intent(addAppointmentStudent.this, addAppointment2.class);
                        intent.putExtra("class", spinnerClass.getSelectedItem().toString());
                        intent.putExtra("Operation","Normal");
                        intent.putExtra("time",getIntent().getStringExtra(""));
                        intent.putExtra("student", spinnerTeacher.getSelectedItem().toString());
                        intent.putExtra("date", selectedDate);
                        intent.putExtra("studentEmail",teacherEmail);
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
    public void SpinnerTeacher(String teacherEmail){
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(teacherEmail).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()){
                        getTeacher.clear();
                        getTeacher.add(document.getString("name"));
                    }
                }

                adapterTeacher.notifyDataSetChanged();


            }
        });
        adapterTeacher = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, getTeacher);
        adapterTeacher.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTeacher.setAdapter(adapterTeacher);
        try {
            Field popup = Spinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);
            android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(spinnerTeacher);
            popupWindow.setHeight(500);
        }
        catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
            // silently fail...
        }
    }

    public void getTeacherEmail(final String className){
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(readFileCurrentUser()).collection("classes").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    if (document.getString("className").equals(spinnerClass.getSelectedItem())){
                        teacherEmail =document.getString("classOwner");
                        SpinnerTeacher(teacherEmail);
                        break;
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
                for (QueryDocumentSnapshot document : task.getResult()) {
                    getClasses.add(document.getString("className"));
                }

            }
        });
    }
    private String readFileCurrentUser()
    {
        String myData = "";
        File path = addAppointmentStudent.this.getExternalFilesDir(null);
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

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}