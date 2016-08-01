package com.zhaolongzhong.nytimessearch;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.zhaolongzhong.nytimessearch.adapters.ArticleAdapter;
import com.zhaolongzhong.nytimessearch.models.Article;
import com.zhaolongzhong.nytimessearch.service.EndlessRecyclerViewScrollListener;
import com.zhaolongzhong.nytimessearch.service.Helper;
import com.zhaolongzhong.nytimessearch.service.ItemClickSupport;
import com.zhaolongzhong.nytimessearch.service.SortOrder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

public class SearchActivity extends AppCompatActivity {
    private static final String TAG = SearchActivity.class.getSimpleName();

    private List<Article> allArticles;
    private List<Article> newArticles;
    private ArticleAdapter articleAdapter;
    private String queryString;
    private boolean isRefresh;

    private SwipeRefreshLayout swipeContainer;
    private StaggeredGridLayoutManager gridLayoutManager;

    @BindView(R.id.search_activity_recycler_view_id) RecyclerView recyclerView;
    @BindView(R.id.search_activity_no_results_text_view_id) TextView noResultsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);
        ButterKnife.bind(this);
        setTitle(null);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setLogo(R.drawable.logo);
        allArticles = new ArrayList<>();
        searchArticle(0);

        articleAdapter = new ArticleAdapter(this, allArticles);
        recyclerView.setAdapter(articleAdapter);
        gridLayoutManager =
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setItemAnimator(new SlideInUpAnimator());

        // a better way of handling item on click
        // https://gist.github.com/nesquena/231e356f372f214c4fe6
        ItemClickSupport.addTo(recyclerView).setOnItemClickListener((RecyclerView recyclerView, int position, View v) -> {
            openLinkInBrowser(allArticles.get(position).getWebUrl());
        });

        // Lookup the swipe container view
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(() -> {
            isRefresh = true;
            searchArticle(0);
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);


        recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(gridLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                isRefresh = false;
                searchArticle(page);
            }
        });
    }

    public void searchArticle(int page) {
        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        //&begin_date=20120101&end_date=20121231
        String beginDate = sharedPreferences.getString(SettingsFragment.BEGIN_DATE, "");
        //q=new+york+times&page=2&sort=oldest&api-key=####
        String sortOrderString = sharedPreferences.getString(SettingsFragment.SORT_ORDER, "");

        //&fq=news_desk:("Sports" "Foreign")
        //&fq=news_desk:("Sports") AND glocations:("NEW YORK CITY")
        String arts = sharedPreferences.getString(SettingsFragment.NEWS_DESK_VALUES_ARTS, "");
        String fashion = sharedPreferences.getString(SettingsFragment.NEWS_DESK_VALUES_FASHION, "");
        String sports = sharedPreferences.getString(SettingsFragment.NEWS_DESK_VALUES_SPORTS, "");

        AsyncHttpClient client = new AsyncHttpClient();
        String url = "https://api.nytimes.com/svc/search/v2/articlesearch.json";
        String key = "c24f096aefb84a1c9c3659841fa6eab3";
        RequestParams params = new RequestParams();
        params.put("api-key", key);
        params.put("page", page);

        if (queryString != null && !queryString.isEmpty()) {
            params.put("q", queryString);
        }

        if (!beginDate.isEmpty()) {

            try {
                DateFormat format = new SimpleDateFormat(getString(R.string.begin_date_format), Locale.US);
                Date newDate = format.parse(beginDate);

                DateFormat newFormat = new SimpleDateFormat(getString(R.string.begin_date_format_nyt), Locale.US);
                beginDate = newFormat.format(newDate);
                params.put("begin_date", beginDate);

            } catch (ParseException e) {
                Log.d(TAG, "Error in parsing begin date.", e);
            }
        }

        if (!sortOrderString.isEmpty()) {
            SortOrder sortOrder = SortOrder.instanceFromName(sortOrderString);
            params.put("sort", sortOrder.getValue());
        }

        //fq=news_desk:("Sports" "Foreign")
        String newsDeskValues = "";
        if (!arts.isEmpty()) {
            newsDeskValues += arts;
        }

        if (!fashion.isEmpty()) {
            newsDeskValues += " " + fashion;
        }

        if (!sports.isEmpty()) {
            newsDeskValues += " " + sports;
        }

        if (!newsDeskValues.isEmpty()) {
            params.put("fq", "news_desk:(" + newsDeskValues + ")");
        }

        if (!Helper.isOnline()) {
            swipeContainer.setRefreshing(false);

            return;
        }

        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONArray articleJsonResults = response.getJSONObject("response").getJSONArray("docs");
                    newArticles = Article.fromJSONArray(articleJsonResults);

                    if (isRefresh) {
                        allArticles.clear();
                    }

                    int curSize = articleAdapter.getItemCount();
                    allArticles.addAll(newArticles);
                    articleAdapter.notifyItemChanged(curSize, allArticles.size() - 1);
                    noResultsTextView.setVisibility(allArticles.size() == 0 ? View.VISIBLE : View.GONE);
                } catch (JSONException e) {
                    Log.d(TAG, "Error int getting response or docs.", e);
                }

                swipeContainer.setRefreshing(false);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);

                swipeContainer.setRefreshing(false);
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                showSettingsDialog();
                break;
            case R.id.action_search:
                SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
                setUpSearchView(searchView);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setUpSearchView(SearchView searchView) {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // workaround to avoid issues with some emulators and keyboard devices firing twice if a keyboard enter is used
                // see https://code.google.com/p/android/issues/detail?id=24599
                searchView.clearFocus();

                queryString = query;
                allArticles.clear();
                searchArticle(0);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.isEmpty() && newText.length() >= 3) {
                    queryString = newText;
                } else {
                    queryString = "";
                }
                allArticles.clear();
                articleAdapter.notifyDataSetChanged();
                searchArticle(0);
                return false;
            }
        });
    }

    private void openLinkInBrowser(final String urlString) {
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.open_in_web_page))
                .setPositiveButton(android.R.string.yes, (DialogInterface dialog, int which) -> {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
                    startActivity(browserIntent);
                })
                .setNegativeButton(android.R.string.no, (DialogInterface dialog, int which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void showSettingsDialog() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        SettingsFragment settingsDialogFragment = SettingsFragment.newInstance();
        settingsDialogFragment.setCallback(() -> {
            allArticles.clear();
            articleAdapter.notifyDataSetChanged();
            searchArticle(0);
        });
        settingsDialogFragment.show(fragmentManager, SettingsFragment.class.getSimpleName());
    }
}
