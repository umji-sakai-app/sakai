package com.example.sakai;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Created by DaiTao on 2015/9/21.
 */
public class MethodActivity extends Activity {
    public String accessJson(String ID, String part, String url){// usage: give the ID, and the name of this part, like announcement, and
        String result = null;                                    // then the url needed to fetch the file.
        FileInputStream in = null;
        BufferedReader reader = null;
        StringBuilder content = new StringBuilder();
        try{
            File file = new File(getFilesDir().getAbsolutePath(),ID+ part +"JsonFile");
            if (file.exists()){
                Log.d(part, "exist");
            }
            else{
                Log.d(part, "non-exist");
            }
            in = openFileInput(ID+ part +"JsonFile");
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while((line = reader.readLine()) != null){
                content.append(line);
            }

            reader.close();
            Log.d(part, "successfully read the json string from the file");
            result = content.toString();
            return result;
        }catch (IOException e){
            HttpGet httpRequest = new HttpGet(url);
            HttpResponse httpresponse;
            try {
                httpresponse = MainActivity.httpclient.execute(httpRequest);
                if (httpresponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    result = EntityUtils.toString(httpresponse.getEntity(), "utf-8");    //json string  //add "utf-8"
                    FileOutputStream out = null;    // write the json string into the file. After that read the json string from the file.
                    BufferedWriter writer = null;
                    try{
                        out = openFileOutput(ID+ part +"JsonFile", Context.MODE_PRIVATE);
                        Log.d(part, "out info: " + out.toString() + "   The file name is " + ID+ part +"JsonFile");

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
                    }
                    Log.d(part, "successfully load the json string into files");

                    Log.d(part, "yes");

                } else {
                    result = "fail to access";
                    Log.d(part, "no");
                }
            } catch (IOException c) {
                c.printStackTrace();
            }
            System.out.println("http");
            return result;
        }finally{
            if(reader != null){
                try{
                    reader.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }

        }
    }
}
