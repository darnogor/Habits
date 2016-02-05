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
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.blakit.petrenko.habits.dao.HabitDao;
import com.blakit.petrenko.habits.dao.UserDao;
import com.blakit.petrenko.habits.model.Action;
import com.blakit.petrenko.habits.model.Habit;
import com.blakit.petrenko.habits.model.HabitDetails;
import com.blakit.petrenko.habits.model.SearchHistory;
import com.blakit.petrenko.habits.model.User;
import com.blakit.petrenko.habits.model.VideoItem;
import com.blakit.petrenko.habits.utils.Resources;
import com.blakit.petrenko.habits.utils.Utils;
import com.blakit.petrenko.habits.view.AutofitGridRecyclerView;
import com.blakit.petrenko.habits.view.MarginDecoration;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager;
import com.google.api.client.util.Charsets;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.common.io.CharStreams;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
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
import java.util.List;
import java.util.Random;

import io.realm.Realm;


public class MainActivity extends AppCompatActivity  {

    private final int NAV_MENU_ITEM_ADDHABITS = 1;
    private final int NAV_MENU_ITEM_MYHABITS  = 2;
    private final int NAV_MENU_ITEM_COMPLETE  = 3;
    private final int NAV_MENU_ITEM_CREATE    = 4;
    private final int NAV_MENU_ITEM_PROFILE   = 5;
    private final int NAV_MENU_ITEM_SETTINGS  = 6;
    private final int NAV_MENU_ITEM_HELP      = 7;
    private final int NAV_MENU_ITEM_INFO      = 8;

    private static final int REQ_SIGN_IN_REQUIRED = 42;
    private static final int CHOOSE_ACCOUNT = 4242;

    private Realm realm;

    private Drawer navDrawer;
    private AccountHeader accountHeader;
    private List<HabitDetails> habitsDetails;

    private AutofitGridRecyclerView recyclerView;

    private String selectedAccountName;

    private long exitTimeBackPressedMillis = System.currentTimeMillis()-4000;
    private String mToken;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Resources.getInstance().loadResources(this);
        setContentView(R.layout.activity_main);

        realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Log.d("КАСТЫЛЪ", "Fix realm query problem after application remove from activity stack");
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle(R.string.nav_menu_item_myhabits);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initNavigationDrawer(toolbar, savedInstanceState);


        recyclerView = (AutofitGridRecyclerView) findViewById(R.id.recycler_view);
        recyclerView.addItemDecoration(new MarginDecoration(this));
        recyclerView.setHasFixedSize(true);
        
        UserDao userDao = new UserDao(realm);

        SharedPreferences main = getSharedPreferences("HabitAppPreferences", MODE_PRIVATE);
        String defaultAccount = main.getString("default_account", "");
        if (!TextUtils.isEmpty(defaultAccount) && 
                userDao.getUserByName(defaultAccount) != null &&
                Utils.isAccountManagerHasAccount(this, defaultAccount)) {
            HabitApplication.getInstance().setCurrentUser(userDao, defaultAccount);
            selectedAccountName = defaultAccount;
            addProfileIfNeed(selectedAccountName);

            new RetrieveTokenTask().execute(selectedAccountName);
            updateData();
            updateRecyclerView();
            
        } else {
            startChooseAccountActivity();
        }

        //NOTE: Generating test habits

        final int count = 100;

        final Button loadTest = (Button) findViewById(R.id.test_button);
        final ProgressBar testBar = (ProgressBar) findViewById(R.id.test_progress);
        testBar.setVisibility(View.GONE);
        testBar.setProgress(0);
        testBar.setMax(count);


        final Random random = new Random(count);

        loadTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadTest.setEnabled(false);
                testBar.setVisibility(View.VISIBLE);
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        for (int i = 0; i < count; ++i) {
                            final Habit h = new Habit(Utils.randomString(28) + "-name", "Action");
                            h.setAuthor("Author " + random.nextInt());
                            h.setAddCount(random.nextInt(100000));
                            h.setCompleteCount(random.nextInt(100000));
                            realm.copyToRealmOrUpdate(h);
                            testBar.setProgress(i+1);
                        }
                    }
                }, new Realm.Transaction.Callback() {
                    @Override
                    public void onSuccess() {
                        loadTest.setEnabled(true);
                        testBar.setVisibility(View.GONE);
                    }
                });
            }
        });

    }

    private void updateData() {

    }

    private void startChooseAccountActivity() {
        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                new String[]{GoogleAccountManager.ACCOUNT_TYPE},
                true, null, null, null, null);
        startActivityForResult(intent, CHOOSE_ACCOUNT);
    }

    private void updateSelectionAccountHeader() {
        User user = HabitApplication.getInstance().getUser();

        for (IProfile profile: accountHeader.getProfiles()) {
            if (profile.getEmail() != null &&
                    profile.getEmail().getText().equals(selectedAccountName)) {
                accountHeader.setActiveProfile(profile);
                accountHeader.getActiveProfile()
                        .withName(user.getDisplayName());
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
        updateRecyclerView();

//        recyclerView.setAdapter(new HabitDetailsAdapter(this, habitsDetails));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
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
                                        Intent addIntent = new Intent(MainActivity.this, AddHabitActivity.class);
                                        startActivity(addIntent);
                                        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left_half);
                                    }
                                }, 350);
                                return false;
                            case NAV_MENU_ITEM_MYHABITS:
                                return false;
                            case NAV_MENU_ITEM_COMPLETE:
                                break;
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
                                break;
                            case NAV_MENU_ITEM_SETTINGS:
                                break;
                            case NAV_MENU_ITEM_HELP:
                                break;
                            case NAV_MENU_ITEM_INFO:
                                UserDao userDao = new UserDao(realm);
                                User userByName = userDao.getUserByName(HabitApplication
                                        .getInstance().getUser().getName());
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
                    .withIdentifier(NAV_MENU_ITEM_COMPLETE),
                new CustomSelectedDrawerItem()
                    .withName(R.string.nav_menu_item_create)
                    .withIcon(GoogleMaterial.Icon.gmd_create)
                    .withIdentifier(NAV_MENU_ITEM_CREATE),
                new CustomSelectedDrawerItem()
                    .withName(R.string.nav_menu_item_profile)
                    .withIcon(GoogleMaterial.Icon.gmd_account_box)
                    .withIdentifier(NAV_MENU_ITEM_PROFILE),
                new DividerDrawerItem(),
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
                        if (iProfile.getEmail() == null)
                            return true;
                        String accountName = iProfile.getEmail().getText();
                        selectedAccountName = accountName;
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


