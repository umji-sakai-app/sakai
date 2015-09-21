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

public class MainMenu extends Activity {

    private boolean MainMenuJsonFile = false;
    private String MainMenuJsonFileName = "MainMenuJsonFile";
	private String result = null;
	private String[] title, ID;
//	private String[] CurrentTitle, CurrentID,RestTitle, RestID;
	private Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.listviewlayout);

		new Thread(new Runnable(){
			public void run(){
				access();
				JSONHandler();
				Intent intent1 = new Intent(MainMenu.this, PollService.class);
				startService(intent1);


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

        FileInputStream in = null;
        BufferedReader reader = null;
        StringBuilder content = new StringBuilder();
        try{
            File file = new File(getFilesDir().getAbsolutePath(),MainMenuJsonFileName);
//                Log.d("Announcement", file.exists() ? "exist":"not exist");
            if (file.exists()){
                Log.d("MainMenu", "exist");
            }
            else{
                Log.d("MainMenu", "non-exist");
            }
            in = openFileInput(MainMenuJsonFileName);
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while((line = reader.readLine()) != null){
                content.append(line);
            }
            result = content.toString();
            reader.close();
            Log.d("MainMenu", "successfully read the json string from the file");
        }catch (IOException e){
//                e.printStackTrace();
            String target = "http://202.120.46.147/direct/site.json";
            HttpGet httpRequest = new HttpGet(target);
            HttpResponse httpresponse;
            try {
                httpresponse = MainActivity.httpclient.execute(httpRequest);
                if (httpresponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    result = EntityUtils.toString(httpresponse.getEntity(), "utf-8");    //json string  //add "utf-8"
                    FileOutputStream out = null;    // write the json string into the file. After that read the json string from the file.
                    BufferedWriter writer = null;
                    try{
                        out = openFileOutput(MainMenuJsonFileName, Context.MODE_PRIVATE);
//                        Log.d("MainMenu", "out info: " + out.toString() + "   The file name is" + ID + "AnnouncementJsonFile");
//                            Log.d("Announcement", wri)
                        writer = new BufferedWriter(new OutputStreamWriter(out));
                        writer.write(result);
                    }catch (IOException a){
                        a.printStackTrace();
                    }finally {
                        try {
                            if (writer != null) {
                                writer.close();
                            }
                        }catch (IOException b){
                            b.printStackTrace();
                        }
//                        AnnouncementFileFlag = true;
                    }
                    Log.d("MainMenu", "successfully load the json string into files");

                    Log.d("MainMenu", "yes");

                } else {
                    result = "fail to access";
                    Log.d("MainMenu", "no");
                }
            } catch (IOException c) {
                c.printStackTrace();
            }
//            System.out.println("http");

        }finally{
            if(reader != null){
                try{
                    reader.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }

//        if(!MainMenuJsonFile) { //This way is not good. Change
//            String target = "http://202.120.46.147/direct/site.json";
//            HttpGet httpRequest = new HttpGet(target);  // load the json
//            HttpResponse httpResponse;
//            try {
//                httpResponse = MainActivity.httpclient.execute(httpRequest);    //get json
//                if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
//                    result = EntityUtils.toString(httpResponse.getEntity(), "utf-8");    //json string  //add "utf-8"
//                    FileOutputStream out = null;    // write the json string into the file. After that read the json string from the file.
//                    BufferedWriter writer = null;
//                    try{
//                        out = openFileOutput(MainMenuJsonFileName, Context.MODE_PRIVATE);
//                        writer = new BufferedWriter(new OutputStreamWriter(out));
//                        writer.write(result);
//                    }catch (IOException e){
//                        e.printStackTrace();
//                    }finally {
//                        try {
//                            if (writer != null) {
//                                writer.close();
//                            }
//                        }catch (IOException e){
//                            e.printStackTrace();
//                        }
//                        MainMenuJsonFile = true;
//                    }
//                    Log.d("MainMenu", "successfully load the json string into files");
//
//                } else {
//                    result = "fail to access";
//                    Log.d("hahaha", "no");
//                }
//
//                Log.d("ACTIVITY_TAG", "succeed");
//            } catch (ClientProtocolException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//                Log.d("ACTIVITY_TAG", "unhappy");
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//                Log.d("ACTIVITY_TAG", "unhappy");
//            }
//        }else{
//            FileInputStream in = null;
//            BufferedReader reader = null;
//            StringBuilder content = new StringBuilder();
//            try{
//                in = openFileInput(MainMenuJsonFileName);
//                reader = new BufferedReader(new InputStreamReader(in));
//                String line = "";
//                while((line = reader.readLine()) != null){
//                    content.append(line);
//                }
//            }catch (IOException e){
//                e.printStackTrace();
//            }finally{
//                if(reader != null){
//                    try{
//                        reader.close();
//                    }catch (IOException e){
//                        e.printStackTrace();
//                    }
//                }
//            }
//            result = content.toString();
//            Log.d("MainMenu", "successfully read the json string from the file");
//        }

	}

	 private void JSONHandler() {
		JSONObject jsonObject = null;
		JSONArray site_collection = null;
		JSONObject info = null; // never used


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
                        || title[i].contains("JI Career")|| title[i].contains("sakai app")||title[i].contains("VE270")){
					CurrentTitleIterator.add(title[i]);
					//System.out.println(CurrentTitle[j]);
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
//		ShowedList.addAll(RestTitle);
		message.obj = ShowedList;
		handler.sendMessage(message);
	}

    @Override
    protected void onDestroy() {
        super.onDestroy();
		Intent stopIntent1 = new Intent(this, PollService.class);
		stopService(stopIntent1); // 停止服务

//        deleteFile()
        Log.d("Destroy", getFilesDir().getAbsolutePath());
//        File f = new File(Environment.getDataDirectory().getAbsolutePath());
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

