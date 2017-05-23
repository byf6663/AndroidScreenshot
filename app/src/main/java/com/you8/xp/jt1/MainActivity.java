package com.you8.xp.jt1;

import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.DataOutputStream;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_MEDIA_PROJECTION = 18;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("byfmsg","MainActivity,onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        upgradeRootPermission(getPackageCodePath());
        requestCapturePermission();
    }
    public void requestCapturePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) { return; }
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager)getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(),REQUEST_MEDIA_PROJECTION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("byfmsg","MainActivity,onActivityResult:"+String.valueOf(requestCode)+"    "+String.valueOf(resultCode));
        switch (requestCode) {
            case REQUEST_MEDIA_PROJECTION:
                if (resultCode == RESULT_OK && data != null) {
                    CoreService.setResultData(data);
                    startService(new Intent(getApplicationContext(), CoreService.class));
                }
                break;
        }
    }
    public static boolean upgradeRootPermission(String pkgCodePath) {
        Log.e("byfmsg","MainActivity,root");
        Process process = null;
        DataOutputStream os = null;
        try {
            String cmd = "chmod 777 " + pkgCodePath;
            process = Runtime.getRuntime().exec("su"); // 切换到root帐号
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            Log.e("byfmsg","MainActivity,root.false");
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
            }
        }
        Log.e("byfmsg","MainActivity,root.true");
        return true;
    }
}
