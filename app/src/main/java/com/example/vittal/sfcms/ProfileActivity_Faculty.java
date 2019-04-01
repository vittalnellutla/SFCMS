package com.example.vittal.sfcms;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vittal.sfcms.AppBase_Faculty;
import com.example.vittal.sfcms.DatabaseHandler;
import com.example.vittal.sfcms.EditStudent_Faculty;
import com.example.vittal.sfcms.ProfileAdapter;
import com.example.vittal.sfcms.R;

import java.util.ArrayList;

public class ProfileActivity_Faculty extends AppCompatActivity{

    DatabaseHandler handler = AppBase_Faculty.handler;
    Activity profileActivity = this;
    ListView listView;
    ProfileAdapter adapter;
    ArrayList<String> dates;
    ArrayList<String> datesALONE;
    ArrayList<Integer> hourALONE;
    ArrayList<Boolean> atts;
    Activity activity =this;
    private View v;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        dates = new ArrayList<>();
        datesALONE = new ArrayList<>();
        hourALONE = new ArrayList<>();
        atts = new ArrayList<>();

        listView = findViewById(R.id.attendProfileView_list);
        FloatingActionButton fab = findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent launchIntent = new Intent(profileActivity,EditStudent_Faculty.class);
                startActivity(launchIntent);
            }
        });

        TextView textView = findViewById(R.id.profileContentView);
        assert textView != null;
        textView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(activity);
                alert.setTitle("Delete Student");
                alert.setMessage("Are you sure ?");
                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText editText = findViewById(R.id.editText);
                        String regno = editText.getText().toString();
                        String qu = "DELETE FROM STUDENT WHERE REGNO = '" + regno.toUpperCase() + "'";
                        if (AppBase_Faculty.handler.execAction(qu)) {
                            Log.d("delete", "done from student");
                            String qa = "DELETE FROM ATTENDANCE WHERE register = '" + regno.toUpperCase() + "'";
                            if (AppBase_Faculty.handler.execAction(qa)) {
                                Toast.makeText(getBaseContext(), "Deleted", Toast.LENGTH_LONG).show();
                                Log.d("delete", "done from attendance");
                            }
                        }
                    }
                });
                alert.setNegativeButton("No", null);
                alert.show();
                return true;
            }
        });


        Button findButton = findViewById(R.id.buttonFind);
        assert findButton != null;
        findButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                find(v);
            }
        });
    }

    public void find(View view) {
        dates.clear();
        atts.clear();
        EditText editText = findViewById(R.id.editText);
        TextView textView = findViewById(R.id.profileContentView);
        String reg = editText.getText().toString();
        String qu = "SELECT * FROM STUDENT WHERE regno = '" + reg.toUpperCase() + "'";
        String qc = "SELECT * FROM ATTENDANCE WHERE register = '" + reg.toUpperCase() + "';";
        String qd = "SELECT * FROM ATTENDANCE WHERE register = '" + reg.toUpperCase() + "' AND isPresent = 1";
        Cursor cursor = AppBase_Faculty.handler.execQuery(qu);
        //Start Count Here

        float att = 0f;
        Cursor cur = handler.execQuery(qc);
        Cursor cur1 = handler.execQuery(qd);
        if(cur==null)
        {
            Log.d("profile","cur null");
        }
        if(cur1==null)
        {
            Log.d("profile","cur1 null");
        }
        if(cur!=null&&cur1!=null)
        {
            cur.moveToFirst();
            cur1.moveToFirst();
            try{
                att = ((float) cur1.getCount()/cur.getCount())*100;
                if(att<=0)
                    att = 0f;
                Log.d("ProfileActivity_Faculty","Total = " + cur.getCount() + " avail = "+cur1.getCount() + " per " + att);
            }catch (Exception e){att = -1;}
        }


        if(cursor==null||cursor.getCount()==0)
        {
            assert textView != null;
            textView.setText("No Data Available");
        }else
        {
            String attendance = "";
            if(att<0)
            {
                attendance = "Attendance Not Available";
            }else
                attendance = " Attendance " + att + " %";
            cursor.moveToFirst();
            String buffer = "";
            buffer += " " + cursor.getString(0) + "\n";
            buffer += " " + cursor.getString(1)+ "\n";
            buffer += " " + cursor.getString(2)+ "\n";
            buffer += " " + cursor.getString(3)+ "\n";
            buffer += " " + cursor.getInt(4)+ "\n";
            buffer += " " + attendance+ "\n";
            textView.setText(buffer);

            String q = "SELECT * FROM ATTENDANCE WHERE register = '" + editText.getText().toString().toUpperCase() + "'";
            Cursor cursorx = handler.execQuery(q);
            if(cursorx==null||cursorx.getCount()==0)
            {
                Toast.makeText(getBaseContext(),"No Attendance Info Available",Toast.LENGTH_LONG).show();
            }else
            {
                cursorx.moveToFirst();
                while(!cursorx.isAfterLast())
                {
                    datesALONE.add(cursorx.getString(0));
                    hourALONE.add(cursorx.getInt(1));
                    dates.add(cursorx.getString(0) + ":" + cursorx.getInt(1) + "th Hour");
                    if(cursorx.getInt(3)==1)
                        atts.add(true);
                    else {
                        Log.d("profile",cursorx.getString(0) + " -> " + cursorx.getInt(3));
                        atts.add(false);
                    }
                    cursorx.moveToNext();
                }
                adapter = new ProfileAdapter(dates,atts,profileActivity,datesALONE,hourALONE,editText.getText().toString().toUpperCase());
                listView.setAdapter(adapter);
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.profile_menu, menu);
        return true;
    }


    public void editStudent(MenuItem item) {
        Intent launchIntent  = new Intent(this,EditStudent_Faculty.class);
        startActivity(launchIntent);
    }
}