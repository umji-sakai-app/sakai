package com.example.sakai;

import android.app.Activity;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;


public class Assignment extends Activity {

    private String ID;
    private Handler handler;
    private String[]  assignmentTitle,assignmentOpen,assignmentDue,assignmentStatus,attachmentTitle ,attachmentURL ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment);
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
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(Assignment.this, android.R.layout.simple_list_item_1, (List) msg.obj);
//                final String[] gradeList = (String[])msg.obj;
                ListView listView = (ListView) findViewById(R.id.list_view_Assignment);
                listView.setAdapter(adapter);

                final List<String> list=(List)msg.obj;
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                        Intent intent = new Intent(Assignment.this, AssignmentDetail.class);
                        String selectedTitle=list.get(position);
                    //    Log.e("gy",selectedTitle);
                        intent.putExtra("title", selectedTitle);
                        intent.putExtra("due",assignmentDue[list.indexOf(selectedTitle)] );
                        intent.putExtra("open",assignmentOpen[list.indexOf(selectedTitle)] );
                        intent.putExtra("status",assignmentStatus[list.indexOf(selectedTitle)] );
                        intent.putExtra("name",attachmentTitle[list.indexOf(selectedTitle)] );
                        intent.putExtra("url", attachmentURL[list.indexOf(selectedTitle)] );
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
        String target = "http://202.120.46.147/direct/assignment/site/"+ID+".json";
        String result=null;
        HttpGet httpRequest = new HttpGet(target);  // load the json
        HttpResponse httpResponse;
        try {
            httpResponse = MainActivity.httpclient.execute(httpRequest);    //Use the MainActivity's httpClient to get the json, which has logged in.
            if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                result = EntityUtils.toString(httpResponse.getEntity(), "utf-8");    //json string  //add "utf-8"
                Log.d("hahaha", "yes");

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
        JSONArray assignmentArray = null;
        try {
            jsonObject = new JSONObject(result);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            assignmentArray = jsonObject.getJSONArray("assignment_collection");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        assignmentTitle = new String[assignmentArray.length()];
        assignmentDue = new String[assignmentArray.length()];
        assignmentOpen = new String[assignmentArray.length()];
        assignmentStatus = new String[assignmentArray.length()];

        attachmentTitle = new String[assignmentArray.length()];
        attachmentURL = new String[assignmentArray.length()];

        List<String>assignmentTitle_list = new ArrayList<String>();
        ListIterator<String> assignmentTitle_list_iterator = assignmentTitle_list.listIterator();

        for (int i=0; i<assignmentArray.length(); i++){
            try {
                JSONObject data = assignmentArray.getJSONObject(i);
                assignmentTitle[i] = data.getString("title");
                assignmentTitle_list_iterator.add(data.getString("title"));
                assignmentDue[i]=data.getString("dueTimeString");
                assignmentOpen[i]=data.getString("openTimeString");
                assignmentStatus[i]=data.getString("status");

                if(data.isNull("attachments") == true){
                    attachmentTitle[i] = "0";
                    attachmentURL[i] = "0";
             //       System.out.println("0");
                }
                else{
                    JSONArray attachment = data.getJSONArray("attachments");
                    JSONObject attachData = attachment.getJSONObject(0);
                    attachmentTitle[i] = attachData.getString("name");
                    attachmentURL[i] = attachData.getString("url");
           //         System.out.println("1");
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        Message message = handler.obtainMessage();
        message.obj = assignmentTitle_list;
        handler.sendMessage(message);

    }
}