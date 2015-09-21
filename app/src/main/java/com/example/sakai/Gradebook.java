package com.example.sakai;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;


public class Gradebook extends MethodActivity {
    private String ID;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gradebook);
        Intent intent = getIntent();
        ID = intent.getStringExtra("id");
        new Thread(new Runnable(){
            public void run(){
                go();

            }
        }).start();

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(Gradebook.this, android.R.layout.simple_list_item_1, (String[]) msg.obj);
//                final String[] gradeList = (String[])msg.obj;
                ListView listView = (ListView) findViewById(R.id.list_view_Gradebook);
                listView.setAdapter(adapter);

                super.handleMessage(msg);
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_gradebook, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void go(){
        String result = null;
        String target = "http://202.120.46.147/direct/gradebook/site/"+ID+".json";
        result = accessJson(ID, "Gradebook", target);

        JSONObject jsonObject = null;
        JSONArray gradeArray = null;
        try {
            jsonObject = new JSONObject(result);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            gradeArray = jsonObject.getJSONArray("assignments");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String[] assignmentGrade = new String[gradeArray.length()];

        for (int i=0; i<gradeArray.length(); i++){
            try {
                JSONObject data = gradeArray.getJSONObject(i);
                assignmentGrade[i] = data.getString("itemName") + "\t\t\t\t" + data.getString("grade") + "/" + data.getString("points");

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        Message message = handler.obtainMessage();
        message.obj = assignmentGrade;
        handler.sendMessage(message);

    }
}
