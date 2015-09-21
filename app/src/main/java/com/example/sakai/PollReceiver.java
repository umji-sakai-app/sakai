package com.example.sakai;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PollReceiver extends BroadcastReceiver {
    public PollReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Intent i;
        i = new Intent(context, PollService.class);
        context.startService(i);
    }
}
