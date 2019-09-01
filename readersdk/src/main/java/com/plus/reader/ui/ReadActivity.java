package com.plus.reader.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.plus.reader.R;
import com.plus.reader.bean.BookBean;
import com.plus.reader.dialog.ReadSettingDialog;
import com.plus.reader.preference.ReadSettingManager;
import com.plus.reader.ui.adapter.CategoryAdapter;
import com.plus.reader.utils.BrightnessUtils;
import com.plus.reader.utils.StringUtils;
import com.plus.reader.utils.SystemBarUtils;
import com.plus.reader.widget.page.PageLoader;
import com.plus.reader.widget.page.PageView;
import com.plus.reader.widget.page.TextPageLoader;
import com.plus.reader.widget.page.TxtChapter;

import java.util.List;


import static android.support.v4.view.ViewCompat.LAYER_TYPE_SOFTWARE;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * usage
 *
 * @author zhengjia.
 * @date 2019-06-27.
 */
public class ReadActivity extends FragmentActivity {
    public static final int REQUEST_MORE_SETTING = 1;


    private static final int WHAT_CATEGORY = 1;
    private static final int WHAT_CHAPTER = 2;


    DrawerLayout mDlSlide;
    /*************top_menu_view*******************/
    RelativeLayout mAblTopMenu;
    ImageView iv_back;
    /***************content_view******************/
    PageView mPvPage;
    /***************bottom_menu_view***************************/

    LinearLayout mLlBottomMenu;
    TextView mTvPreChapter;
    SeekBar mSbChapterProgress;
    TextView mTvNextChapter;
    TextView mTvCategory;
    TextView mTvNightMode;
    TextView mTvSetting;
    /***************left slide*******************************/
    ListView mLvCategory;
    /*****************view******************/
    private ReadSettingDialog mSettingDialog;
    private PageLoader mPageLoader;
    private Animation mTopInAnim;
    private Animation mTopOutAnim;
    private Animation mBottomInAnim;
    private Animation mBottomOutAnim;
    private CategoryAdapter mCategoryAdapter;
    private BookBean mCollBook;


