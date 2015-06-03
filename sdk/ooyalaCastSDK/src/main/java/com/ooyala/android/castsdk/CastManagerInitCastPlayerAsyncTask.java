package com.ooyala.android.castsdk;

import android.os.AsyncTask;

import com.ooyala.android.EmbedTokenGenerator;
import com.ooyala.android.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * An async task that can handle the blocking embed token generation needed to init the player
 */
public class CastManagerInitCastPlayerAsyncTask extends AsyncTask<Void, Integer, Void> {
  CastManager manager;
  String embedCode;
  int playheadTime;
  boolean isPlaying;
  EmbedTokenGenerator embedTokenGenerator;

  public CastManagerInitCastPlayerAsyncTask(CastManager manager, String embedCode, int playheadTimeInMillis, boolean isPlaying, EmbedTokenGenerator generator) {
    super();
    this.manager = manager;
    this.embedCode = embedCode;
    this.playheadTime = playheadTimeInMillis;
    this.isPlaying = isPlaying;
    this.embedTokenGenerator = generator;
  }

  @Override
  protected Void doInBackground(Void...params) {
    List<String> embedCodes = new ArrayList<>();
    embedCodes.add(embedCode);

    manager.initCastPlayer(embedCode, playheadTime, isPlaying, Utils.blockingGetEmbedTokenForEmbedCodes(embedTokenGenerator, embedCodes));
    return null;
  }

  @Override
  protected void onPostExecute(Void aVoid) {
    super.onPostExecute(aVoid);
  }

}
