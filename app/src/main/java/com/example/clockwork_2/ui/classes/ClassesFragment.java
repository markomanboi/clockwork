package com.example.clockwork_2.ui.classes;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.clockwork_2.R;
import com.example.clockwork_2.addClassStudent;
import com.example.clockwork_2.addClassTeacher;
import com.example.clockwork_2.classDetailsActivity;
import com.example.clockwork_2.landingPage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ClassesFragment extends Fragment {

    private ClassesViewModel classesViewModel;
    List<String> getClassNames, getClassDays,getClassTimes, getClassCodes;
    String userType;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        userType=readFileCurrentUserType();
        classesViewModel =
                ViewModelProviders.of(this).get(ClassesViewModel.class);
        View root = inflater.inflate(R.layout.fragment_classes, container, false);
        ImageView btnAddClass=root.findViewById(R.id.btnAddClass);
        final TextView textNoClasses=root.findViewById(R.id.textNoClasses);
        final GridView gridView=root.findViewById(R.id.classes_grid);

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(readFileCurrentUser()).collection("classes").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                getClassNames=new ArrayList<>();
                getClassDays=new ArrayList<>();
                getClassTimes=new ArrayList<>();
                getClassCodes=new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    getClassCodes.add(document.getId());
                    getClassNames.add(document.getString("className"));
                    getClassDays.add(document.getString("classDay"));
                    getClassTimes.add(document.getString("time"));
                }
                MyAdapter myAdapter = new MyAdapter();
                gridView.setAdapter(myAdapter);
                if (gridView.getCount()!=0){
                    textNoClasses.setVisibility(View.GONE);
                }
            }
        });

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent=new Intent(getActivity().getApplicationContext(), classDetailsActivity.class);
                intent.putExtra("classCode", getClassCodes.get(i));
                intent.putExtra("className", getClassNames.get(i));
                startActivity(intent);
            }
        });

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("The code for this class is:"+getClassCodes.get(i));
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                builder.show();
                return false;
            }
        });

        btnAddClass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (userType.equals("Teacher"))
                    startActivity(new Intent(getActivity(), addClassTeacher.class));
                else
                    startActivity(new Intent(getActivity(), addClassStudent.class));
            }
        });


        return root;
    }

    class MyAdapter extends BaseAdapter{

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
        public View getView(int i, View view, ViewGroup viewGroup) {
            View v= LayoutInflater.from(getActivity().getApplicationContext()).inflate(R.layout.cardviewmodel,null);
            TextView className=v.findViewById(R.id.title);
            TextView classDay=v.findViewById(R.id.classDay);
            TextView classTime=v.findViewById(R.id.classTime);

            className.setText(getClassNames.get(i));
            classDay.setText(getClassDays.get(i));
            classTime.setText(getClassTimes.get(i));
            return v;
        }
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
        File path = getActivity().getExternalFilesDir(null);
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