    public static void startActivity(Context context, boolean isCollected) {
        context.startActivity(new Intent(context, ReadActivity.class));
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case WHAT_CATEGORY:
                    mLvCategory.setSelection(mPageLoader.getChapterPos());
                    break;
                case WHAT_CHAPTER:
                    mPageLoader.openChapter();
                    break;
            }
        }
    };


    /***************params*****************/
    private boolean isNightMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);

        initView();

        initData();
        initWidget();
        initClick();


        getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {

                // 设置 CollBook
                mPageLoader.getCollBook().bookChapterList = mCollBook.bookChapterList;
                // 刷新章节列表
                mPageLoader.refreshChapterList();

            }
        });
    }


    private void initView() {
        //半透明化StatusBar
        SystemBarUtils.transparentStatusBar(this);

        mDlSlide = findViewById(R.id.read_dl_slide);
        mAblTopMenu = findViewById(R.id.read_abl_top_menu);
        iv_back = findViewById(R.id.iv_back);
        mPvPage = findViewById(R.id.read_pv_page);
        mLlBottomMenu = findViewById(R.id.read_ll_bottom_menu);
        mTvPreChapter = findViewById(R.id.read_tv_pre_chapter);
        mSbChapterProgress = findViewById(R.id.read_sb_chapter_progress);
        mTvNextChapter = findViewById(R.id.read_tv_next_chapter);
        mTvCategory = findViewById(R.id.read_tv_category);
        mTvNightMode = findViewById(R.id.read_tv_night_mode);
        mTvSetting = findViewById(R.id.read_tv_setting);
        mLvCategory = findViewById(R.id.read_iv_category);
    }

    protected void initData() {
        isNightMode = ReadSettingManager.getInstance().isNightMode();

        mCollBook = TextPageLoader.getCollBookBean();
    }

    protected void initWidget() {

        // 如果 API < 18 取消硬件加速
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mPvPage.setLayerType(LAYER_TYPE_SOFTWARE, null);
        }

        //获取页面加载器
        mPageLoader = mPvPage.getPageLoader(mCollBook);
        //禁止滑动展示DrawerLayout
        mDlSlide.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        //侧边打开后，返回键能够起作用
        mDlSlide.setFocusableInTouchMode(false);
        mSettingDialog = new ReadSettingDialog(this, mPageLoader);

        setUpAdapter();

        //设置当前Activity的Brightness
        if (ReadSettingManager.getInstance().isBrightnessAuto()) {
            BrightnessUtils.setDefaultBrightness(this);
        } else {
            BrightnessUtils.setBrightness(this, ReadSettingManager.getInstance().getBrightness());
        }


        //初始化BottomMenu
        initBottomMenu();

    }

    private void initBottomMenu() {
        //设置mBottomMenu的底部距离
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mLlBottomMenu.getLayoutParams();
        params.bottomMargin = 0;
        mLlBottomMenu.setLayoutParams(params);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }


    private void setUpAdapter() {
        mCategoryAdapter = new CategoryAdapter();
        mLvCategory.setAdapter(mCategoryAdapter);
        mLvCategory.setFastScrollEnabled(true);
    }

    protected void initClick() {

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mPageLoader.setOnPageChangeListener(
                new PageLoader.OnPageChangeListener() {

                    @Override
                    public void onChapterChange(int pos) {
                        mCategoryAdapter.setChapter(pos);
                    }

                    @Override
                    public void requestChapters(List<TxtChapter> requestChapters) {

                    }

                    @Override
                    public void onCategoryFinish(List<TxtChapter> chapters) {
                        for (TxtChapter chapter : chapters) {
                            chapter.setTitle(StringUtils.convertCC(chapter.getTitle(), mPvPage.getContext()));
                        }
                        mCategoryAdapter.refreshItems(chapters);
                    }

                    @Override
                    public void onPageCountChange(int count) {
                        mSbChapterProgress.setMax(Math.max(0, count - 1));
                        mSbChapterProgress.setProgress(0);
                        // 如果处于错误状态，那么就冻结使用
                        if (mPageLoader.getPageStatus() == PageLoader.STATUS_LOADING
                                || mPageLoader.getPageStatus() == PageLoader.STATUS_ERROR) {
                            mSbChapterProgress.setEnabled(false);
                        } else {
                            mSbChapterProgress.setEnabled(true);
                        }
                    }

                    @Override
                    public void onPageChange(int pos) {
                        mSbChapterProgress.post(
                                () -> mSbChapterProgress.setProgress(pos)
                        );
                    }
                }
        );

        mSbChapterProgress.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (mLlBottomMenu.getVisibility() == VISIBLE) {
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        //进行切换
                        int pagePos = mSbChapterProgress.getProgress();
                        if (pagePos != mPageLoader.getPagePos()) {
                            mPageLoader.skipToPage(pagePos);
                        }
                    }
                }
        );

        mPvPage.setTouchListener(new PageView.TouchListener() {
            @Override
            public boolean onTouch() {
                return !hideReadMenu();
            }

            @Override
            public void center() {
                toggleMenu(true);
            }

            @Override
            public void prePage() {
            }

            @Override
            public void nextPage() {
                Log.d("jj", "nextPage....");
            }

            @Override
            public void cancel() {
            }
        });

        mLvCategory.setOnItemClickListener(
                (parent, view, position, id) -> {
                    mDlSlide.closeDrawer(Gravity.START);
                    mPageLoader.skipToChapter(position);
                }
        );

        mTvCategory.setOnClickListener(
                (v) -> {
                    //移动到指定位置
                    if (mCategoryAdapter.getCount() > 0) {
                        mLvCategory.setSelection(mPageLoader.getChapterPos());
                    }
                    //切换菜单
                    toggleMenu(true);
                    //打开侧滑动栏
                    mDlSlide.openDrawer(Gravity.START);
                }
        );
        mTvSetting.setOnClickListener(
                (v) -> {
                    toggleMenu(false);
                    mSettingDialog.show();
                }
        );

        mTvPreChapter.setOnClickListener(
                (v) -> {
                    if (mPageLoader.skipPreChapter()) {
                        mCategoryAdapter.setChapter(mPageLoader.getChapterPos());
                    }
                }
        );

        mTvNextChapter.setOnClickListener(
                (v) -> {

                    mCollBook = ((TextPageLoader) mPageLoader).add(mCollBook);

                    //                    mPageLoader.setBookBean(mCollBook);
                    mPageLoader.skipNextChapter();

                    //                    if (mPageLoader.skipNextChapter()) {
                    //                        mCategoryAdapter.setChapter(mPageLoader.getChapterPos());
                    //                    }
                }
        );

        mTvNightMode.setOnClickListener(
                (v) -> {
                    if (isNightMode) {
                        isNightMode = false;
                    } else {
                        isNightMode = true;
                    }
                    mPageLoader.setNightMode(isNightMode);
                }
        );

    }

    /**
     * 隐藏阅读界面的菜单显示
     *
     * @return 是否隐藏成功
     */
    private boolean hideReadMenu() {
        if (mAblTopMenu.getVisibility() == VISIBLE) {
            toggleMenu(true);
            return true;
        } else if (mSettingDialog.isShowing()) {
            mSettingDialog.dismiss();
            return true;
        }
        return false;
    }

    /**
     * 切换菜单栏的可视状态
     * 默认是隐藏的
     */
    private void toggleMenu(boolean hideStatusBar) {
        initMenuAnim();

        if (mAblTopMenu.getVisibility() == View.VISIBLE) {
            //关闭
            mAblTopMenu.startAnimation(mTopOutAnim);
            mLlBottomMenu.startAnimation(mBottomOutAnim);
            mAblTopMenu.setVisibility(GONE);
            mLlBottomMenu.setVisibility(GONE);

        } else {
            mAblTopMenu.setVisibility(View.VISIBLE);
            mLlBottomMenu.setVisibility(View.VISIBLE);
            mAblTopMenu.startAnimation(mTopInAnim);
            mLlBottomMenu.startAnimation(mBottomInAnim);

        }
    }

    //初始化菜单动画
    private void initMenuAnim() {
        if (mTopInAnim != null) return;

        mTopInAnim = AnimationUtils.loadAnimation(this, R.anim.slide_top_in);
        mTopOutAnim = AnimationUtils.loadAnimation(this, R.anim.slide_top_out);
        mBottomInAnim = AnimationUtils.loadAnimation(this, R.anim.slide_bottom_in);
        mBottomOutAnim = AnimationUtils.loadAnimation(this, R.anim.slide_bottom_out);
        //退出的速度要快
        mTopOutAnim.setDuration(200);
        mBottomOutAnim.setDuration(200);
    }


    @Override
    public void onBackPressed() {
        if (mAblTopMenu.getVisibility() == View.VISIBLE) {
            // 非全屏下才收缩，全屏下直接退出
            if (!ReadSettingManager.getInstance().isFullScreen()) {
                toggleMenu(true);
                return;
            }
        } else if (mSettingDialog.isShowing()) {
            mSettingDialog.dismiss();
            return;
        } else if (mDlSlide.isDrawerOpen(Gravity.START)) {
            mDlSlide.closeDrawer(Gravity.START);
            return;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


        mHandler.removeMessages(WHAT_CATEGORY);
        mHandler.removeMessages(WHAT_CHAPTER);

        mPageLoader.closeBook();
        mPageLoader = null;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean isVolumeTurnPage = ReadSettingManager
                .getInstance().isVolumeTurnPage();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (isVolumeTurnPage) {
                    return mPageLoader.skipToPrePage();
                }
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (isVolumeTurnPage) {
                    return mPageLoader.skipToNextPage();
                }
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        SystemBarUtils.hideStableStatusBar(this);
        if (requestCode == REQUEST_MORE_SETTING) {
            boolean fullScreen = ReadSettingManager.getInstance().isFullScreen();

            // 刷新BottomMenu
            initBottomMenu();


            SystemBarUtils.showStableNavBar(this);
        }
    }
}
