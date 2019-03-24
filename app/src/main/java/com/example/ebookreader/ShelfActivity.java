package com.example.ebookreader;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.os.Environment.getExternalStorageDirectory;

public class ShelfActivity extends BasicsFragmentActivity implements View.OnClickListener {
    @Override
    protected Fragment createFragment() {
        return new ShelfFragment();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestAllPower();
        //String mainDir = Environment.getExternalStorageDirectory().getAbsolutePath();
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.System.canWrite(this)) {
                    Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse(Settings.ACTION_MANAGE_WRITE_SETTINGS));
                    intent.setData(Uri.parse("package:" + this.getPackageName()));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    this.startActivity(intent);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String mainDir = getFilesDir().getAbsolutePath();

    }


    public boolean getPer() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.System.canWrite(this)) {
                    Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse(Settings.ACTION_MANAGE_WRITE_SETTINGS));
                    intent.setData(Uri.parse("package:" + this.getPackageName()));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    this.startActivity(intent);
                }
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    }, 102);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnImportBook:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 1);
                break;
        }
    }

    public void requestAllPower() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PERMISSION_GRANTED) {
                    Toast.makeText(this, "" + "Jurisdiction" + permissions[i] + "Successful application", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "" + "Jurisdiction" + permissions[i] + "Application failure", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    private void writeStringToFile(String str) {
//        if (!isExternalStorageWritable()) {
//            return;
//        }

        File dir = getExternalStorageDirectory();
        //new File("/storage/emulated/0/Android/test");
//                getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);

        //Log.e(TAG, "writeStringToFile: dir = " + dir.getAbsolutePath());

        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(dir, "str.txt");
        if (file.exists()) {
            file.delete();
        }

        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        try {
            file.createNewFile();

            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);

            bos.write(str.getBytes());

            bos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bos != null) {
                    bos.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}