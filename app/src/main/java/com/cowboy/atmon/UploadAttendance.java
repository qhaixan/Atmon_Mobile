package com.cowboy.atmon;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.net.NetworkInterface;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class UploadAttendance extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference weekRef,signInRef,macRef;
    TextView textView;
    Boolean isReplacement=false;
    String studentKey, subjectKey, classKey, weekKey,getWeekKey;
    String weekIndex;
    int Date, Month, Year;
    double  hours;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        database = FirebaseDatabase.getInstance();
        weekRef = database.getReference("week");
        Calendar calendar = Calendar.getInstance();
        final java.util.Date date = calendar.getTime();
        setContentView(R.layout.activity_upload_attendance);
        Intent intent = getIntent();
        isReplacement=intent.getBooleanExtra("isReplacement",false);
        studentKey = intent.getStringExtra("studentKey");
        subjectKey = intent.getStringExtra("subjectKey");
        classKey = intent.getStringExtra("classKey");
        getWeekKey = intent.getStringExtra("weekKey");
        hours = intent.getDoubleExtra("hours",0);

        textView = (TextView) findViewById(R.id.textView4);

        textView.setText("Signing in, please wait...");
        Calendar c = Calendar.getInstance();
        Date = c.get(Calendar.DAY_OF_MONTH);
        Month = c.get(Calendar.MONTH)+1;
        Year = c.get(Calendar.YEAR);

        if(!isReplacement) {
            weekRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {//look for weeks
                        String tsy = data.child("startY").getValue().toString();
                        String tey = data.child("endY").getValue().toString();
                        String tsm = data.child("startM").getValue().toString();
                        String tem = data.child("endM").getValue().toString();
                        String tsd = data.child("startD").getValue().toString();
                        String ted = data.child("endD").getValue().toString();

                        int isy = Integer.parseInt(tsy);
                        int iey = Integer.parseInt(tey);
                        int ism = Integer.parseInt(tsm);
                        int iem = Integer.parseInt(tem);
                        int isd = Integer.parseInt(tsd);
                        int ied = Integer.parseInt(ted);

                        if (ism == Month && iem == Month)//if within same month
                        {
                            if (isd <= Date && ied >= Date) {
                                weekKey = data.getKey();
                                signIn();
                            }
                        } else if (ism == Month && iem == Month + 1)//before cross month
                        {
                            if (isd <= Date && ied < 7) {
                                weekKey = data.getKey();
                                signIn();
                            }
                        } else if (ism == Month - 1 && iem == Month)//after cross month
                        {
                            if (isd >= 23 && Date <= ied) {
                                weekKey = data.getKey();
                                signIn();
                            }
                        }
                    }
                    weekRef.removeEventListener(this);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }


            });
        }else{
            weekKey=getWeekKey;
            signIn();
        }
    }
    public static String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(Integer.toHexString(b & 0xFF) + ":");
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
            //handle exception
        }
        return "";
    }
    public void signIn()
    {
        //textView.append("\n"+weekKey);
        markMAC();
        signInRef=database.getReference("student")
                .child(studentKey)
                .child("sub")
                .child(subjectKey)
                .child("class")
                .child(classKey)
                .child("weekly")
                .child(weekKey);
        signInRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                    Toast.makeText(getApplicationContext(), "Successful", Toast.LENGTH_LONG).show();
                    signInRef.child("attended").setValue(true);
                    signInRef.removeEventListener(this);
                    gotomain();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
    public void markMAC(){
        macRef=database.getReference("subject")
                .child(subjectKey)
                .child("class")
                .child(classKey)
                .child("weekly")
                .child(weekKey);
        macRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                macRef.push().child("signed").setValue(getMacAddr());
                macRef.removeEventListener(this);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(UploadAttendance.this, "Do not leave while verifying.",
                Toast.LENGTH_LONG).show();
    }
    public void gotomain() {
        Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
        getApplicationContext().startActivity(myIntent);
        finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
