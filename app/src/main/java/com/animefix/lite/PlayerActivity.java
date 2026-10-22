package com.animefix.lite;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Gravity;
import android.net.Uri;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

public class PlayerActivity extends Activity {
    private ExoPlayer player;
    private PlayerView playerView;
    private TextView statusView;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);
        String url=getIntent().getStringExtra("url");
        String title=getIntent().getStringExtra("title");
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
        root.addView(statusView,new LinearLayout.LayoutParams(-1,-2));
        setContentView(root);
        if(url==null||url.trim().isEmpty()){Toast.makeText(this,"Video URL is missing",Toast.LENGTH_LONG).show();finish();return;}
        player=new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        player.addListener(new Player.Listener(){
            @Override public void onPlayerError(PlaybackException error){
                String details = "Playback failed: " + error.errorCodeName;
                if (error.getCause() != null && error.getCause().getMessage() != null) {
                    details += "\\n" + error.getCause().getMessage();
                }
                statusView.setText(details + "\\nTry opening the same HTTPS URL in Nox Browser to check connectivity.");
                Toast.makeText(PlayerActivity.this, "Video failed: " + error.errorCodeName, Toast.LENGTH_LONG).show();
            }
            @Override public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_BUFFERING) statusView.setText("Buffering video…");
                else if (playbackState == Player.STATE_READY) statusView.setText("Video ready");
                else if (playbackState == Player.STATE_ENDED) statusView.setText("Playback finished");
            }
        });
        player.setMediaItem(MediaItem.fromUri(Uri.parse(url)));
        player.prepare();
        player.play();
    }
    @Override protected void onStop(){super.onStop();if(player!=null){player.release();player=null;}}
}
