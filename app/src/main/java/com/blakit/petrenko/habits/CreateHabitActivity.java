package com.blakit.petrenko.habits;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blakit.petrenko.habits.dao.HabitDao;
import com.blakit.petrenko.habits.model.Action;
import com.blakit.petrenko.habits.model.Article;
import com.blakit.petrenko.habits.model.Habit;
import com.blakit.petrenko.habits.model.VideoItem;
import com.blakit.petrenko.habits.utils.Config;
import com.blakit.petrenko.habits.utils.Utils;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.youtube.player.YouTubeStandalonePlayer;
import com.google.android.youtube.player.internal.ac;
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

import org.joda.time.Period;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;
import org.parceler.Parcels;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CreateHabitActivity extends AppCompatActivity {

    private Habit habit;

    private String defaultAction = "";
    private int ids[];
    private boolean isDefaults[];

    private EditText defaultActions;
    private YouTubeVideoPicker youtubeVideoPicker;

    private AddYouTubeVideoDialog addVideoDialog;
    private AddArticleDialog addArticleDialog;
    private LinearLayout videoNon;
    private LinearLayout articleNon;

    private AlertDialog exitDialog;
    private boolean isDataChanged = false;
    private AlertDialog saveDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_habit);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle(R.string.nav_menu_item_create);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        addVideoDialog = new AddYouTubeVideoDialog();
        addArticleDialog = new AddArticleDialog();

        ContextThemeWrapper themeWrapper = new ContextThemeWrapper(CreateHabitActivity.this, R.style.AlertDialogTheme);
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
                .setCancelable(false)
                .create();

        saveDialog = new AlertDialog.Builder(themeWrapper)
                .setTitle(R.string.create_habit_save_error_dialog_title)
                .setMessage(R.string.create_habit_save_error_dialog_message)
                .setPositiveButton(R.string.ok, null)
                .setCancelable(false)
                .create();

        if(savedInstanceState != null) {
            habit = Parcels.unwrap(savedInstanceState.getParcelable("habit"));
            defaultAction = savedInstanceState.getString("default_action");
            isDefaults = savedInstanceState.getBooleanArray("is_defaults");
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
        if (habit == null) {
            habit = new Habit("","");
//            habit.getRelatedVideoItems().add(new VideoItem("iMHgQdZ8AGQ", habit));
//            habit.getRelatedVideoItems().add(new VideoItem("IojjiLWmVDk", habit));
        }
        habit.setAuthor(HabitApplication.getInstance().getUser().getName());
        ids = new int[21];
        if (isDefaults == null) {
            isDefaults = new boolean[21];
            for (int i = 0; i < isDefaults.length; ++i) {
                isDefaults[i] = true;
            }
        }


        final EditText nameEdit = (EditText) findViewById(R.id.create_habit_name);
        nameEdit.setText(habit.getName());
        nameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                habit.setName(s.toString());
                isDataChanged = true;
                nameEdit.setBackgroundResource(R.drawable.edit_text_back);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        nameEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    v.setBackgroundResource(R.drawable.edit_text_back);
                }
//                if (!hasFocus && TextUtils.isEmpty(nameEdit.getText().toString())) {
//                    v.setBackgroundColor(ContextCompat
//                            .getColor(CreateHabitActivity.this, R.color.md_red_200));
//                }
            }
        });

        final EditText descEdit = (EditText) findViewById(R.id.create_habit_description);
        descEdit.setText(habit.getDescription());
        descEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                habit.setDescription(s.toString());
                isDataChanged = true;
                descEdit.setBackgroundResource(R.drawable.edit_text_back);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        descEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    v.setBackgroundResource(R.drawable.edit_text_back);
                }
