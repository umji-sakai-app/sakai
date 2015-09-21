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
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;


public class Announcement extends MethodActivity {
    private String announcementTitle[];
    private String announcementBody[];
    private Handler handler;
    private String ID;
    private String attachmentURL[], attachmentTitle[];
    private String result = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcement);
        Intent intent = getIntent();
        ID = intent.getStringExtra("id");
        new Thread(new Runnable(){
            public void run(){
                getAnnouncement();
                announcementJSONHandler();
            }
        }).start();

        handler = new Handler(){
            @Override
            public void handleMessage( Message msg) {
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(Announcement.this, android.R.layout.simple_list_item_1, (List) msg.obj);
                ListView listView = (ListView) findViewById(R.id.announcement_list);
                listView.setAdapter(adapter);
                final List<String> announcementTitle_list=(List)msg.obj;
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                        Intent intent = new Intent(Announcement.this, AnnouncementContext.class);
                        String selectedTitle=announcementTitle_list.get(position);
                        intent.putExtra("title", selectedTitle);
                        String selectedBody=announcementBody[announcementTitle_list.indexOf(selectedTitle)];
                        intent.putExtra("body", selectedBody);
                        String selectedAttachName=attachmentTitle[announcementTitle_list.indexOf(selectedTitle)];
                        intent.putExtra("attachTitle", selectedAttachName);
                        String selectedAttachURL=attachmentURL[announcementTitle_list.indexOf(selectedTitle)];
                        intent.putExtra("attachURL", selectedAttachURL);
                        System.out.println("handler");
                        startActivity(intent);
                    }
                });
                super.handleMessage(msg);
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_announcement, menu);
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

    //get announcement from sakai
    public void getAnnouncement(){
        String target = "http://202.120.46.147/direct/announcement/site/" + ID + ".json?n=40&d=21";
        result = accessJson(ID, "Announcement", target);
    }

    //handle the json file we get. Giving value to announcementTitle, announcementBody,attachmentURL[], attachmentTitle[] and nnouncementTitle_list.
    public void announcementJSONHandler(){
        JSONObject jsonobject = null;
        JSONArray announcement_collection = null;
        try {
            jsonobject = new JSONObject(result);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            announcement_collection = jsonobject.getJSONArray("announcement_collection");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        announcementTitle = new String[announcement_collection.length()];
        announcementBody = new String[announcement_collection.length()];
        attachmentTitle = new String[announcement_collection.length()];
        attachmentURL = new String[announcement_collection.length()];
        List<String>announcementTitle_list = new ArrayList<String>();
        ListIterator<String> announcementTitle_list_iterator = announcementTitle_list.listIterator();

        for(int i = 0;i<announcement_collection.length();i++){
            try {
                JSONObject data = announcement_collection.getJSONObject(i);
                announcementTitle[i] = data.getString("title");
                announcementTitle_list_iterator.add(data.getString("title"));
                announcementBody[i] = data.getString("body");

                if(data.isNull("attachments") == true){
                    attachmentTitle[i] = "0";
                    attachmentURL[i] = "0";
                    System.out.println("0");
                }
                else{
                    JSONArray attachment = data.getJSONArray("attachments");
                    JSONObject attachData = attachment.getJSONObject(0);
                    attachmentTitle[i] = attachData.getString("name");
                    attachmentURL[i] = attachData.getString("url");
                    System.out.println("1");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Message message = handler.obtainMessage();
        message.obj = announcementTitle_list;
        handler.sendMessage(message);
        System.out.println("json");
    }
}
