package com.blakit.petrenko.habits;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.blakit.petrenko.habits.adapter.HabitDetailsAdapter;
import com.blakit.petrenko.habits.dao.HabitDao;
import com.blakit.petrenko.habits.dao.UserDao;
import com.blakit.petrenko.habits.model.Action;
import com.blakit.petrenko.habits.model.Category;
import com.blakit.petrenko.habits.model.Habit;
import com.blakit.petrenko.habits.model.HabitDetails;
import com.blakit.petrenko.habits.model.SearchHistory;
import com.blakit.petrenko.habits.model.User;
import com.blakit.petrenko.habits.model.VideoItem;
import com.blakit.petrenko.habits.utils.Resources;
import com.blakit.petrenko.habits.utils.Utils;
import com.blakit.petrenko.habits.view.AutofitGridRecyclerView;
import com.blakit.petrenko.habits.view.CustomSelectedDrawerItem;
import com.blakit.petrenko.habits.view.decoration.MarginDecoration;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.api.client.util.Charsets;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.common.io.CharStreams;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;


public class MainActivity extends AppCompatActivity  {

    private final int NAV_MENU_ITEM_ADDHABITS       = 1;
    private final int NAV_MENU_ITEM_MYHABITS        = 2;
    private final int NAV_MENU_ITEM_COMPLETE_HABITS = 3;
    private final int NAV_MENU_ITEM_CREATED_HABITS  = 4;
    private final int NAV_MENU_ITEM_CREATE          = 5;
    private final int NAV_MENU_ITEM_PROFILE         = 6;
    private final int NAV_MENU_ITEM_SETTINGS        = 7;
    private final int NAV_MENU_ITEM_HELP            = 8;
    private final int NAV_MENU_ITEM_INFO            = 9;

    private static final int REQ_SIGN_IN_REQUIRED = 42;
    private static final int CHOOSE_ACCOUNT = 4242;

    private Realm realm;

    private String selectedAccountName;
    private User user;
    private RealmResults<HabitDetails> habitsDetails;

    private Drawer navDrawer;
    private AccountHeader accountHeader;

    private HabitDetailsAdapter adapter;
    private AutofitGridRecyclerView recyclerView;

    private long exitTimeBackPressedMillis = System.currentTimeMillis()-4000;
    private String mToken;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Resources.getInstance().loadResources(this);
        setContentView(R.layout.activity_main);

        realm = Realm.getDefaultInstance();

//        realm.executeTransaction(new Realm.Transaction() {
//            @Override
//            public void execute(Realm realm) {
//                Log.d("КАСТЫЛЪ", "Fix realm query problem after application remove from activity stack");
//            }
//        });
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Category eating      = new Category("category_healthy_eating", "#00E676");
                Category sport       = new Category("category_sport", "#80deea");
                Category badHabits   = new Category("category_bad_habits", "#ff8a80");
                Category hobby       = new Category("category_hobby", "#ffcc80");
                Category training    = new Category("category_training", "#ba68c8");
                Category lifeImprove = new Category("category_life_improve", "#7986cb");

