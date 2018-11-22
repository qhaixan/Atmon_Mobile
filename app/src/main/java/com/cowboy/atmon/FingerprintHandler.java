package com.cowboy.atmon;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import java.util.concurrent.CancellationException;

/**
 * Created by KevinLor on 2/28/2018.
 */

public class FingerprintHandler extends FingerprintManager.AuthenticationCallback{

    private Context context;
    boolean isReplacement=false;
    String studentKey, subjectKey, classKey,weekKey;
    CancellationSignal cenCancellationSignal;
    double hours;

    public  FingerprintHandler(Context context){
        this.context = context;
    }

    public void passData(String stu, String sub, String cls, Double hour) {
        studentKey=stu;
        subjectKey=sub;
        classKey=cls;
        isReplacement=false;
        //hours=hour;
    }
    public void passDataR(String k1, String k2, String k3, String k4) {
        studentKey=k1;
        subjectKey=k2;
        classKey=k3;
        weekKey=k4;
        isReplacement=true;
        //hours=hour;
    }

    public void startAuthentication(FingerprintManager fingerprintManager, FingerprintManager.CryptoObject cryptoObject) {
        cenCancellationSignal = new CancellationSignal();
        if(ActivityCompat.checkSelfPermission(context, android.Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED){
            return;
        }
        fingerprintManager.authenticate(cryptoObject,cenCancellationSignal,0,this,null);
    }

    public void stopScanner(FingerprintManager fingerprintManager){
        fingerprintManager.equals(null);
        //fingerprintManager.authenticate(null,null,0,null,null);
    }
    @Override
    public void onAuthenticationFailed() {
        super.onAuthenticationFailed();
        Toast.makeText(context, "Auth failed, try again.",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        super.onAuthenticationSucceeded(result);
        Intent intent1 = new Intent("fgsdgdfgg");
        context.sendBroadcast(intent1);

        Intent myIntent = new Intent(context, UploadAttendance.class);
        myIntent.putExtra("isReplacement", isReplacement);
        myIntent.putExtra("studentKey", studentKey);
        myIntent.putExtra("subjectKey", subjectKey);
        myIntent.putExtra("classKey", classKey);
        myIntent.putExtra("weekKey", weekKey);
        //myIntent.putExtra("hours", hours);
        context.startActivity(myIntent);
    }
}
