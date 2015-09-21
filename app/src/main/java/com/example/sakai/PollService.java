package com.example.sakai;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

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

public class PollService extends Service {
    private boolean MainMenuJsonFile = false;
    private String MainMenuJsonFileName = "MainMenuJsonFile";
    private static String announcementResult[];
    private static String assignmentResult[];
    private static String gradebookResult[];
    private static String resultIni;

    private static String firstAnnouncementID;
    private static String firstAnnouncementTitle;
    private static String firstAnnouncementBody;
    private static String firstAnnouncementAttachmentTitle;
    private static String firstAnnouncementAttachmentURL;

    private static String lastAssignmentID;
    private static String lastAssignmentTitle;
    private static String lastAssignmentOpen;
    private static String lastAssignmentDue;
    private static String lastAssignmentStatus;
    private static String lastAssignmentAttachmentTitle;
    private static String lastAssignmentAttachmentURL;

    private static Integer newLength;
    private static String changeID;
    private static String changeTitle;

    private static ArrayList<String> ID;
    private static ArrayList<String> title;
    private static ArrayList<String> gradebookID;
    private static ArrayList<String> gradebookTitle;

    private static String[] currentAnnouncementFirstID;
    private static String[] currentAssigmentLastID;
    private static Integer[] currentLength;


    //    private static int times = 0;
    private static boolean ifFinished=false;
    private static boolean ifAnnouncement=false;
    private static boolean ifAssignment=false;
    private static boolean ifGradebook=false;
    public PollService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ID=new ArrayList<String>();
        title=new ArrayList<String>();
        gradebookID=new ArrayList<String>();
        gradebookTitle=new ArrayList<String>();
        new Thread(new Runnable(){
            public void run(){
                Log.d("gy", "Service Start");
                initial();
                getAnnouncement();
                getAssignment();
                getGradebook();
                initialCurrent();
                ifFinished=true;

                //startActivity(new Intent(MainActivity.this,MainMenu.class));
            }}).start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anMinute = 2*60 * 1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + anMinute;
        Intent i = new Intent(this, PollReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!ifFinished){

                }
                Log.d("gy", "Service run");
                getAnnouncement();
                getAssignment();
                getGradebook();
                if (ifAnnouncementChanged()||ifAssignmentChanged()||ifGradebookChanged()) {
                    if(ifAnnouncement){
                        Log.d("gy","New Annoucement");
                        ifAnnouncement=false;
                        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        Notification notification = new Notification(R.drawable.ic_launcher, "An new announcement posted", System.currentTimeMillis());
                        Intent intent = new Intent(PollService.this, AnnouncementContext.class);
                        String selectedTitle = firstAnnouncementTitle;
                        intent.putExtra("title", selectedTitle);
                        String selectedBody = firstAnnouncementBody;
                        intent.putExtra("body", selectedBody);
                        String selectedAttachName = firstAnnouncementAttachmentTitle;
                        intent.putExtra("attachTitle", selectedAttachName);
                        String selectedAttachURL = firstAnnouncementAttachmentURL;
                        intent.putExtra("attachURL", selectedAttachURL);

                        PendingIntent pi = PendingIntent.getActivity(PollService.this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                        notification.setLatestEventInfo(PollService.this, "An new announcement posted", firstAnnouncementTitle, pi);
                        manager.notify(1, notification);
                    }else if(ifAssignment){
                        Log.d("gy","New Assignment");
                        ifAssignment=false;
                        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        Notification notification = new Notification(R.drawable.ic_launcher, "An new assignment posted", System.currentTimeMillis());
                        Intent intent = new Intent(PollService.this, AssignmentDetail.class);
                        String selectedTitle = lastAssignmentTitle;
                        intent.putExtra("title", selectedTitle);
                        String selectedOpen = lastAssignmentOpen;
                        intent.putExtra("open", selectedOpen);
                        String selectedDue = lastAssignmentDue;
                        intent.putExtra("due", selectedDue);
                        String selectedStatus = lastAssignmentStatus;
                        intent.putExtra("status", selectedStatus);
                        String selectedAttachName = lastAssignmentAttachmentTitle;
                        intent.putExtra("name", selectedAttachName);
                        String selectedAttachURL = lastAssignmentAttachmentURL;
                        intent.putExtra("url", selectedAttachURL);

                        PendingIntent pi = PendingIntent.getActivity(PollService.this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                        notification.setLatestEventInfo(PollService.this, "An new assignment posted", lastAssignmentTitle, pi);
                        manager.notify(2, notification);
                    }else if(ifGradebook){
                        Log.d("gy","New Gradebook");
                        ifGradebook=false;
                        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        Notification notification = new Notification(R.drawable.ic_launcher, "Gradebook updated", System.currentTimeMillis());
                        Intent intent = new Intent(PollService.this, Gradebook.class);
                        intent.putExtra("id", changeID);
                        PendingIntent pi = PendingIntent.getActivity(PollService.this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                        notification.setLatestEventInfo(PollService.this, "Gradebook updated", changeTitle, pi);
                        manager.notify(3, notification);
                    }
                }
            }
        }).start();

