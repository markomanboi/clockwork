package com.example.clockwork_2.ui.schedule;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.clockwork_2.R;
import com.example.clockwork_2.ReminderBroadcast;
import com.example.clockwork_2.Sinch.LoginActivity;
import com.example.clockwork_2.addAppointment;
import com.example.clockwork_2.addAppointment2;
import com.example.clockwork_2.addAppointmentStudent;
import com.example.clockwork_2.addClassTeacher;
import com.example.clockwork_2.landingPage;
import com.example.clockwork_2.ui.classes.ClassesFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    String selectedDate;
    List<String> getClassNames, getAppointmentTime,getAppointee,getScheduleStatus, getTargetEmail;
    View root;
    TextView textViewNoSchedule;
    ListView listView;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        root = inflater.inflate(R.layout.fragment_schedule, container, false);
        final TextView textView = root.findViewById(R.id.text_home);
        final CalendarView calendarView=root.findViewById(R.id.calendarView);
        final ImageView addAppointment=root.findViewById(R.id.btnAddAppointment);

        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
        selectedDate = sdf.format(new Date(calendarView.getDate()));
        scheduleData();
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int dayOfMonth) {
                selectedDate = String.format("%02d",month+1)+"-"+String.format("%02d",dayOfMonth)+"-"+String.valueOf(year);
                scheduleData();
            }
        });
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        addAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (readFileCurrentUserType().equals("Teacher")){
                    Intent intent=new Intent(getActivity(), addAppointment.class);
                    intent.putExtra("Operation","Normal");
                    startActivity(intent);
                }
                else{Intent intent=new Intent(getActivity(), addAppointmentStudent.class);
                    intent.putExtra("Operation","Normal");
                    startActivity(intent);

                }
            }



        });

        listView=root.findViewById(R.id.listSchedule);
        textViewNoSchedule=root.findViewById(R.id.textView10);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                Intent intent = new Intent(getActivity().getApplicationContext(), LoginActivity.class);
                                intent.putExtra("currentEmail",readFileCurrentUser());
                                intent.putExtra("targetEmail",getTargetEmail.get(i));
                                intent.putExtra("targetName",getAppointee.get(i));
                                startActivity(intent);

                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Start a video call?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();

            }
        });
        return root;
    }
    public void scheduleData(){
        //---------------------------Populate Arraylists-----------------------------------------------
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(readFileCurrentUser()).collection("schedule").
                document(selectedDate).collection("daySchedule").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                List<String> list = new ArrayList<>();
                getClassNames=new ArrayList<>();
                getAppointmentTime=new ArrayList<>();
                getAppointee=new ArrayList<>();
                getScheduleStatus=new ArrayList<>();
                getTargetEmail=new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    list.add(document.getId());
                    getClassNames.add(document.getString("className"));
                    getAppointmentTime.add(document.getString("time"));
                    getAppointee.add(document.getString("name"));
                    getScheduleStatus.add(document.getString("status"));
                    getTargetEmail.add(document.getString("email"));
                }
                //----------------------ListView---------------------------------------
                MyAdapter myAdapter = new MyAdapter();
                listView.setAdapter(myAdapter);

                if (listView.getCount()!=0)
                    textViewNoSchedule.setVisibility(View.GONE);
                else
                    textViewNoSchedule.setVisibility(View.VISIBLE);
            }
        });
        //---------------------------------------------------------------------------
    }
    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return getClassNames.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            View v = LayoutInflater.from(getActivity().getApplicationContext()).inflate(R.layout.listviewmodelschedule, null);
            ImageView imageView = v.findViewById(R.id.imageView3);
            TextView className = v.findViewById(R.id.txtClassName);
            TextView classTime = v.findViewById(R.id.txtClassTime);
            TextView appointee = v.findViewById(R.id.txtAppointee);
            final TextView status = v.findViewById(R.id.txtStatus);
            final ImageView btnCancel = v.findViewById(R.id.btnCancel);
            final ImageView btnResched = v.findViewById(R.id.btnResched);
            final ImageView btnAccept = v.findViewById(R.id.btnAccept);
            if (getScheduleStatus.get(i) != null) {
                if (getScheduleStatus.get(i).equals("PendingReceiver")) {
                    status.setVisibility(View.GONE);
                    btnCancel.setImageResource(R.drawable.x);
                    btnResched.setImageResource(R.drawable.resched);
                    btnAccept.setImageResource(R.drawable.check);
                    btnCancel.setVisibility(View.VISIBLE);
                    btnResched.setVisibility(View.VISIBLE);
                    btnAccept.setVisibility(View.VISIBLE);

                    btnCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case DialogInterface.BUTTON_POSITIVE:
                                            cancelAppointment(getAppointmentTime.get(i), getTargetEmail.get(i));
                                            Toast.makeText(getActivity().getApplicationContext(), "Appointment Cancelled!", Toast.LENGTH_SHORT).show();
                                            btnCancel.setVisibility(View.GONE);
                                            btnResched.setVisibility(View.GONE);
                                            btnAccept.setVisibility(View.GONE);
                                            status.setVisibility(View.VISIBLE);
                                            status.setText("Declined!");
                                            break;

                                        case DialogInterface.BUTTON_NEGATIVE:
                                            //No button clicked
                                            break;
                                    }
                                }
                            };

                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setMessage("Reject this appointment?").setPositiveButton("Yes", dialogClickListener)
                                    .setNegativeButton("No", dialogClickListener).show();

                        }
                    });


                    btnResched.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case DialogInterface.BUTTON_POSITIVE:
                                            if (readFileCurrentUserType().equals("Teacher")){
                                                Intent intent=new Intent(getActivity(), addAppointment.class);
                                                intent.putExtra("Operation","Reschedule");
                                                intent.putExtra("className",getClassNames.get(i));
                                                intent.putExtra("target", getAppointee.get(i));
                                                intent.putExtra("time",getAppointmentTime.get(i));
                                                startActivity(intent);
                                            }
                                            else{
                                                Intent intent=new Intent(getActivity(), addAppointmentStudent.class);
                                                intent.putExtra("Operation","Reschedule");
                                                intent.putExtra("className",getClassNames.get(i));
                                                intent.putExtra("target", getAppointee.get(i));
                                                intent.putExtra("time",getAppointmentTime.get(i));
                                                startActivity(intent);
                                            }

                                        case DialogInterface.BUTTON_NEGATIVE:
                                            //No button clicked
                                            break;
                                    }
                                }
                            };

                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setMessage("Reschedule this appointment?").setPositiveButton("Yes", dialogClickListener)
                                    .setNegativeButton("No", dialogClickListener).show();

                        }
                    });

                    btnAccept.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case DialogInterface.BUTTON_POSITIVE:
                                            acceptAppointment(getAppointmentTime.get(i), getTargetEmail.get(i));
                                            setNotification(getAppointee.get(i),getClassNames.get(i),getAppointmentTime.get(i),selectedDate);
                                            Toast.makeText(getActivity().getApplicationContext(), "Appointment Accepted!", Toast.LENGTH_SHORT).show();
                                            btnCancel.setVisibility(View.GONE);
                                            btnResched.setVisibility(View.GONE);
                                            btnAccept.setVisibility(View.GONE);
                                            status.setVisibility(View.VISIBLE);
                                            status.setText("Accepted!");
                                        case DialogInterface.BUTTON_NEGATIVE:
                                            //No button clicked
                                            break;
                                    }
                                }
                            };

                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setMessage("Accept this appointment?").setPositiveButton("Yes", dialogClickListener)
                                    .setNegativeButton("No", dialogClickListener).show();

                        }
                    });
                } else if (getScheduleStatus.get(i).equals("PendingSender")) {
                    status.setVisibility(View.VISIBLE);
                    status.setText("Pending...");
                } else if (getScheduleStatus.get(i).equals("Cancelled")) {
                    status.setVisibility(View.VISIBLE);
                    status.setText("Declined!");
                } else if (getScheduleStatus.get(i).equals("Accepted")) {
                    status.setVisibility(View.VISIBLE);
                    status.setText("Accepted!");
                }


                imageView.setImageResource(R.drawable.calendar_listview);
                className.setText(getClassNames.get(i));
                classTime.setText(getAppointmentTime.get(i));
                appointee.setText("Consultation with " + getAppointee.get(i));
            }
            return v;
        }
    }
    public void cancelAppointment(String time, String targetEmail){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(readFileCurrentUser())
                .collection("schedule").document(selectedDate).collection("daySchedule").document(time)
                .update("status","Cancelled")
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firebase", "Updated status");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Firebase", "ERROR");
                        Toast.makeText(getActivity().getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                    }
                });


        db.collection("users").document(targetEmail)
                .collection("schedule").document(selectedDate).collection("daySchedule").document(time)
                .update("status","Cancelled")
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firebase", "Updated status");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Firebase", "ERROR");
                        Toast.makeText(getActivity().getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void acceptAppointment(String time, String targetEmail){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(readFileCurrentUser())
                .collection("schedule").document(selectedDate).collection("daySchedule").document(time)
                .update("status","Accepted")
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firebase", "Updated status");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Firebase", "ERROR");
                        Toast.makeText(getActivity().getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                    }
                });


        db.collection("users").document(targetEmail)
                .collection("schedule").document(selectedDate).collection("daySchedule").document(time)
                .update("status","Accepted")
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firebase", "Updated status");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Firebase", "ERROR");
                        Toast.makeText(getActivity().getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name="Clockwork";
            String description="Appointment Notification";
            int importance= NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel=new NotificationChannel("notifyAppointment",name,importance);
            channel.setDescription(description);

            NotificationManager notificationManager=getActivity().getApplicationContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void setNotification(String student, String className, String time, String date){
        String[] splitTime={};
        splitTime=time.split("-");
        String firstTime = splitTime[0];
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

        Intent intent=new Intent(getActivity().getApplicationContext(), ReminderBroadcast.class);
        intent.putExtra("Name",student);
        intent.putExtra("className",className);
        intent.putExtra("channelID",channelID);

        PendingIntent pendingIntent=PendingIntent.getBroadcast(getActivity().getApplicationContext(), 0, intent,0);

        AlarmManager alarmManager=(AlarmManager) getActivity().getApplicationContext().getSystemService(Context.ALARM_SERVICE);

        long timeAtButtonClick=System.currentTimeMillis();

        Calendar calendar=Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("MM-dd-yyyy HH:mm");
        Date date2 = null;
        String formattedDate2 = date+" "+firstTime;
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
        Toast.makeText(getActivity().getApplicationContext(), "Notification Set!", Toast.LENGTH_SHORT).show();
    }

    private String readFileCurrentUser()
    {
        String myData = "";
        File path = getActivity().getExternalFilesDir(null);
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
        File path =getActivity().getApplicationContext().getExternalFilesDir(null);
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

    private String readCurrentChannelID()
    {
        String myData = "";
        File path = getActivity().getApplicationContext().getExternalFilesDir(null);
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
            File path = getActivity().getExternalFilesDir(null);
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