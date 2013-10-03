package com.ooyala.android.imasdk;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.util.Log;

import com.google.ads.interactivemedia.v3.api.player.VideoAdPlayer;
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate;
import com.ooyala.android.AdSpot;
import com.ooyala.android.OoyalaPlayer;

/**
 * The OoyalaPlayerIMAWrapper provides the interface between the OoyalaAdManager and the OoyalaPlayer.
 *
 * @author michael.len
 *
 */
class OoyalaPlayerIMAWrapper implements VideoAdPlayer, Observer {
  private static String TAG = "OoyalaPlayerIMAWrapper";

  OoyalaPlayer _player;
  private AdSpot _adSpot;
  private boolean _isPlayingIMAAd;
  private final List<VideoAdPlayerCallback> _adCallbacks = new ArrayList<VideoAdPlayerCallback>(1);
  private int _liveContentTimePlayed;

  /**
   * A simple interface to allow for a callback when content is completed
   * @author michael.len
   *
   */
  interface CompleteCallback {
    void onComplete();
  }

  /**
   * Wrap an instantiated OoyalaPlayer to provide the IMA interface
   * @param player the OoyalaPlayer to use
   * @param callback a callback for when content is completed
   */
  public OoyalaPlayerIMAWrapper(OoyalaPlayer player){
    this._player = player;
    Log.d(TAG, "IMA Ad Wrapper: Initializing");
    _isPlayingIMAAd = false;
    _liveContentTimePlayed = 0;
    player.addObserver(this);
  }

  // Methods implementing VideoAdPlayer interface.
  @Override
  public void playAd() {
    Log.d(TAG, "IMA Ad Wrapper: Playing Ad");
    _isPlayingIMAAd = true;

    _player.playAd(_adSpot);
  }

  @Override
  public void stopAd() {
    Log.d(TAG, "IMA Ad Wrapper: Stopping Ad");
    if(_isPlayingIMAAd && _player.isShowingAd()) {
      _player.suspend();
    }
    else {
      Log.i(TAG, "Stopping an ad when an IMA Ad isn't even playing!!");
    }
  }

  @Override
  public void loadAd(String url) {
    Log.d(TAG, "IMA Ad Wrapper: Loading Ad: " + url);
    _adSpot = new IMAAdSpot(url);
  }

  @Override
  public void pauseAd() {
    Log.d(TAG, "IMA Ad Wrapper: Pausing Ad");
    if(_isPlayingIMAAd && _player.isShowingAd()) {
      _player.pause();
    }
    else {
      Log.i(TAG, "Pausing an ad when an IMA Ad isn't even playing!!");
    }
  }

  @Override
  public void resumeAd() {
    Log.d(TAG, "IMA Ad Wrapper: Resuming Ad");
    if(_isPlayingIMAAd && _player.isShowingAd()) {
      _player.resume();
    }
    else {
      Log.i(TAG, "Resuming an ad when an IMA Ad isn't even playing!!");
    }
  }

  @Override
  public void addCallback(VideoAdPlayerCallback callback) {
    _adCallbacks.add(callback);
  }

  @Override
  public void removeCallback(VideoAdPlayerCallback callback) {
    _adCallbacks.remove(callback);
  }

  @Override
  public VideoProgressUpdate getProgress() {
   int durationMs = _player.getDuration();
   int playheadMs = _player.getPlayheadTime();

   if(!_isPlayingIMAAd) {
     playheadMs += _liveContentTimePlayed;
   }

    if (durationMs == 0) durationMs = Integer.MAX_VALUE;
    Log.v(TAG, "GetProgress time: " + playheadMs + ", duration: " + durationMs);
    return new VideoProgressUpdate(playheadMs, durationMs);
  }

  /**
   * Only called from the IMAManager when content should be paused. Note: This does not really pause content.
   * However, it informs the player wrapper that content will be paused.
   */
  public void pauseContent(){
    if(_player.getCurrentItem().isLive()) {
      _liveContentTimePlayed = _liveContentTimePlayed + _player.getPlayheadTime();
    }
  }

  /**
   * Called when the IMAManager wants to resume content after advertisements.  This is how content is resumed
   * after IMA ads are played.
   */
  public void playContent(){
    for (VideoAdPlayerCallback callback : _adCallbacks) {
      callback.onPlay();
    }
    if(_isPlayingIMAAd) {
      _player.skipAd();
    } else {
      _player.resume();
    }
  }

  @Override
  public void update(Observable arg0, Object arg) {
    OoyalaPlayer player = (OoyalaPlayer) arg0;
    String notification = arg.toString();

    //If playing a non-IMA ad
    if(!_isPlayingIMAAd && player.isShowingAd()) {

      //If starting a non-IMA Ad, we're pausing the content
      if (notification.equals(OoyalaPlayer.AD_STARTED_NOTIFICATION)) {
        Log.d(TAG, "IMA Ad Update: Non IMA ad playing");
        for (VideoAdPlayerCallback callback : _adCallbacks) {
          callback.onPause();
        }
      }
      //If completing a non-IMA ad
      else if (notification.equals(OoyalaPlayer.AD_COMPLETED_NOTIFICATION)) {
        Log.d(TAG, "IMA Ad Update: Non IMA ad completed");
      }
    }

    //If an IMA ad is playing while state is being changed
    else if (_isPlayingIMAAd){
      if(notification.equals(OoyalaPlayer.STATE_CHANGED_NOTIFICATION) && player.isShowingAd()) {
        switch (player.getState()) {
        case PLAYING:
          Log.d(TAG, "IMA Ad Update: Player Ad start");
          for (VideoAdPlayerCallback callback : _adCallbacks) {
            callback.onPlay();
          }
          break;
        case PAUSED:
          Log.d(TAG, "IMA Ad Update: Player Ad Pause");
          for (VideoAdPlayerCallback callback : _adCallbacks) {
            callback.onPause();
          }
          break;
        case SUSPENDED:
          Log.d(TAG, "IMA Ad Update: Player Ad Pause on Suspend");
          for (VideoAdPlayerCallback callback : _adCallbacks) {
            callback.onPause();
          }
          break;
        default:
          break;
        }
      }

      //If we get an AD_COMPLETE during an IMA ad, our ad has finished
      else if (notification.equals(OoyalaPlayer.AD_COMPLETED_NOTIFICATION)) {
        Log.d(TAG, "IMA Ad Update: Player Ad Complete");
        _isPlayingIMAAd = false;
        for (VideoAdPlayerCallback callback : _adCallbacks) {
          callback.onEnded();
        }
      }
      else if(notification.equals(OoyalaPlayer.CURRENT_ITEM_CHANGED_NOTIFICATION)) {
        for (VideoAdPlayerCallback callback : _adCallbacks) {
          callback.onEnded();
        }
      }
    }

    //Notifications from content playback
    else {
      if (notification.equals(OoyalaPlayer.STATE_CHANGED_NOTIFICATION)) {
        switch (player.getState()) {
        case PLAYING:
          Log.d(TAG, "IMA Ad Update: Player Content start");
          for (VideoAdPlayerCallback callback : _adCallbacks) {
            callback.onPlay();
          }
          break;
        case PAUSED:
          Log.d(TAG, "IMA Ad Update: Player Content Pause");
          for (VideoAdPlayerCallback callback : _adCallbacks) {
            callback.onPause();
          }
          break;
        case SUSPENDED:
          Log.d(TAG, "IMA Ad Update: Player Content Pause on Suspend");
          for (VideoAdPlayerCallback callback : _adCallbacks) {
            callback.onPause();
          }
          break;
        default:
          break;
        }
      }
    }
  }

}