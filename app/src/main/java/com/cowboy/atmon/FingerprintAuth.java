package com.cowboy.atmon;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CountDownTimer;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EdgeEffect;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.net.NetworkInterface;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class FingerprintAuth extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference studentRef,subRef,classRef, weekRef, apRef, replacementRef, MacRef;


    TextView apText, timeText, descText, weekText;

    //ArrayList<String> classes = new ArrayList<String>();
    int intHrS, intMinS, intHrE, intMinE, Date, Month, Year, weekIndex;
    String strHrS, strMinS, strHrE, strMinE, currentHr, currentMin, currentSec;
    String studentKey, subKey, subKey2, classKey, weekKey="", weekDay, currentWeek="...";
    String classroom="";
    boolean gotClass = false;

    ImageView imageFinger;
    TextView  alertText;

    String connectedKey,connectedAP, savedID, Mac;
    String classesA[], classes[], subjectsA[], subjects[];

    private KeyStore keyStore;
    private static final String KEY_NAME="EDMTDev";
    private Cipher cipher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        registerReceiver(mMessageReceiver, new IntentFilter("fgsdgdfgg"));

        Calendar calendar = Calendar.getInstance();
        final java.util.Date date = calendar.getTime();
        weekDay = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(date.getTime());
        database = FirebaseDatabase.getInstance();
        studentRef = database.getReference("student");
        weekRef = database.getReference("week");
        replacementRef=database.getReference("replacement");

        //timeRef = database.getReference("server");

        setContentView(R.layout.activity_fingerprint_auth);
        Intent intent = getIntent();
        //classes = intent.getStringArrayListExtra("classes");
        connectedKey = intent.getStringExtra("APK");
        connectedAP = intent.getStringExtra("AP");
        savedID = intent.getStringExtra("ID");
        Mac = intent.getStringExtra("MAC");

        apText = (TextView) findViewById(R.id.apText);
        timeText = (TextView) findViewById(R.id.timeText);
        descText = (TextView) findViewById(R.id.descText);
        weekText = (TextView) findViewById(R.id.weekText);
        alertText = (TextView) findViewById(R.id.alertText);
        imageFinger = (ImageView) findViewById(R.id.imageFinger);

        loaded(false);
        Calendar c = Calendar.getInstance();
        Date = c.get(Calendar.DAY_OF_MONTH);
        Month = c.get(Calendar.MONTH)+1;
        Year = c.get(Calendar.YEAR);


        apText.setText("Connected to : "+connectedAP);
        weekText.setText("Loading...");

        apRef = database.getReference("AP").child(connectedKey).child("classroom");
        apRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int i=0;
                classesA = new String[999];
                for(DataSnapshot data: dataSnapshot.getChildren()){
                    classesA[i]=data.child("id").getValue(String.class);
                    i+=1;
                    classes = new String[i];
                    for(int j=0;j<i;j++)
                    {
                        classes[j]=classesA[j];
                    }
                }
                apRef.removeEventListener(this);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        weekRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot data: dataSnapshot.getChildren()){//look for weeks
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

                    if(ism==Month && iem==Month)//if within same month
                    {
                        if(isd<=Date&&ied>=Date)
                        {
                            weekKey = data.getKey().toString();
                            currentWeek = data.child("name").getValue().toString();
                            weekIndex = Integer.parseInt(data.child("index").getValue().toString());
                        }
                    }
                    else if(ism==Month && iem==Month+1)//before cross month
                    {
                        if(isd<=Date&&ied<7)
                        {
                            weekKey = data.getKey().toString();
                            currentWeek = data.child("name").getValue().toString();
                            weekIndex = Integer.parseInt(data.child("index").getValue().toString());
                        }
                    }
                    else if(ism==Month-1 && iem==Month)//after cross month
                    {
                        if(isd>=23&&Date<=ied)
                        {
                            weekKey = data.getKey().toString();
                            currentWeek = data.child("name").getValue().toString();
                            weekIndex = Integer.parseInt(data.child("index").getValue().toString());
                        }
                    }
                }
                weekRef.removeEventListener(this);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        studentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot data: dataSnapshot.getChildren()){

                    if(data.child("id").getValue(String.class).equals(savedID))//look for current student
                    {
                        studentKey = data.getKey();
                        subRef = database.getReference("student").child(studentKey).child("sub");

                        subRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot2) {
                                int i=0;
                                subjectsA = new String[999];
                                int subjectCount = (int)dataSnapshot2.getChildrenCount();
                                for(final DataSnapshot data2: dataSnapshot2.getChildren()){//look for subjects taken by this student

                                    subjectsA[i]=data2.child("id").getValue().toString();
                                    i+=1;
                                    final boolean[] gotClass = {false};

                                    subKey = data2.getKey();
                                    classRef = database.getReference("student").child(studentKey).child("sub").child(subKey).child("class");
                                    classRef.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot3) {

                                            for(DataSnapshot data3: dataSnapshot3.getChildren()){//look for classes
                                                if(data3.child("day").getValue().equals(weekDay)) {
                                                    gotClass[0] =true;
                                                    intHrS = Integer.parseInt(data3.child("startH").getValue().toString());
                                                    intMinS = Integer.parseInt(data3.child("startM").getValue().toString());
                                                    intHrE = Integer.parseInt(data3.child("endH").getValue().toString());
                                                    intMinE = Integer.parseInt(data3.child("endM").getValue().toString());

                                                    if(intHrS == Integer.parseInt(currentHr) && Integer.parseInt(currentMin)<=intMinS+29) {
                                                        timeFormat(); //convert int to str
                                                        classKey = data3.getKey();
                                                        subKey2 = data2.getKey();

                                                        classroom = data3.child("room").getValue().toString();
                                                        descText.setText("\n\nCurrent Subject :" +
                                                                "\n\t* " + data2.child("id").getValue() + " - " + data2.child("title").getValue() +
                                                                "\n\t* " + data3.child("title").getValue() +
                                                                "\n\t* " + data3.child("room").getValue() +
                                                                "\n\t* " + data3.child("day").getValue() + " (" +
                                                                strHrS + ":" +
                                                                strMinS + " - " +
                                                                strHrE + ":" +
                                                                strMinE + ")");

                                                        int gg = classes.length;
                                                        int wrong=0;
                                                        for(int i=0;i<gg;i++)
                                                        {
                                                            if (!classes[i].equals(classroom)) {
                                                                wrong += 1;
                                                                if(wrong==gg)
                                                                {
                                                                    studentRef.removeEventListener(this);
                                                                    Toast.makeText(FingerprintAuth.this, "Current class is not in range of connected WIFI",
                                                                            Toast.LENGTH_LONG).show();
                                                                    gotomain();
                                                                }
                                                            } else {
                                                                if(!existMac(subKey2,classKey,"")) {
                                                                    loaded(true);
                                                                    Toast.makeText(FingerprintAuth.this, "Session expires in 30s",
                                                                            Toast.LENGTH_LONG).show();
                                                                }
                                                                else{
                                                                    Toast.makeText(FingerprintAuth.this, "This device already signed in.",
                                                                            Toast.LENGTH_LONG).show();
                                                                    gotomain();
                                                                    classRef.removeEventListener(this);
                                                                }

                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            classRef.removeEventListener(this);
                                        }
                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                        }
                                    });
                                    if(i==subjectCount&&!gotClass[0])
                                    {
                                        subRef.removeEventListener(this);
                                        checkReplacement(subjectCount);
                                    }
                                }
                                subRef.removeEventListener(this);
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                    }
                }
                studentRef.removeEventListener(this);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        newtimer.start();
    }

    public void confirmReplacement(final String subID, final String classT, final int week, final String Time, final String Room) {

        studentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot data: dataSnapshot.getChildren()){

                    if(data.child("id").getValue(String.class).equals(savedID))//look for current student
                    {
                        studentKey = data.getKey();
                        subRef = studentRef.child(studentKey).child("sub");

                        subRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot2) {
                                int subjectCount = (int)dataSnapshot2.getChildrenCount();
                                for(final DataSnapshot data2: dataSnapshot2.getChildren()){//look for subjects taken by this student

                                    if(data2.child("id").getValue().toString().equals(subID))
                                    {
                                        final String subDesc = subID+ " - " +data2.child("title").getValue().toString();
                                        subKey = data2.getKey();
                                        classRef = subRef.child(subKey).child("class");
                                        classRef.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot3) {
                                                for(final DataSnapshot data3: dataSnapshot3.getChildren()) {
                                                    if(data3.child("title").getValue().toString().equals(classT))
                                                    {
                                                        //Toast.makeText(FingerprintAuth.this, subID,Toast.LENGTH_LONG).show();
                                                        classKey=data3.getKey();
                                                        weekRef=classRef.child(classKey).child("weekly");
                                                        weekRef.addValueEventListener(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot4) {
                                                                for(final DataSnapshot data4: dataSnapshot4.getChildren()) {
                                                                    int w = Integer.parseInt(data4.child("index").getValue().toString());
                                                                    if(w==week)
                                                                    {
                                                                        weekKey=data4.getKey();
                                                                        descText.setText("\n\nCurrent Subject : (Replacement)" +
                                                                                "\n\t* " + subDesc +
                                                                                "\n\t* " + classT +
                                                                                "\n\t* " + Room );
                                                                        weekRef.removeEventListener(this);

                                                                        if(!existMac(subKey,classKey,weekKey)) {
                                                                            activateScannerR(studentKey,subKey,classKey,weekKey);
                                                                            Toast.makeText(FingerprintAuth.this, "Session expires in 30s",
                                                                                    Toast.LENGTH_LONG).show();
                                                                        }
                                                                        else{
                                                                            Toast.makeText(FingerprintAuth.this, "This device already signed in.",
                                                                                    Toast.LENGTH_LONG).show();
                                                                            gotomain();
                                                                            classRef.removeEventListener(this);
                                                                        }
                                                                    }
                                                                }
                                                                weekRef.removeEventListener(this);
                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {
                                                            }
                                                        });
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                            }
                                        });
                                    }
                                }
                                subRef.removeEventListener(this);
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                    }
                }
                studentRef.removeEventListener(this);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
    public void checkReplacement(final int count) {
        replacementRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {

                    int intHrS = Integer.parseInt(data.child("startH").getValue().toString());
                    int intMinS = Integer.parseInt(data.child("startM").getValue().toString());
                    int intHrE = Integer.parseInt(data.child("endH").getValue().toString());
                    int intMinE = Integer.parseInt(data.child("endM").getValue().toString());

                    String HrS,MinS,HrE,MinE;
                    if(intHrS<10){ HrS = "0"+Integer.toString(intHrS); }
                    else { HrS = Integer.toString(intHrS); }
                    if(intMinS<10){ MinS = "0"+Integer.toString(intMinS); }
                    else { MinS = Integer.toString(intMinS); }
                    if(intHrE<10){ HrE = "0"+Integer.toString(intHrE); }
                    else { HrE = Integer.toString(intHrE); }
                    if(intMinE<10){ MinE = "0"+Integer.toString(intMinE); }
                    else { MinE = Integer.toString(intMinE); }

                    for(int i=0;i<count;i++)
                    {
                        if(data.child("subject").getValue().toString().equals(subjectsA[i])
                                && (intHrS == Integer.parseInt(currentHr) && Integer.parseInt(currentMin)<=intMinS+29)
                                && (Date == Integer.parseInt(data.child("D").getValue().toString())
                                    && Month == Integer.parseInt(data.child("M").getValue().toString())
                                    && Year == Integer.parseInt(data.child("Y").getValue().toString())))
                        {
                            String Title = data.child("title").getValue().toString();
                            String Room = data.child("room").getValue().toString();
                            String Time = "("+HrS+":"+HrE+" - "+MinS+":"+MinE+")";
                            int w = Integer.parseInt(data.child("week").getValue().toString());
                            gotClass=true;
                            confirmReplacement(subjectsA[i],Title,w,Time,Room);
                            //Toast.makeText(FingerprintAuth.this, subjectsA[i],Toast.LENGTH_LONG).show();
                        }
                    }
                }
                replacementRef.removeEventListener(this);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
    public void loaded(boolean ready) {
        if(ready)
        {
            activateScanner(true);
            gotClass = true;
            //btnSignIn.setEnabled(true);
        } else {
            alertText.setText("No ongoing class.\nScanner not activated");
            gotClass = false;
            //btnSignIn.setEnabled(false);
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
    public boolean existMac(String subKey, String classKey, String wikKey) {

        final boolean[] exist = {true};
        final boolean[] check = {false};

        if (wikKey != "")
            weekKey = wikKey;

        MacRef = database.getReference("subject").child(subKey).child("class").child(classKey).child("weekly").child(wikKey);
        MacRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                classesA = new String[999];
                for(DataSnapshot data: dataSnapshot.getChildren()){
                    if(data.child("signed").getValue(String.class).equals(getMacAddr()))
                    {
                        exist[0] = true;
                    }
                }
                //check[0]=true;
                MacRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }

        });

        //if(check[0]);
        return exist[0];
    }
    public void timeFormat() {
        if(intHrS<10){ strHrS = "0"+Integer.toString(intHrS); }
        else { strHrS = Integer.toString(intHrS); }
        if(intMinS<10){ strMinS = "0"+Integer.toString(intMinS); }
        else { strMinS = Integer.toString(intMinS); }
        if(intHrE<10){ strHrE = "0"+Integer.toString(intHrE); }
        else { strHrE = Integer.toString(intHrE); }
        if(intMinE<10){ strMinE = "0"+Integer.toString(intMinE); }
        else { strMinE = Integer.toString(intMinE); }
    }

    public double calculateHour() {
        double start=0,end=0;
        if(intMinS==30)
            start+=0.5;
        if(intMinE==30)
            end+=0.5;
        start+=intHrS;
        end+=intHrE;

        return end-start;
    }

    public int getWeekIndex(){
        return weekIndex;
    }
    public void activateScannerR(String k1,String k2,String k3,String k4) {

        KeyguardManager keyguardManager = (KeyguardManager)getSystemService(KEYGUARD_SERVICE);
        FingerprintManager fingerprintManager = (FingerprintManager)getSystemService(FINGERPRINT_SERVICE);
        FingerprintHandler helper = new FingerprintHandler(this);


            if (!fingerprintManager.isHardwareDetected()) {
                alertText.setText("Fingerprint auth permission not enabled.");
            } else {
                if (!fingerprintManager.hasEnrolledFingerprints())
                    alertText.setText("No fingerprint found.\nPlease register at least 1 fingerprint in Settings");
                else {
                    if (!keyguardManager.isKeyguardSecure())
                        alertText.setText("Lock screen not set.\nPlease enable lock screen in Settings");
                    else
                        genKey();

                    if (cipherInit()) {
                        FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(cipher);
                        helper.passDataR(k1, k2, k3,k4);
                        helper.startAuthentication(fingerprintManager, cryptoObject);
                    }
                }
            }

    }
    public void activateScanner(boolean activate) {

        KeyguardManager keyguardManager = (KeyguardManager)getSystemService(KEYGUARD_SERVICE);
        FingerprintManager fingerprintManager = (FingerprintManager)getSystemService(FINGERPRINT_SERVICE);
        FingerprintHandler helper = new FingerprintHandler(this);

        if(!activate)
        {
            helper.stopScanner(fingerprintManager);
        }
        else {
            if (!fingerprintManager.isHardwareDetected()) {
                alertText.setText("Fingerprint auth permission not enabled.");
            } else {
                if (!fingerprintManager.hasEnrolledFingerprints())
                    alertText.setText("No fingerprint found.\nPlease register at least 1 fingerprint in Settings");
                else {
                    if (!keyguardManager.isKeyguardSecure())
                        alertText.setText("Lock screen not set.\nPlease enable lock screen in Settings");
                    else
                        genKey();

                    if (cipherInit()) {
                        FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(cipher);
                        helper.passData(studentKey, subKey2, classKey,calculateHour());
                        helper.startAuthentication(fingerprintManager, cryptoObject);
                    }
                }
            }
        }
    }
    boolean cipherInit() {
        try {
            cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES+"/"+KeyProperties.BLOCK_MODE_CBC+"/"+KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }

        alertText.setText("Fingerprint scanner ready.\nPlace your finger on the scanner.");
        try {
            keyStore.load(null);
            SecretKey key = (SecretKey)keyStore.getKey(KEY_NAME,null);
            cipher.init(Cipher.ENCRYPT_MODE,key);
            return true;
        } catch (IOException e1) {
            e1.printStackTrace();
            return false;
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
            return false;
        } catch (CertificateException e1) {
            e1.printStackTrace();
            return false;
        } catch (UnrecoverableKeyException e1) {
            e1.printStackTrace();
            return false;
        } catch (KeyStoreException e1) {
            e1.printStackTrace();
            return false;
        } catch (InvalidKeyException e1) {
            e1.printStackTrace();
            return false;
        }
    }

    void genKey() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }

        KeyGenerator keyGenerator = null;
        try {
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }

        try {
            keyStore.load(null);
            keyGenerator.init(new KeyGenParameterSpec.Builder(KEY_NAME,KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7).build());
            keyGenerator.generateKey();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
    }
    CountDownTimer newtimer = new CountDownTimer(1000000000, 1000) {

        int session=30;
        public void onTick(long millisUntilFinished) {
            Calendar c = Calendar.getInstance();
            Date = c.get(Calendar.DAY_OF_MONTH);
            Month = c.get(Calendar.MONTH)+1;
            Year = c.get(Calendar.YEAR);

            if(c.get(Calendar.HOUR_OF_DAY)<10)
                currentHr = "0"+Integer.toString(c.get(Calendar.HOUR_OF_DAY));
            else
                currentHr = Integer.toString(c.get(Calendar.HOUR_OF_DAY));
            if(c.get(Calendar.MINUTE)<10)
                currentMin = "0"+Integer.toString(c.get(Calendar.MINUTE));
            else
                currentMin = Integer.toString(c.get(Calendar.MINUTE));
            if(c.get(Calendar.SECOND)<10)
                currentSec = "0"+Integer.toString(c.get(Calendar.SECOND));
            else
                currentSec = Integer.toString(c.get(Calendar.SECOND));
            session--;
            if(session==0)
            {
                Toast.makeText(FingerprintAuth.this, "Session expired",
                        Toast.LENGTH_LONG).show();
                gotomain();
            }

            timeText.setText("Current Time : "+currentHr+":"+currentMin+":"+currentSec+" ("+session+")");
            weekText.setText("Current week : "+weekIndex+" ("+weekDay+")");
        }
        public void onFinish() {

        }
    };
    public void gotomain() {
        Intent myIntent = new Intent(FingerprintAuth.this, MainActivity.class);
        FingerprintAuth.this.startActivity(myIntent);
        finish();
    }
    @Override
    public void onBackPressed() {
        if(gotClass)
        Toast.makeText(FingerprintAuth.this, "Place your finger on the scanner.",
                Toast.LENGTH_LONG).show();
        else
            gotomain();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }

    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        activateScanner(false);
        newtimer.cancel();
        unregisterReceiver(mMessageReceiver);
    }
}
