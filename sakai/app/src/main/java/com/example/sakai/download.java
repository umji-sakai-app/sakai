package com.example.sakai;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

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

    @Override
    protected void onHandleIntent(Intent intent) {
        String target = intent.getStringExtra("url");
        String name = intent.getStringExtra("name");
        HttpGet httpget = new HttpGet(target);
        try {
            HttpResponse httpresponse = MainActivity.httpclient.execute(httpget);
            if(httpresponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                String path = "sakai/"+name;
                FileOutputStream output = this.openFileOutput(name, this.MODE_APPEND);
                InputStream input = httpresponse.getEntity().getContent();
                byte[] b = new byte[1024];
                int j;
                while((j = input.read(b)) != -1){
                    output.write(b,0,j);
                }
                output.flush();
                output.close();
                System.out.println("can download");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

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
