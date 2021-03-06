package com.ooyala.android;

import com.ooyala.android.item.Stream;
import com.ooyala.android.player.ExoMoviePlayer;
import com.ooyala.android.player.MoviePlayer;
import com.ooyala.android.player.PlayerFactory;

import java.util.Set;

/**
 * Created by zchen on 1/29/16.
 */
public class ExoPlayerFactory implements PlayerFactory {
  private int priority;

  public ExoPlayerFactory(int priority) {
    this.priority = priority;
  }

  @Override
  public boolean canPlayVideo(Set<Stream> streams) {
    if (streams == null) {
      return false;
    }

    if (Stream.streamSetContainsDeliveryType(streams, Stream.DELIVERY_TYPE_HLS) ||
        Stream.streamSetContainsDeliveryType(streams, Stream.DELIVERY_TYPE_DASH) ||
        Stream.streamSetContainsDeliveryType(streams, Stream.DELIVERY_TYPE_MP4)) {
      return true;
    }
    return false;
  }

  @Override
  public MoviePlayer createPlayer() throws OoyalaException {
    return new ExoMoviePlayer();
  }

  public int priority() {
    return priority;
  }
}
