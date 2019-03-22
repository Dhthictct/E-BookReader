package com.example.ebookreader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.example.ebookreader.model.Book;
import com.example.ebookreader.model.BookLab;
import com.example.ebookreader.util.bookPage.BookPageFactory;
import com.example.ebookreader.util.bookPage.Label;
import com.example.ebookreader.util.bookPage.ReadInfo;
import com.example.ebookreader.util.SaveHelper;
import com.example.ebookreader.view.FlipView;
import com.example.ebookreader.view.popupWindow.ContentPopup;
import com.example.ebookreader.view.popupWindow.FontPopup;
import com.example.ebookreader.view.popupWindow.LabelPopup;
import com.example.ebookreader.view.popupWindow.SettingPopup;

import java.util.ArrayList;
import java.util.List;

public class ReadingFragment extends Fragment implements View.OnClickListener {
    public static final String ARG_FLIP_BOOK_ID = "ARG_FLIP_BOOK_ID ";
    public static final int TEXT_SIZE_DELTA = 50;

    private Context mContext;
    private int mBookId;
    private Book mBook;
    private BookPageFactory mBookPageFactory;

    private Bitmap mPrePage;
    private Bitmap mNextPage;
    private List<Bitmap> mPageList = new ArrayList<>();

    private int[] mBgColors;

    private FlipView mFlipView;

    private LinearLayout mBottomBar;
    private Button[] mBottomBtns;

    private SettingPopup mSettingPopup;
    private ContentPopup mContentPopup;
    private FontPopup mFontPopup;
    private LabelPopup mLabelPopup;

    private boolean isBottomBarShow = true;
    private boolean isFirstRead = true;//是否是第一次进入

    private BatteryPowerReceiver mBatteryReceiver;//电池电量广播接收者

