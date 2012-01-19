package com.ooyala.android.sampleapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;

import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayerLayout;
import com.ooyala.android.OoyalaPlayerLayoutController;


public class PlayerDetailActivity extends Activity
{
  private static final String TAG = "PlayerDetailActivity";
  
  private OoyalaPlayer player;

  @Override
  public void onCreate(Bundle savedInstanceState)
  {

    super.onCreate(savedInstanceState);
    String embedCode = getIntent().getStringExtra("com.ooyala.embedcode");
    Thread.setDefaultUncaughtExceptionHandler(onUncaughtException);
    try {
      setContentView(R.layout.main);
      
    } catch (Exception e) {
      e.printStackTrace();
    }
    OoyalaPlayerLayoutController layoutController = new OoyalaPlayerLayoutController((OoyalaPlayerLayout)findViewById(R.id.player), "l1am06xhbSxa0OtyZsBTshW2DMtp.qDW-_", "GkUqcxL-5aeVBYG71aYQmlkMh62iBRgq8O-d6Y5w", "l1am06xhbSxa0OtyZsBTshW2DMtp", "www.ooyala.com");
    player = layoutController.getPlayer();
    if (player.setEmbedCode(embedCode)) {
        Log.d(TAG, "TEST - yay!");
        player.play();
      } else {
        Log.d(TAG, "TEST - lame :(" + embedCode);
      }    
  }

  @Override
  protected void onDestroy() {
      super.onDestroy();
      player.pause();
  }  
  
  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    Log.d(TAG, "TEST - onConfigurationChangedd");
    super.onConfigurationChanged(newConfig);
  }

  private Thread.UncaughtExceptionHandler onUncaughtException = new Thread.UncaughtExceptionHandler()
  {
    public void uncaughtException(Thread thread, Throwable ex)
    {
      Log.e(TAG, "Uncaught exception", ex);
      showErrorDialog(ex);
    }
  };

  private void showErrorDialog(Throwable t)
  {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);

    builder.setTitle("Exception!");
    builder.setMessage(t.toString());
    builder.setPositiveButton("OK", null);
    builder.show();
  }
}
