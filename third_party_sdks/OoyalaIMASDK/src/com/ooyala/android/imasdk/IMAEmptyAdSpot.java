package com.ooyala.android.imasdk;

import java.util.HashSet;
import java.util.Set;

import com.ooyala.android.item.AdSpot;
import com.ooyala.android.item.Stream;

// TODO: get rid of this class, no longer required.
public class IMAEmptyAdSpot extends AdSpot implements IIMAAdSpot {

  private final OoyalaIMAManager _imaManager;

  public IMAEmptyAdSpot( OoyalaIMAManager imaManager ) {
    super();
    _imaManager = imaManager;
  }

  @Override
  public boolean fetchPlaybackInfo() {
    return true;
  }
  
  @Override
  public Set<Stream> getStreams() {
    return new HashSet<Stream>();
  }

  public OoyalaIMAManager getImaManager() {
    return _imaManager;
  }
}