//    public void startActivityWithExplodeAnimation(int position, final float x, final float y,
//                      final Intent intent, long duration) {
//
//        Transition explode = new Explode();
//        explode.setEpicenterCallback(new Transition.EpicenterCallback() {
//            @Override
//            public Rect onGetEpicenter(Transition transition) {
//                return new Rect((int) x - 1, (int) y - 1, (int) x + 1, (int) y + 1);
//            }
//        });
//        explode.setPropagation(new CircularPropagation());
//        explode.setDuration(duration);
//        explode.setInterpolator(new AccelerateInterpolator());
//
//        TransitionManager.beginDelayedTransition(recyclerView, explode);
//
////        List<Habit> elem = habits.subList(position, position + 1);
////        recyclerView.setAdapter(new HabitDetailsAdapter(this, null));
//
////        HabitDetailsAdapter adapter = (HabitDetailsAdapter) recyclerView.getAdapter();
////        adapter.removeItemsBesidesPosition(position);
//
//        for (int i = habitsDetails.size()-1; i >= 0; --i) {
//            if (i != position)
//                recyclerView.removeViewAt(i);
//        }
//
//
//
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                startActivity(intent);
//                overridePendingTransition(0, 0);
//            }
//        }, duration);
//    }


    public void startActivityWithScaleAnimation(final Intent intent, long duration) {

        LayoutAnimationController animationController =
                AnimationUtils.loadLayoutAnimation(this, R.anim.grid_layout_animation_reverse);
        recyclerView.setLayoutAnimation(animationController);
        recyclerView.setLayoutAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                recyclerView.setAdapter(new HabitDetailsAdapter(MainActivity.this, null));
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        recyclerView.removeAllViews();
        recyclerView.startLayoutAnimation();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        }, duration);
    }

    public void updateRecyclerView() {
        User user = HabitApplication.getInstance().getUser();
        if (user != null) {
            habitsDetails = user.getMyHabits();
        }
        recyclerView.setAdapter(new HabitDetailsAdapter(this, habitsDetails));
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
                    selectedAccountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    addProfileIfNeed(selectedAccountName);
                    new RetrieveTokenTask().execute(selectedAccountName);
                } else {
                    startChooseAccountActivity();
                }
                break;
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
                            .withIdentifier(100+length),
                    length-1);
            this.selectedAccountName = selectedAccountName;
            accountHeader.setActiveProfile(100+length);
        }
    }

    private class RetrieveTokenTask extends AsyncTask<String, Void, JSONObject> {

        private static final String TAG = "Retrieve Token Task";
        private User user = null;
        private Bitmap img = null;
        private Bitmap coverImg = null;



        @Override
        protected JSONObject doInBackground(String... params) {
            img = null;
            coverImg = null;
            String accountName = params[0];
            user = new User(selectedAccountName);
            String scopes = "oauth2:https://www.googleapis.com/auth/plus.login "
                    + YouTubeScopes.YOUTUBE_READONLY;
            String token = null;
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
                                    String imgUrl = jsonObject.getJSONObject("image").getString("url");
                                    user.setImgURL(imgUrl);
                                    img = ImageLoader.getInstance()
                                            .loadImageSync(imgUrl);
                                }
                                if (jsonObject.has("cover") &&
                                        jsonObject.getJSONObject("cover").has("coverPhoto")) {
                                    String coverImgUrl = jsonObject
                                            .getJSONObject("cover")
                                            .getJSONObject("coverPhoto")
                                            .getString("url");
                                    user.setCoverImgURL(coverImgUrl);
                                    coverImg = ImageLoader.getInstance()
                                            .loadImageSync(coverImgUrl);
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
            if (jsonObject == null) {
                updateSelectionAccountHeader();
                return;
            }

            IProfile activeProfile = accountHeader.getActiveProfile();
            if (jsonObject.has("displayName")) {
                try {
                    user.setDisplayName(jsonObject.getString("displayName"));
                    activeProfile.withName(jsonObject.getString("displayName"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
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

            updateUserData();
//            HabitApplication.getInstance().updateUser(new UserDao(realm), user);
            MainActivity.this.getSharedPreferences("HabitAppPreferences", MODE_PRIVATE)
                    .edit().putString("default_account", selectedAccountName).commit();
        }

        private void updateUserData() {
            UserDao userDao = new UserDao(realm);
            User userFromDB = userDao.getUserByName(selectedAccountName);
            if (userFromDB == null) {
                HabitApplication.getInstance().setCurrentUser(userDao, user);
            } else {
                realm.beginTransaction();
                if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
                    userFromDB.setDisplayName(user.getDisplayName());
                }
                if (user.getImgURL() != null && !user.getImgURL().isEmpty()) {
                    userFromDB.setImgURL(user.getImgURL());
                }
                if (user.getCoverImgURL() != null && !user.getCoverImgURL().isEmpty()) {
                    userFromDB.setCoverImgURL(user.getCoverImgURL());
                }
                realm.commitTransaction();
                HabitApplication.getInstance().setCurrentUser(userDao, userFromDB);
            }
        }

    }
}
