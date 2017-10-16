package com.example.hyx.downloadfile;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

/**
 * 类描述：<br/>
 * Created by hyx on 2017/10/14.
 */

public class DownloadTask extends AsyncTask<String, Integer, Integer> {

    private static final int TYPE_SUCCESS = 1;
    private static final int TYPE_PAUSED = 2;
    private static final int TYPE_CANCELED = 3;
    private static final int TYPE_FAILED = 4;

    private DownloadListener downloadListener;

    private boolean isCanceled;
    private boolean isPaused;

    private long downloadedLength;

    private int lastProgress;

    public DownloadTask(DownloadListener downloadListener) {
        this.downloadListener = downloadListener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Integer doInBackground(String... strings) {
        InputStream is = null;
        RandomAccessFile randomAccessFile = null;
        long contentLength;
        String url = strings[0];

        String fileName = url.substring(strings[0].lastIndexOf("/") + 1);
        File targetFile = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS), fileName);

        contentLength = getContentLength(url);
        if (targetFile.exists()) {
            downloadedLength = targetFile.length();
        }
        if (contentLength == 0) {
            return TYPE_FAILED;
        } else if (contentLength == downloadedLength) {
            return TYPE_SUCCESS;
        }
        try {
            OkHttpClient client = new OkHttpClient.Builder().build();
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("RANGE", "bytes=" + downloadedLength + "-")
                    .build();
            Response response = client.newCall(request).execute();
            is = response.body().byteStream();
            randomAccessFile = new RandomAccessFile(targetFile, "rw");
            randomAccessFile.seek(downloadedLength);
            byte[] b = new byte[1024];
            int len = 0;
            long total = 0;
            while ((len = is.read(b)) != -1) {
                randomAccessFile.write(b, 0, len);
                total += len;
                int progress = (int) ((total + downloadedLength) * 100 / contentLength);

                if (progress > lastProgress){
                    lastProgress = progress;
                    publishProgress(progress);
                }

                if (isPaused) {
                    return TYPE_PAUSED;
                } else if (isCanceled) {
                    return TYPE_CANCELED;
                }
            }
            response.body().close();
            return TYPE_SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
                if (isCanceled && targetFile.exists()) {
                    targetFile.delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return TYPE_FAILED;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        downloadListener.onProgress(values[0]);
    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);
        switch (integer) {
            case TYPE_SUCCESS:
                downloadListener.onSuccess();
                break;
            case TYPE_FAILED:
                downloadListener.onFailed();
                break;
            case TYPE_PAUSED:
                downloadListener.onPaused();
                break;
            case TYPE_CANCELED:
                downloadListener.onCanceled();
                break;
        }
    }

    private long getContentLength(String string) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        Request request = new Request.Builder().url(string).build();
        try {
            Response response = okHttpClient.newCall(request).execute();
            if (response != null && response.isSuccessful()) {
                long contentLenght = response.body().contentLength();
                Log.e("contentLength", String.valueOf(contentLenght));
                response.close();
                return contentLenght;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
        return 0;
    }

    public void pauseDownload() {
        isPaused = true;
    }

    public void cancelDownload() {
        isCanceled = true;
    }
}
