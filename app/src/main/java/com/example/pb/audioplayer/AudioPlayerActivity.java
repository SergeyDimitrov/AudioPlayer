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

    private TextView statusView;
    private Button commandButton;
    private boolean isBound = false;

    private AudioPlayerService serviceLink;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            isBound = true;
            serviceLink = (AudioPlayerService)((AudioPlayerService.AudioServiceBinder)service).getService();
            updateUI(serviceLink.getState());
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceLink = null;
        }
    };

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            AudioPlayerActivity.this.updateUI(serviceLink.getState());
            if (serviceLink.getState() == AudioPlayerService.STATE_IDLE) {
                isBound = false;
                unbindService(connection);
            }
        }
    };

    private static final String BOUND_KEY = "bound_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);
        statusView = (TextView)findViewById(R.id.status_view);
        commandButton = (Button)findViewById(R.id.command_button);

        IntentFilter filter = new IntentFilter();
        filter.addAction(AudioPlayerService.STATE_CHANGED);
        registerReceiver(receiver, filter);

        if (savedInstanceState != null) {
            isBound = savedInstanceState.getBoolean(BOUND_KEY);
        }

        if (isBound) {
            bindToService();
        }
        updateUI(AudioPlayerService.STATE_IDLE);
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
        savedInstanceState.putBoolean(BOUND_KEY, isBound);
    }

    private void bindToService() {
        Intent bindIntent = new Intent(this, AudioPlayerService.class);
        bindService(bindIntent, connection, Context.BIND_AUTO_CREATE);
    }


    private void updateUI(int state) {
        switch(state) {
            case AudioPlayerService.STATE_IDLE:
                statusView.setText(R.string.status_idle);
                commandButton.setText(R.string.play);
                commandButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startService(new Intent(AudioPlayerActivity.this, AudioPlayerService.class));
                        bindToService();
                    }
                });
                break;
            case AudioPlayerService.STATE_PLAYING:
                statusView.setText(R.string.status_playing);
                commandButton.setText(R.string.pause);
                commandButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        serviceLink.setPause();
                    }
                });
                break;
            case AudioPlayerService.STATE_PAUSED:
                statusView.setText(R.string.status_paused);
                commandButton.setText(R.string.play);
                commandButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startService(new Intent(AudioPlayerActivity.this, AudioPlayerService.class));
                    }
                });
                break;
        }
    }
}
