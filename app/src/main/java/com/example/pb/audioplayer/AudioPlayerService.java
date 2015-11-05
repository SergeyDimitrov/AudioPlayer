package com.example.pb.audioplayer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class AudioPlayerService extends Service {

    public class AudioServiceBinder extends Binder {
        public Context getService() {
            return AudioPlayerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private final AudioServiceBinder binder = new AudioServiceBinder();
    private MediaPlayer player;

    public AudioPlayerService() {
    }

    @Override
    public void onCreate() {
        player = MediaPlayer.create(AudioPlayerService.this, R.raw.tristram);
    }

    public void onStart(Intent intent, int startid) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                player.start();
                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        sendBroadcast(new Intent().setAction(AudioPlayerActivity.PLAYING_IS_OVER));
                        stopSelf();
                    }
                });
            }
        }).start();
    }

    public void onDestroy() {
        player.stop();
        player = null;
    }

    public void setPause() {
        player.pause();
    }
}
