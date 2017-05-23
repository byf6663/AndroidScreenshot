package com.you8.xp.jt1;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

public class CoreService extends Service {

    private static Intent mResultData = null;
    private int mScreenWidth;
    private int mScreenHeight;
    private int mScreenDensity;
    private WindowManager mWindowManager;
    private ImageReader mImageReader;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;

    private  static final int port = 20000;
    ServerSocket sockets;

    public CoreService() {
        Log.e("byfmsg","CoreService,CoreService");
        try {
            sockets = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("byfmsg","CoreService,CoreService.tcp.error");
        }
    }

    @Override
    public IBinder onBind(Intent intent)  {
        Log.e("byfmsg","CoreService,onBind");
        return null;
    }
    public static void setResultData(Intent mResultData) {
        Log.e("byfmsg","CoreService,setResultData");
        CoreService.mResultData = mResultData;
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("byfmsg","CoreService,onCreate");
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;
        Log.e("byfmsg","widht:"+String.valueOf(mScreenWidth)+",height:"+String.valueOf(mScreenHeight)+",dpi:"+String.valueOf(mScreenWidth));
        mImageReader = ImageReader.newInstance(mScreenWidth, mScreenHeight, PixelFormat.RGBA_8888, 10);
        startget();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void startget() {
        Log.e("byfmsg","CoreService,startget");

        if (mMediaProjection == null) {
            setUpMediaProjection();
        }
        virtualDisplay();

        new Thread(){
            @Override
            public void run() {
                super.run();
                OutputStream output = null;
                Socket socket;
                Image image = null;
                int num = 0;
                int status = 0;
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                Bitmap bitmap;
                while (true){
                    try {
                        Log.e("byfmsg","tcp.accept");
                        socket = sockets.accept();
                        Log.e("byfmsg","tcp.socket:"+socket.getInetAddress());
                        output = socket.getOutputStream();

                        while (true){

                            Thread.sleep(20);

                            image = mImageReader.acquireLatestImage();
                            if (image == null){
                                Log.e("byfmsg","CoreService,startget.image.null");
                                //virtualDisplay();
                                if (status == 0){
                                    output.write("start".getBytes());
                                    output.flush();
                                    status = 1;
                                }
                            }else{
                                status = 0;
                                num = num+1;
                                output.write("start".getBytes());
                                output.flush();
                                Log.e("byfmsg","tcp.socket.output."+("conn:"+String.valueOf(num)));

                                int width = image.getWidth();
                                int height = image.getHeight();
                                final Image.Plane[] planes = image.getPlanes();
                                final ByteBuffer buffer = planes[0].getBuffer();
                                int pixelStride = planes[0].getPixelStride();
                                int rowStride = planes[0].getRowStride();
                                int rowPadding = rowStride - pixelStride * width;
                                bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
                                bitmap.copyPixelsFromBuffer(buffer);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 30, out);
                                int compressedLen = out.toByteArray().length;
                                Log.e("byfmsg","num:"+String.valueOf(compressedLen));
                                output.write(out.toByteArray());
                                output.flush();
                                //out.close();
                                out.reset();

                                //output.close();

                            }

                            if (1 != 1){ break; }
                        }
                        Log.e("byfmsg","tcp.socket.over");

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e("byfmsg","tcp.socket.IOException");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Log.e("byfmsg","tcp.socket.InterruptedException");
                    }

                }

            }
        }.start();


        Log.e("byfmsg","CoreService,startget.over");
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setUpMediaProjection() {
        if (mResultData == null) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            startActivity(intent);
        } else {
            mMediaProjection = getMediaProjectionManager().getMediaProjection(Activity.RESULT_OK, mResultData);
        }
    }

    private MediaProjectionManager getMediaProjectionManager() {
        return (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void virtualDisplay() {
        mVirtualDisplay = mMediaProjection.createVirtualDisplay("screen-mirror",
                mScreenWidth, mScreenHeight, mScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.getSurface(), null, null);
    }
}
