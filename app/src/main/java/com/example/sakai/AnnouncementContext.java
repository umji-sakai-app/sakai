package com.example.sakai;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


public class AnnouncementContext extends Activity {
    private String title, body, attachName, attachURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcement_context);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(1);
        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        body = intent.getStringExtra("body");
        attachName = intent.getStringExtra("attachTitle");
        attachURL = intent.getStringExtra("attachURL");
        TextView textTitle = (TextView)findViewById(R.id.announcement_context_title);
        textTitle.setText(title);
        TextView textBody = (TextView)findViewById(R.id.announcement_context_body);
        Spanned tbody = Html.fromHtml(body);
        textBody.setText(tbody);

        textBody.setMovementMethod(LinkMovementMethod.getInstance());

        if(attachName != "0") {
            TextView textAttach = (TextView) findViewById(R.id.announcement_context_download);
            textAttach.setText("\n\n"+attachName);
            textAttach.setOnClickListener(new View.OnClickListener() {

                                              @Override
                                              public void onClick(View v) {
                                                  Intent intent = new Intent(AnnouncementContext.this, download.class);
                                                  intent.putExtra("url", attachURL);
                                                  intent.putExtra("name", attachName);
                                                  startService(intent);
                                              }
                                          }
            );
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_announcement_context, menu);
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
