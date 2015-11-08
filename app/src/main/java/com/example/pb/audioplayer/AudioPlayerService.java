package com.example.pb.audioplayer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.URI;

public class AudioPlayerService extends Service {

    private int currState;

    public static final int STATE_IDLE = 1;
    public static final int STATE_PLAYING = 2;
    public static final int STATE_PAUSED = 3;
    public static final String STATE_CHANGED = "state_changed";
    private int notificationID = 99;

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
        currState = STATE_IDLE;
        Uri trackPath = Uri.parse("android.resource://com.example.pb.audioplayer/" + R.raw.howl);
        player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            player.setDataSource(getApplicationContext(), trackPath);
        } catch (IOException e) {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
        }
        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                setPlay();
            }
        });
        player.prepareAsync();
    }

    public int onStartCommand(Intent intent, int flags, int startid) {
        if (currState == STATE_PAUSED) setPlay();
        return super.onStartCommand(intent, flags, startid);
    }

    public void onDestroy() {
        player.release();
        player = null;
    }

    public void setPlay() {
        currState = STATE_PLAYING;
        player.start();

        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                currState = STATE_IDLE;
                sendBroadcast(new Intent().setAction(STATE_CHANGED));
                stopForeground(true);
                stopSelf();
            }
        });
        sendBroadcast(new Intent().setAction(STATE_CHANGED));
        startForeground(notificationID, buildNotification());
    }

    public void setPause() {
        currState = STATE_PAUSED;
        player.pause();
        sendBroadcast(new Intent().setAction(STATE_CHANGED));
        startForeground(notificationID, buildNotification());
    }
    public int getState() {
        return currState;
    }

    private Notification buildNotification() {
        Notification.Builder notBuilder = new Notification.Builder(this);
        notBuilder.setOngoing(true);
        switch(currState) {
            case STATE_PLAYING:
                notBuilder.setContentTitle(getString(R.string.status_playing))
                        .setContentText(getString(R.string.audio_name))
                        .setSmallIcon(android.R.drawable.arrow_up_float)
                        .setTicker(getString(R.string.status_playing));
                break;
            case STATE_PAUSED:
                notBuilder.setContentTitle(getString(R.string.status_paused))
                        .setContentText(getString(R.string.audio_name))
                        .setSmallIcon(android.R.drawable.arrow_down_float)
                        .setTicker(getString(R.string.status_paused));
                break;
        }


        return notBuilder.build();
    }

}
