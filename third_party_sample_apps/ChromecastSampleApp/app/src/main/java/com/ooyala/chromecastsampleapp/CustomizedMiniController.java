package com.ooyala.chromecastsampleapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.sample.castcompanionlibrary.cast.exceptions.NoConnectionException;
import com.google.sample.castcompanionlibrary.cast.exceptions.TransientNetworkDisconnectionException;
import com.ooyala.android.OoyalaPlayer.State;
import com.ooyala.android.castsdk.OOCastManager;
import com.ooyala.android.castsdk.OOMiniController;
import com.ooyala.android.util.DebugMode;

import java.lang.ref.WeakReference;

public class CustomizedMiniController extends RelativeLayout implements OOMiniController {

  private static final String TAG = "CustomizedMiniController";

  private WeakReference<OOCastManager> castManager;
  
  protected ImageView icon;
  protected TextView title;
  protected TextView subTitle;
  protected ImageView playPause;
  private View container;
  private Drawable pauseDrawable;
  private Drawable playDrawable;
  
  public CustomizedMiniController(Context context, AttributeSet attrs) {
    super(context, attrs);
    LayoutInflater inflater = LayoutInflater.from(context);
    View miniControllerView = inflater.inflate(R.layout.customized_ooyala_mini_controller, this);
    pauseDrawable = getResources().getDrawable(R.drawable.ic_mini_controller_pause);
    playDrawable = getResources().getDrawable(R.drawable.ic_mini_controller_play);
    loadViews(miniControllerView);
    setupCallbacks();
  }
  
  public void setCastManager(OOCastManager castManager) {
    this.castManager = new WeakReference<OOCastManager>(castManager);
  }
  
  private void loadViews(View miniControllerView) {
    icon = (ImageView) miniControllerView.findViewById(R.id.iconView);
    title = (TextView) miniControllerView.findViewById(R.id.titleView);
    subTitle = (TextView) miniControllerView.findViewById(R.id.subTitleView);
    playPause = (ImageView) miniControllerView.findViewById(R.id.playPauseView);
    container = miniControllerView.findViewById(R.id.bigContainer);
  }
  
  private void setupCallbacks() {

    playPause.setOnClickListener(new OnClickListener() {

        @Override
        public void onClick(View v) {
          if (castManager.get().getCastPlayer()  == null) {
            return;
          } else if (castManager.get().getCastPlayer().getState() == State.PAUSED ||
                    castManager.get().getCastPlayer().getState() == State.READY ||
                    castManager.get().getCastPlayer().getState() == State.COMPLETED){
            castManager.get().getCastPlayer().play();
          } else if (castManager.get().getCastPlayer() .getState() == State.PLAYING){
            castManager.get().getCastPlayer().pause();
          }
        }
      });

    container.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {

        if (castManager.get().getTargetActivity() != null) {
          try {
            onTargetActivityInvoked(getContext());
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
    });
  }

  private void onTargetActivityInvoked(Context context) throws TransientNetworkDisconnectionException,
      NoConnectionException {
    Intent intent = new Intent(context, castManager.get().getTargetActivity());
    intent.putExtra("embedcode", castManager.get().getCastPlayer().getEmbedCode());
    context.startActivity(intent);
  }
  
  public void setIcon(Bitmap bitmap) {
    icon.setImageBitmap(bitmap);
  }
  
  @Override
  public void updatePlayPauseButtonImage(boolean isPlaying) {
    if (isPlaying) {
      playPause.setVisibility(View.VISIBLE);
      playPause.setImageDrawable(pauseDrawable);
    } else {
      playPause.setVisibility(View.VISIBLE);
      playPause.setImageDrawable(playDrawable);
    }  
  }

  public void show() {
    super.setVisibility(VISIBLE);
    hideControls(false);
    updateUIInfo();
  }

  public void dismiss() {
    super.setVisibility(GONE);
  }
  
  private void hideControls(boolean hide) {
    int visibility = hide ? View.GONE : View.VISIBLE;
    icon.setVisibility(visibility);
    title.setVisibility(visibility);
    subTitle.setVisibility(visibility);
    if (hide) playPause.setVisibility(visibility);
  }
  
  private void updateUIInfo() {
    DebugMode.logD(TAG, "Update MiniController UI Info");
    title.setText(castManager.get().getCastPlayer().getCastItemTitle());
    subTitle.setText(castManager.get().getCastPlayer().getCastItemDescription());
    setIcon(castManager.get().getCastPlayer().getCastImageBitmap());
  }
}
























































