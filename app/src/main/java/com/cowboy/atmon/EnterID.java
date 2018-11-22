package com.cowboy.atmon;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EnterID extends AppCompatActivity {

    EditText etID;
    Button btnSave;
    TextView tvStatus;

    FirebaseDatabase database;
    DatabaseReference studentRef;

    String askName=null;
    Boolean confirm = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = FirebaseDatabase.getInstance();
        studentRef = database.getReference("student");

        setContentView(R.layout.activity_enter_id);

        etID = (EditText)findViewById( R.id.etID );
        btnSave = (Button) findViewById( R.id.btnSave );
        tvStatus = (TextView)findViewById( R.id.tvStatus );

        final SharedPreferences prefW = PreferenceManager.getDefaultSharedPreferences(this);

        btnSave.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // TODO Auto-generated method stub


                studentRef.orderByChild("id").equalTo(etID.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        tvStatus.setText("Loading...");
                        for(DataSnapshot data: dataSnapshot.getChildren()){

                            if(data.child("id").getValue(String.class).equals(etID.getText().toString()))
                            {
                                askName =data.child("name").getValue(String.class);
                            }
                        }
                        if(askName!=null)
                        {
                            confirm(etID.getText().toString());
                            etID.setText("");
                            tvStatus.setText("Found 1 result");
                        } else {
                            tvStatus.setText("Invalid ID. You can:\n- Recheck the ID format.\n- Recheck your connection.");
                        }
                        studentRef.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(EnterID.this, "Connection cancelled",
                                Toast.LENGTH_LONG).show();
                    }
                });

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

    public void confirm(final String id){
        final SharedPreferences prefW = PreferenceManager.getDefaultSharedPreferences(this);
        new AlertDialog.Builder(this)
                .setTitle("Proceed if this is you.")
                .setMessage(askName)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        confirm=true;
                        SharedPreferences.Editor editor = prefW.edit();
                        editor.putString("ID", id);
                        editor.apply();
                        Toast.makeText(EnterID.this, "Saved",
                                Toast.LENGTH_LONG).show();
                        Intent myIntent = new Intent(EnterID.this, MainActivity.class);
                        EnterID.this.startActivity(myIntent);
                        finish();
                    }})
                .setNegativeButton(android.R.string.no, null).show();
    }
}
