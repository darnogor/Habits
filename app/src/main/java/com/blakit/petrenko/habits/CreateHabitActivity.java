package com.blakit.petrenko.habits;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.blakit.petrenko.habits.adapter.CategorySpinnerAdapter;
import com.blakit.petrenko.habits.adapter.CreateDaysAdapter;
import com.blakit.petrenko.habits.adapter.TextWatcherAdapter;
import com.blakit.petrenko.habits.dao.HabitDao;
import com.blakit.petrenko.habits.model.Action;
import com.blakit.petrenko.habits.model.Article;
import com.blakit.petrenko.habits.model.Category;
import com.blakit.petrenko.habits.model.Habit;
import com.blakit.petrenko.habits.model.VideoItem;
import com.blakit.petrenko.habits.utils.Callable;
import com.blakit.petrenko.habits.utils.Config;
import com.blakit.petrenko.habits.utils.Resources;
import com.blakit.petrenko.habits.utils.Utils;
import com.blakit.petrenko.habits.view.FontTextView;
import com.blakit.petrenko.habits.view.decoration.GridLineItemDecoration;
import com.blakit.petrenko.habits.view.decoration.MarginDecoration;
import com.blakit.petrenko.habits.view.WrapGridLayoutManager;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.youtube.player.YouTubeStandalonePlayer;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.view.IconicsImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.joda.time.Period;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;
import org.parceler.Parcels;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import io.realm.Realm;

public class CreateHabitActivity extends AppCompatActivity {

    private Realm realm;

    private Habit habit;

    private boolean isSavePressed = false;
    private int showingDay = -1;

    private EditText nameEdit;
    private EditText descEdit;
    private EditText defaultActions;
    private RelativeLayout daysHeader;
    private RelativeLayout actionsDetailsView;
    private RecyclerView daysView;

    private YouTubeVideoPicker youtubeVideoPicker;

    private AddYouTubeVideoDialog addVideoDialog;
    private AddArticleDialog addArticleDialog;
    private LinearLayout videoNon;
    private LinearLayout articleNon;

