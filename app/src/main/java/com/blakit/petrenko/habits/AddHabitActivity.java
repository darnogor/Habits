package com.blakit.petrenko.habits;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Toast;

import com.blakit.petrenko.habits.adapter.TabsPagerFragmentAdapter;
import com.blakit.petrenko.habits.dao.UserDao;
import com.blakit.petrenko.habits.model.SearchHistory;
import com.blakit.petrenko.habits.model.User;
import com.blakit.petrenko.habits.utils.Utils;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchResult;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.Sort;

public class AddHabitActivity extends AppCompatActivity {

    private Realm realm;

    private User user;

    private AppBarLayout appBarLayout;
    private TabLayout tabLayout;
    private SearchBox search;
    private View dimView;

    private String categoryNameRes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_habit);

        realm = Realm.getDefaultInstance();

        user = new UserDao(realm).getUserByName(HabitApplication.getInstance().getUsername());

        categoryNameRes = getIntent().getStringExtra("category");

        initToolbar();
        initTabs();
    }


    private void initTabs() {
        TabsPagerFragmentAdapter adapter = new TabsPagerFragmentAdapter(this,
                getSupportFragmentManager(), realm, categoryNameRes);

        ViewPager viewPager = (ViewPager) findViewById(R.id.add_habit_viewPager);
        viewPager.setAdapter(adapter);

        tabLayout = (TabLayout) findViewById(R.id.add_habit_tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.post(new Runnable() {
            @Override
            public void run() {
                int maxWidth = 0;

                for (int i = 0; i < tabLayout.getTabCount(); ++i) {
                    View view = ((ViewGroup) tabLayout.getChildAt(0)).getChildAt(i);

                    int width = view.getWidth();
                    if (maxWidth < width) {
                        maxWidth = width;
                    }
                }
                for (int i = 0; i < tabLayout.getTabCount(); ++i) {
                    View view = ((ViewGroup) tabLayout.getChildAt(0)).getChildAt(i);
                    view.getLayoutParams().width = maxWidth;
                }
            }
        });
    }


    private void initToolbar() {
        appBarLayout = (AppBarLayout) findViewById(R.id.appBarLayout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.add_habit_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            if (TextUtils.isEmpty(categoryNameRes)) {
                getSupportActionBar().setTitle(R.string.nav_menu_item_addhabit);
            } else {
                getSupportActionBar().setTitle(Utils.getStringByResName(this, categoryNameRes));
            }
        }

        search = (SearchBox) findViewById(R.id.searchbox);
        search.setAnimateDrawerLogo(false);
        search.enableVoiceRecognition(this);

        dimView = findViewById(R.id.add_habit_dim_view);
        dimView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search.toggleSearch();
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.enter_from_left_half, R.anim.exit_to_right);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_habit_toolbar, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_search:
                openSearch();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void startBarAnimation(float from, float to, long duration, final int visibilityAfter) {
        Animation barAnimation = new AlphaAnimation(from, to);

        barAnimation.setDuration(duration+20);
        barAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                appBarLayout.setVisibility(visibilityAfter);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        if (visibilityAfter == View.VISIBLE) {
            Utils.expand(tabLayout);
        } else {
            Utils.collapse(tabLayout);
        }
        appBarLayout.startAnimation(barAnimation);
    }


    public void openSearch() {
        startBarAnimation(1.0f, 0.0f, 350, View.GONE);

        search.revealFromMenuItem(R.id.action_search, this);

        List<SearchHistory> historyList = user.getSearchHistories()
                .where().findAllSorted("date", Sort.DESCENDING);
        for (SearchHistory history: historyList) {
            String result = history.getWord();
            if (search.isSearchablesHasString(result)) {
                continue;
            }
            SearchResult option = new SearchResult(result,
                    new IconicsDrawable(this)
                            .icon(GoogleMaterial.Icon.gmd_history)
                            .colorRes(R.color.md_grey_600)
                            .sizeDp(30));
            search.addSearchable(option);
        }
        search.setMenuListener(new SearchBox.MenuListener() {

            @Override
            public void onMenuClick() {
                // Hamburger has been clicked
                Toast.makeText(AddHabitActivity.this, "Menu click",
                        Toast.LENGTH_LONG).show();
            }

        });
        search.setSearchListener(new SearchBox.SearchListener() {

            @Override
            public void onSearchOpened() {
                dimView.setVisibility(View.VISIBLE);
                View decorView = AddHabitActivity.this.getWindow().getDecorView();
                int uiOptions = View.SYSTEM_UI_FLAG_LOW_PROFILE;
                decorView.setSystemUiVisibility(uiOptions);
            }

            @Override
            public void onSearchClosed() {
                closeSearch();
            }

            @Override
            public void onSearchTermChanged(String term) {
                // React to the search term changing
                // Called after it has updated results
            }

            @Override
            public void onSearch(String searchTerm) {
                startSearchActivity(searchTerm);
            }

            @Override
            public void onResultClick(SearchResult result) {
                startSearchActivity(result.title);
            }

            @Override
            public void onSearchCleared() {

            }

        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1234 && resultCode == RESULT_OK) {
            ArrayList<String> matches = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            search.populateEditText(matches.get(0));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    protected void closeSearch() {
        dimView.setVisibility(View.GONE);
        appBarLayout.setVisibility(View.VISIBLE);
        startBarAnimation(0.0f, 1.0f, 350, View.VISIBLE);

        search.hideCircularly(this);
        View decorView = AddHabitActivity.this.getWindow().getDecorView();
        decorView.setSystemUiVisibility(0);
    }


    private void startSearchActivity(String searchStr) {
        UserDao userDao = new UserDao(realm);

        if (!search.isSearchablesHasString(searchStr)) {
            search.addSearchableFront(new SearchResult(searchStr,
                    new IconicsDrawable(AddHabitActivity.this)
                            .icon(GoogleMaterial.Icon.gmd_history)
                            .colorRes(R.color.md_grey_600)
                            .sizeDp(30)));
            userDao.updateUserSearchHistory(user, searchStr);
        } else {
            userDao.updatePresentUserSearchHistory(user, searchStr);
        }

        Intent intent = new Intent(this, SearchActivity.class);
        intent.putExtra("search_str", searchStr);

//        dimView.setVisibility(View.GONE);
//        appBarLayout.setVisibility(View.VISIBLE);
//        search.toggleSearch();

        startActivity(intent);
        overridePendingTransition(0, 0);


    }

}