//                if (!hasFocus && TextUtils.isEmpty(nameEdit.getText().toString())) {
//                    v.setBackgroundColor(ContextCompat
//                            .getColor(CreateHabitActivity.this, R.color.md_red_200));
//                }
            }
        });


        final CheckBox sendCheckBox = (CheckBox) findViewById(R.id.create_habit_send_checkbox);
        sendCheckBox.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/OpenSans-Light.ttf"));


        defaultActions = (EditText) findViewById(R.id.create_habit_actions_default);
        defaultActions.setText(defaultAction);
        defaultActions.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                defaultAction = s.toString();
                isDataChanged = true;
                defaultActions.setBackgroundResource(R.drawable.edit_text_back);
                for (int i = 0; i < 21; ++i) {
                    if (isDefaults[i]) {
                        EditText e = (EditText) findViewById(ids[i]);
                        e.setText(defaultAction);
                        if (!TextUtils.isEmpty(s)) {
                            e.setBackgroundResource(R.drawable.edit_text_back);
                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

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


        final LinearLayout daysListView = (LinearLayout) findViewById(R.id.create_habit_actions_details);
        for (int i = 0; i < 21; ++i) {
            View view = getActionView(daysListView, i);
            daysListView.addView(view);
        }
        daysListView.setVisibility(View.GONE);

        final IconicsImageView arrow = (IconicsImageView) findViewById(R.id.create_habit_actions_arrow);
        arrow.setIcon(GoogleMaterial.Icon.gmd_arrow_drop_down);

        final RelativeLayout daysHeader = (RelativeLayout) findViewById(R.id.create_habit_actions_details_header);
        daysHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (daysListView.isShown()) {
                    daysHeader.setBackgroundColor(0x00000000);
                    arrow.setIcon(GoogleMaterial.Icon.gmd_arrow_drop_down);
                    slide(daysListView, R.anim.slide_up).setAnimationListener(
                            new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {
                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    daysListView.setVisibility(View.GONE);
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {
                                }
                            }
                    );
                } else {
                    daysHeader.setBackgroundColor(ContextCompat
                            .getColor(CreateHabitActivity.this, R.color.actionDetailsExpanded));
                    arrow.setIcon(GoogleMaterial.Icon.gmd_arrow_drop_up);
                    daysListView.setVisibility(View.VISIBLE);
                    slide(daysListView, R.anim.slide_down);
                }
            }
        });


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

        List<VideoItem> videoItems = new ArrayList<>(habit.getRelatedVideoItems());
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


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.create_habit_save);
        fab.setImageDrawable(new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_save)
                .color(Color.WHITE));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isInputOK = true, isDefaultNeed = false;
                if(TextUtils.isEmpty(nameEdit.getText().toString())) {
                    isInputOK = false;
                    setErrorBackground(nameEdit);
                }
                if(TextUtils.isEmpty(descEdit.getText().toString())) {
                    isInputOK = false;
                    setErrorBackground(descEdit);
                }
                for (int i = 0; i < ids.length; ++i) {
                    if (!Utils.getAction(habit, i+1).isSkipped()) {
                        EditText e = (EditText) findViewById(ids[i]);
                        if (TextUtils.isEmpty(defaultAction) &&
                                TextUtils.isEmpty(e.getText().toString())) {
                            isDefaultNeed = true;
                            isInputOK = false;
                            setErrorBackground(e);
                        }
                    }
                }
                if (isDefaultNeed) {
                    setErrorBackground(defaultActions);
                }
                if (isInputOK) {
                    HabitDao habitDao = HabitApplication.getInstance().getHabitDao();
                    habitDao.createOrUpdate(habit);
                    isDataChanged = false;
                    onBackPressed();
                } else {
                    saveDialog.show();
                    int color = ContextCompat.getColor(CreateHabitActivity.this, R.color.md_grey_600);
                    saveDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(color);
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
        final View view = inflater.inflate(R.layout.articles_item_create_habit, articlesLayout, false);

        TextView title = (TextView) view.findViewById(R.id.create_habit_article_title);
        IconicsImageView more = (IconicsImageView) view.findViewById(R.id.create_habit_article_menu);

        title.setText(article.getTitle());

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

        more.setIcon(GoogleMaterial.Icon.gmd_more_vert);
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


    private View getActionView(ViewGroup parent, final int position) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.actions_item_create_habit, parent, false);

        final TextView dayTextView = (TextView) view.findViewById(R.id.create_habit_action_day_textview);
        final IconicsImageView checked = (IconicsImageView) view.findViewById(R.id.create_habit_actions_checked);
        final TextView skippedTextView = (TextView) view.findViewById(R.id.create_habit_action_skipped);
        final EditText dayEditText = (EditText) view.findViewById(R.id.create_habit_action_day);

        dayTextView.setText(new StringBuilder()
                .append(getResources().getString(R.string.day))
                .append(" ")
                .append(position + 1)
                .toString());

        Action action = Utils.getAction(habit, position+1);

        ids[position] = Utils.generateViewId();
        dayEditText.setId(ids[position]);
        dayEditText.setText(action.getAction());

        if (!action.isSkipped()) {
            checked.setIcon(GoogleMaterial.Icon.gmd_check_circle);
            checked.setColorRes(R.color.colorPrimaryDark);
            skippedTextView.setText(R.string.create_habit_action_not_skipped);
        } else {
            checked.setIcon(GoogleMaterial.Icon.gmd_block);
            checked.setColorRes(R.color.createActionsSmallTextColor);
            skippedTextView.setText(R.string.create_habit_action_skipped);
            dayEditText.setEnabled(false);
        }
        skippedTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Action action = Utils.getAction(habit, position + 1);

                if (!action.isSkipped()) {
                    action.setIsSkipped(true);
                    checked.setIcon(GoogleMaterial.Icon.gmd_block);
                    checked.setColorRes(R.color.createActionsSmallTextColor);
                    skippedTextView.setText(R.string.create_habit_action_skipped);
                    dayEditText.setEnabled(false);
                } else {
                    action.setIsSkipped(false);
                    checked.setIcon(GoogleMaterial.Icon.gmd_check_circle);
                    checked.setColorRes(R.color.colorPrimaryDark);
                    skippedTextView.setText(R.string.create_habit_action_not_skipped);
                    dayEditText.setEnabled(true);
                }
            }
        });


        dayEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if(dayEditText.getText().toString().equals(defaultAction) ||
                            dayEditText.getText().toString().equals("")) {
                        isDefaults[position] = true;
                        dayEditText.setText(defaultAction);
                    }
