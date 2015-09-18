package com.example.sakai;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class download extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "com.example.sakai.action.FOO";
    private static final String ACTION_BAZ = "com.example.sakai.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.example.sakai.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.example.sakai.extra.PARAM2";

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent(context, download.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, download.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    public download() {
        super("download");
    }
    String download_status;
    @Override
    protected void onHandleIntent(Intent intent) {

        String target = intent.getStringExtra("url");
        String name = intent.getStringExtra("name");
        HttpGet httpget = new HttpGet(target);
        File extDir = Environment.getExternalStorageDirectory();
        File dir = new File(extDir,name);
        if(dir.exists() == false) {
            try {
                HttpResponse response = MainActivity.httpclient.execute(httpget);
                InputStream in = response.getEntity().getContent();
                dir.createNewFile();
                dir.setWritable(Boolean.TRUE);
                FileOutputStream out = new FileOutputStream(dir);
                byte[] b = new byte[1024];
                int len = 0;
                while ((len = in.read(b)) != -1) {
                    out.write(b, 0, len);
                }
                in.close();
                out.close();
                Log.d("ACTIVITY_TAG", "download");
                download_status = "download succeed";
                NotificationManager nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                Notification.Builder n = new Notification.Builder(getApplicationContext())
                        .setContentText(name+" "+download_status)
                        .setContentTitle("sakai")
                        .setTicker("sakai")
                        .setWhen(System.currentTimeMillis())
                        .setVibrate(new long[] {0,300,500,700})
                        .setSmallIcon(R.drawable.icon);
                nm.notify(1, n.build());
            } catch (ClientProtocolException e) {
                e.printStackTrace();
                download_status = "download fail";
            } catch (IOException e) {
                e.printStackTrace();
                download_status = "download fail";
            } finally {

            }
        }
        else{
            download_status = "file existed";
        }
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),download_status,Toast.LENGTH_SHORT).show();
            }
        });

    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
