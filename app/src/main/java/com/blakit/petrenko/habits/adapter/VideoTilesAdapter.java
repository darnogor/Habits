package com.blakit.petrenko.habits.adapter;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blakit.petrenko.habits.R;
import com.blakit.petrenko.habits.model.VideoItem;
import com.blakit.petrenko.habits.utils.Config;
import com.blakit.petrenko.habits.utils.Utils;
import com.google.android.youtube.player.YouTubeStandalonePlayer;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.view.IconicsImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

/**
 * Created by user_And on 03.04.2016.
 */
public class VideoTilesAdapter extends RecyclerView.Adapter<VideoTilesAdapter.ViewHolder> {

    private Context context;
    private List<VideoItem> videos;
    private boolean isLandOrientation;


    public VideoTilesAdapter(Context context, List<VideoItem> videos, boolean isLandOrientation) {
        this.context = context;
        this.videos = videos;
        this.isLandOrientation = isLandOrientation;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(isLandOrientation ? R.layout.item_video_card_tile : R.layout.item_video_card_line, parent, false);
        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final VideoItem video = videos.get(position);

        ImageLoader.getInstance().displayImage(video.getThumbnailURL(), holder.thumbnail);

        holder.duration.setText(Utils.videoDurationByFormattedString(video.getDuration()));
        holder.title.setText(video.getTitle());
        holder.channel.setText(video.getChanel());
        holder.viewsCount.setText(Utils.viewsCountByNumberString(context, video.getViewsCount()));

        final PopupMenu menu = new PopupMenu(context, holder.menu);
        menu.inflate(R.menu.menu_video_item);
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_video_item_copy_link:
                        ClipboardManager clipboardManager =
                                (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData data = ClipData.newPlainText("Copied URL",
                                "https://youtu.be/" + video.getVideoId());
                        clipboardManager.setPrimaryClip(data);
                        Toast.makeText(context, "URL copied to clipboard", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.menu_video_item_share:
                        return true;
                }
                return false;
            }
        });
        holder.menu.setIcon(GoogleMaterial.Icon.gmd_more_vert);
        holder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.show();
            }
        });

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = YouTubeStandalonePlayer.createVideoIntent(
                        (Activity) context, Config.GOOGLE_BROWSER_API_KEY, video.getVideoId());
                context.startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return videos.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        private View view;

        private ImageView thumbnail;
        private TextView duration;
        private TextView title;
        private TextView channel;
        private TextView viewsCount;
        private IconicsImageView menu;

        public ViewHolder(View itemView) {
            super(itemView);

            view = itemView.findViewById(R.id.video_item_view);

            thumbnail = (ImageView) itemView.findViewById(R.id.video_item_youtube_thumb);
            duration = (TextView) itemView.findViewById(R.id.video_item_youtube_duration);
            title = (TextView) itemView.findViewById(R.id.video_item_youtube_title);
            channel = (TextView) itemView.findViewById(R.id.video_item_youtube_channel);
            viewsCount = (TextView) itemView.findViewById(R.id.video_item_youtube_views);
            menu = (IconicsImageView) itemView.findViewById(R.id.video_item_youtube_menu);
        }
    }
}
