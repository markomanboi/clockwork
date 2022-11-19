package com.example.clockwork_2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class addAppointment2 extends AppCompatActivity {
    String className, student,studentEmail,date, getTeacherName,operation,oldTime;
    Spinner spinnerHour1,spinnerHour2,spinnerMinute1,spinnerMinute2;
    List<String> getCurrentUserTimes, getAppointeeTimes, getStatusCurrentUser, getStatusTargetUser;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_appointment2);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
        actionBar.setElevation(0);
        actionBar.setTitle("");

        className=getIntent().getStringExtra("class");
        student=getIntent().getStringExtra("student");
        studentEmail=getIntent().getStringExtra("studentEmail");
        date=getIntent().getStringExtra("date");
        operation=getIntent().getStringExtra("Operation");
        oldTime=getIntent().getStringExtra("time");

        populateArrayLists();
        Button btnAddAppointment=findViewById(R.id.btnAddNewAppointment);
        btnAddAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar=Calendar.getInstance();
                String time=spinnerHour1.getSelectedItem().toString()+":"+spinnerMinute1.getSelectedItem().toString();
                SimpleDateFormat df = new SimpleDateFormat("MM-dd-yyyy HH:mm");
                Date date2 = null;
                String formattedDate2 = date+" "+time;
                try {
                    date2=df.parse(formattedDate2);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                long seconds = (date2.getTime()-calendar.getTime().getTime())/1000;
                if (seconds<0)
                    Toast.makeText(addAppointment2.this, "Please check the schedule again!", Toast.LENGTH_SHORT).show();
                else {
                    if (checkIfValidTime(getCurrentUserTimes, getAppointeeTimes,getStatusCurrentUser,getStatusTargetUser) == true) {
                        addData(student, className, date);
                        if (operation.equals("Reschedule"))
                            removeOldAppointment(oldTime);
                        startActivity(new Intent(addAppointment2.this, landingPage.class));
                    }
                    else{
                        Toast.makeText(addAppointment2.this, "Time conflict for you or the target!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        spinnerHour1=findViewById(R.id.spinnerHour1);
        spinnerHour2=findViewById(R.id.spinnerHour2);
        spinnerMinute1=findViewById(R.id.spinnerMinute1);
        spinnerMinute2=findViewById(R.id.spinnerMinute2);

        ArrayAdapter<CharSequence> adapterhours = ArrayAdapter.createFromResource(this,
                R.array.time_hours, android.R.layout.simple_spinner_item);
        adapterhours.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHour1.setAdapter(adapterhours);
        spinnerHour2.setAdapter(adapterhours);
        try {
            Field popup = Spinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);

            // Get private mPopup member variable and try cast to ListPopupWindow
            android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(spinnerHour1);
            android.widget.ListPopupWindow popupWindow2 = (android.widget.ListPopupWindow) popup.get(spinnerHour2);
            // Set popupWindow height to 500px
            popupWindow.setHeight(500);
            popupWindow2.setHeight(500);
        }
        catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
            // silently fail...
        }

        ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this,
                R.array.time_minutes, android.R.layout.simple_spinner_item);
        adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMinute1.setAdapter(adapterMinutes);
        spinnerMinute2.setAdapter(adapterMinutes);
        try {
            Field popup = Spinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);

            // Get private mPopup member variable and try cast to ListPopupWindow
            android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(spinnerMinute1);
            android.widget.ListPopupWindow popupWindow2 = (android.widget.ListPopupWindow) popup.get(spinnerMinute2);

            // Set popupWindow height to 500px
            popupWindow.setHeight(500);
            popupWindow2.setHeight(500);
        }
        catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
            // silently fail...
        }
    }


    public void addData(String name, final String className, final String date){
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final String time=spinnerHour1.getSelectedItem().toString()+":"+spinnerMinute1.getSelectedItem().toString()+"-"+spinnerHour2.getSelectedItem().toString()+":"+spinnerMinute2.getSelectedItem().toString();
        final Map<String, Object> schedule = new HashMap<>();
        schedule.put("name", name);
        schedule.put("className", className);
        schedule.put("date", date);
        schedule.put("time", time);
        schedule.put("status","PendingSender");
        schedule.put("email",studentEmail);
        final DocumentReference[] docRef = {db.collection("users").document(readFileCurrentUser())
                .collection("schedule").document(date).collection("daySchedule").document(time)};
        //add appointment to teacher database
        docRef[0].get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    db.collection("users").document(readFileCurrentUser())
                            .collection("schedule").document(date).collection("daySchedule").document(time)
                                .set(schedule)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("Firebase", "Appointment set in teacher firebase.");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("Firebase", "ERROR");
                                        Toast.makeText(addAppointment2.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                    }
                                });

                }
            }
        });

        final Map<String, Object> schedule2 = new HashMap<>();
        FirebaseFirestore db2 = FirebaseFirestore.getInstance();
        docRef[0] =  db2.collection("users").document(readFileCurrentUser());
        docRef[0].get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    getTeacherName = document.getString("name");
                    Log.d("Firebase",getTeacherName);

                    schedule2.put("name", getTeacherName);
                    schedule2.put("className", className);
                    schedule2.put("date", date);
                    schedule2.put("time", time);
                    schedule2.put("status","PendingReceiver");
                    schedule2.put("email",readFileCurrentUser());
                    //add appointment to student database
                    docRef[0] = db.collection("users").document(studentEmail)
                            .collection("schedule").document(date).collection("daySchedule").document(time);
                    docRef[0].get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                db.collection("users").document(studentEmail)
                                        .collection("schedule").document(date).collection("daySchedule").document(time)
                                        .set(schedule2)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                setNotification();
                                                Log.d("Firebase", "Appointment set in student firebase.");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d("Firebase", "ERROR");
                                                Toast.makeText(addAppointment2.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                            }
                        }
                    });
                }
            }
        });
    }

    public void removeOldAppointment(String time){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(readFileCurrentUser())
                .collection("schedule").document(date).collection("daySchedule").document(time)
                .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("FIREBASE","Deleted old appointment time.");
            }
        });


        db.collection("users").document(studentEmail)
                .collection("schedule").document(date).collection("daySchedule").document(time)
                .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("FIREBASE","Deleted old appointment time.");
            }
        });
    }
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    public void populateArrayLists(){
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        getCurrentUserTimes=new ArrayList<>();
        getAppointeeTimes=new ArrayList<>();
        getStatusCurrentUser=new ArrayList<>();
        getStatusTargetUser=new ArrayList<>();
        db.collection("users").document(readFileCurrentUser())
                .collection("schedule").document(date).collection("daySchedule").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        List<String> list = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            getCurrentUserTimes.add(document.getId());
                            getStatusCurrentUser.add(document.getString("status"));
                        }

                    }
                });

        db.collection("users").document(studentEmail)
                .collection("schedule").document(date).collection("daySchedule").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                List<String> list = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    getAppointeeTimes.add(document.getId());
                    getStatusTargetUser.add(document.getString("status"));
                }

            }
        });
    }

    public Boolean checkIfValidTime(List <String> getCurrentUserTimes, List <String> getAppointeeTimes, List <String> getStatusCurrentUser, List <String> getStatusTargetUser){
        String selectedTime1=spinnerHour1.getSelectedItem().toString()+":"+spinnerMinute1.getSelectedItem().toString();
        String selectedTime2=spinnerHour2.getSelectedItem().toString()+":"+spinnerMinute2.getSelectedItem().toString();
        if (operation.equals("Reschedule")){
            String[] splitTime=oldTime.split("-");
            String from = splitTime[0];
            String to = splitTime[1];
            String n = selectedTime1;
            String n2=selectedTime2;
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
            Date date_from = null;
            try {
                date_from = formatter.parse(from);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Date date_to = null;
            try {
                date_to = formatter.parse(to);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Date dateNow = null;
            Date dateNow2 = null;
            try {
                dateNow = formatter.parse(n);
                dateNow2 = formatter.parse(n2);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (dateNow.after(date_from) && dateNow2.before(date_to)) {
                return true;
            }
            else if (from.equals(n) && dateNow2.before(date_to)){
                return true;
            }
            else if (dateNow.after(date_from) && to.equals(n2)){
                return true;
            }
        }
        if (getCurrentUserTimes!=null){
            for (int i=0; i<getCurrentUserTimes.size();i++) {
                String[] splitTime={};
                splitTime=getCurrentUserTimes.get(i).split("-");
                String from = splitTime[0];
                String to = splitTime[1];
                String n = selectedTime1;
                String n2=selectedTime2;
                SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
                Date date_from = null;
                if (!getStatusCurrentUser.get(i).equals("Cancelled") && (selectedTime1.equals(splitTime[0]) || selectedTime2.equals(splitTime[1]))){
                    return false;
                }

                try {
                    date_from = formatter.parse(from);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Date date_to = null;
                try {
                    date_to = formatter.parse(to);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Date dateNow = null;
                Date dateNow2 = null;
                try {
                    dateNow = formatter.parse(n);
                    dateNow2 = formatter.parse(n2);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (date_from.before(dateNow) && date_to.after(dateNow) && !getStatusCurrentUser.get(i).equals("Cancelled")) {
                    return false;
                }
                else if (date_from.before(dateNow2) && date_to.after(dateNow2) && !getStatusCurrentUser.get(i).equals("Cancelled")){
                    return false;
                }
                else if (dateNow.after(date_from) && dateNow2.before(date_to) && !getStatusCurrentUser.get(i).equals("Cancelled")) {
                    return false;
                }
                else if (dateNow2.before(dateNow)){
                    Toast.makeText(this, "This is not a correct time!", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        }

        if (getAppointeeTimes!=null){
            for (int j=0; j<getAppointeeTimes.size();j++) {
                String[] splitTime={};
                splitTime=getAppointeeTimes.get(j).split("-");
                String from = splitTime[0];
                String to = splitTime[1];
                String n = selectedTime1;
                String n2=selectedTime2;
                SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
                Date date_from = null;
                if (!getStatusTargetUser.get(j).equals("Cancelled") && (selectedTime1.equals(splitTime[0]) || selectedTime2.equals(splitTime[1]))){
                    return false;
                }

                try {
                    date_from = formatter.parse(from);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Date date_to = null;
                try {
                    date_to = formatter.parse(to);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Date dateNow = null;
                Date dateNow2 = null;
                try {
                    dateNow = formatter.parse(n);
                    dateNow2 = formatter.parse(n2);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (date_from.before(dateNow) && date_to.after(dateNow) && !getStatusTargetUser.get(j).equals("Cancelled")) {
                    return false;
                }
                else if (date_from.before(dateNow2) && date_to.after(dateNow2) && !getStatusTargetUser.get(j).equals("Cancelled")){
                    return false;
                }
                else if (dateNow.after(date_from) && dateNow2.before(date_to) && !getStatusTargetUser.get(j).equals("Cancelled")) {
                    return false;
                }
                else if (dateNow2.before(dateNow)){
                    Toast.makeText(this, "This is not a correct time!", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        }


        return true;
    }


    private void createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name="Clockwork";
            String description="Appointment Notification";
            int importance= NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel=new NotificationChannel("notifyAppointment",name,importance);
            channel.setDescription(description);

            NotificationManager notificationManager=getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void setNotification(){
        createNotificationChannel();
        String channelID="1";
        if (!readCurrentChannelID().equals("")){
            channelID=readCurrentChannelID();
            int add1=Integer.parseInt(channelID)+1;
            saveCurrentChannelID(Integer.toString(add1));
        }
        else{
            saveCurrentChannelID("1");
        }

        Intent intent=new Intent(addAppointment2.this, ReminderBroadcast.class);
        intent.putExtra("Name",student);
        intent.putExtra("className",className);
        intent.putExtra("channelID",channelID);

        PendingIntent pendingIntent=PendingIntent.getBroadcast(addAppointment2.this, 0, intent,0);

        AlarmManager alarmManager=(AlarmManager) getSystemService(ALARM_SERVICE);

        long timeAtButtonClick=System.currentTimeMillis();

        Calendar calendar=Calendar.getInstance();
        String time=spinnerHour1.getSelectedItem().toString()+":"+spinnerMinute1.getSelectedItem().toString();
        SimpleDateFormat df = new SimpleDateFormat("MM-dd-yyyy HH:mm");
        Date date2 = null;
        String formattedDate2 = date+" "+time;
        try {
            date2=df.parse(formattedDate2);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long seconds = (date2.getTime()-calendar.getTime().getTime())/1000;

        long tenSecondsInMillis=1000*(seconds-300);
        if (tenSecondsInMillis<0)
            tenSecondsInMillis=1000*10;


        alarmManager.set(AlarmManager.RTC_WAKEUP,timeAtButtonClick+tenSecondsInMillis,pendingIntent);
        Toast.makeText(addAppointment2.this, "Appointment Set!", Toast.LENGTH_SHORT).show();
    }

    private String readFileCurrentUser()
    {
        String myData = "";
        File path = addAppointment2.this.getExternalFilesDir(null);
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

    private String readCurrentChannelID()
    {
        String myData = "";
        File path = addAppointment2.this.getExternalFilesDir(null);
        File myExternalFile = new File(path, "/currentChannelID.txt");
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

    public void saveCurrentChannelID(String sBody) {
        try {
            File path = addAppointment2.this.getExternalFilesDir(null);
            File file = new File(path , "/currentChannelID.txt");
            if (!file.exists()) {
                file.createNewFile();
                FileWriter writer = new FileWriter(file);
                writer.append("1");
                writer.flush();
                writer.close();
            }
            else {
                FileWriter writer = new FileWriter(file);
                writer.append(sBody);
                writer.flush();
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}