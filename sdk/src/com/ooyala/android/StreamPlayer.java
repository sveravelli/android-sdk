package com.ooyala.android;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Build;
import android.os.Handler;
import android.os.Message;

public abstract class StreamPlayer extends Player {
  public static PlayerInfo defaultPlayerInfo = new DefaultPlayerInfo();

  private PlayerInfo customPlayerInfo;
  public PlayerInfo getPlayerInfo() {
    return customPlayerInfo != null ? customPlayerInfo : defaultPlayerInfo;
  }

  public void setPlayerInfo(PlayerInfo info) {
    customPlayerInfo = info;
  }

  protected Timer _playheadUpdateTimer = null;

  protected static final long TIMER_DELAY = 0;
  protected static final long TIMER_PERIOD = 250;

  // Playhead time update notifications
  private static class TimerHandler extends Handler {
    private WeakReference<StreamPlayer> _player;
    private int _lastPlayhead = -1;

    public TimerHandler(StreamPlayer player) {
      _player = new WeakReference<StreamPlayer>(player);
    }

    public void handleMessage(Message msg) {
      StreamPlayer player = _player.get();
      if (player != null && _lastPlayhead != player.currentTime() && player.isPlaying()) {
        player.notifyTimeChanged();
      }
    }
  }

  private final Handler _playheadUpdateTimerHandler = new TimerHandler(this);

  // Timer tasks for playhead updates
  protected void startPlayheadTimer() {
    if (_playheadUpdateTimer != null) {
      stopPlayheadTimer();
    }
    _playheadUpdateTimer = new Timer();
    _playheadUpdateTimer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        _playheadUpdateTimerHandler.sendEmptyMessage(0);
      }
    }, TIMER_DELAY, TIMER_PERIOD);
  }

  protected void stopPlayheadTimer() {
    if (_playheadUpdateTimer != null) {
      _playheadUpdateTimer.cancel();
      _playheadUpdateTimer = null;
    }
  }

  protected void notifyTimeChanged() {
    setChanged();
    notifyObservers(OoyalaPlayer.TIME_CHANGED_NOTIFICATION);
  }

}
