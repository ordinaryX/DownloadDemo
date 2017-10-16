package com.example.hyx.downloadfile;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.widget.Toast;

import java.io.File;

public class DownloadService extends Service {

    private DownloadTask downloadTask;

    private DownloadListener downloadListener;

    private DownloadBinder mBinder = new DownloadBinder() ;

    public DownloadService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    class DownloadBinder extends Binder {

        private String downloadUrl;

        void startDownload(String url) {
            this.downloadUrl = url;
            if (downloadTask == null) {
                downloadListener = new MyDownloadListener();
                downloadTask = new DownloadTask(downloadListener);
            }
            downloadTask.execute(url);
            startForeground(1, getNotification("start download...", -1));
        }

        void pauseDownload() {
            if (downloadTask != null)
                downloadTask.pauseDownload();
        }

        void cancelDownload() {
            if (downloadTask != null) {
                downloadTask.cancelDownload();
            } else {
                if (downloadUrl != null) {
                    String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/") + 1);
                    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
                    if (file.exists()) {
                        file.delete();
                        Toast.makeText(DownloadService.this, "cancel download and delete file...", Toast.LENGTH_SHORT).show();
                    }

                    stopForeground(true);
                }
            }
        }
    }

    class MyDownloadListener implements DownloadListener {

        private int lastProgress;

        @Override
        public void onProgress(int progress) {
            lastProgress = progress;
            getNotificationManager().notify(1, getNotification("downloading...", progress));
        }

        @Override
        public void onSuccess() {
            downloadTask = null;
            stopForeground(true);
            getNotificationManager().notify(1, getNotification("download success", -1));
            Toast.makeText(DownloadService.this, "download success!!!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPaused() {
            downloadTask = null;
            getNotificationManager().notify(1, getNotification("download paused", lastProgress));
            Toast.makeText(DownloadService.this, "pause download...", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCanceled() {
            downloadTask = null;
            stopForeground(true);
            Toast.makeText(DownloadService.this, "cancel download...", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailed() {
            downloadTask = null;
            stopForeground(true);
            getNotificationManager().notify(1, getNotification("download failed", -1));
            Toast.makeText(DownloadService.this, "download failed...", Toast.LENGTH_SHORT).show();
        }
    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    private Notification getNotification(String titile, int progress) {
        Intent intent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(intent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "myChannelId");
        builder.setSmallIcon(R.mipmap.ic_launcher);
//        builder.setDefaults(Notification.DEFAULT_ALL);
        builder.setContentTitle(titile);
        builder.setContentIntent(pendingIntent);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        if (progress > 0) {
            builder.setProgress(100, progress, false);
            builder.setContentText("Progress: " + progress + "%");
        }
        return builder.build();
    }
}