        return super.onStartCommand(intent, flags, startId);
    }

    //get announcement from sakai
    public void getAnnouncement() {
        //   FileInputStream in = null;
        //    BufferedReader reader = null;
        StringBuilder content = new StringBuilder();

        for (int i = 0; i < title.size(); i++) {
            announcementResult[i] = content.toString();
            Log.d("MainMenu", "successfully read the json string from the file");
            String target = "http://202.120.46.147/direct/announcement/site/" + ID.get(i) + ".json?n=10&d=21";
            HttpGet httpRequest = new HttpGet(target);
            HttpResponse httpresponse;
            try {
                httpresponse =MainActivity.httpclient.execute(httpRequest);
                if (httpresponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    announcementResult[i] = EntityUtils.toString(httpresponse.getEntity(), "utf-8");    //json string  //add "utf-8"
                    FileOutputStream out = null;    // write the json string into the file. After that read the json string from the file.
                    BufferedWriter writer = null;
                    try {
                        out = openFileOutput(ID.get(i) + "AnnouncementJsonFile", Context.MODE_PRIVATE);
                        writer = new BufferedWriter(new OutputStreamWriter(out));
                        writer.write(announcementResult[i]);
                    } catch (IOException a) {
                        a.printStackTrace();
                    } finally {
                        try {
                            if (writer != null) {
                                writer.close();
                            }
                        } catch (IOException b) {
                            b.printStackTrace();
                        }
//                        AnnouncementFileFlag = true;
                    }
                    Log.d("Announcement", "successfully load the json string into files");

                    Log.d("Announcement", "yes");

                } else {
                    announcementResult[i] = "fail to access";
                    Log.d("Announcement", "no");
                }
            } catch (IOException c) {
                c.printStackTrace();
            }

        }


    }

    void getAssignment() {
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < title.size(); i++) {
            assignmentResult[i] = content.toString();
            Log.d("MainMenu", "successfully read the json string from the file");
            String target = "http://202.120.46.147/direct/assignment/site/" + ID.get(i) + ".json?n=10&d=21";
            HttpGet httpRequest = new HttpGet(target);
            HttpResponse httpresponse;
            try {
                httpresponse = MainActivity.httpclient.execute(httpRequest);
                if (httpresponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    assignmentResult[i] = EntityUtils.toString(httpresponse.getEntity(), "utf-8");    //json string  //add "utf-8"
                    FileOutputStream out = null;    // write the json string into the file. After that read the json string from the file.
                    BufferedWriter writer = null;
                    try {
                        out = openFileOutput(ID.get(i) + "AssignmentJsonFile", Context.MODE_PRIVATE);
                        writer = new BufferedWriter(new OutputStreamWriter(out));
                        writer.write(assignmentResult[i]);
                    } catch (IOException a) {
                        a.printStackTrace();
                    } finally {
                        try {
                            if (writer != null) {
                                writer.close();
                            }
                        } catch (IOException b) {
                            b.printStackTrace();
                        }
//                        AnnouncementFileFlag = true;
                    }
                    Log.d("Assignment", "successfully load the json string into files");

                    Log.d("Assignment", "yes");

                } else {
                    assignmentResult[i] = "fail to access";
                    Log.d("Assignment", "no");
                }
            } catch (IOException c) {
                c.printStackTrace();
            }
            System.out.println("http");
        }

    }

    public void getGradebook() {
        //   FileInputStream in = null;
        //    BufferedReader reader = null;
        StringBuilder content = new StringBuilder();

        for (int i = 0; i < gradebookTitle.size(); i++) {

            gradebookResult[i] = content.toString();
            String target = "http://202.120.46.147/direct/gradebook/site/" + gradebookID.get(i) + ".json";
            HttpGet httpRequest = new HttpGet(target);
            HttpResponse httpresponse;
            try {
                httpresponse =MainActivity.httpclient.execute(httpRequest);
                if (httpresponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    gradebookResult[i] = EntityUtils.toString(httpresponse.getEntity(), "utf-8");    //json string  //add "utf-8"
                    FileOutputStream out = null;    // write the json string into the file. After that read the json string from the file.
                    BufferedWriter writer = null;
                    try {
                        out = openFileOutput(gradebookID.get(i) + "GradebookJsonFile", Context.MODE_PRIVATE);
                        writer = new BufferedWriter(new OutputStreamWriter(out));
                        writer.write(gradebookResult[i]);
                    } catch (IOException a) {
                        a.printStackTrace();
                    } finally {
                        try {
                            if (writer != null) {
                                writer.close();
                            }
                        } catch (IOException b) {
                            b.printStackTrace();
                        }
                    }

                } else {
                    gradebookResult[i] = "fail to access";
                }
            } catch (IOException c) {
                c.printStackTrace();
            }

        }
    }

    //handle the json file we get. Giving value to announcementTitle, announcementBody,attachmentURL[], attachmentTitle[] and nnouncementTitle_list.
    public boolean ifAnnouncementChanged() {
        JSONObject jsonobject = null;
        JSONArray announcement_collection = null;
        for (int i = 0; i < title.size(); i++) {
            try {
                jsonobject = new JSONObject(announcementResult[i]);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                announcement_collection = jsonobject.getJSONArray("announcement_collection");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                JSONObject data = announcement_collection.getJSONObject(0);
                firstAnnouncementID = data.getString("announcementId");
                firstAnnouncementTitle = data.getString("title");
                firstAnnouncementBody = data.getString("body");

                if (data.isNull("attachments") == true) {
                    firstAnnouncementAttachmentTitle = "0";
                    firstAnnouncementAttachmentURL = "0";
                } else {
                    JSONArray attachment = data.getJSONArray("attachments");
                    JSONObject attachData = attachment.getJSONObject(0);
                    firstAnnouncementAttachmentTitle = attachData.getString("name");
                    firstAnnouncementAttachmentURL = attachData.getString("url");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (!currentAnnouncementFirstID[i].equals( firstAnnouncementID)) {
                currentAnnouncementFirstID[i] = firstAnnouncementID;
                ifAnnouncement=true;
                return true;
            }

        }

        return false;

    }

    public boolean ifAssignmentChanged() {
        JSONObject jsonobject = null;
        JSONArray assignment_collection = null;
        for (int i = 0; i < title.size(); i++) {
            try {
                jsonobject = new JSONObject(assignmentResult[i]);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                assignment_collection = jsonobject.getJSONArray("assignment_collection");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            int last=assignment_collection.length()-1;
            if (assignment_collection.isNull(last)){

                lastAssignmentID = "0";
                lastAssignmentTitle = "0";
                lastAssignmentDue = "0";
                lastAssignmentOpen = "0";
                lastAssignmentStatus = "0";
                lastAssignmentAttachmentTitle = "0";
                lastAssignmentAttachmentURL = "0";
            }else{
                try {
                    JSONObject data = assignment_collection.getJSONObject(last);
                    lastAssignmentID = data.getString("entityId");
                    lastAssignmentTitle = data.getString("title");
                    lastAssignmentDue = data.getString("dueTimeString");
                    lastAssignmentOpen = data.getString("openTimeString");
                    lastAssignmentStatus = data.getString("status");

                    if (data.isNull("attachments") == true) {
                        lastAssignmentAttachmentTitle = "0";
                        lastAssignmentAttachmentURL = "0";
                        System.out.println("0");
                    } else {
                        JSONArray attachment = data.getJSONArray("attachments");
                        JSONObject attachData = attachment.getJSONObject(0);
                        lastAssignmentAttachmentTitle = attachData.getString("name");
                        lastAssignmentAttachmentURL = attachData.getString("url");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (!currentAssigmentLastID[i].equals(lastAssignmentID)) {
                currentAssigmentLastID[i] = lastAssignmentID;
                ifAssignment=true;
                return true;
            }
        }
        return false;

    }

    public boolean ifGradebookChanged() {
        JSONObject jsonObject = null;
        JSONArray gradeArray = null;
        for (int i = 0; i < gradebookTitle.size(); i++) {
            try {
                jsonObject = new JSONObject(gradebookResult[i]);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if(jsonObject.isNull("assignments")){
                newLength=0;
            }
            else {
                try {
                    gradeArray = jsonObject.getJSONArray("assignments");
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                newLength=gradeArray.length();
            }


            if (newLength!=currentLength[i]) {
                currentLength[i] = newLength;
                changeID=gradebookID.get(i);
                changeTitle=gradebookTitle.get(i);
                ifGradebook=true;
                return true;
            }
        }
        return false;

    }

    public void initial() {
        FileInputStream in = null;
        BufferedReader reader = null;
        StringBuilder content = new StringBuilder();
        try {
            File file = new File(getFilesDir().getAbsolutePath(), MainMenuJsonFileName);
//                Log.d("Announcement", file.exists() ? "exist":"not exist");
            if (file.exists()) {
                Log.d("MainMenu", "exist");
            } else {
                Log.d("MainMenu", "non-exist");
            }
            in = openFileInput(MainMenuJsonFileName);
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            resultIni = content.toString();
            reader.close();
            Log.d("MainMenu", "successfully read the json string from the file");
        } catch (IOException e) {
//                e.printStackTrace();
            String target = "http://202.120.46.147/direct/site.json";
            HttpGet httpRequest = new HttpGet(target);
            HttpResponse httpresponse;
            try {
                httpresponse = MainActivity.httpclient.execute(httpRequest);
                if (httpresponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    resultIni = EntityUtils.toString(httpresponse.getEntity(), "utf-8");    //json string  //add "utf-8"
                    FileOutputStream out = null;    // write the json string into the file. After that read the json string from the file.
                    BufferedWriter writer = null;
                    try {
                        out = openFileOutput(MainMenuJsonFileName, Context.MODE_PRIVATE);
//                        Log.d("MainMenu", "out info: " + out.toString() + "   The file name is" + ID + "AnnouncementJsonFile");
//                            Log.d("Announcement", wri)
                        writer = new BufferedWriter(new OutputStreamWriter(out));
                        writer.write(resultIni);
                    } catch (IOException a) {
                        a.printStackTrace();
                    } finally {
                        try {
                            if (writer != null) {
                                writer.close();
                            }
                        } catch (IOException b) {
                            b.printStackTrace();
                        }
//                        AnnouncementFileFlag = true;
                    }
                    Log.d("MainMenu", "successfully load the json string into files");

                    Log.d("MainMenu", "yes");

                } else {
                    resultIni = "fail to access";
                    Log.d("MainMenu", "no");
                }
            } catch (IOException c) {
                c.printStackTrace();
            }
//            System.out.println("http");

        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        JSONObject jsonObject = null;
        JSONArray site_collection = null;
        JSONObject info = null; // never used


        try {
            jsonObject = new JSONObject(resultIni);
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


        for (int i = 0; i < site_collection.length(); i++) {

            try {
                JSONObject data = site_collection.getJSONObject(i);
                if(data.getString("title").contains("FA2015") || data.getString("title").contains("Current Student") || data.getString("title").contains("Scholarship and Awards")
                        || data.getString("title").contains("JI Career")|| data.getString("title").contains("sakai app")){
                    title.add(data.getString("title"));
                    ID.add(data.getString("entityId"));
                    if(!data.getString("title").contains("VG101")) {
                        gradebookTitle.add(data.getString("title"));
                        gradebookID.add(data.getString("entityId"));
                    }
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                Log.e("gy", "error");
                e.printStackTrace();
            }
        }
        announcementResult=new String[title.size()];
        assignmentResult=new String[title.size()];
        gradebookResult=new String[gradebookTitle.size()];
    }


    public void initialCurrent() {
        JSONObject jsonObject1 = null;
        JSONObject jsonObject2 = null;
        JSONObject jsonObject3 = null;
        JSONArray announcement_collection = null;
        JSONArray assignment_collection = null;
        JSONArray gradeArray = null;

        currentLength = new Integer[gradebookTitle.size()];
        currentAnnouncementFirstID = new String[title.size()];
        currentAssigmentLastID= new String[title.size()];
        for (int i = 0; i < title.size(); i++) {
            try {
                jsonObject1 = new JSONObject(announcementResult[i]);
                jsonObject2 = new JSONObject(assignmentResult[i]);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                announcement_collection = jsonObject1.getJSONArray("announcement_collection");
                assignment_collection = jsonObject2.getJSONArray("assignment_collection");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                JSONObject data = announcement_collection.getJSONObject(0);
                currentAnnouncementFirstID[i] = data.getString("announcementId");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            int last=assignment_collection.length()-1;
            if (assignment_collection.isNull(last) == true){
                currentAssigmentLastID[i] = "0";
            }
            else {
                try {
                    JSONObject data = assignment_collection.getJSONObject(last);
                    currentAssigmentLastID[i] = data.getString("entityId");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        for (int i = 0; i < gradebookTitle.size(); i++) {
            try {
                jsonObject3 = new JSONObject(gradebookResult[i]);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if(jsonObject3.isNull("assignments")){
                currentLength[i] = 0;
            }
            else {
                try {
                    gradeArray = jsonObject3.getJSONArray("assignments");
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                currentLength[i] = gradeArray.length();
            }
        }
    }
}
