package com.example.ebookreader;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.WindowManager;

public class ReadingActivity extends BasicsFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return null;
    }

    @Override
    protected void setScreen() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

}
