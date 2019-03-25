package com.example.ebookreader;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import com.example.ebookreader.MyX509TrustManager;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.os.Environment.getExternalStorageDirectory;

public class ShelfActivity extends BasicsFragmentActivity implements View.OnClickListener {
    private static int BUFFER_SIZE = 1000000;

    @Override
    protected void onResume() {
        super.onResume();
//        onCreate(null);
    }

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
                    //finish();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String dir = getExternalStorageDirectory().getAbsolutePath();
//        copyAssets(this, "text", dir + "/text");
//        copyAssets(this, "image", dir + "/image");
//        copy(this, "imageLib/02_Tarzan of the Apes.jpg", "image/02_Tarzan of the Apes.jpg");
    }


    public static String post(String path, Map<String, String> parameters) throws IOException, NoSuchAlgorithmException, GeneralSecurityException {

        // 创建SSLContext对象，并使用我们指定的信任管理器初始化
        TrustManager[] tm = {new MyX509TrustManager()};
        SSLContext sslContext = SSLContext.getInstance("SSL");//, "SunJSSE");
        sslContext.init(null, tm, new java.security.SecureRandom());
        // 从上述SSLContext对象中得到SSLSocketFactory对象
        SSLSocketFactory ssf = sslContext.getSocketFactory();
        URL url = new URL(path);
        HttpsURLConnection httpsConn = (HttpsURLConnection) url.openConnection();
        httpsConn.setSSLSocketFactory(ssf);
        httpsConn.setDoInput(true);// 打开输入流，以便从服务器获取数据
        httpsConn.setDoOutput(true);// 打开输出流，以便向服务器提交数据
        if (parameters != null) {
            url = new URL(url.toString() + buildGetParameterString(parameters));
        }
        //InputStream rea = httpsConn.getInputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(httpsConn.getInputStream()));
        String line = null;
        StringBuffer rt = new StringBuffer();
        while ((line = in.readLine()) != null) {
            rt.append(line);
            System.out.println(line);
        }
        return rt.toString();
    }

    private static String buildGetParameterString(Map<String, String> parameters) {
        String getParameterString = "";

        for (Map.Entry<String, String> param : parameters.entrySet()) {
            if (param.getValue() == null) {
                continue;
            }

            getParameterString += (getParameterString.length() < 1) ? ("?") : ("&");

            getParameterString += param.getKey() + "=" + param.getValue();
        }

        return (getParameterString);
    }

    private class DownloadUpdate extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            try {
                return post("https://pan.baidu.com/s/1ryAbhMPFZhV5B0JLjOS0qQ", null);//"https://raw.githubusercontent.com/Dhthictct/EbookReaderLib/master/text/03_The%20Iron%20Heel%20.txt",null);
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (GeneralSecurityException e1) {
                e1.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String temperature) {
            //Update the temperature displayed
            //((TextView) findViewById(R.id.temperature_of_the_day)).setText(temperature);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnImportBook:
                Intent intent = new Intent(this, LibActivity.class);
                startActivity(intent);
//                new DownloadUpdate().execute();
//                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
//                intent.addCategory(Intent.CATEGORY_OPENABLE);
//                startActivityForResult(intent, 1);
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

    public static void copy(Context context, String zipPath, String targetPath) {
        if (TextUtils.isEmpty(zipPath) || TextUtils.isEmpty(targetPath)) {
            return;
        }
        File dest = new File(targetPath);
        dest.getParentFile().mkdirs();
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new BufferedInputStream(context.getAssets().open(zipPath));
            out = new BufferedOutputStream(new FileOutputStream(dest));
            byte[] buffer = new byte[BUFFER_SIZE];
            int length = 0;
            while ((length = in.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void copyAssets(Context context, String assetDir, String targetDir) {
        if (TextUtils.isEmpty(assetDir) || TextUtils.isEmpty(targetDir)) {
            return;
        }
        String separator = File.separator;
        try {
            // 获取assets目录assetDir下一级所有文件以及文件夹
            String[] fileNames = context.getResources().getAssets().list(assetDir);
            // 如果是文件夹(目录),则继续递归遍历
            if (fileNames.length > 0) {
                File targetFile = new File(targetDir);
                if (!targetFile.exists() && !targetFile.mkdirs()) {
                    return;
                }
                for (String fileName : fileNames) {
                    copyAssets(context, assetDir + separator + fileName, targetDir + separator + fileName);
                }
            } else { // 文件,则执行拷贝
                copy(context, assetDir, targetDir);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    private void writeStringToFile(String str) {
//        if (!isExternalStorageWritable()) {
//            return;
//        }
//
//        File dir = getExternalStorageDirectory();
//        new File("/storage/emulated/0/Android/test");
//                getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
//
//        Log.e(TAG, "writeStringToFile: dir = " + dir.getAbsolutePath());
//
//        if (!dir.exists()) {
//            dir.mkdirs();
//        }
//
//        File file = new File(dir, "str.txt");
//        if (file.exists()) {
//            file.delete();
//        }
//
//        FileOutputStream fos = null;
//        BufferedOutputStream bos = null;
//        try {
//            file.createNewFile();
//
//            fos = new FileOutputStream(file);
//            bos = new BufferedOutputStream(fos);
//
//            bos.write(str.getBytes());
//
//            bos.close();
//            fos.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (bos != null) {
//                    bos.close();
//                }
//                if (fos != null) {
//                    fos.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
}