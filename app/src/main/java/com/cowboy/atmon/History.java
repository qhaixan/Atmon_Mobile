package com.cowboy.atmon;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class History extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference studentRef, subRef;

    LinearLayout layout;
    Button button1, button2, button3, button4, button5, button6, button7, button8;

    String savedID, studentKey;
    String[] subKey= new String[8];
    int index;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = FirebaseDatabase.getInstance();
        studentRef = database.getReference("student");

        setContentView(R.layout.activity_history);
        Intent intent = getIntent();
        savedID = intent.getStringExtra("ID");

        layout = (LinearLayout) findViewById(R.id.LLayout);

        declareBtn();

        loaded(false);
        studentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot data: dataSnapshot.getChildren()){

                    if(data.child("id").getValue(String.class).equals(savedID))
                    {
                        studentKey = data.getKey();
                        subRef = database.getReference("student").child(studentKey).child("sub");
                        subRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot2) {
                                int i=0;
                                for(DataSnapshot data2: dataSnapshot2.getChildren()) {
                                    subKey[i] = data2.getKey();

                                    initBtn(i,data2.child("id").getValue().toString(),data2.child("title").getValue().toString());

                                    i+=1;


                                }
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
        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                index=0;
                Button b = (Button)v;
                nextPage(b.getText().toString());
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                index=1;
                Button b = (Button)v;
                nextPage(b.getText().toString());
            }
        });
        button3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                index=2;
                Button b = (Button)v;
                nextPage(b.getText().toString());
            }
        });
        button4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                index=3;
                Button b = (Button)v;
                nextPage(b.getText().toString());
            }
        });
        button5.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                index=4;
                Button b = (Button)v;
                nextPage(b.getText().toString());
            }
        });
        button6.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                index=5;
                Button b = (Button)v;
                nextPage(b.getText().toString());
            }
        });
        button7.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                index=6;
                Button b = (Button)v;
                nextPage(b.getText().toString());
            }
        });
        button8.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                index=7;
                Button b = (Button)v;
                nextPage(b.getText().toString());
            }
        });
    }
    public void nextPage(String desc) {/*
        Intent myIntent = new Intent(History.this, Attendances.class);
        History.this.startActivity(myIntent);
        finish();*/
        Intent myIntent = new Intent(History.this, Attendances.class);
        myIntent.putExtra("savedID", savedID);
        myIntent.putExtra("ID", studentKey);
        myIntent.putExtra("subject", subKey[index]);
        myIntent.putExtra("desc", desc);
        History.this.startActivity(myIntent);
        finish();
    }
    public void gotomain() {
        Intent myIntent = new Intent(History.this, MainActivity.class);
        History.this.startActivity(myIntent);
        finish();
    }
    public void initBtn(int i, String id, String title) {
        if(i==0)
        {
            button1.setText(id+" - "+title);
            button1.setVisibility(View.VISIBLE);
        }
        else if(i==1)
        {
            button2.setText(id+" - "+title);
            button2.setVisibility(View.VISIBLE);
        }
        else if(i==2)
        {
            button3.setText(id+" - "+title);
            button3.setVisibility(View.VISIBLE);
        }
        else if(i==3)
        {
            button4.setText(id+" - "+title);
            button4.setVisibility(View.VISIBLE);
        }
        else if(i==4)
        {
            button5.setText(id+" - "+title);
            button5.setVisibility(View.VISIBLE);
        }
        else if(i==5)
        {
            button6.setText(id+" - "+title);
            button6.setVisibility(View.VISIBLE);
        }
        else if(i==6)
        {
            button7.setText(id+" - "+title);
            button7.setVisibility(View.VISIBLE);
        }
        else if(i==7)
        {
            button8.setText(id+" - "+title);
            button8.setVisibility(View.VISIBLE);
        }
    }
    public void declareBtn() {
        button1 = (Button) findViewById(R.id.button1);
        button1.setVisibility(View.GONE);
        button2 = (Button) findViewById(R.id.button2);
        button2.setVisibility(View.GONE);
        button3 = (Button) findViewById(R.id.button3);
        button3.setVisibility(View.GONE);
        button4 = (Button) findViewById(R.id.button4);
        button4.setVisibility(View.GONE);
        button5 = (Button) findViewById(R.id.button5);
        button5.setVisibility(View.GONE);
        button6 = (Button) findViewById(R.id.button6);
        button6.setVisibility(View.GONE);
        button7 = (Button) findViewById(R.id.button7);
        button7.setVisibility(View.GONE);
        button8 = (Button) findViewById(R.id.button8);
        button8.setVisibility(View.GONE);
    }
    public void loaded(boolean done) {

    }
    @Override
    public void onBackPressed() {
        gotomain();
    }
}

