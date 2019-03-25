package com.example.ebookreader;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public class LibActivity extends BasicsFragmentActivity {
    public static final String EXTRA_BOOK_ID = "EXTRA_BOOK_ID";

    public static Intent newIntent(Context context, int bookId) {
        Intent intent = new Intent(context, LibActivity.class);
        intent.putExtra(EXTRA_BOOK_ID, bookId);
        return intent;
    }


    @Override
    protected Fragment createFragment() {
        return new LibFragment();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
