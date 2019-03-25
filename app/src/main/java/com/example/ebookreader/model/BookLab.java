package com.example.ebookreader.model;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static android.os.Environment.getExternalStorageDirectory;

public class BookLab {
    public static final String TEXT = "text";
    public static final String IMAGE = "image";
    private static BookLab sBookLab;

    private AssetManager mAssetManager;
    private List<Book> mBookList;
    //assets中的文件名清单
    private List<String> mAssetsImageList;
    private List<String> mAssetsTextList;


    private BookLab(Context context) {
        mAssetManager = context.getAssets();
        loadAssetsFiles();
    }

    public static List<String> getFilesAllName(String path) {
        File file = new File(path);
        File[] files = file.listFiles();
        if (files == null) {
            Log.e("error", "Empty directory");
            return null;
        }
        List<String> s = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            s.add(files[i].getAbsolutePath());
        }
        return s;
    }

    public static BookLab newInstance(Context context) {
        if (sBookLab == null) {
            sBookLab = new BookLab(context);
        }
        return sBookLab;
    }

    //加载assets中的文件
    private void loadAssetsFiles() {
        mBookList = new ArrayList<>();
        //获取image、text中的文件名清单
//        mAssetsImageList = mAssetManager.list(IMAGE);
//        mAssetsTextList = mAssetManager.list(TEXT);
        String patxt = getExternalStorageDirectory().getAbsolutePath() + "/image";
        String paimg = getExternalStorageDirectory().getAbsolutePath() + "/text";
        File filetxt = new File(patxt);
        File fileimg = new File(paimg);
        if (filetxt.exists() && fileimg.exists()) {
            mAssetsImageList = getFilesAllName(patxt);
            mAssetsTextList = getFilesAllName(paimg);
            for (int i = 0; i < mAssetsTextList.size(); ++i) {
                //获取书名
                String[] nameSplit = mAssetsTextList.get(i).split("_");
                String nameSecond = nameSplit[nameSplit.length - 1];
                String bookTitle = nameSecond.replace(".txt", "");
                System.out.println("++++++++++++++++++++++++++++++++++++++++++++" + bookTitle);
                //获取封面
                String imagePath = mAssetsImageList.get(i);//paimg + "/" + mAssetsImageList.get(i);
                //String imagePath = paimg + "/" + mAssetsImageList.get(i);
                Bitmap bookCover = loadImage(imagePath);

                //获取文本
                String textPath = mAssetsTextList.get(i);//patxt + "/" + mAssetsTextList.get(i);
                //String textPath = patxt + "/" + mAssetsTextList.get(i);
                String bodyText = loadText(textPath);

                Book book = new Book(bookTitle, bookCover, bodyText);
                mBookList.add(book);
            }
        }
    }


    //从assets中读取文本
    private String loadText(String path) {
        InputStream in = null;
        BufferedReader reader = null;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            File srcFile = new File(path);
            InputStream instream = new FileInputStream(srcFile);
            if (instream != null) {
                InputStreamReader inputreader = new InputStreamReader(instream, "UTF-8");
                reader = new BufferedReader(inputreader);
                String line = "";
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return stringBuilder.toString();
    }

    //从assets中读取图片
    private Bitmap loadImage(String path) {
        Bitmap image = null;
        InputStream in = null;
//        try {
        //in = mAssetManager.open(path);
        //FileInputStream stream = new FileInputStream(path);
        //image = BitmapFactory.decodeStream(in);
        image = BitmapFactory.decodeFile(path);
//        } finally {
//            try {
//                in.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
        return image;
    }

    public List<Book> getBookList() {
        return mBookList;
    }
}
