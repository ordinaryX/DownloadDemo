package com.example.hyx.downloadfile;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private EditText mEdittext;

    private DownloadService.DownloadBinder mDownloadBinder;

    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mDownloadBinder = (DownloadService.DownloadBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mEdittext = findViewById(R.id.edittext);
        Intent intent = new Intent(this,DownloadService.class);
        startService(intent);
        bindService(intent,connection,BIND_AUTO_CREATE);
    }

    public void onClick(View view) {

        if (!checkPermission()) return;

        switch (view.getId()){
            case R.id.btn_start:
                String url = mEdittext.getText().toString();
                if (TextUtils.isEmpty(url)){
                    Toast.makeText(this, "URL 不可用！", Toast.LENGTH_SHORT).show();
                    return;
                }
                mDownloadBinder.startDownload(url);
                break;
            case R.id.btn_cancel:
                mDownloadBinder.cancelDownload();
                break;
            case R.id.btn_pause:
                mDownloadBinder.pauseDownload();
                break;
        }
    }

    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0){
            if (requestCode == 1) {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"申请存储权限失败",Toast.LENGTH_SHORT).show();
                }
            }
        }else {
            Toast.makeText(this,"申请存储权限失败",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        unbindService(connection);
        super.onDestroy();
    }
}
