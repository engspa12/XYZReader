package com.example.xyzreader.ui;


import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentStatePagerAdapter;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import android.util.TypedValue;

import android.view.View;

import android.view.WindowInsets;

import android.widget.ProgressBar;

import android.support.v4.app.FragmentManager;

import android.support.v4.app.Fragment;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;

/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */
public class ArticleDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG = ArticleDetailActivity.class.getSimpleName();

    private Cursor mCursor;
    private long mStartId;

    private long mSelectedItemId;
    private int mSelectedItemUpButtonFloor = Integer.MAX_VALUE;
    private int mTopInset;

    private ProgressBar mProgressBar;

    //private FrameLayout viewpagerContainer;

    private ViewPager mPager;
    private MyPagerAdapter mPagerAdapter;
    private View mUpButtonContainer;
    private View mUpButton;

    private FloatingActionButton fabButton;

    private boolean desiredFragment;
    private int desiredPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
        setContentView(R.layout.activity_article_detail);

        desiredFragment = false;

        fabButton = (FloatingActionButton) findViewById(R.id.share_fab);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mUpButton = findViewById(R.id.action_up);
        mPager = (ViewPager) findViewById(R.id.pager);
        //viewpagerContainer = (FrameLayout) findViewById(R.id.viewpager_container);

        getSupportLoaderManager().initLoader(0,null,this);

        mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        mUpButtonContainer = findViewById(R.id.up_container);

        mUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //onSupportNavigateUp();
                //NavUtils.navigateUpFromSameTask(ArticleDetailActivity.this);
                onBackPressed();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mUpButtonContainer.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                @Override
                public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                    view.onApplyWindowInsets(windowInsets);
                    mTopInset = windowInsets.getSystemWindowInsetTop();
                    mUpButtonContainer.setTranslationY(mTopInset);
                    //updateUpButtonPosition();
                    return windowInsets;
                }
            });
        }

        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
                mStartId = ItemsContract.Items.getItemId(getIntent().getData());
                mSelectedItemId = mStartId;
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        //mProgressBar.setVisibility(View.VISIBLE);
        fabButton.setVisibility(View.GONE);
        mUpButton.setVisibility(View.GONE);
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mCursor = cursor;

        mPager.setAdapter(mPagerAdapter);
        mPager.setPageMargin((int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
        mPager.setPageMarginDrawable(new ColorDrawable(0x22000000));

        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                mPager.getParent().requestDisallowInterceptTouchEvent(true);
            }

            @Override
            public void onPageSelected(int position) {
                if (mCursor != null) {
                    mCursor.moveToPosition(position);
                }
                mSelectedItemId = mCursor.getLong(ArticleLoader.Query._ID);
                //mCursor.moveToPosition(position);
                //ArticleDetailFragment.newInstance(mCursor.getLong(ArticleLoader.Query._ID), true);
                //updateUpButtonPosition();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                //mUpButton.animate()
                //       .alpha((state == ViewPager.SCROLL_STATE_IDLE) ? 1f : 0f)
                //      .setDuration(500);
                if(state == ViewPager.SCROLL_STATE_IDLE){
                    fabButton.setVisibility(View.VISIBLE);
                    mUpButton.setVisibility(View.VISIBLE);
                } else{
                    fabButton.setVisibility(View.GONE);
                    mUpButton.setVisibility(View.GONE);
                }
            }
        });

        mPagerAdapter.notifyDataSetChanged();

        // Select the start ID
        if (mStartId > 0) {
            mCursor.moveToFirst();
            // TODO: optimize
            while (!mCursor.isAfterLast()) {
                if (mCursor.getLong(ArticleLoader.Query._ID) == mStartId) {
                    int position = mCursor.getPosition();
                    desiredPosition = position;
                    desiredFragment = true;
                    mPager.setCurrentItem(position, false);
                    break;
                }
                mCursor.moveToNext();
            }
            mStartId = 0;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        mPagerAdapter.notifyDataSetChanged();
    }

    //public void onUpButtonFloorChanged(long itemId, ArticleDetailFragment fragment) {
    //    if (itemId == mSelectedItemId) {
    //       mSelectedItemUpButtonFloor = fragment.getUpButtonFloor();
    //       updateUpButtonPosition();
    //    }
   //}

   // private void updateUpButtonPosition() {
   //     int upButtonNormalBottom = mTopInset + mUpButton.getHeight();
    //    mUpButton.setTranslationY(Math.min(mSelectedItemUpButtonFloor - upButtonNormalBottom, 0));
   // }

    private class MyPagerAdapter extends FragmentStatePagerAdapter {

        public MyPagerAdapter(FragmentManager fm){
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if(desiredFragment && position == (desiredPosition)){
                fabButton.setVisibility(View.VISIBLE);
                mUpButton.setVisibility(View.VISIBLE);
            }
            mCursor.moveToPosition(position);
            return ArticleDetailFragment.newInstance(mCursor.getLong(ArticleLoader.Query._ID));
        }

        @Override
        public int getCount() {
            return (mCursor != null) ? mCursor.getCount() : 0;
        }

        // @Override
        //public void setPrimaryItem(ViewGroup container, int position, Object object) {
         //   super.setPrimaryItem(container, position, object);
            //ArticleDetailFragment fragment = (ArticleDetailFragment) object;
            //if (fragment != null) {
              //  Log.v(LOG,"TEST FOR FRAGMENT");
               //mSelectedItemUpButtonFloor = fragment.getUpButtonFloor();
               //updateUpButtonPosition();
            //}
        //}



    }
}
