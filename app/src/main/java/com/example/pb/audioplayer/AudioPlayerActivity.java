package com.example.pb.audioplayer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AudioPlayerActivity extends AppCompatActivity {
    public static final String PLAYING_IS_OVER = "playing_is_over";

    private TextView statusView;
    private Button commandButton;
    private int currState = STATE_IDLE;
    private boolean isBound = false;
    private AudioPlayerService serviceLink;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            serviceLink = (AudioPlayerService)((AudioPlayerService.AudioServiceBinder)service).getService();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceLink = null;
        }
    };

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            AudioPlayerActivity.this.updateUI(STATE_IDLE);
            unbindService(connection);
        }
    };

    private static final String STATE_KEY = "state_key";
    private static final String BOUND_KEY = "bound_key";
    private static final int STATE_IDLE = 1;
    private static final int STATE_PLAYING = 2;
    private static final int STATE_PAUSED = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);
        statusView = (TextView)findViewById(R.id.status_view);
        commandButton = (Button)findViewById(R.id.command_button);

        IntentFilter filter = new IntentFilter();
        filter.addAction(PLAYING_IS_OVER);
        registerReceiver(receiver, filter);

        if (savedInstanceState != null) {
            currState = savedInstanceState.getInt(STATE_KEY);
            isBound = savedInstanceState.getBoolean(BOUND_KEY);
        }

        if (isBound) {
            bindToService();
        }
        updateUI(currState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        if (isBound) unbindService(connection);
        if (!isChangingConfigurations()) stopService(new Intent(AudioPlayerActivity.this, AudioPlayerService.class));
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt(STATE_KEY, currState);
        savedInstanceState.putBoolean(BOUND_KEY, isBound);
    }

    private void bindToService() {
        Intent bindIntent = new Intent(this, AudioPlayerService.class);
        bindService(bindIntent, connection, Context.BIND_AUTO_CREATE);
    }

    public void updateUI(int state) {
        currState = state;
        switch (state) {
            case STATE_IDLE:
                isBound = false;
                statusView.setText(R.string.status_idle);
                commandButton.setText(R.string.play);
                commandButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startService(new Intent(AudioPlayerActivity.this, AudioPlayerService.class));
                        bindToService();
                        AudioPlayerActivity.this.updateUI(STATE_PLAYING);
                    }
                });
                break;
            case STATE_PLAYING:
                isBound = true;
                statusView.setText(R.string.status_playing);
                commandButton.setText(R.string.pause);
                commandButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        serviceLink.setPause();
                        AudioPlayerActivity.this.updateUI(STATE_PAUSED);
                    }
                });
                break;
            case STATE_PAUSED:
                statusView.setText(R.string.status_paused);
                commandButton.setText(R.string.play);
                commandButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startService(new Intent(AudioPlayerActivity.this, AudioPlayerService.class));
                        bindToService();
                        AudioPlayerActivity.this.updateUI(STATE_PLAYING);
                    }
                });
                break;
        }
    }
}
