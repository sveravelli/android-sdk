package com.ooyala.android;

import java.util.Observable;
import java.util.Observer;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.opengl.Visibility;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ooyala.android.OoyalaPlayer.State;

public class DefaultOoyalaPlayerInlineControls extends AbstractDefaultOoyalaPlayerControls implements
    SeekBar.OnSeekBarChangeListener, Button.OnClickListener, Observer {
  private LinearLayout _bottomBar = null;
  private LinearLayout _seekWrapper = null;
  private LinearLayout _liveWrapper = null;
  private PlayPauseButton _playPause = null;
  private FullscreenButton _fullscreen = null;
  private SeekBar _seek = null;
  private TextView _currTime = null;
  private TextView _duration = null;
  private TextView _liveIndicator = null;
  private ProgressBar _spinner = null;
  private boolean _wasPlaying;
  private boolean _seeking;

  private boolean _adUi = false;

  public DefaultOoyalaPlayerInlineControls(OoyalaPlayer player, OoyalaPlayerLayout layout) {
    setParentLayout(layout);
    setOoyalaPlayer(player);
    setupControls();
  }

  @Override
  protected void updateButtonStates() {
    if (_playPause != null) {
      _playPause.setPlaying(_player.isPlaying());
    }
    if (_fullscreen != null) {
      _fullscreen.setFullscreen(_player.isFullscreen());
    }

    if (_seekWrapper != null && _player.getCurrentItem() != null) {
      if(_player.getCurrentItem().isLive()) {
        _seekWrapper.setVisibility(View.GONE);
      } else {
        _seekWrapper.setVisibility(_adUi ? View.INVISIBLE : View.VISIBLE);
      }
    }

    if (_liveWrapper != null && _player.getCurrentItem() != null) {
      _liveWrapper.setVisibility(_player.getCurrentItem().isLive() ? View.VISIBLE : View.GONE);
      if (Build.VERSION.SDK_INT >= Constants.SDK_INT_HONEYCOMB) {
        _liveWrapper.setAlpha(_player.isShowingAd() ? 0.4f : 1f); // supported only 11+
      }
    }
  }

  @Override
  protected void setupControls() {
    if (_layout == null) { return; }
    _baseLayout = new FrameLayout(_layout.getContext());
    _baseLayout.setBackgroundColor(Color.TRANSPARENT);

    _bottomBar = new LinearLayout(_baseLayout.getContext());
    _bottomBar.setOrientation(LinearLayout.HORIZONTAL);
    _bottomBar.setBackgroundDrawable(Images.gradientBackground(GradientDrawable.Orientation.BOTTOM_TOP));

    _playPause = new PlayPauseButton(_bottomBar.getContext());
    _playPause.setPlaying(_player.isPlaying());
    ViewGroup.LayoutParams ppLP = new ViewGroup.LayoutParams(Images.dpToPixels(_baseLayout.getContext(),
        PREFERRED_BUTTON_WIDTH_DP), Images.dpToPixels(_baseLayout.getContext(), PREFERRED_BUTTON_HEIGHT_DP));
    _playPause.setLayoutParams(ppLP);
    _playPause.setOnClickListener(this);

    _seekWrapper = new LinearLayout(_bottomBar.getContext());
    _seekWrapper.setOrientation(LinearLayout.HORIZONTAL);
    _currTime = new TextView(_seekWrapper.getContext());
    _currTime.setText("00:00:00");
    LinearLayout.LayoutParams currTimeLP = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT);
    currTimeLP.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
    _currTime.setLayoutParams(currTimeLP);
    _seek = new SeekBar(_seekWrapper.getContext());
    LinearLayout.LayoutParams seekLP = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,
        1f);
    seekLP.gravity = Gravity.CENTER;
    seekLP.leftMargin = Images.dpToPixels(_baseLayout.getContext(), MARGIN_SIZE_DP);
    seekLP.rightMargin = Images.dpToPixels(_baseLayout.getContext(), MARGIN_SIZE_DP);
    _seek.setLayoutParams(seekLP);
    _seek.setOnSeekBarChangeListener(this);
    _duration = new TextView(_seekWrapper.getContext());
    _duration.setText("00:00:00");
    LinearLayout.LayoutParams durationLP = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT);
    durationLP.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
    _duration.setLayoutParams(durationLP);
    _seekWrapper.addView(_currTime);
    _seekWrapper.addView(_seek);
    _seekWrapper.addView(_duration);
    LinearLayout.LayoutParams seekWrapperLP = new LinearLayout.LayoutParams(0,
        ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
    seekWrapperLP.gravity = Gravity.CENTER;
    seekWrapperLP.leftMargin = Images.dpToPixels(_baseLayout.getContext(), MARGIN_SIZE_DP);
    seekWrapperLP.rightMargin = Images.dpToPixels(_baseLayout.getContext(), MARGIN_SIZE_DP);
    _seekWrapper.setLayoutParams(seekWrapperLP);

    _liveWrapper = new LinearLayout(_bottomBar.getContext());
    _liveWrapper.setVisibility(View.GONE);
    _liveWrapper.setOrientation(LinearLayout.HORIZONTAL);
    _liveIndicator = new TextView(_liveWrapper.getContext());
    _liveIndicator.setText(LocalizationSupport.localizedStringFor("LIVE"));
    _liveIndicator.setGravity(Gravity.CENTER_HORIZONTAL);
    @SuppressWarnings("deprecation")
    LinearLayout.LayoutParams liveIndicatorLP = new LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    liveIndicatorLP.gravity = Gravity.CENTER;
    _liveIndicator.setLayoutParams(liveIndicatorLP);
    _liveWrapper.addView(_liveIndicator);
    LinearLayout.LayoutParams liveWrapperLP = new LinearLayout.LayoutParams(0,
        ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
    liveWrapperLP.gravity = Gravity.CENTER;
    liveWrapperLP.leftMargin = Images.dpToPixels(_baseLayout.getContext(), MARGIN_SIZE_DP);
    liveWrapperLP.rightMargin = Images.dpToPixels(_baseLayout.getContext(), MARGIN_SIZE_DP);
    _liveWrapper.setLayoutParams(liveWrapperLP);

    _fullscreen = new FullscreenButton(_bottomBar.getContext());
    _fullscreen.setFullscreen(_player.isFullscreen());
    LinearLayout.LayoutParams fsLP = new LinearLayout.LayoutParams(Images.dpToPixels(
        _baseLayout.getContext(), PREFERRED_BUTTON_HEIGHT_DP), Images.dpToPixels(_baseLayout.getContext(),
        PREFERRED_BUTTON_HEIGHT_DP));
    fsLP.leftMargin = (PREFERRED_BUTTON_WIDTH_DP - PREFERRED_BUTTON_HEIGHT_DP) / 2;
    fsLP.rightMargin = (PREFERRED_BUTTON_WIDTH_DP - PREFERRED_BUTTON_HEIGHT_DP) / 2;
    _fullscreen.setLayoutParams(fsLP);
    _fullscreen.setOnClickListener(this);

    _bottomBar.addView(_playPause);
    _bottomBar.addView(_seekWrapper);
    _bottomBar.addView(_liveWrapper);
    _bottomBar.addView(_fullscreen);
    FrameLayout.LayoutParams bottomBarLP = new FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM
            | Gravity.CENTER_HORIZONTAL);
    _baseLayout.addView(_bottomBar, bottomBarLP);

    _spinner = new ProgressBar(_layout.getContext());
    FrameLayout.LayoutParams spinnerLP = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
        FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER | Gravity.CENTER_HORIZONTAL);
    _layout.addView(_spinner, spinnerLP);

    FrameLayout.LayoutParams baseLP = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
        FrameLayout.LayoutParams.MATCH_PARENT);
    _layout.addView(_baseLayout, baseLP);
    hide();
    _player.addObserver(this);
  }

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    if (fromUser) {
      _player.seekToPercent(progress);
      update(null, null);
    }
  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {
    _seeking = true;
    _wasPlaying = _player.isPlaying();
    _player.pause();
  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
    _seeking = false;
    if (_wasPlaying) {
      _player.play();
    }
  }

  @Override
  public void onClick(View v) {
    if (v == _playPause) {
      if (_player.isPlaying()) {
        _player.pause();
      } else {
        _player.play();
      }
      show();
    } else if (v == _fullscreen && _isPlayerReady) {
      _player.setFullscreen(!_player.isFullscreen());
      updateButtonStates();
      hide();
    }
  }

  @Override
  public void update(Observable arg0, Object arg1) {
    if (_seek != null && !_seeking) {
      _seek.setProgress(_player.getPlayheadPercentage());
      _seek.setSecondaryProgress(_player.getBufferPercentage());
    }
    if (_duration != null && _currTime != null) {
      boolean includeHours = _player.getDuration() >= 1000 * 60 * 60;
      _duration.setText(Utils.timeStringFromMillis(_player.getDuration(), includeHours));
      _currTime.setText(Utils.timeStringFromMillis(_player.getPlayheadTime(), includeHours));
    }

    // update UI on adStarted/adCompleted
    if(arg1 == OoyalaPlayer.AD_STARTED_NOTIFICATION) {
      _isPlayerReady = true;
      _adUi = true;
      updateButtonStates();
    }

    if(arg1 == OoyalaPlayer.AD_COMPLETED_NOTIFICATION ||
        arg1 == OoyalaPlayer.AD_SKIPPED_NOTIFICATION ||
        arg1 == OoyalaPlayer.AD_ERROR_NOTIFICATION ) {
      _isPlayerReady = false;
      _adUi = false;
      updateButtonStates();
    }

    // update spinner
    if (arg1 == OoyalaPlayer.STATE_CHANGED_NOTIFICATION) {
      State currentState = _player.getState();

      updateButtonStates();

      if (currentState == State.INIT || currentState == State.LOADING) {
        _spinner.setVisibility(View.VISIBLE);
      } else {
        _spinner.setVisibility(View.INVISIBLE);
      }

      if (currentState == State.READY || currentState == State.PLAYING || currentState == State.PAUSED) {
        _isPlayerReady = true;
      }

      if (currentState == State.SUSPENDED) {
        _isPlayerReady = false;
        hide();
      }
      if (!isShowing() && currentState != State.INIT && currentState != State.LOADING
          && currentState != State.ERROR && currentState != State.SUSPENDED && !_player.isFullscreen()) {
        show();
      }
    }
  }

  @Override
  public int bottomBarOffset() {
    return (PREFERRED_BUTTON_HEIGHT_DP + MARGIN_SIZE_DP * 4);
  }

}
