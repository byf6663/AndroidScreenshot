package com.you8.xp.jt1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class CoreReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String name = intent.getStringExtra("msg");
        Toast.makeText(context, "广播"+name, Toast.LENGTH_SHORT).show();
        Log.e("byfmsg","广播"+name);

    }
}
