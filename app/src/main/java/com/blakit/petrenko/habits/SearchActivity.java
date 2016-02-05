package com.blakit.petrenko.habits;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.blakit.petrenko.habits.dao.HabitDao;
import com.blakit.petrenko.habits.dao.UserDao;
import com.blakit.petrenko.habits.model.Habit;
import com.blakit.petrenko.habits.model.SearchHistory;
import com.blakit.petrenko.habits.utils.Utils;
import com.blakit.petrenko.habits.view.FontTextView;
import com.blakit.petrenko.habits.view.MarginDecoration;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

public class SearchActivity extends AppCompatActivity {

    private Realm realm;
    private RealmResults<Habit> habits;

    private HabitAdapter adapter;
    private RecyclerView recyclerView;
    private FontTextView notFound;

    private SearchBox search;
    private Stack<String> searchStack;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        realm = Realm.getDefaultInstance();

        if (savedInstanceState != null) {
            searchStack = (Stack<String>) savedInstanceState.getSerializable("search_stack");
        }
        if (searchStack == null) {
            searchStack = new Stack<>();
            searchStack.push(getIntent().getStringExtra("search_str"));
        }

        notFound = (FontTextView) findViewById(R.id.search_not_found_textview);

        initSearch();
        initRecyclerView();
    }


    private void initSearch() {
        search = (SearchBox) findViewById(R.id.searchbox);

        search.enableVoiceRecognition(this);
        search.setAnimateDrawerLogo(false);

        search.setMenuListener(new SearchBox.MenuListener() {

            @Override
            public void onMenuClick() {
                onBackPressed();
            }

        });
        search.setSearchListener(new SearchBox.SearchListener() {

            @Override
            public void onSearchOpened() {
                //Use this to tint the screen
            }

            @Override
            public void onSearchClosed() {
                //Use this to un-tint the screen
            }

            @Override
            public void onSearchTermChanged(String term) {
                //React to the search term changing
                //Called after it has updated results
            }

            @Override
            public void onSearch(String searchTerm) {
                startSearch(searchTerm);
            }

            @Override
            public void onResultClick(SearchResult result) {
                startSearch(result.title);
            }

            @Override
            public void onSearchCleared() {
                //Called when the clear button is clicked
            }

        });
    }


    private void initRecyclerView() {
        int spanCount = 1;
        if (Utils.isActivityLand(this)) {
            spanCount = 2;
        }

        recyclerView = (RecyclerView) findViewById(R.id.search_habit_list_recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));
        recyclerView.addItemDecoration(new MarginDecoration(this));

        updateSearch();
    }


    private void startSearch(String searchStr) {
        if (searchStack.peek().equals(searchStr)) {
            return;
        }
        if (!search.isSearchablesHasString(searchStr)) {
            search.addSearchableFront(new SearchResult(searchStr,
                    new IconicsDrawable(this)
                            .icon(GoogleMaterial.Icon.gmd_history)
                            .colorRes(R.color.md_grey_600)
                            .sizeDp(30)));
            HabitApplication.getInstance().updateCurrentUserSearchHistory(new UserDao(realm), searchStr);
        } else {
            HabitApplication.getInstance().updateCurrentUserPresentSearchHistory(new UserDao(realm), searchStr);
        }
        searchStack.push(searchStr);
        updateSearch();
    }

    private void updateSearch() {
        habits = new HabitDao(realm).getHabitsBySearchString(searchStack.peek());
        habits.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                adapter.notifyDataSetChanged();
                if (adapter.getItemCount() < 1) {
                    notFound.setText(searchStack.peek());
                    notFound.setVisibility(View.VISIBLE);
                } else {
                    notFound.setVisibility(View.GONE);
                }
                Toast.makeText(SearchActivity.this, "onChange triggered", Toast.LENGTH_SHORT).show();
            }
        });

        adapter = new HabitAdapter(this, habits);
        recyclerView.setAdapter(adapter);

        search.setLogoText(searchStack.peek());
        search.setSearchString(searchStack.peek());

        search.clearSearchable();
        List<SearchHistory> historyList = HabitApplication.getInstance().getUser().getSearchHistories()
                .where().findAllSorted("date", Sort.DESCENDING);
        for (SearchHistory history: historyList) {
            String historyStr = history.getWord();
            if (search.isSearchablesHasString(historyStr)) {
                continue;
            }
            SearchResult option = new SearchResult(historyStr,
                    new IconicsDrawable(this)
                            .icon(GoogleMaterial.Icon.gmd_history)
                            .colorRes(R.color.md_grey_600)
                            .sizeDp(30));
            search.addSearchable(option);
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("search_stack", searchStack);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        realm.close();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        searchStack.pop();
        if (!searchStack.empty()) {
            updateSearch();
        } else {
            super.onBackPressed();
            overridePendingTransition(0, 0);
        }
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
}
