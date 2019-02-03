package com.example.xyzreader.ui;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.transition.TransitionInflater;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import android.database.Cursor;

import android.graphics.Typeface;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ShareCompat;

import android.text.Html;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class ArticleDetailFragment extends Fragment {
    private static final String TAG = "ArticleDetailFragment";

    public static final String ARG_ITEM_ID = "item_id";

    private long mItemId;
    private View mRootView;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private AppBarLayout appBarLayout;

    private ImageView mPhotoView;

    private int indexString;
    private CharSequence longText;


    private TextView bodyView;


    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    // Use default locale format
    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2,1,1);


    private static final String EXTRA_TITLE = "title";
    private static final String EXTRA_AUTHOR = "author";
    private static final String EXTRA_BODY = "body";
    private static final String EXTRA_PHOTO_URL = "photo_url";
    private static final String EXTRA_PUBLISHED_DATE = "published_date";

    private String articleTitle;
    private String articleAuthor;
    private String articleBody;
    private String articlePhotoUrl;
    private String articlePublishedDate;




    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(long itemId, String title, String author, String body
            , String photoUrl, String publishedDate) {

        Bundle arguments = new Bundle();
        ArticleDetailFragment fragment = new ArticleDetailFragment();

        arguments.putLong(ARG_ITEM_ID, itemId);
        arguments.putString(EXTRA_TITLE,title);
        arguments.putString(EXTRA_AUTHOR,author);
        arguments.putString(EXTRA_BODY,body);
        arguments.putString(EXTRA_PHOTO_URL,photoUrl);
        arguments.putString(EXTRA_PUBLISHED_DATE,publishedDate);
        fragment.setArguments(arguments);
        return fragment;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }

    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);

        mPhotoView = (ImageView) mRootView.findViewById(R.id.photo);

        collapsingToolbarLayout = (CollapsingToolbarLayout) mRootView.findViewById(R.id.collapsing_toolbar_layout);
        appBarLayout = (AppBarLayout) mRootView.findViewById(R.id.app_bar_layout);


        return mRootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        articleTitle = getArguments().getString(EXTRA_TITLE);
        articleAuthor = getArguments().getString(EXTRA_AUTHOR);
        articleBody = getArguments().getString(EXTRA_BODY);
        articlePhotoUrl = getArguments().getString(EXTRA_PHOTO_URL);
        articlePublishedDate = getArguments().getString(EXTRA_PUBLISHED_DATE);

        bindViews();
    }

    private Date parsePublishedDate() {
        try {
            String date = articlePublishedDate;
            return dateFormat.parse(date);
        } catch (ParseException ex) {
            Log.e(TAG, ex.getMessage());
            Log.i(TAG, "passing today's date");
            return new Date();
        }
    }

    private void bindViews() {
        if (mRootView == null) {
            return;
        }

        TextView titleView = (TextView) mRootView.findViewById(R.id.article_title);
        TextView bylineView = (TextView) mRootView.findViewById(R.id.article_byline);
        bylineView.setMovementMethod(new LinkMovementMethod());
        bodyView = (TextView) mRootView.findViewById(R.id.article_body);
        Button loadMoreButton = (Button) mRootView.findViewById(R.id.load_more_button);


        bodyView.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "Rosario-Regular.ttf"));

            mRootView.setAlpha(0);
            mRootView.setVisibility(View.VISIBLE);
            mRootView.animate().alpha(1);
            titleView.setText(articleTitle);
            Date publishedDate = parsePublishedDate();
            if (!publishedDate.before(START_OF_EPOCH.getTime())) {
                bylineView.setText(Html.fromHtml(
                        DateUtils.getRelativeTimeSpanString(
                                publishedDate.getTime(),
                                System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                                DateUtils.FORMAT_ABBREV_ALL).toString()
                                + " by <font color='#ffffff'>"
                                + articleAuthor
                                + "</font>"));

            } else {
                // If date is before 1902, just show the string
                bylineView.setText(Html.fromHtml(
                        outputFormat.format(publishedDate) + " by <font color='#ffffff'>"
                        + articleAuthor
                                + "</font>"));

            }


            longText = Html.fromHtml(articleBody.replaceAll("(\r\n|\n)","<br />"));


            //Set the index to identify the number of characters of the article
            indexString = 0;

            bodyView.setText(longText.subSequence(0,1000));
            indexString = 1000;
            loadMoreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Verify if we reach the end of the article
                    if(indexString + 1000 <= longText.length()){
                        bodyView.append(longText.subSequence(indexString,indexString+1000));
                        indexString = indexString + 1000;
                    } else {
                        bodyView.append(longText.subSequence(indexString,longText.length()));
                    }
                }
            });

            Picasso.get().load(articlePhotoUrl)
                    .noFade()
                    .into(mPhotoView, new Callback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError(Exception e) {
                        }
                    });

            //This allows to change the title in the article when the transition takes place
            appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                boolean isShow = true;
                int scrollRange = -1;

                @Override
                public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                   if (scrollRange == -1) {
                        scrollRange = appBarLayout.getTotalScrollRange();
                   }
                    if (scrollRange + verticalOffset == 0) {
                        collapsingToolbarLayout.setTitle(articleTitle);
                        isShow = true;
                    } else if(isShow) {
                        collapsingToolbarLayout.setTitle(" ");//careful there should a space between double quote otherwise it won't work
                        isShow = false;
                   }
                }
            });

    }


}
