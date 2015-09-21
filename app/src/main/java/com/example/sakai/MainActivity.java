package com.example.sakai;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;


import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {

	private String result;
	private Handler handler;

	private String username;
	private String password;
	public static HttpClient httpclient;

	@Override

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final HttpClient httpclient = new DefaultHttpClient();
		Button signin = (Button)this.findViewById(R.id.signin);
		final EditText usn = (EditText)this.findViewById(R.id.username_edit);
		final EditText psw = (EditText)this.findViewById(R.id.password_edit);

		signin.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				username = usn.getText().toString();
				password = psw.getText().toString();
				new Thread(new Runnable(){
					public void run(){
						login();
						//startActivity(new Intent(MainActivity.this,MainMenu.class));
					}}).start();
				//startActivity(new Intent(MainActivity.this,MainMenu.class));
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void login(){
		Log.e("ACTIVITY_TAG","hi");
		String target = "http://202.120.46.147/portal/xlogin";
		httpclient = new DefaultHttpClient();
		HttpPost httpRequest = new HttpPost(target);
		List<NameValuePair>params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("eid",username));
		params.add(new BasicNameValuePair("pw",password));
		params.add(new BasicNameValuePair("submit","Login"));
		try{
			httpRequest.setEntity(new UrlEncodedFormEntity(params,HTTP.UTF_8));
			HttpResponse httpResponse = httpclient.execute(httpRequest);
			if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
				result+=EntityUtils.toString(httpResponse.getEntity());
				Log.d("ACTIVITY_TAG","succeed");
				startActivity(new Intent(MainActivity.this,MainMenu.class));
			}
			else{
				Log.d("ACTIVITY_TAG","fail");
			}
		}
		catch(ClientProtocolException e){
			e.printStackTrace();
			Log.e("ACTIVITY_TAG","fail1");
		}
		catch(IOException e){
			e.printStackTrace();
			Log.e("ACTIVITY_TAG","fail2");
		}
	}

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//
////        deleteFile()
//        Log.d("Destroy", getFilesDir().getAbsolutePath());
////        File f = new File(Environment.getDataDirectory().getAbsolutePath());
//        File f = new File(getFilesDir().getAbsolutePath());
//        final Pattern p = Pattern.compile(".*File");
//        File[] fileLists = f.listFiles(new FileFilter() {
//            @Override
//            public boolean accept(File file) {
//                return p.matcher(file.getName()).matches();
//            }
//        });
//        Log.d("Destroy",  "The array length: " + Integer.toString(fileLists.length));
//        for(int i = 0; i < fileLists.length; i++){
//            Log.d("delete",fileLists[i].getName() + "is deleted");
//                fileLists[i].delete();
//        }
//    }
}