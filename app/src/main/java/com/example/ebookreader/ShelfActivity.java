package com.example.ebookreader;

import android.support.v4.app.Fragment;

public class ShelfActivity extends BasicsFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new ShelfFragment();
    }
}