//                    if (isDefaults[position] && defaultAction.equals("")) {
//                        v.setBackgroundColor(ContextCompat
//                                .getColor(CreateHabitActivity.this, R.color.md_red_200));
//                    }

                } else {
                    v.setBackgroundResource(R.drawable.edit_text_back);
                }
            }
        });
        dayEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isDataChanged = true;
                Utils.getAction(habit, position + 1).setAction(s.toString());
                if (!defaultAction.equals(s.toString())) {
                    isDefaults[position] = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        return view;
    }


    public Animation slide(View v, int resourseId) {
        Animation a = AnimationUtils.loadAnimation(this, resourseId);
        if(a != null){
            a.reset();
            if(v != null){
                v.clearAnimation();
                v.startAnimation(a);
            }
        }
        return a;
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("habit", Parcels.wrap(Habit.class, habit));
        outState.putString("default_action", defaultAction);
        outState.putBooleanArray("is_defaults", isDefaults);
        outState.putBoolean("is_add_video_dialog_shown", addVideoDialog.isShowing());
        outState.putString("add_video_dialog_text", addVideoDialog.getText());
        outState.putBoolean("is_add_article_dialog_shown", addArticleDialog.isShowing());
        outState.putString("add_article_dialog_uri", addArticleDialog.getUri());
        outState.putString("add_article_dialog_title", addArticleDialog.getTitle());
        super.onSaveInstanceState(outState);
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
            try {
                String newToken = GoogleAuthUtil.getToken(context,
                        context.getIntent().getStringExtra("accName"),
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
                if (videoItem.getThumbnail() != null) {
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
                            try {
                                videoItem.setThumbnail(BitmapFactory.decodeStream((InputStream) new URL(url).getContent()));
                            } catch (IOException e) {
                                e.printStackTrace();
                                return null;
                            }
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
            final View view = inflater.inflate(R.layout.videos_item_create_habit, parent, false);

            ImageView thumbnailView = (ImageView) view.findViewById(R.id.create_habit_youtube_thumb);
            TextView durationView = (TextView) view.findViewById(R.id.create_habit_youtube_duration);
            TextView titleView = (TextView) view.findViewById(R.id.create_habit_youtube_title);
            TextView channelView = (TextView) view.findViewById(R.id.create_habit_youtube_channel);
            TextView viewsView = (TextView) view.findViewById(R.id.create_habit_youtube_views);
            IconicsImageView moreView = (IconicsImageView) view.findViewById(R.id.create_habit_youtube_menu);

            int width = videoItem.getThumbnail().getWidth(),
                    height = videoItem.getThumbnail().getHeight();
            thumbnailView.getLayoutParams().height = height;
            thumbnailView.getLayoutParams().width = width;
            thumbnailView.setImageBitmap(videoItem.getThumbnail());


            PeriodFormatter formatter = ISOPeriodFormat.standard();
            Period p = formatter.parsePeriod(videoItem.getDuration());
            StringBuilder builder = new StringBuilder();
            if (p.toStandardHours().getHours() > 0) {
                builder.append(p.toStandardHours().getHours());
                builder.append(":");
            }
            if (p.getMinutes() < 10) {
                builder.append("0");
            }
            builder.append(p.getMinutes());
            builder.append(":");
            if (p.getSeconds() < 10) {
                builder.append("0");
            }
            builder.append(p.getSeconds());
            durationView.setText(builder);

            String num = videoItem.getViewsCount();
            builder.setLength(0);
            builder.append(num);
            for (int i = num.length()-3; i > 0; i -= 3) {
                builder.insert(i, ',');
            }
            builder.insert(0, context.getString(R.string.create_habit_youtube_views) + " ");

            titleView.setText(videoItem.getTitle());
            channelView.setText(videoItem.getChanel());
            viewsView.setText(builder);


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
            view = inflater.inflate(R.layout.add_video_dialog_create_habit, null, false);

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
                    .setCancelable(false)
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
//                    if (true) {
//                        throw new NullPointerException("Заебало");
//                    }
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
                                try {
                                    videoItem.setThumbnail(BitmapFactory.decodeStream((InputStream) new URL(url).getContent()));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    return null;
                                }
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
                    int width = videoItem.getThumbnail().getWidth(),
                            height = videoItem.getThumbnail().getHeight();
                    thumbnailView.getLayoutParams().height = height;
                    thumbnailView.getLayoutParams().width = width;
                    thumbnailView.setImageBitmap(videoItem.getThumbnail());

                    Log.d("Duration", videoItem.getDuration());
                    PeriodFormatter formatter = ISOPeriodFormat.standard();
                    Period p = formatter.parsePeriod(videoItem.getDuration());
                    StringBuilder builder = new StringBuilder();
                    if (p.toStandardHours().getHours() > 0) {
                        builder.append(p.toStandardHours().getHours());
                        builder.append(":");
                    }
                    if (p.getMinutes() < 10) {
                        builder.append("0");
                    }
                    builder.append(p.getMinutes());
                    builder.append(":");
                    if (p.getSeconds() < 10) {
                        builder.append("0");
                    }
                    builder.append(p.getSeconds());
                    durationView.setText(builder);

                    String num = videoItem.getViewsCount();
                    builder.setLength(0);
                    builder.append(num);
                    for (int i = num.length()-3; i > 0; i -= 3) {
                        builder.insert(i, ',');
                    }
                    builder.insert(0, context.getString(R.string.create_habit_youtube_views) + " ");

                    titleView.setText(videoItem.getTitle());
                    channelView.setText(videoItem.getChanel());
                    viewsView.setText(builder);
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

        private TextView urlWarning;
        private TextView titleWarning;

        protected AddArticleDialog() {
            CreateHabitActivity context = CreateHabitActivity.this;

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.add_article_dialog_create_habit, null, false);

            urlInput = (EditText) view.findViewById(R.id.create_habit_article_dialog_url);
            titleInput = (EditText) view.findViewById(R.id.create_habit_article_dialog_title);

            urlWarning = (TextView) view.findViewById(R.id.create_habit_article_dialog_url_warning);
            titleWarning = (TextView) view.findViewById(R.id.create_habit_article_dialog_title_warning);

//            IconicsDrawable okDrawable = new IconicsDrawable(CreateHabitActivity.this)
//                    .icon(GoogleMaterial.Icon.gmd_check_circle)
//                    .sizeDp(22)
//                    .colorRes(R.color.colorPrimaryDark);
//            urlInput.setCompoundDrawablesWithIntrinsicBounds(null, null, okDrawable, null);
            urlInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

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
                }
            });

            titleInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

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

            ContextThemeWrapper themeWrapper = new ContextThemeWrapper(CreateHabitActivity.this, R.style.AlertDialogTheme);
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
                    .setCancelable(false)
                    .create();
        }

        private void validateArticle() {
            String articleUri = article.getUri();
            if (!articleUri.startsWith("http://") &&
                    !articleUri.startsWith("https://")) {
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
