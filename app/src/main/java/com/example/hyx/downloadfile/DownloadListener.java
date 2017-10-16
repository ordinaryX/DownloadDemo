package com.example.hyx.downloadfile;

/**
 * 类描述：<br/>
 * Created by hyx on 2017/10/14.
 */

public interface DownloadListener {

    void onProgress(int progress);

    void onSuccess();

    void onPaused();

    void onCanceled();

    void onFailed();
}
