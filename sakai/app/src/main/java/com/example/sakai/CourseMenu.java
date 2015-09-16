package com.example.sakai;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;


public class CourseMenu extends Activity {
    private String title;
    private String ID;
    private String[] site;
    private Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_menu);
        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        ID=intent.getStringExtra("id");
        Log.d("CourseMenu", title);
        Log.d("CourseMenu", ID);
        new Thread(new Runnable(){
            public void run(){
                go();

            }
        }).start();

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(CourseMenu.this, android.R.layout.simple_list_item_1, (List) msg.obj);
                final List<String> listCourse = (List)msg.obj;
                ListView listView = (ListView) findViewById(R.id.list_view_CourseMenu);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                        if (listCourse.get(position).equals("Gradebook")){
                            Intent intentGradebook = new Intent(CourseMenu.this, Gradebook.class);
                            intentGradebook.putExtra("id", ID);
                            startActivity(intentGradebook);
                        }
                        if (listCourse.get(position).equals("Announcements")||listCourse.get(position).equals("֪ͨ")){
                            Intent intentAnnouncement = new Intent(CourseMenu.this, Announcement.class);
                            intentAnnouncement.putExtra("id", ID);
                            startActivity(intentAnnouncement);
                            System.out.println("go announcement");
                        }
                        if (listCourse.get(position).equals("Assignments")){
                          //  Log.e("gy", "kkkk");
                            Intent intent = new Intent(CourseMenu.this, Assignment.class);
                            intent.putExtra("id", ID);
                            startActivity(intent);
                        }
                    }
                });
                super.handleMessage(msg);
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_course_menu, menu);
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

    void go() {
        String result=null;
        String target = "http://202.120.46.147/direct/site/"+ID+".json";
        HttpGet httpRequest = new HttpGet(target);  // load the json
        HttpResponse httpResponse;
        try {
            httpResponse = MainActivity.httpclient.execute(httpRequest);    //get json
            if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                result = EntityUtils.toString(httpResponse.getEntity(), "utf-8");    //json string  //add "utf-8"
                Log.d("hahaha","yes");

            }
            else{
                result = "fail to access";
                Log.d("hahaha","no");
            }

            Log.d("ACTIVITY_TAG","succeed");
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.d("ACTIVITY_TAG","unhappy");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.d("ACTIVITY_TAG","unhappy");
        }

        JSONObject jsonObject = null;
        JSONArray  sitePages = null;
        JSONObject info = null; // never used

        try {
            jsonObject = new JSONObject(result);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            sitePages = jsonObject.getJSONArray("sitePages");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        site= new String[sitePages.length()];

        List<String> Site = new ArrayList<String>();
        ListIterator<String> SiteIterator = Site.listIterator();

        for(int i = 0;i<sitePages.length();i++){
            try {
                JSONObject data = sitePages.getJSONObject(i);
                site[i] = data.getString("title");
                SiteIterator.add(site[i]);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        Message message = handler.obtainMessage();
        List<String> ShowedList = new ArrayList<String>();
        ShowedList.addAll(Site);
        message.obj = ShowedList;
        handler.sendMessage(message);

    }

    }

