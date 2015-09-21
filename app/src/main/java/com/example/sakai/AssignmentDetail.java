package com.example.sakai;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;


public class AssignmentDetail extends Activity {

    private String ID;
    private String assignmentTitle, assignmentOpen, assignmentDue, assignmentStatus, attachmentTitle, attachmentURL;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment_detail);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(2);
        Intent intent = getIntent();
        assignmentTitle = intent.getStringExtra("title");
        assignmentOpen = intent.getStringExtra("open");
        assignmentDue = intent.getStringExtra("due");
        assignmentStatus = intent.getStringExtra("status");
        attachmentTitle = intent.getStringExtra("name");
        attachmentURL = intent.getStringExtra("url");


        TextView textTitle = (TextView) findViewById(R.id.assignment_context_title);
        textTitle.setText(assignmentTitle);
        TextView textBody = (TextView) findViewById(R.id.assignment_context_body);
        String body = "Open time  "+assignmentOpen + "\n" +"Due time  "+ assignmentDue + "\n" + "Status  "+assignmentStatus;
        textBody.setText(body);
        if (attachmentTitle != "0") {
            TextView textAttach = (TextView) findViewById(R.id.assignment_context_download);
            textAttach.setText("\n\n"+attachmentTitle);

            textAttach.setOnClickListener(new View.OnClickListener() {
                                              @Override
                                              public void onClick(View v) {
                                                  Intent intent = new Intent(AssignmentDetail.this, download.class);
                                                  intent.putExtra("url", attachmentURL);
                                                  intent.putExtra("name", attachmentTitle);
                                                  startService(intent);
                                              }
                                          }
            );
        }

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
}