    public static ReadingFragment newInstance(int bookId) {

        Bundle args = new Bundle();
        args.putInt(ARG_FLIP_BOOK_ID, bookId);
        ReadingFragment fragment = new ReadingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDatas();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_reading, container, false);
        initViews(v);
        initEvents();
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        //注册电量变化广播接收者
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        mBatteryReceiver = new BatteryPowerReceiver();
        mContext.registerReceiver(mBatteryReceiver, filter);
    }


    @Override
    public void onStop() {
        super.onStop();
        mContext.unregisterReceiver(mBatteryReceiver); //取消广播接收者

        //SettingPopup
        SaveHelper.save(mContext, SaveHelper.THEME, mSettingPopup.getTheme());
        SaveHelper.save(mContext, SaveHelper.FLIP_STYLE, mSettingPopup.getFlipStyle());

        //FlipView
        SaveHelper.save(mContext, SaveHelper.IS_PRE_PAGE_OVER, mFlipView.isPrePageOver());

        //BookPageFactory
        SaveHelper.saveObject(mContext, mBookId + SaveHelper.DRAW_INFO, mBookPageFactory.getReadInfo());
        SaveHelper.saveObject(mContext, SaveHelper.PAINT_INFO, mBookPageFactory.getPaintInfo());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCatalog:
                //设置出现动画和位置
                mContentPopup.setAnimationStyle(R.style.pop_window_anim_style);
                mContentPopup.showAsDropDown(mBottomBar, 0, -mContentPopup.getHeight());
                break;
            case R.id.btnLabel:
                saveLabel();
                Toast.makeText(mContext, "Labels have been added, long press to display the list of labels", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    //数据库存取书签
    private void saveLabel() {
        Time time = new Time();
        time.setToNow(); // 取得系统时间。
        String timeStr = time.year + "/" + time.month + "/" + time.monthDay;

        ReadInfo readInfo = mBookPageFactory.getReadInfo();
        String objectStr = SaveHelper.serObject(readInfo);

        Label label = new Label();
        label.setBookId(mBookId);
        label.setDetails(mBookPageFactory.getCurContent());
        label.setProgress(mBookPageFactory.getPercentStr());
        label.setTime(timeStr);
        label.setPrePageOver(mFlipView.isPrePageOver());
        label.setReadInfoStr(objectStr);
        label.save();
    }

    private void initDatas() {
        mContext = getActivity();
        mBookId = getArguments().getInt(ARG_FLIP_BOOK_ID);
        mBook = BookLab.newInstance(mContext).getBookList().get(mBookId);
        mBookPageFactory = new BookPageFactory(mContext, mBookId);

        mBgColors = new int[]{
                0xffe7dcbe,  //复古
                0xffffffff,  // 常规
                0xffcbe1cf,  //护眼
                0xff333232  //夜间
        };
    }

    private void initEvents() {

        if (isBottomBarShow)
            hideBottomBar();

        int theme = SaveHelper.getInt(mContext, SaveHelper.THEME);
        setTheme(theme);

        mFlipView.setOnPageFlippedListener(new FlipView.OnPageFlippedListener() {
            @Override
            public List<Bitmap> onNextPageFlipped() {
                //向后读一页
                mNextPage = mBookPageFactory.drawNextPage();//mPowerPercent);

                if (mNextPage == null)
                    return null;

                mPageList.remove(0);
                mPageList.add(mNextPage);
                return mPageList;
            }

            @Override
            public List<Bitmap> onPrePageFlipped() {
                mPrePage = mBookPageFactory.drawPrePage();//mPowerPercent);
                if (mPrePage == null)
                    return null;

                mPageList.remove(1);
                mPageList.add(0, mPrePage);
                return mPageList;
            }

            @Override
            public void onFlipStarted() {
                if (isBottomBarShow)
                    hideBottomBar();
            }

            @Override
            public void onFoldViewClicked() {
                if (isBottomBarShow)
                    hideBottomBar();
                else
                    showBottomBar();
            }
        });

        for (Button button : mBottomBtns) {
            button.setOnClickListener(this);
        }

        mBottomBtns[3].setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mLabelPopup.updateUI(); //刷新书签列表

                mLabelPopup.setAnimationStyle(R.style.pop_window_anim_style);
                mLabelPopup.showAsDropDown(mBottomBar, 0, -mLabelPopup.getHeight());
                return true;
            }
        });
        setPopupWindowListener();
    }


    private void setPopupWindowListener() {

        mSettingPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                hideBottomBar();
            }
        });
        mContentPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                hideBottomBar();

            }
        });

        mFontPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                hideBottomBar();
            }
        });

        mLabelPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                hideBottomBar();
            }
        });


        mSettingPopup.setOnSettingChangedListener(new SettingPopup.OnSettingChangedListener() {
            @Override
            public void onSizeChanged(int progress) {
                mPageList = mBookPageFactory.updateTextSize(progress + TEXT_SIZE_DELTA);//, mPowerPercent);
                mFlipView.updateBitmapList(mPageList);

            }

            @Override
            public void onThemeChanged(int theme) {
                setTheme(theme);
                mPageList = mBookPageFactory.updateTheme(theme);//, mPowerPercent);
                mFlipView.updateBitmapList(mPageList);
            }

            @Override
            public void onFlipStyleChanged(int style) {
                mFlipView.setFlipStyle(style);

            }
        });


        mContentPopup.setOnContentClicked(new ContentPopup.OnContentSelectedListener() {

            @Override
            public void OnContentClicked(int paraIndex) {
                mPageList = mBookPageFactory.updatePagesByContent(paraIndex);//, mPowerPercent);
                mFlipView.setPageByContent(mPageList);

                mContentPopup.dismiss();
            }
        });

        mFontPopup.setOnFontSelectedListener(new FontPopup.OnFontSelectedListener() {

            @Override
            public void onTypefaceSelected(int typeIndex) {
                mPageList = mBookPageFactory.updateTypeface(typeIndex);//, mPowerPercent);
                mFlipView.updateBitmapList(mPageList);
            }

            @Override
            public void onColorSelected(int color) {

                mPageList = mBookPageFactory.updateTextColor(color);//, mPowerPercent);
                mFlipView.updateBitmapList(mPageList);


            }
        });


        mLabelPopup.setOnLabelClicked(new LabelPopup.OnLabelSelectedListener() {
            @Override
            public void OnLabelClicked(Label label) {

                String objectStr = label.getReadInfoStr();
                ReadInfo readInfo = SaveHelper.deserObject(objectStr);
                boolean isPrePageOver = label.isPrePageOver();

                mBookPageFactory.setReadInfo(readInfo);
                mPageList = mBookPageFactory.drawCurTwoPages();//mPowerPercent);

                mFlipView.setPrePageOver(isPrePageOver);
                mFlipView.updateBitmapList(mPageList);

            }
        });


    }

    private void setTheme(int theme) {
        mBottomBar.setBackgroundColor(mBgColors[theme]);
        mContentPopup.setBackgroundColor(mBgColors[theme]);
        mLabelPopup.setBackgroundColor(mBgColors[theme]);
    }


    private void initViews(View v) {
        mFlipView = (FlipView) v.findViewById(R.id.flip_view);
        mBottomBar = (LinearLayout) v.findViewById(R.id.bottom_bar_layout);

        mBottomBtns = new Button[]{
                (Button) v.findViewById(R.id.btnCatalog),
                (Button) v.findViewById(R.id.btnSetting),
                (Button) v.findViewById(R.id.btnFont),
                (Button) v.findViewById(R.id.btnLabel)
        };

        mContentPopup = new ContentPopup(mContext, mBook);
        mSettingPopup = new SettingPopup(mContext);
        mFontPopup = new FontPopup(mContext);
        mLabelPopup = new LabelPopup(mContext, mBookId);

    }


    private void showBottomBar() {
        mBottomBar.setVisibility(View.VISIBLE);
        isBottomBarShow = true;
    }

    private void hideBottomBar() {
        mBottomBar.setVisibility(View.INVISIBLE);
        isBottomBarShow = false;
    }

    private class BatteryPowerReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isFirstRead) {
                ReadInfo readInfo = SaveHelper.getObject(mContext, mBookId + SaveHelper.DRAW_INFO);

                if (readInfo != null) {
                    mPageList = mBookPageFactory.drawCurTwoPages();
                    mFlipView.updateBitmapList(mPageList);

                } else {
                    mPageList.add(mBookPageFactory.drawNextPage());
                    mPageList.add(mBookPageFactory.drawNextPage());
                    mFlipView.setPrePageOver(false);
                    mFlipView.updateBitmapList(mPageList);
                }
                isFirstRead = false;
            }
        }
    }
}