    private AlertDialog removeWeekDialog;
    private AlertDialog exitDialog;
    private boolean isDataChanged = false;
    private AlertDialog saveErrorDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_habit);

        realm = Realm.getDefaultInstance();

        initToolbar();
        initDialogs();

        if(savedInstanceState != null) {
            habit = Parcels.unwrap(savedInstanceState.getParcelable("habit"));
            isSavePressed = savedInstanceState.getBoolean("is_save_pressed");
            showingDay    = savedInstanceState.getInt("showing_day");

            addVideoDialog.setText(savedInstanceState.getString("add_video_dialog_text"));
            if (savedInstanceState.getBoolean("is_add_video_dialog_shown")) {
                addVideoDialog.show();
            }

            addArticleDialog.setUri(savedInstanceState.getString("add_article_dialog_uri"));
            addArticleDialog.setTitle(savedInstanceState.getString("add_article_dialog_title"));
            if (savedInstanceState.getBoolean("is_add_article_dialog_shown")) {
                addArticleDialog.show();
            }
        }

        initHabit();
        initCommons();
        initActions();

        int videoTitleHeight = initVideos();
        initArticles(videoTitleHeight);

        initFab();
    }


    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (!getIntent().getBooleanExtra("is_edit", false)) {
                getSupportActionBar().setTitle(R.string.nav_menu_item_create);
            } else {
                getSupportActionBar().setTitle(R.string.add_habit_details_dialog_edit_title);
            }
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }


    private void initDialogs() {
        addVideoDialog   = new AddYouTubeVideoDialog();
        addArticleDialog = new AddArticleDialog();

        ContextThemeWrapper themeWrapper = new ContextThemeWrapper(this, R.style.AlertDialogTheme);

        removeWeekDialog = new AlertDialog.Builder(themeWrapper)
                .setTitle(R.string.create_habit_remove_week)
                .setMessage(R.string.create_habit_remove_week_message)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((CreateDaysAdapter) daysView.getAdapter()).removeWeek();
                    }
                })
                .setNegativeButton(R.string.no, null)
                .setCancelable(true)
                .create();

        exitDialog = new AlertDialog.Builder(themeWrapper)
                .setTitle(R.string.back_to_menu)
                .setMessage(R.string.exit_dialog_save_message)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isDataChanged = false;
                        exitDialog.hide();
                        CreateHabitActivity.this.onBackPressed();
                    }
                })
                .setNegativeButton(R.string.no, null)
                .setCancelable(true)
                .create();

        saveErrorDialog = new AlertDialog.Builder(themeWrapper)
                .setTitle(R.string.create_habit_save_error_dialog_title)
                .setMessage(R.string.create_habit_save_error_dialog_message)
                .setPositiveButton(R.string.ok, null)
                .setCancelable(true)
                .create();
    }


    private void initHabit() {
        if (habit == null) {
            habit = Parcels.unwrap(getIntent().getParcelableExtra("habit"));
            if (habit == null) {
                habit = new Habit("", "");
            }
            if(!getIntent().getBooleanExtra("is_edit", false)) {
                habit.setId(UUID.randomUUID().toString());
                habit.setAddCount(0);
                habit.setCompleteCount(0);
                for (Action action: habit.getActions()) {
                    action.setId(UUID.randomUUID().toString());
                }
                for (Article article: habit.getRelatedArticles()) {
                    article.setId(UUID.randomUUID().toString());
                }
            }
            habit.setCreationDate(new Date());
        }
        habit.setAuthor(HabitApplication.getInstance().getUsername());
    }


    private void initCommons() {
        nameEdit = (EditText) findViewById(R.id.create_habit_name);
        descEdit = (EditText) findViewById(R.id.create_habit_description);

        nameEdit.setText(habit.getName());
        descEdit.setText(habit.getDescription());

        nameEdit.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                habit.setName(s.toString());
                isDataChanged = true;
                nameEdit.setBackgroundResource(R.drawable.edit_text_back);
            }
        });
        descEdit.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                habit.setDescription(s.toString());
                isDataChanged = true;
                descEdit.setBackgroundResource(R.drawable.edit_text_back);
            }
        });

        nameEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    v.setBackgroundResource(R.drawable.edit_text_back);
                }
            }
        });
        descEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    v.setBackgroundResource(R.drawable.edit_text_back);
                }
            }
        });

        Spinner categorySpinner = (Spinner) findViewById(R.id.create_habit_category_spinner);

        CategorySpinnerAdapter categoryAdapter
                = new CategorySpinnerAdapter(this, new HabitDao(realm).getCategories());
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        categorySpinner.setAdapter(categoryAdapter);
        if (habit.getCategory() != null) {
            categorySpinner.setSelection(categoryAdapter
                    .getCategoryPosition(habit.getCategory().getNameRes()));
        }
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                habit.setCategory((Category) parent.getItemAtPosition(position));
            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        final CheckBox sendCheckBox = (CheckBox) findViewById(R.id.create_habit_send_checkbox);
        sendCheckBox.setTypeface(Resources.getInstance().getTypeface("OpenSans-Light"));
    }


    private void initActions() {
        defaultActions = (EditText) findViewById(R.id.create_habit_actions_default);

        defaultActions.setText(habit.getDefaultAction());
        defaultActions.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                habit.setDefaultAction(s.toString());
                isDataChanged = true;
                defaultActions.setBackgroundResource(R.drawable.edit_text_back);
            }
        });
        defaultActions.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    v.setBackgroundResource(R.drawable.edit_text_back);
                }
            }
        });


        actionsDetailsView = (RelativeLayout) findViewById(R.id.create_habit_actions_details);
        daysView = (RecyclerView) findViewById(R.id.create_habit_days_recycler);

        actionsDetailsView.setVisibility(View.GONE);

        daysView.setLayoutManager(new WrapGridLayoutManager(this, 7));
        daysView.addItemDecoration(new MarginDecoration(this));
        daysView.addItemDecoration(new GridLineItemDecoration(this));
        daysView.setNestedScrollingEnabled(false);

        CreateDaysAdapter adapter = new CreateDaysAdapter(this, daysView, defaultActions, habit.getActions());
        adapter.setSavePressed(isSavePressed);
        adapter.showDialog(showingDay);

        daysView.setAdapter(adapter);

        final FontTextView addWeek            = (FontTextView) findViewById(R.id.create_habit_add_week);
        final FontTextView removeWeek         = (FontTextView) findViewById(R.id.create_habit_remove_week);
        final IconicsImageView addWeekIcon    = (IconicsImageView) findViewById(R.id.create_habit_add_week_icon);
        final IconicsImageView removeWeekIcon = (IconicsImageView) findViewById(R.id.create_habit_remove_week_icon);

        View.OnClickListener add = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((CreateDaysAdapter) daysView.getAdapter()).addWeek();
            }
        };
        View.OnClickListener remove = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (daysView.getAdapter().getItemCount() <= 21) {
                    return;
                }
                removeWeekDialog.show();
                int color = ContextCompat.getColor(CreateHabitActivity.this, R.color.md_grey_600);
                removeWeekDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(color);
                removeWeekDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(color);
            }
        };

        addWeek.setOnClickListener(add);
        addWeekIcon.setOnClickListener(add);
        removeWeek.setOnClickListener(remove);
        removeWeekIcon.setOnClickListener(remove);

        final IconicsImageView arrow = (IconicsImageView) findViewById(R.id.create_habit_actions_arrow);
        arrow.setIcon(GoogleMaterial.Icon.gmd_arrow_drop_down);

        daysHeader = (RelativeLayout) findViewById(R.id.create_habit_actions_details_header);
        daysHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (actionsDetailsView.isShown()) {
                    daysHeader.setBackgroundColor(0x00000000);
                    arrow.setIcon(GoogleMaterial.Icon.gmd_arrow_drop_down);

                    Utils.collapse(actionsDetailsView, 200);
                } else {
                    daysHeader.setBackgroundColor(ContextCompat
                            .getColor(CreateHabitActivity.this, R.color.actionDetailsExpanded));
                    arrow.setIcon(GoogleMaterial.Icon.gmd_arrow_drop_up);

                    Utils.expand(actionsDetailsView, 200);
                }
            }
        });
        daysHeader.post(new Runnable() {
            @Override
            public void run() {
                if (addWeekIcon.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) addWeekIcon.getLayoutParams();
                    params.leftMargin = daysHeader.getWidth() / 14 - Utils.dpToPx(CreateHabitActivity.this, 10);
                }
                if (removeWeek.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) removeWeek.getLayoutParams();
                    params.rightMargin = daysHeader.getWidth() / 14 - Utils.dpToPx(CreateHabitActivity.this, 10);
                }
            }
        });
    }


    private int initVideos() {
        View.OnClickListener addVideoClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addVideoDialog.show();
            }
        };

        TextView videosTitle = (TextView) findViewById(R.id.create_habit_videos_title);
        videosTitle.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int videoTitleHeight = (int) (videosTitle.getMeasuredHeight()*0.8);

        IconicsImageView addVideo = (IconicsImageView) findViewById(R.id.create_habit_add_video);
        addVideo.setIcon(GoogleMaterial.Icon.gmd_add);
        addVideo.getLayoutParams().height = videoTitleHeight;
        addVideo.getLayoutParams().width = videoTitleHeight;
        addVideo.setOnClickListener(addVideoClick);

        videoNon   = (LinearLayout) findViewById(R.id.create_habit_video_non);

        List<VideoItem> videoItems = habit.getRelatedVideoItems();
        if ((youtubeVideoPicker = (YouTubeVideoPicker)
                getLastCustomNonConfigurationInstance()) == null) {
            youtubeVideoPicker = new YouTubeVideoPicker(this);
            youtubeVideoPicker.execute(videoItems);
        } else {
            youtubeVideoPicker.setContext(this);
        }
        if (youtubeVideoPicker.getStatus().toString().toUpperCase().equals("FINISHED")) {
            youtubeVideoPicker.onPostExecute(videoItems);
        } else if (youtubeVideoPicker.isCancelled()){
            youtubeVideoPicker.execute(videoItems);
        }


        videoNon.setOnClickListener(addVideoClick);
        if (videoItems.isEmpty()) {
            videoNon.setVisibility(View.VISIBLE);
        } else {
            videoNon.setVisibility(View.GONE);
        }

        return videoTitleHeight;
    }


    private void initArticles(int videoTitleHeight) {
        View.OnClickListener addArticleClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addArticleDialog.show();
            }
        };

        IconicsImageView addArticle = (IconicsImageView) findViewById(R.id.create_habit_add_article);
        addArticle.setIcon(GoogleMaterial.Icon.gmd_add);
        addArticle.getLayoutParams().height = videoTitleHeight;
        addArticle.getLayoutParams().width = videoTitleHeight;
        addArticle.setOnClickListener(addArticleClick);


        for (Article a: habit.getRelatedArticles()) {
            addAtricteToList(a);
        }

        articleNon = (LinearLayout) findViewById(R.id.create_habit_article_non);
        articleNon.setOnClickListener(addArticleClick);

        Collection<Article> articles = habit.getRelatedArticles();
        if (articles.isEmpty()) {
            articleNon.setVisibility(View.VISIBLE);
        } else {
            articleNon.setVisibility(View.GONE);
        }
    }


    private void initFab() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.create_habit_save);

        fab.setImageDrawable(new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_save)
                .color(Color.WHITE));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (Action action: habit.getActions()) {
                    if (action.isUseDefault()) {
                        action.setAction(habit.getDefaultAction());
                    }
                }
                boolean isInputOK = true, isDefaultNeed = false;
                if(TextUtils.isEmpty(nameEdit.getText().toString())) {
                    isInputOK = false;
                    setErrorBackground(nameEdit);
                }
                if(TextUtils.isEmpty(descEdit.getText().toString())) {
                    isInputOK = false;
                    setErrorBackground(descEdit);
                }
                for (Action action: habit.getActions()) {
                    if (!action.isSkipped()) {
                        if (TextUtils.isEmpty(habit.getDefaultAction()) && action.isUseDefault()) {
                            isDefaultNeed = true;
                            isInputOK = false;
                            break;
                        }
                        if (!action.isUseDefault() && TextUtils.isEmpty(action.getAction())) {
                            isInputOK = false;
                        }
                    }
                }
                if (isDefaultNeed) {
                    setErrorBackground(defaultActions);
                }
                if (isInputOK) {
                    HabitDao habitDao = new HabitDao(realm);
                    habitDao.createOrUpdate(habit);

                    Intent intent = new Intent(CreateHabitActivity.this, HabitListActivity.class);
                    intent.putExtra("habits_type", HabitListActivity.HABITS_CREATED);

                    startActivity(intent);
                    overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left_half);

                    isDataChanged = false;
                    finish();
                } else {
                    ((CreateDaysAdapter) daysView.getAdapter()).setSavePressed(true);
                    daysView.getAdapter().notifyDataSetChanged();
                    if (!actionsDetailsView.isShown()) {
                        daysHeader.performClick();
                    }

                    saveErrorDialog.show();
                    int color = ContextCompat.getColor(CreateHabitActivity.this, R.color.md_grey_600);
                    saveErrorDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(color);
                }
            }
        });
    }


    private void setErrorBackground(View v) {
        v.setBackgroundColor(ContextCompat.getColor(this, R.color.md_red_200));
    }


    private void addAtricteToList(final Article article) {
        final LinearLayout articlesLayout = (LinearLayout) findViewById(R.id.create_habit_articles);

        if (habit.getRelatedArticles().size() > 1) {
            View divider = new View(this);
            divider.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    (int) getResources().getDimension(R.dimen.create_habit_video_divider_height)));
            divider.setBackgroundColor(ContextCompat.getColor(this, R.color.editTextBackground));
            articlesLayout.addView(divider, 0);
        }

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.item_article_line, articlesLayout, false);

        TextView titleTextView = (TextView) view.findViewById(R.id.create_habit_article_title);
        TextView urlTextView   = (TextView) view.findViewById(R.id.create_habit_article_url);
        IconicsImageView more  = (IconicsImageView) view.findViewById(R.id.create_habit_article_menu);

        titleTextView.setText(article.getTitle());
        try {
            urlTextView.setText(new URL(article.getUri()).getAuthority());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        final PopupMenu menu = new PopupMenu(this, more);
        menu.inflate(R.menu.menu_create_habit);
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_create_habit_copy_link:
                        ClipboardManager clipboardManager =
                                (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        ClipData data = ClipData.newPlainText("Copied URL", article.getUri());
                        clipboardManager.setPrimaryClip(data);
                        Toast.makeText(CreateHabitActivity.this,
                                "URL copied to clipboard", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.menu_create_habit_share:
                        return true;
                    case R.id.menu_create_habit_delete:
                        habit.getRelatedArticles().remove(article);
                        articlesLayout.removeView(view);
                        if (articlesLayout.getChildCount() < 1) {
                            articleNon.setVisibility(View.VISIBLE);
                        }
                        return true;
                }
                return false;
            }
        });

        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.show();
            }
        });

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(article.getUri()));
                startActivity(i);
            }
        });

        articlesLayout.addView(view, 0);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("habit", Parcels.wrap(Habit.class, habit));
        outState.putBoolean("is_save_pressed", isSavePressed);
        outState.putInt("showing_day", ((CreateDaysAdapter) daysView.getAdapter()).getShowingDay());
        outState.putBoolean("is_add_video_dialog_shown", addVideoDialog.isShowing());
        outState.putString("add_video_dialog_text", addVideoDialog.getText());
        outState.putBoolean("is_add_article_dialog_shown", addArticleDialog.isShowing());
        outState.putString("add_article_dialog_uri", addArticleDialog.getUri());
        outState.putString("add_article_dialog_title", addArticleDialog.getTitle());

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    @Override
    public void onBackPressed() {
        if (isDataChanged) {
            exitDialog.show();
            int color = ContextCompat.getColor(CreateHabitActivity.this, R.color.md_grey_600);
            exitDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(color);
            exitDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(color);
        } else {
            super.onBackPressed();
            overridePendingTransition(R.anim.enter_from_left_half, R.anim.exit_to_right);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        youtubeVideoPicker.setContext(null);
        return youtubeVideoPicker;
    }


    public Habit getHabit() {
        return habit;
    }

    public ViewGroup getVideoNon() {
        return videoNon;
    }



    private static class YouTubeVideoPicker extends AsyncTask<List<VideoItem>, Void, List<VideoItem>> {

        private CreateHabitActivity context;
        private ProgressBar progressBar;

        public YouTubeVideoPicker(CreateHabitActivity context) {
            this.context = context;
        }

        public void setContext(CreateHabitActivity context) {
            this.context = context;
        }


        @Override
        protected void onPreExecute() {
            progressBar = (ProgressBar) context.findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<VideoItem> doInBackground(List<VideoItem>... params) {
            List<VideoItem> videoItems = params[0];

            String token = context.getIntent().getStringExtra("mToken");
            String accountName = context.getIntent().getStringExtra("accName");
            try {
                String newToken = GoogleAuthUtil.getToken(context, accountName,
                        "oauth2:"+YouTubeScopes.YOUTUBE_READONLY);
                if (newToken != null) {
                    token = newToken;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (GoogleAuthException e) {
                e.printStackTrace();
            }
            YouTube youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(),
                    new HttpRequestInitializer() {
                        @Override
                        public void initialize(HttpRequest request) throws IOException {}
                    }).setApplicationName(context.getResources().getString(R.string.app_name)).build();


            for (VideoItem videoItem : videoItems) {
                if (videoItem.getThumbnailURL() != null) {
                    continue;
                }
                try {
                    VideoListResponse listResponse = youtube.videos()
                            .list("snippet,contentDetails,statistics")
                            .setFields("items(id,snippet,contentDetails,statistics)")
                            .setId(videoItem.getVideoId())
                            .setOauthToken(token)
                            .execute();

                    // Getting channel title using channel id
                    for (Video v : listResponse.getItems()) {
                        ChannelListResponse channelListResponse = youtube.channels()
                                .list("snippet")
                                .setFields("items(snippet(title))")
                                .setId(v.getSnippet().getChannelId())
                                .setOauthToken(token)
                                .execute();
                        for (Channel channel : channelListResponse.getItems()) {
                            v.getSnippet().setChannelId(channel.getSnippet().getTitle());
                            break;
                        }
                    }

                    List<Video> videos = listResponse.getItems();

                    if (videos == null || videos.isEmpty()) {
                        //Faled to load this video
                        return null;
                    } else {
                        Video video = videos.get(0);

                        videoItem.setTitle(video.getSnippet().getTitle());
                        videoItem.setChanel(video.getSnippet().getChannelId());
                        videoItem.setPublishDate(video.getSnippet().getPublishedAt().toString());
                        videoItem.setViewsCount(video.getStatistics().getViewCount().toString());
                        videoItem.setDuration(video.getContentDetails().getDuration());

                        String url = video.getSnippet().getThumbnails().getMedium().getUrl();
                        if (url != null) {
                            videoItem.setThumbnailURL(url);
                        }
                        Log.d("Title", videoItem.getTitle());
                        Log.d("Channel", videoItem.getChanel());
                        Log.d("Publish Date", videoItem.getPublishDate());
                        Log.d("Views count", videoItem.getViewsCount().toString());
                        Log.d("Duration", videoItem.getDuration());
                        Log.d("Thumbnail URL", videoItem.getThumbnailURL());
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            return videoItems;
        }


        @Override
        protected void onPostExecute(List<VideoItem> videos) {
            progressBar.setVisibility(View.GONE);
            if(videos == null) {
                // TODO: ADD and SET VISIBLE failed message
                return;
            }
            final LinearLayout videosList = (LinearLayout)
                    context.findViewById(R.id.create_habit_videos);

            for (int i = 0; i < videos.size(); ++i) {
                if (videosList.getChildCount() != 0) {
                    View divider = new View(context);
                    divider.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            (int)context.getResources().getDimension(R.dimen.create_habit_video_divider_height)));
                    divider.setBackgroundColor(ContextCompat.getColor(context, R.color.editTextBackground));
                    videosList.addView(divider, 0);
                }
                videosList.addView(getView(videosList, videos.get(i)), 0);
            }
        }


        private View getView(final ViewGroup parent, final VideoItem videoItem) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
            final View view         = inflater.inflate(R.layout.item_video_line, parent, false);

            ImageView thumbnailView   = (ImageView) view.findViewById(R.id.create_habit_youtube_thumb);
            TextView durationView     = (TextView) view.findViewById(R.id.create_habit_youtube_duration);
            TextView titleView        = (TextView) view.findViewById(R.id.create_habit_youtube_title);
            TextView channelView      = (TextView) view.findViewById(R.id.create_habit_youtube_channel);
            TextView viewsView        = (TextView) view.findViewById(R.id.create_habit_youtube_views);
            IconicsImageView moreView = (IconicsImageView) view.findViewById(R.id.create_habit_youtube_menu);

//            int width  = videoItem.getThumbnail().getWidth();
//            int height = videoItem.getThumbnail().getHeight();
//
//            thumbnailView.getLayoutParams().height = height;
//            thumbnailView.getLayoutParams().width  = width;
//            thumbnailView.setImageBitmap(videoItem.getThumbnail());

            ImageLoader.getInstance().displayImage(videoItem.getThumbnailURL(), thumbnailView);

            durationView.setText(Utils.videoDurationByFormattedString(videoItem.getDuration()));
            titleView.setText(videoItem.getTitle());
            channelView.setText(videoItem.getChanel());
            viewsView.setText(Utils.viewsCountByNumberString(context, videoItem.getViewsCount()));


            final PopupMenu menu = new PopupMenu(context, moreView);
            menu.inflate(R.menu.menu_create_habit);
            menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.menu_create_habit_copy_link:
                            ClipboardManager clipboardManager =
                                    (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
                            ClipData data = ClipData.newPlainText("Copied URL",
                                    "https://youtu.be/" + videoItem.getVideoId());
                            clipboardManager.setPrimaryClip(data);
                            Toast.makeText(context, "URL copied to clipboard", Toast.LENGTH_SHORT).show();
                            return true;
                        case R.id.menu_create_habit_share:
                            return true;
                        case R.id.menu_create_habit_delete:
                            context.getHabit().getRelatedVideoItems().remove(videoItem);
                            parent.removeView(view);
                            if (parent.getChildCount() < 1) {
                                context.getVideoNon().setVisibility(View.VISIBLE);
                            }
                            return true;
                    }
                    return false;
                }
            });

            moreView.setIcon(GoogleMaterial.Icon.gmd_more_vert);
            moreView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    menu.show();
                }
            });


            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = YouTubeStandalonePlayer.createVideoIntent(
                            context, Config.GOOGLE_BROWSER_API_KEY, videoItem.getVideoId());
                    context.startActivity(intent);
                }
            });
            return view;
        }
    }


    private class AddYouTubeVideoDialog {

        private AlertDialog addVideoDialog;
        private View view;
        private EditText input;
        private ProgressBar progress;
        private View warningView;
        private IconicsImageView warningImageView;
        private TextView warningTextView;
        private View videoView;
        private ImageView thumbnailView;
        private TextView durationView;
        private TextView titleView;
        private TextView channelView;
        private TextView viewsView;

        private VideoItem video;
        private AsyncTask<String, Void, VideoItem> loadTask;

        CreateHabitActivity context = CreateHabitActivity.this;

        protected AddYouTubeVideoDialog() {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.dialog_add_video_create_habit, null, false);

            input = (EditText) view.findViewById(R.id.create_habit_youtube_dialog_url);
            progress = (ProgressBar) view.findViewById(R.id.progressBar_in_dialog);
            warningView = view.findViewById(R.id.create_habit_dialog_youtube_warning_view);
            warningImageView = (IconicsImageView) view.findViewById(R.id.create_habit_dialog_youtube_warning_icon);
            warningTextView = (TextView) view.findViewById(R.id.create_habit_dialog_youtube_warning_message);
            videoView = view.findViewById(R.id.create_habit_dialog_youtube_videoview);
            thumbnailView = (ImageView) view.findViewById(R.id.create_habit_dialog_youtube_thumb);
            durationView = (TextView) view.findViewById(R.id.create_habit_dialog_youtube_duration);
            titleView = (TextView) view.findViewById(R.id.create_habit_dialog_youtube_title);
            channelView = (TextView) view.findViewById(R.id.create_habit_dialog_youtube_channel);
            viewsView = (TextView) view.findViewById(R.id.create_habit_dialog_youtube_views);

            input.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    video = null;
                    videoView.setVisibility(View.GONE);
                    if (TextUtils.isEmpty(input.getText().toString())) {
                        warningView.setVisibility(View.GONE);
                        return;
                    }
                    String parseString = Utils.parseYoutubeVideoId(input.getText().toString());
                    if (parseString == null) {
                        warningTextView.setText(R.string.create_habit_youtube_dialog_invalid_url);
                        warningView.setVisibility(View.VISIBLE);
                        return;
                    }
                    Collection<VideoItem> relatedVideoItems = habit.getRelatedVideoItems();
                    for (VideoItem item : relatedVideoItems) {
                        if (item.getVideoId().equals(parseString)) {
                            warningTextView.setText(R.string.create_habit_youtube_dialog_warning_already_set);
                            warningView.setVisibility(View.VISIBLE);
                            return;
                        }
                    }
                    warningView.setVisibility(View.GONE);
                    warningTextView.setClickable(false);
                    //loadTask.cancel(true);
                    initLoadTask();
                    loadTask.execute(parseString);
                }
            });

            warningView.setVisibility(View.GONE);
            warningImageView.setIcon(GoogleMaterial.Icon.gmd_warning);
            warningTextView.setClickable(false);
            warningTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    initLoadTask();
                    loadTask.execute(Utils.parseYoutubeVideoId(input.getText().toString()));
                    warningView.setVisibility(View.GONE);
                }
            });

            ContextThemeWrapper themeWrapper = new ContextThemeWrapper(CreateHabitActivity.this, R.style.AlertDialogTheme);
            addVideoDialog = new AlertDialog.Builder(themeWrapper)
                    .setTitle(R.string.create_habit_youtube_dialog_title)
                    .setView(view)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (video != null) {
                                isDataChanged = true;
                                List<VideoItem> videos = new ArrayList<>();
                                videos.add(video);
                                habit.getRelatedVideoItems().add(video);
                                youtubeVideoPicker.onPostExecute(videos);
                                input.setText("");
                                videoView.setVisibility(View.GONE);
                                video = null;
                                videoNon.setVisibility(View.GONE);
                            }
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .setCancelable(true)
                    .create();
        }

        private void initLoadTask() {
            loadTask = new AsyncTask<String, Void, VideoItem>() {

                @Override
                protected void onPreExecute() {
                    progress.setVisibility(View.VISIBLE);
                }

                @Override
                protected VideoItem doInBackground(String... params) {
                    String token = context.getIntent().getStringExtra("mToken");
                    try {
                        String newToken = GoogleAuthUtil.getToken(context,
                                context.getIntent().getStringExtra("accName"),
                                "oauth2:" + YouTubeScopes.YOUTUBE_READONLY);
                        if (newToken != null) {
                            token = newToken;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (GoogleAuthException e) {
                        e.printStackTrace();
                    }
                    Log.d("Load task", "Before youtube builder");

                    YouTube youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(),
                            new HttpRequestInitializer() {
                                @Override
                                public void initialize(HttpRequest request) throws IOException {}
                            }).setApplicationName(context.getResources().getString(R.string.app_name)).build();
                    Log.d("Load task","After youtube builder");
                    try {
                        VideoListResponse listResponse = youtube.videos()
                                .list("snippet,contentDetails,statistics")
                                .setFields("items(id,snippet,contentDetails,statistics)")
                                .setId(params[0])
                                .setOauthToken(token)
                                .execute();
                        Log.d("Load task","After youtube video list response");

                        // Getting channel title using channel id
                        for (Video v : listResponse.getItems()) {
                            ChannelListResponse channelListResponse = youtube.channels()
                                    .list("snippet")
                                    .setFields("items(snippet(title))")
                                    .setId(v.getSnippet().getChannelId())
                                    .setOauthToken(token)
                                    .execute();
                            for (Channel channel : channelListResponse.getItems()) {
                                v.getSnippet().setChannelId(channel.getSnippet().getTitle());
                                break;
                            }
                        }
                        Log.d("Load task","After youtube channel list response");

                        List<Video> videos = listResponse.getItems();

                        if (videos == null || videos.isEmpty()) {
                            //Faled to load this video
                            return null;
                        } else {
                            Video video = videos.get(0);
                            VideoItem videoItem = new VideoItem(params[0]);

                            videoItem.setTitle(video.getSnippet().getTitle());
                            videoItem.setChanel(video.getSnippet().getChannelId());
                            videoItem.setPublishDate(video.getSnippet().getPublishedAt().toString());
                            videoItem.setViewsCount(video.getStatistics().getViewCount().toString());
                            videoItem.setDuration(video.getContentDetails().getDuration());

                            String url = video.getSnippet().getThumbnails().getMedium().getUrl();
                            if (url != null) {
                                videoItem.setThumbnailURL(url);
                            }
                            Log.d("Title", videoItem.getTitle());
                            Log.d("Channel", videoItem.getChanel());
                            Log.d("Publish Date", videoItem.getPublishDate());
                            Log.d("Views count", videoItem.getViewsCount().toString());
                            Log.d("Duration", videoItem.getDuration());
                            Log.d("Thumbnail URL", videoItem.getThumbnailURL());
                            return videoItem;
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                }

                @Override
                protected void onPostExecute(VideoItem videoItem) {
                    Log.d("Load task", "onPostExecute start");
                    progress.setVisibility(View.GONE);
                    if (videoItem == null) {
                        warningTextView.setText(R.string.create_habit_youtube_dialog_failed_load);
                        warningTextView.setClickable(true);
                        warningView.setVisibility(View.VISIBLE);
                        return;
                    }
                    video = videoItem;
                    videoView.setVisibility(View.VISIBLE);

                    ImageLoader.getInstance().displayImage(videoItem.getThumbnailURL(), thumbnailView);

                    durationView.setText(Utils.videoDurationByFormattedString(videoItem.getDuration()));
                    titleView.setText(videoItem.getTitle());
                    channelView.setText(videoItem.getChanel());
                    viewsView.setText(Utils.viewsCountByNumberString(context, videoItem.getViewsCount()));
                }
            };
        }

        public void show() {
            addVideoDialog.show();
            int color = ContextCompat.getColor(CreateHabitActivity.this, R.color.md_grey_600);
            addVideoDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(color);
            addVideoDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(color);
        }

        public boolean isShowing() {
            return addVideoDialog.isShowing();
        }

        public void setText(String text) {
            input.setText(text);
        }

        public String getText() {
            return input.getText().toString();
        }
    }


    private class AddArticleDialog {
        private AlertDialog dialog;
        private Article article;

        private EditText urlInput;
        private EditText titleInput;

        private ProgressBar progress;

        private TextView urlWarning;
        private TextView titleWarning;

        protected AddArticleDialog() {
            CreateHabitActivity context = CreateHabitActivity.this;

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.dialog_add_article_create_habit, null, false);

            urlInput     = (EditText) view.findViewById(R.id.create_habit_article_dialog_url);
            titleInput   = (EditText) view.findViewById(R.id.create_habit_article_dialog_title);

            progress     = (ProgressBar) view.findViewById(R.id.create_habit_article_dialog_progress);

            urlWarning   = (TextView) view.findViewById(R.id.create_habit_article_dialog_url_warning);
            titleWarning = (TextView) view.findViewById(R.id.create_habit_article_dialog_title_warning);

//            IconicsDrawable okDrawable = new IconicsDrawable(CreateHabitActivity.this)
//                    .icon(GoogleMaterial.Icon.gmd_check_circle)
//                    .sizeDp(22)
//                    .colorRes(R.color.colorPrimaryDark);
//            urlInput.setCompoundDrawablesWithIntrinsicBounds(null, null, okDrawable, null);
            urlInput.addTextChangedListener(new TextWatcherAdapter() {

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    urlWarning.setVisibility(View.GONE);
                }

                @Override
                public void afterTextChanged(Editable s) {
                    urlWarning.setVisibility(View.GONE);

                    String uriStr = s.toString();
                    for (Article a: habit.getRelatedArticles()) {
                        if (a.getUri().equals(uriStr)) {
                            urlWarning.setText(R.string.create_habit_article_dialog_url_present);
                            urlWarning.setVisibility(View.VISIBLE);
                            return;
                        }
                    }
                    if (!TextUtils.isEmpty(uriStr) && !Utils.isUri(uriStr)) {
                        urlWarning.setText(R.string.create_habit_article_dialog_url_invalid);
                        urlWarning.setVisibility(View.VISIBLE);
                        return;
                    }

                    if (article == null) {
                        article = new Article(uriStr);
                    } else {
                        article.setUri(uriStr);
                    }
                    article.setTitle(getTitle());

                    if (TextUtils.isEmpty(titleInput.getText().toString())) {
                        Utils.parseTitleByURL(uriStr, new Callable<String, Void>() {
                            @Override
                            public Void call(String... strings) {
                                if (TextUtils.isEmpty(titleInput.getText().toString())) {
                                    titleInput.setText(strings[0]);
                                }
                                return null;
                            }
                        }, progress);
                    }
                }
            });

            titleInput.addTextChangedListener(new TextWatcherAdapter() {

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    titleWarning.setVisibility(View.GONE);
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (TextUtils.isEmpty(s.toString())) {
                        titleWarning.setVisibility(View.VISIBLE);
                    }
                    if (article != null) {
                        article.setTitle(s.toString());
                    }
                }
            });

            ContextThemeWrapper themeWrapper = new ContextThemeWrapper(context, R.style.AlertDialogTheme);
            dialog = new AlertDialog.Builder(themeWrapper)
                    .setTitle(R.string.create_habit_article_dialog_title)
                    .setView(view)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (article != null && !TextUtils.isEmpty(article.getTitle())) {
                                isDataChanged = true;
                                validateArticle();
                                habit.getRelatedArticles().add(article);
                                addAtricteToList(article);
                                article = null;
                                urlInput.setText("");
                                titleInput.setText("");
                                urlWarning.setVisibility(View.GONE);
                                titleWarning.setVisibility(View.GONE);
                                articleNon.setVisibility(View.GONE);
                            }
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .setCancelable(true)
                    .create();
        }

        private void validateArticle() {
            String articleUri = article.getUri();
            if (!articleUri.startsWith("http://") && !articleUri.startsWith("https://")) {
                article.setUri("http://"+articleUri);
            }
        }

        public void show() {
            dialog.show();
            int color = ContextCompat.getColor(CreateHabitActivity.this, R.color.md_grey_600);
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(color);
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(color);
        }

        public boolean isShowing() {
            return dialog.isShowing();
        }

        public String getUri() {
            return urlInput.getText().toString();
        }

        public void setUri(String uri) {
            urlInput.setText(uri);
        }

        public String getTitle() {
            return titleInput.getText().toString();
        }

        public void setTitle(String title) {
            titleInput.setText(title);
        }
    }
}
