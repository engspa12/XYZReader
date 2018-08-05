package com.example.xyzreader.ui;


import android.content.Intent;
import android.support.transition.TransitionInflater;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentStatePagerAdapter;

import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.TransitionSet;
import android.util.TypedValue;

import android.view.MenuItem;
import android.view.View;

import android.view.Window;
import android.view.WindowInsets;

import android.view.animation.AccelerateDecelerateInterpolator;
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

    private ViewPager mPager;
    private MyPagerAdapter mPagerAdapter;


    private FloatingActionButton fabButton;
    private Toolbar myChildToolbar;
    private int desiredPosition;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
        setContentView(R.layout.activity_article_detail);

        myChildToolbar = (Toolbar) findViewById(R.id.my_child_toolbar);
        setSupportActionBar(myChildToolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle("");

        desiredPosition = -1;

        fabButton = (FloatingActionButton) findViewById(R.id.share_fab);
        mPager = (ViewPager) findViewById(R.id.pager);


        getSupportLoaderManager().initLoader(0,null,this);

        fabButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        shareContent();

                    }
                });

        mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());

        mPager.setAdapter(mPagerAdapter);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (mCursor != null) {
                    mCursor.moveToPosition(position);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if(state == ViewPager.SCROLL_STATE_IDLE){
                    fabButton.setVisibility(View.VISIBLE);
                    myChildToolbar.setVisibility(View.VISIBLE);
                } else{
                    fabButton.setVisibility(View.GONE);
                    myChildToolbar.setVisibility(View.GONE);
                }
            }
        });


        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
                mStartId = ItemsContract.Items.getItemId(getIntent().getData());

            }
        }
    }

    public void shareContent(){
        ShareCompat.IntentBuilder.from(this)
                .setChooserTitle("Share")
                .setType("text/plain")
                .setText("Some sample text")
                .startChooser();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        fabButton.setVisibility(View.GONE);
        myChildToolbar.setVisibility(View.GONE);
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mCursor = cursor;

        mPagerAdapter.notifyDataSetChanged();

        // Select the start ID
        if (mStartId > 0) {
            mCursor.moveToFirst();
            while (!mCursor.isAfterLast()) {
                if (mCursor.getLong(ArticleLoader.Query._ID) == mStartId) {
                    int position = mCursor.getPosition();
                    desiredPosition = position;
                    if (desiredPosition == 0 || desiredPosition == 1) {
                        fabButton.setVisibility(View.VISIBLE);
                        myChildToolbar.setVisibility(View.VISIBLE);
                    }
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


    private class MyPagerAdapter extends FragmentStatePagerAdapter {

        public MyPagerAdapter(FragmentManager fm){
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            if(position == desiredPosition){
                fabButton.setVisibility(View.VISIBLE);
                myChildToolbar.setVisibility(View.VISIBLE);
            }

            TransitionSet set = new TransitionSet();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                set.addTransition(new Fade().setDuration(200).setInterpolator(new AccelerateDecelerateInterpolator()));
            }

            mCursor.moveToPosition(position);

            Fragment fragment =  ArticleDetailFragment.newInstance(mCursor.getLong(ArticleLoader.Query._ID)
                    ,mCursor.getString(ArticleLoader.Query.TITLE)
                    ,mCursor.getString(ArticleLoader.Query.AUTHOR)
                    ,mCursor.getString(ArticleLoader.Query.BODY)
                    ,mCursor.getString(ArticleLoader.Query.PHOTO_URL)
                    ,mCursor.getString(ArticleLoader.Query.PUBLISHED_DATE));


            fragment.setAllowEnterTransitionOverlap(false);
            fragment.setEnterTransition(set);


            return fragment;
        }

        @Override
        public int getCount() {
            return (mCursor != null) ? mCursor.getCount() : 0;
        }

    }

   @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    finishAfterTransition();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
