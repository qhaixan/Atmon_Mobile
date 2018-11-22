package com.cowboy.atmon;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;

public class Attendances extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference subjectRef, classRef;

    TextView textView,textView2;

    String savedID, studentKey, subjectKey, classKey, passDesc;
    String[] classesKey;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = FirebaseDatabase.getInstance();
        setContentView(R.layout.activity_attendances);

        textView = (TextView) findViewById(R.id.textView);
        textView2 = (TextView) findViewById(R.id.textView2);

        Intent intent = getIntent();
        savedID = intent.getStringExtra("savedID");
        studentKey = intent.getStringExtra("ID");
        subjectKey = intent.getStringExtra("subject");
        passDesc = intent.getStringExtra("desc");

        textView.setText("Attendance for\n"+passDesc+"\n");
        subjectRef = database.getReference("student").child(studentKey).child("sub").child(subjectKey).child("class");

        subjectRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                final int[] total = {0};
                final double[] totalHours = {0};
                final double[] totalPercentage = {0};
                textView.setText("Attendance for\n"+passDesc+"\n");
                for(final DataSnapshot data: dataSnapshot.getChildren()){//look for subjects
                    //textView.append("\n"+data.child("title").getValue().toString());

                    classKey = data.getKey();
                    final double hours = Double.valueOf(data.child("hours").getValue().toString());
                    totalHours[0] += hours;
                    final int[] attended = {0};
                    classRef = subjectRef.child(classKey).child("weekly");
                    classRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot2) {
                            for(DataSnapshot data2: dataSnapshot2.getChildren()){//look for week
                                total[0] +=1;
                                if(total[0]==1) {
                                    textView.append("\n\n" + data.child("title").getValue().toString()+" ("+hours+" hours)" + "\n\n");
                                }
                                textView.append(" "+data2.child("index").getValue().toString());
                                if(data2.child("attended").getValue().toString()=="false")
                                {
                                    textView.append(Html.fromHtml("[<font color=\"red\">X</font>]"));
                                }
                                else {
                                    attended[0] +=1;
                                    textView.append(Html.fromHtml("[<font color=\"green\">/</font>]"));
                                }
                            }

                            String percentA = String.format("%.2f", (((double)attended[0]/(double)total[0])*100)*hours/totalHours[0]);
                            //textView.append("\n"+"["+percentA+"%]");
                            totalPercentage[0]+= (((double)attended[0]/(double)total[0])*100)*hours/totalHours[0];
                            String percentT = String.format("%.2f",totalPercentage[0]);
                            textView2.setText("Total rate: "+percentT+"%");
                            total[0]=0;
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
    public void goback() {
        Intent myIntent = new Intent(Attendances.this, History.class);
        myIntent.putExtra("ID", savedID);
        Attendances.this.startActivity(myIntent);
        finish();
    }
    @Override
    public void onBackPressed() {
        goback();
    }
}
