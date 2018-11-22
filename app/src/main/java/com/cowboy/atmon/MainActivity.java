package com.cowboy.atmon;

import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.fingerprint.FingerprintManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.net.NetworkInterface;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.crypto.Cipher;

public class MainActivity extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference studentRef, apRef, apRef2;

    TextView IDdisplay;
    Button btnCheck, btnTake;


    String ID="",studName="...";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = FirebaseDatabase.getInstance();
        studentRef = database.getReference("student");
        apRef = database.getReference("AP");
        apRef2 = database.getReference("AP");

        setContentView(R.layout.activity_main);

        IDdisplay = (TextView) findViewById(R.id.IDdisplay);
        btnCheck = (Button) findViewById(R.id.btnCheck);
        btnTake = (Button) findViewById(R.id.btnTake);

        isFirstTime();

        loaded(false);
        studentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot data: dataSnapshot.getChildren()){

                    if(data.child("id").getValue(String.class).equals(ID))
                    {
                        studName =data.child("name").getValue(String.class);
                        IDdisplay.setText("ID : "+ID+"\nName : "+studName);
                        loaded(true);
                    }
                }
                studentRef.removeEventListener(this);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        btnTake.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                apRef.orderByChild("add").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //textView.setText(getMacAddr()+dataSnapshot.getChildrenCount());
                        //boolean validAP=false;
                        String apName="";
                        String apKey="";// COPY THIS TO NEXT ACTIVITY INSTEAD
                        final int[] classSize = {99};
                        final String[] classesA = new String [999];
                        for(DataSnapshot data: dataSnapshot.getChildren()){
                            //textView.append("\n"+data.child("add").getValue(String.class));
                            if(data.child("add").getValue(String.class).equals(getMacAddr()))
                            {
                                apName=data.child("name").getValue(String.class);
                                apKey=data.getKey();
                            }
                        }
                        if(apName!="")
                        {
                            Intent myIntent = new Intent(MainActivity.this, FingerprintAuth.class);
                            //myIntent.putStringArrayListExtra("classes", classes);
                            myIntent.putExtra("APK", apKey);
                            myIntent.putExtra("AP", apName);
                            myIntent.putExtra("ID", ID);
                            myIntent.putExtra("MAC", getSelfMac().toLowerCase());
                            MainActivity.this.startActivity(myIntent);
                            finish();
                        } else {
                            Toast.makeText(MainActivity.this, "Invalid WiFi",
                                    Toast.LENGTH_LONG).show();
                        }
                        apRef.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
        });
        btnCheck.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, History.class);
                myIntent.putExtra("ID", ID);
                MainActivity.this.startActivity(myIntent);
                finish();
            }
        });
    }

    private Boolean exit = false;
    @Override
    public void onBackPressed() {
        if (exit) {
            // finish activity
            finish();
        } else {
            Toast.makeText(this, "Press Back again to Exit.",
                    Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);
        }
    }

    public void isFirstTime() {
        SharedPreferences prefR = PreferenceManager.getDefaultSharedPreferences(this);
        ID = prefR.getString("ID", "");
        if(ID.equals("")) {
            Intent myIntent = new Intent(MainActivity.this, EnterID.class);
            MainActivity.this.startActivity(myIntent);
            finish();
        }
    }
    public void loaded(boolean ready) {
        if(ready)
        {
            btnCheck.setVisibility(View.VISIBLE);
            btnTake.setVisibility(View.VISIBLE);
        } else {
            btnCheck.setVisibility(View.GONE);
            btnTake.setVisibility(View.GONE);
        }
    }

    public String getMacAddr() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo;

        wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
            if(!wifiInfo.getBSSID().equals(null))
                return wifiInfo.getBSSID();
        }
        return null;
    }
    public String getSelfMac() {
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
                    res1.append(String.format("%02X:",b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
        }
        return "02:00:00:00:00:00";

    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