                realm.copyToRealmOrUpdate(eating);
                realm.copyToRealmOrUpdate(sport);
                realm.copyToRealmOrUpdate(badHabits);
                realm.copyToRealmOrUpdate(hobby);
                realm.copyToRealmOrUpdate(training);
                realm.copyToRealmOrUpdate(lifeImprove);
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle(R.string.nav_menu_item_myhabits);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initNavigationDrawer(toolbar, savedInstanceState);
        initHabitsDetails();
        initAccount();
        initFab();
//        generateTest();
    }


    private void initNavigationDrawer(Toolbar toolbar, Bundle savedInstanceState) {
        accountHeader = createAccountHeader(savedInstanceState);

        navDrawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(accountHeader)
//                .withDisplayBelowToolbar(true)
                .withActionBarDrawerToggleAnimated(true)
                .addDrawerItems(initDrawerItems())
                .withSelectedItem(NAV_MENU_ITEM_MYHABITS)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem iDrawerItem) {
                        int id = iDrawerItem.getIdentifier();
                        switch (id) {
                            case NAV_MENU_ITEM_ADDHABITS:
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent intent = new Intent(MainActivity.this, AddHabitActivity.class);
                                        startActivity(intent);
                                        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left_half);
                                    }
                                }, 350);
                                return false;
                            case NAV_MENU_ITEM_MYHABITS:
                                return false;
                            case NAV_MENU_ITEM_COMPLETE_HABITS:
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent intent = new Intent(MainActivity.this, HabitListActivity.class);
                                        intent.putExtra("habits_type", HabitListActivity.HABITS_COMPLETED);
                                        startActivity(intent);
                                        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left_half);
                                    }
                                }, 350);
                                return false;
                            case NAV_MENU_ITEM_CREATED_HABITS:
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent intent = new Intent(MainActivity.this, HabitListActivity.class);
                                        intent.putExtra("habits_type", HabitListActivity.HABITS_CREATED);
                                        startActivity(intent);
                                        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left_half);
                                    }
                                }, 350);
                                return false;
                            case NAV_MENU_ITEM_CREATE:
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent intent = new Intent(MainActivity.this, CreateHabitActivity.class);
                                        intent.putExtra("mToken", mToken);
                                        intent.putExtra("accName", selectedAccountName);
                                        startActivity(intent);
                                        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left_half);
                                    }
                                }, 350);
                                return false;
                            case NAV_MENU_ITEM_PROFILE:
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                                        intent.putExtra("mToken", mToken);
                                        intent.putExtra("accName", selectedAccountName);
                                        startActivity(intent);
                                        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left_half);
                                    }
                                }, 350);
                                return false;
                            case NAV_MENU_ITEM_SETTINGS:
                                break;
                            case NAV_MENU_ITEM_HELP:
                                break;
                            case NAV_MENU_ITEM_INFO:
                                UserDao userDao = new UserDao(realm);
                                User userByName = userDao.getUserByName(HabitApplication
                                        .getInstance().getUsername());
                                Log.d("!!!!User", userByName.getDisplayName());
                                for (SearchHistory h: userByName.getSearchHistories()) {
                                    Log.d("!!!!History", h.getWord()+ " "+h.getDate().toString());
                                }
                                HabitDao habitDao = new HabitDao(realm);
                                for (Habit h: habitDao.getHabits()) {
                                    Log.d("!!!!Habit: ", h.getId()+" name="+h.getName());
                                    for (Action a: h.getActions()) {
                                        Log.d("!!!!Action: ", a.getAction()+" day="+a.getDay());
                                    }
                                    for (VideoItem v: h.getRelatedVideoItems()) {
                                        Log.d("!!!!Video: ", v.getTitle());
                                    }
                                }
                                break;
                        }
                        return true;
                    }
                })
                .withSavedInstance(savedInstanceState)
                .build();
    }


    private IDrawerItem[] initDrawerItems() {
        return new IDrawerItem[] {
                new CustomSelectedDrawerItem()
                        .withName(R.string.nav_menu_item_addhabit)
                        .withIcon(GoogleMaterial.Icon.gmd_add_box)
                        .withIdentifier(NAV_MENU_ITEM_ADDHABITS),
                new CustomSelectedDrawerItem()
                        .withName(R.string.nav_menu_item_myhabits)
                        .withIcon(GoogleMaterial.Icon.gmd_view_module)
                        .withIdentifier(NAV_MENU_ITEM_MYHABITS),
                new CustomSelectedDrawerItem()
                        .withName(R.string.nav_menu_item_complete)
                        .withIcon(GoogleMaterial.Icon.gmd_check_box)
                        .withIdentifier(NAV_MENU_ITEM_COMPLETE_HABITS),
                new CustomSelectedDrawerItem()
                        .withName(R.string.nav_menu_item_created_habits)
                        .withIcon(GoogleMaterial.Icon.gmd_folder)
                        .withIdentifier(NAV_MENU_ITEM_CREATED_HABITS),
                new CustomSelectedDrawerItem()
                        .withName(R.string.nav_menu_item_create)
                        .withIcon(GoogleMaterial.Icon.gmd_create)
                        .withIdentifier(NAV_MENU_ITEM_CREATE),
                new DividerDrawerItem(),
                new CustomSelectedDrawerItem()
                .withName(R.string.nav_menu_item_profile)
                        .withIcon(GoogleMaterial.Icon.gmd_account_box)
                        .withIdentifier(NAV_MENU_ITEM_PROFILE),
                new CustomSelectedDrawerItem()
                        .withName(R.string.nav_menu_item_settings)
                        .withIcon(GoogleMaterial.Icon.gmd_settings)
                        .withIdentifier(NAV_MENU_ITEM_SETTINGS),
                new CustomSelectedDrawerItem()
                        .withName(R.string.nav_menu_item_help)
                        .withIcon(GoogleMaterial.Icon.gmd_help)
                        .withIdentifier(NAV_MENU_ITEM_HELP),
                new CustomSelectedDrawerItem()
                        .withName(R.string.nav_menu_item_info)
                        .withIcon(GoogleMaterial.Icon.gmd_info)
                        .withIdentifier(NAV_MENU_ITEM_INFO)
        };
    }


    private AccountHeader createAccountHeader(Bundle savedInstanceState) {
        final AccountManager accountManager = AccountManager.get(this);
        Account[] accounts = accountManager.getAccountsByType("com.google");

        IProfile[] profiles = new IProfile[accounts.length+1];
        for (int i = 0; i < accounts.length; ++i) {
            String displayedName = null;
            Bitmap icon = null;
            User user = new UserDao(realm).getUserByName(accounts[i].name);
            if (user != null) {
                displayedName = user.getDisplayName();
                if (user.getImgURL() != null) {
                    icon = ImageLoader.getInstance().loadImageSync(user.getImgURL());
                }
            }
            profiles[i] = new ProfileDrawerItem()
                    .withEmail(accounts[i].name)
                    .withName(displayedName)
                    .withIdentifier(100 + i);
            if (icon != null) {
                profiles[i].withIcon(icon);
            }
        }
        profiles[accounts.length] = new ProfileSettingDrawerItem()
                .withName(getResources().getString(R.string.nav_manu_item_add_account))
                .withIcon(GoogleMaterial.Icon.gmd_add)
                .withIdentifier(99)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        accountManager.addAccount("com.google", null, null, null, MainActivity.this,
                                new AccountManagerCallback<Bundle>() {
                                    @Override
                                    public void run(AccountManagerFuture<Bundle> future) {
                                        try {
                                            Bundle result = future.getResult();
                                            String accountName = result.getString(AccountManager.KEY_ACCOUNT_NAME);
                                            addProfileIfNeed(accountName);
                                        } catch (OperationCanceledException e) {
                                            e.printStackTrace();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        } catch (AuthenticatorException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }, null);
                        return false;
                    }
                });

        return new AccountHeaderBuilder()
                .withActivity(this)
                .addProfiles(profiles)
                .withHeaderBackground(R.drawable.header)
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {

                    @Override
                    public boolean onProfileChanged(View view, IProfile iProfile, boolean b) {
                        if (iProfile.getEmail() == null) {
                            return true;
                        }
                        String accountName = iProfile.getEmail().getText();
                        setSelectedAccountName(accountName);

                        new RetrieveTokenTask().execute(accountName);
                        return false;
                    }
                })
                .withCloseDrawerOnProfileListClick(false)
                .withOnlyMainProfileImageVisible(true)
                .withThreeSmallProfileImages(false)
                .withSavedInstance(savedInstanceState)
                .build();
    }


    private void initHabitsDetails() {
        recyclerView = (AutofitGridRecyclerView) findViewById(R.id.recycler_view);
        recyclerView.addItemDecoration(new MarginDecoration(this));
        recyclerView.setHasFixedSize(true);
    }


    private void initAccount() {
        UserDao userDao        = new UserDao(realm);
        SharedPreferences main = getSharedPreferences("HabitAppPreferences", MODE_PRIVATE);
        String defaultAccount  = main.getString("default_account", "");

        if (!TextUtils.isEmpty(defaultAccount) &&
                userDao.getUserByName(defaultAccount) != null &&
                Utils.isAccountManagerHasAccount(this, defaultAccount)) {

            setSelectedAccountName(defaultAccount);
            addProfileIfNeed(selectedAccountName);

            new RetrieveTokenTask().execute(selectedAccountName);
        } else {
            startChooseAccountActivity();
        }
    }


    private void initFab() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.main_activity_fab);

        fab.setImageDrawable(new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_add)
                .color(Color.WHITE)
                .sizeDp(16));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addIntent = new Intent(MainActivity.this, AddHabitActivity.class);
                startActivity(addIntent);
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left_half);
            }
        });
    }


