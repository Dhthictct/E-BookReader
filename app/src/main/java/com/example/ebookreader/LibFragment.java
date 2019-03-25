package com.example.ebookreader;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.ebookreader.model.Book;
import com.example.ebookreader.model.BookLib;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static android.os.Environment.getExternalStorageDirectory;

public class LibFragment extends Fragment {
    private static final int BUFFER_SIZE = 1000000;
    private Context mContext;
    private List<Book> mBookList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_lib, container, false);
        try {
            initEvents(v);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return v;

    }

    private void initEvents(View v) throws IOException {
        mContext = getActivity();
        mBookList = BookLib.newInstance(mContext).getBookList();

        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.booklib_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(mContext, 3));
        recyclerView.setAdapter(new LibFragment.BookAdapter(mBookList));

    }


    private class BookHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView mBookCover;
        private Book mBook;

        public BookHolder(View itemView) {
            super(itemView);
            mBookCover = (ImageView) itemView.findViewById(R.id.item_recycler_view_image_view);
            itemView.setOnClickListener(this);
        }

        public void bind(Book book) {
            mBook = book;
            mBookCover.setImageBitmap(mBook.getBookCover());
        }

        @Override
        public void onClick(View v) {
//            Intent intent = ReadingActivity.newIntent(mContext, mBookList.indexOf(mBook));
//            startActivity(intent);
//            Intent intent = new Intent(this, ShelfActivity.class);
//            startActivity(intent);
            String dir = getExternalStorageDirectory().getAbsolutePath();
            copyAssets(mContext, "text", dir + "/text");
            copyAssets(mContext, "image", dir + "/image");
            try {
                String[] fileNames = mContext.getResources().getAssets().list("textLib");

                int inde = mBookList.indexOf(mBook);
                String im = "imageLib/" + fileNames[inde];
                String assimg = im.replace(".txt", ".jpg");
                String asstxt = "textLib/" + fileNames[inde];
                copy(mContext, asstxt, dir + "/text/" + fileNames[inde]);
                String img = fileNames[inde].replace(".txt", ".jpg");

                copy(mContext, assimg, dir + "/image/" + img);
                Toast.makeText(mContext, "Please restart application refresh bookshelf information", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(mContext, ShelfActivity.class);
                startActivity(intent);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private class BookAdapter extends RecyclerView.Adapter<LibFragment.BookHolder> {
        private List<Book> bookList = new ArrayList<>();

        public BookAdapter(List<Book> bookList) {
            this.bookList = bookList;
        }

        @Override
        public LibFragment.BookHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.item_recycler_view_shelf, parent, false);

            return new LibFragment.BookHolder(view);
        }

        @Override
        public void onBindViewHolder(LibFragment.BookHolder holder, int position) {
            holder.bind(bookList.get(position));
        }

        @Override
        public int getItemCount() {
            return bookList.size();
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
}
