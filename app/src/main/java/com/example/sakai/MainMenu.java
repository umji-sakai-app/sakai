package com.example.sakai;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

public class MainMenu extends MethodActivity {

    private boolean MainMenuJsonFile = false;
    private String MainMenuJsonFileName = "MainMenuJsonFile";
	private String result = null;
	private String[] title, ID;
	private Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.listviewlayout);
		new Thread(new Runnable(){
			public void run(){
				access();
				JSONHandler();
			}
		}).start();

		handler = new Handler(){
			@Override
			public void handleMessage( Message msg) {
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainMenu.this, android.R.layout.simple_list_item_1, (List) msg.obj);
				ListView listView = (ListView) findViewById(R.id.list_view);
				listView.setAdapter(adapter);
				final List<String> showList=(List)msg.obj;
				listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
						Intent intent = new Intent(MainMenu.this, CourseMenu.class);
						String selectedTitle=showList.get(position);
						intent.putExtra("title", selectedTitle);
						String selectedID=ID[Arrays.asList(title).indexOf(selectedTitle)];
						intent.putExtra("id", selectedID);
						Log.e("gy", "title="+selectedTitle+"   id=" +selectedID);
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
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	private void access(){
		String target = "http://202.120.46.147/direct/site.json";
		result = accessJson("", "MainMenu", target);
	}

	 private void JSONHandler() {
		JSONObject jsonObject = null;
		JSONArray site_collection = null;

		try {
			jsonObject = new JSONObject(result);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			site_collection = jsonObject.getJSONArray("site_collection");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		title = new String[site_collection.length()];
		ID = new String[site_collection.length()];
		List<String> CurrentTitle = new ArrayList<String>();
		ListIterator<String> CurrentTitleIterator = CurrentTitle.listIterator();
		List<String> CurrentID = new ArrayList<String>();
		ListIterator<String> CurrentIDIterator = CurrentID.listIterator();
		List<String> RestTitle = new ArrayList<String>();
		ListIterator<String> RestTitleIterator = RestTitle.listIterator();
		List<String> RestID = new ArrayList<String>();
		ListIterator<String> RestIDIterator = RestID.listIterator();

		for(int i = 0;i<site_collection.length();i++){

			try {

				JSONObject data = site_collection.getJSONObject(i);
				title[i] = data.getString("title");
				ID[i] = data.getString("entityId");

				if(title[i].contains("FA2015") || title[i].contains("Current Student") || title[i].contains("Scholarship and Awards")
                        || title[i].contains("JI Career") || title[i].contains("sakai app")){
					CurrentTitleIterator.add(title[i]);
					CurrentIDIterator.add(ID[i]);
				}else{
					RestTitleIterator.add(title[i]);
					RestIDIterator.add(ID[i]);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				Log.e("gy", "error");
				e.printStackTrace();
			}
		}
		Message message = handler.obtainMessage();
		List<String> ShowedList = new ArrayList<String>();
		ShowedList.addAll(CurrentTitle);
		message.obj = ShowedList;
		handler.sendMessage(message);
	}

    @Override
	protected void onDestroy() {
        super.onDestroy();

//        deleteFile()
        Log.d("Destroy", getFilesDir().getAbsolutePath());
        File f = new File(getFilesDir().getAbsolutePath());
        final Pattern p = Pattern.compile(".*File");
        File[] fileLists = f.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return p.matcher(file.getName()).matches();
            }
        });
        Log.d("Destroy",  "The array length: " + Integer.toString(fileLists.length));
        for(int i = 0; i < fileLists.length; i++){
            Log.d("delete",fileLists[i].getName() + " is deleted");
            fileLists[i].delete();
        }
    }
}

