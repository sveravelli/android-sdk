package com.ooyala.android.item;

/**
 * The base of OoyalaAdSpot, VastAdSpot and other type of ad spot
 */
public abstract class AdSpotBase implements Comparable<AdSpotBase> {
  /**
   * Fetch the time at which this AdSpot should play.
   * @return The time at which this AdSpot should play in milliseconds.
   */
  public abstract int getTime();

  /**
   * compare two ad spots based on time, which is required to properly sort ad
   * spots.
   * 
   * @param ad
   *          the ad to be compared
   */
  public int compareTo(AdSpotBase ad) {
    return this.getTime() - ad.getTime();
  }

}