//    private void generateTest() {
//        //NOTE: Generating test habits
//
//        final int count = 100;
//
//        final Button loadTest = (Button) findViewById(R.id.test_button);
//        final Button deleteTest = (Button) findViewById(R.id.test_button_delete);
//        final ProgressBar testBar = (ProgressBar) findViewById(R.id.test_progress);
//        testBar.setVisibility(View.GONE);
//        testBar.setProgress(0);
//        testBar.setMax(count);
//
//
//        final Random random = new Random(count);
//
//        loadTest.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                loadTest.setEnabled(false);
//                deleteTest.setEnabled(false);
//                testBar.setVisibility(View.VISIBLE);
//
//                realm.executeTransactionAsync(new Realm.Transaction() {
//                    @Override
//                    public void execute(Realm realm) {
//                        List<Category> categories = realm.where(Category.class).findAll();
//                        Random randCategory = new Random();
//
//                        for (int i = 0; i < count; ++i) {
//                            final Habit h = new Habit(Utils.randomString(25), "Action");
//
//                            h.setDescription("Very very big description");
//                            h.setAuthor("Author " + random.nextInt());
//                            h.setAddCount(random.nextInt(100000));
//                            h.setCompleteCount(random.nextInt(100000));
//                            h.setCategory(categories.get(randCategory.nextInt(categories.size())));
//
//                            realm.copyToRealmOrUpdate(h);
//
//                            testBar.setProgress(i+1);
//                        }
//                    }
//                }, new Realm.Transaction.OnSuccess() {
//                    @Override
//                    public void onSuccess() {
//                        loadTest.setEnabled(true);
//                        deleteTest.setEnabled(true);
//                        testBar.setVisibility(View.GONE);
//                    }
//                });
//            }
//        });
//
//        deleteTest.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                loadTest.setEnabled(false);
//                deleteTest.setEnabled(false);
//                testBar.setVisibility(View.VISIBLE);
//                realm.executeTransactionAsync(new Realm.Transaction() {
//                    @Override
//                    public void execute(Realm realm) {
//                        RealmResults<Habit> results = realm.where(Habit.class).findAllSorted("creationDate", Sort.DESCENDING);
//                        testBar.setMax(Math.min(count, results.size()));
//                        for (int i = 0; i < Math.min(count, results.size()); ++i) {
//                            results.remove(0);
//                            testBar.setProgress(i+1);
//                        }
//                    }
//                }, new Realm.Transaction.OnSuccess() {
//                    @Override
//                    public void onSuccess() {
//                        loadTest.setEnabled(true);
//                        deleteTest.setEnabled(true);
//                        testBar.setVisibility(View.GONE);
//                    }
//                });
//            }
//        });
//    }


    private void startChooseAccountActivity() {
        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                new String[]{"com.google"},
                true, null, null, null, null);
        startActivityForResult(intent, CHOOSE_ACCOUNT);
    }

    private void updateSelectionAccountHeader() {
        for (IProfile profile: accountHeader.getProfiles()) {
            if (profile.getEmail() != null && profile.getEmail().getText().equals(selectedAccountName)) {
                accountHeader.setActiveProfile(profile);
                accountHeader.getActiveProfile().withName(user.getDisplayName());

                if (user.getImgURL() != null) {
                    accountHeader.getActiveProfile()
                            .withIcon(ImageLoader.getInstance().loadImageSync(user.getImgURL()));
                }
                if (user.getCoverImgURL() != null) {
                    accountHeader.setBackground(new BitmapDrawable(getResources(),
                            ImageLoader.getInstance().loadImageSync(user.getCoverImgURL())));
                }
                break;
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        navDrawer.setSelection(NAV_MENU_ITEM_MYHABITS);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (habitsDetails != null) {
            habitsDetails.removeChangeListeners();
        }
        realm.close();
    }


    @Override
    public void onBackPressed() {
        if (navDrawer != null && navDrawer.isDrawerOpen()) {
            navDrawer.closeDrawer();
        } else {
            if (System.currentTimeMillis() - exitTimeBackPressedMillis < 3000) {
                super.onBackPressed();
            } else {
                Toast.makeText(this,R.string.main_activity_exit_toast,Toast.LENGTH_SHORT).show();
                exitTimeBackPressedMillis = System.currentTimeMillis();
            }
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState = navDrawer.saveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQ_SIGN_IN_REQUIRED:
                if (resultCode == RESULT_OK) {
                    // We had to sign in - now we can finish off the mToken request.
                    new RetrieveTokenTask().execute(selectedAccountName);
                }
                break;
            case CHOOSE_ACCOUNT:
                if (resultCode == RESULT_OK) {
                    setSelectedAccountName(data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME));
                    addProfileIfNeed(selectedAccountName);

                    new RetrieveTokenTask().execute(selectedAccountName);
                } else {
                    startChooseAccountActivity();
                }
                break;
        }
    }


    public void setSelectedAccountName(String selectedAccountName) {
        if (TextUtils.isEmpty(this.selectedAccountName) ||
                !this.selectedAccountName.equals(selectedAccountName)) {
            this.selectedAccountName = selectedAccountName;

            HabitApplication.getInstance().setUsername(selectedAccountName);
            getSharedPreferences("HabitAppPreferences", MODE_PRIVATE)
                    .edit()
                    .putString("default_account", selectedAccountName)
                    .commit();

            UserDao userDao = new UserDao(realm);

            user = userDao.getUserByName(selectedAccountName);

            if (user == null) {
                user = userDao.createOrUpdate(new User(selectedAccountName));
            }

            if (habitsDetails != null) {
                habitsDetails.removeChangeListeners();
            }
            habitsDetails = new UserDao(realm).findAllAvailableHabitHetails(selectedAccountName);
            habitsDetails.addChangeListener(new RealmChangeListener() {
                @Override
                public void onChange() {
                    adapter.notifyDataSetChanged();
                    Log.d("!!!!Change listener", "Notify data set change adapter");
                }
            });

            adapter = new HabitDetailsAdapter(this, habitsDetails);
            adapter.setHasStableIds(true);

            recyclerView.setAdapter(adapter);
        }
    }


    private void addProfileIfNeed(String selectedAccountName) {
        Log.d("Selected account name:", selectedAccountName);
        if (TextUtils.isEmpty(selectedAccountName)) {
            return;
        }
        boolean isPresent = false;
        for (IProfile profile: accountHeader.getProfiles()) {
            if (profile.getIdentifier() > 99 && profile.getEmail().getText().equals(selectedAccountName)) {
                isPresent = true;
                accountHeader.setActiveProfile(profile);
            }
        }
        if (!isPresent) {
            int length = accountHeader.getProfiles().size();
            accountHeader.addProfile(new ProfileDrawerItem()
                            .withEmail(selectedAccountName)
                            .withIdentifier(100 + length),
                    length - 1);
            setSelectedAccountName(selectedAccountName);
            accountHeader.setActiveProfile(100+length);
        }
    }


    public Realm getRealm() {
        return realm;
    }


    private class RetrieveTokenTask extends AsyncTask<String, Void, JSONObject> {

        private static final String TAG = "Retrieve Token Task";
        private String displayName;
        private String imgUrl;
        private String coverImgUrl;
        private Bitmap img;
        private Bitmap coverImg;

        @Override
        protected JSONObject doInBackground(String... params) {
            displayName = null;
            imgUrl = null;
            coverImgUrl = null;
            img = null;
            coverImg = null;
            String accountName = params[0];

            String scopes = "oauth2:https://www.googleapis.com/auth/plus.login "
                    + YouTubeScopes.YOUTUBE_READONLY;
            String token;
            JSONObject jsonObject = null;

            try {
                token = GoogleAuthUtil.getToken(getApplicationContext(), accountName, scopes);

                URL url = new URL("https://www.googleapis.com/plus/v1/people/me");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.addRequestProperty("Authorization","Bearer "+token);

                int sc = conn.getResponseCode();
                if (sc == 200) {
                    mToken = token;
                    String content = CharStreams.toString(new InputStreamReader(conn.getInputStream(), Charsets.UTF_8));
                    if(!TextUtils.isEmpty(content)) {
                        try {
                            jsonObject = new JSONObject(content);
                            if (jsonObject != null) {
                                if (jsonObject.has("image")) {
                                    imgUrl = jsonObject.getJSONObject("image").getString("url");
                                    img = ImageLoader.getInstance().loadImageSync(imgUrl);
                                }
                                if (jsonObject.has("cover") &&
                                        jsonObject.getJSONObject("cover").has("coverPhoto")) {
                                    coverImgUrl = jsonObject
                                            .getJSONObject("cover")
                                            .getJSONObject("coverPhoto")
                                            .getString("url");
                                    coverImg = ImageLoader.getInstance().loadImageSync(coverImgUrl);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.d(TAG, "Empty content");
                    }
                }
                if (sc == 401) {
                    GoogleAuthUtil.invalidateToken(MainActivity.this, token);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (UserRecoverableAuthException e) {
                startActivityForResult(e.getIntent(), REQ_SIGN_IN_REQUIRED);
            } catch (GoogleAuthException e) {
                e.printStackTrace();
            }

            return jsonObject;
        }


        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            if (jsonObject == null || realm.isClosed()) {
                updateSelectionAccountHeader();
                return;
            }

            updateUserData();
            updateAccountHeader();
        }


        private void updateUserData() {
            realm.beginTransaction();

            if (!TextUtils.isEmpty(displayName) && !displayName.equals(user.getDisplayName())) {
                user.setDisplayName(displayName);
            }
            if (!TextUtils.isEmpty(imgUrl) && !imgUrl.equals(user.getImgURL())) {
                user.setImgURL(imgUrl);
            }
            if (!TextUtils.isEmpty(coverImgUrl) && !coverImgUrl.equals(user.getCoverImgURL())) {
                user.setCoverImgURL(coverImgUrl);
            }

            realm.commitTransaction();
        }


        private void updateAccountHeader() {
            IProfile activeProfile = accountHeader.getActiveProfile();
            activeProfile.withName(user.getDisplayName());
            if (img != null) {
                activeProfile.withIcon(img);
            }
            if (coverImg != null) {
                HabitApplication app = HabitApplication.getInstance();
                accountHeader.setBackground(new BitmapDrawable(app.getResources(), coverImg));

            } else {
                accountHeader.setBackgroundRes(R.drawable.header);
            }
            accountHeader.updateProfile(activeProfile);
        }
    }
}
