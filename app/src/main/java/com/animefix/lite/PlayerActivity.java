package com.animefix.lite;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Gravity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.net.Uri;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.ui.TrackSelectionDialogBuilder;
import java.util.Collections;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import org.json.JSONArray;
import org.json.JSONObject;

public class PlayerActivity extends Activity {
    private ExoPlayer player;
    private PlayerView playerView;
    private TextView statusView;
    private DefaultTrackSelector trackSelector;
    private String currentUrl;
    private String currentTitle;
    private static final int PICK_SUBTITLE = 501;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);
        String url=getIntent().getStringExtra("url");
        String title=getIntent().getStringExtra("title");
        currentUrl=url; currentTitle=title;
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.BLACK);
        TextView heading=new TextView(this);
        heading.setText(title==null?"AnimeFix Lite Player":title);
        heading.setTextColor(Color.WHITE); heading.setTextSize(18);
        heading.setPadding(16,18,16,18); root.addView(heading);
        statusView=new TextView(this);
        statusView.setTextColor(Color.WHITE); statusView.setTextSize(13);
        statusView.setPadding(16,12,16,12); statusView.setGravity(Gravity.CENTER);
        statusView.setText("Connecting to video…");
        playerView=new PlayerView(this);
        playerView.setUseController(true);
        root.addView(playerView,new LinearLayout.LayoutParams(-1,0,1f));
        LinearLayout options = new LinearLayout(this);
        options.setOrientation(LinearLayout.HORIZONTAL);
        options.setGravity(Gravity.CENTER);
        Button subtitles = optionButton("Subtitles");
        Button audio = optionButton("Audio / Language");
        Button quality = optionButton("Quality");
        Button addSubtitle = optionButton("Add subtitle file");
        options.addView(subtitles,new LinearLayout.LayoutParams(0,-2,1f));
        options.addView(audio,new LinearLayout.LayoutParams(0,-2,1f));
        options.addView(quality,new LinearLayout.LayoutParams(0,-2,1f));
        root.addView(options,new LinearLayout.LayoutParams(-1,-2));
        root.addView(addSubtitle,new LinearLayout.LayoutParams(-1,-2));
        root.addView(statusView,new LinearLayout.LayoutParams(-1,-2));
        subtitles.setOnClickListener(v -> showTrackDialog(C.TRACK_TYPE_TEXT, "Subtitles"));
        audio.setOnClickListener(v -> showTrackDialog(C.TRACK_TYPE_AUDIO, "Audio / Language"));
        quality.setOnClickListener(v -> showTrackDialog(C.TRACK_TYPE_VIDEO, "Video quality"));
        addSubtitle.setOnClickListener(v -> chooseSubtitleFile());
        setContentView(root);
        if(url==null||url.trim().isEmpty()){Toast.makeText(this,"Video URL is missing",Toast.LENGTH_LONG).show();finish();return;}
        trackSelector = new DefaultTrackSelector(this);
        player=new ExoPlayer.Builder(this).setTrackSelector(trackSelector).build();
        playerView.setPlayer(player);
        player.addListener(new Player.Listener(){
            @Override public void onPlayerError(PlaybackException error){
                String details = "Playback failed: " + PlaybackException.getErrorCodeName(error.errorCode);
                if (error.getCause() != null && error.getCause().getMessage() != null) {
                    details += "\\n" + error.getCause().getMessage();
                }
                statusView.setText(details + "\\nTry opening the same HTTPS URL in Nox Browser to check connectivity.");
                Toast.makeText(PlayerActivity.this, "Video failed: " + PlaybackException.getErrorCodeName(error.errorCode), Toast.LENGTH_LONG).show();
            }
            @Override public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_BUFFERING) statusView.setText("Buffering video…");
                else if (playbackState == Player.STATE_READY) statusView.setText("Video ready");
                else if (playbackState == Player.STATE_ENDED) statusView.setText("Playback finished");
            }
        });
        long resumePosition=getIntent().getLongExtra("resume_position",0L);
        player.setMediaItem(MediaItem.fromUri(Uri.parse(url)));
        player.prepare();
        if(resumePosition>0)player.seekTo(resumePosition);
        player.play();
    }

    private Button optionButton(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextSize(11);
        b.setAllCaps(false);
        b.setTextColor(Color.WHITE);
        b.setBackgroundColor(Color.rgb(48, 43, 68));
        return b;
    }

    private void showTrackDialog(int trackType, String title) {
        if (player == null) {
            Toast.makeText(this, "Player is not ready", Toast.LENGTH_SHORT).show();
            return;
        }
        new TrackSelectionDialogBuilder(this, title, player, trackType).build().show();
    }

    private void chooseSubtitleFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        try {
            startActivityForResult(intent, PICK_SUBTITLE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No file picker is available", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != PICK_SUBTITLE || resultCode != RESULT_OK || data == null || data.getData() == null) return;
        Uri subtitleUri = data.getData();
        String path = subtitleUri.toString().toLowerCase(java.util.Locale.ROOT);
        String mime;
        if (path.endsWith(".vtt")) mime = "text/vtt";
        else if (path.endsWith(".ttml") || path.endsWith(".xml")) mime = "application/ttml+xml";
        else if (path.endsWith(".ssa") || path.endsWith(".ass")) mime = "text/x-ssa";
        else mime = "application/x-subrip";
        try {
            getContentResolver().takePersistableUriPermission(subtitleUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (Exception ignored) { }
        if (player == null || currentUrl == null) return;
        long position = player.getCurrentPosition();
        boolean wasPlaying = player.isPlaying();
        MediaItem.SubtitleConfiguration subtitle = new MediaItem.SubtitleConfiguration.Builder(subtitleUri)
                .setMimeType(mime)
                .setLanguage("und")
                .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                .build();
        MediaItem item = new MediaItem.Builder()
                .setUri(Uri.parse(currentUrl))
                .setMediaId(currentTitle == null ? "video" : currentTitle)
                .setSubtitleConfigurations(Collections.singletonList(subtitle))
                .build();
        player.setMediaItem(item, position);
        player.prepare();
        if (wasPlaying) player.play();
        Toast.makeText(this, "Subtitle loaded. Use Subtitles to enable or disable it.", Toast.LENGTH_LONG).show();
    }

    @Override protected void onStop(){
        if(player!=null){
            long position=player.getCurrentPosition();
            android.content.SharedPreferences prefs=getSharedPreferences("zerox_playback",MODE_PRIVATE);
            String savedTitle=currentTitle==null?"Untitled video":currentTitle;
            String savedUrl=currentUrl==null?"":currentUrl;
            try{
                JSONArray oldHistory=new JSONArray(prefs.getString("watch_history","[]"));
                JSONArray updated=new JSONArray();
                JSONObject current=new JSONObject();
                current.put("title",savedTitle);current.put("url",savedUrl);current.put("position",position);
                updated.put(current);
                for(int i=0;i<oldHistory.length()&&updated.length()<30;i++){
                    JSONObject item=oldHistory.optJSONObject(i);if(item==null)continue;
                    if(savedUrl.equals(item.optString("url","")))continue;
                    updated.put(item);
                }
                prefs.edit().putString("last_title",savedTitle)
                        .putString("last_url",savedUrl)
                        .putLong("last_position",position)
                        .putString("watch_history",updated.toString()).apply();
            }catch(Exception ignored){
                prefs.edit().putString("last_title",savedTitle).putString("last_url",savedUrl).putLong("last_position",position).apply();
            }
            player.release();player=null;
        }
        super.onStop();
    }
}
