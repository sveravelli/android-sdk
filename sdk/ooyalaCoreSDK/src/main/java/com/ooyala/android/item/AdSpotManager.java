package com.ooyala.android.item;

import com.ooyala.android.util.DebugMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A helper class help us to manage ad spots
 */
public class AdSpotManager<T extends AdSpot> {
  private static final String TAG = "AdSpotManager";
  private List<T> _ads;
  private Set<T> _playedAds;
  private int _timeAlignment;

  public AdSpotManager() {
    _ads = new ArrayList<T>();
    _playedAds = new HashSet<T>();
    _timeAlignment = 0;
  }

  /**
   * Mark all adspots as unplayed
   */
  public void resetAds() {
    _playedAds.clear();
  }

  /**
   * Clear all adspots
   */
  public void clear() {
    _playedAds.clear();
    _ads.clear();
    _timeAlignment = 0;
  }

  /**
   * Insert an adSpot
   * 
   * @param ad
   *          the adSpot to insert
   */
  public void insertAd(T ad) {
    if (ad == null) {
      DebugMode.assertFail(TAG, "try to insert a null ad");
      return;
    }

    if (_ads.contains(ad)) {
      DebugMode.assertFail(TAG, ad.toString() + " already exist");
      return;
    }

    _ads.add(ad);
    Collections.sort(_ads);
  }

  /**
   * Insert an adSpot
   * 
   * @param adSpots
   *          the adSpot list to insert
   */
  public void insertAds(List<T> adSpots) {
    _ads.addAll(adSpots);
    Collections.sort(_ads);
  }

  /**
   * get the adspot before a certain time,
   * 
   * @param time
   *          in millisecond
   * @return the unplayed adspot before the specified time which, null if no
   *          such adspot
   */
  public T adBeforeTime(int time) {
    T candidate = null;
    int candidateTime = 0;
    for (T ad : _ads) {
      int adTime = alignedAdTime(ad.getTime());
      if (time < adTime) {
        break;
      } else {
        if (adTime > candidateTime) {
          candidateTime = adTime;
          candidate = null;
        }
        if (!this._playedAds.contains(ad)) {
          candidate = ad;
        }
      }
    }

    return candidate;

  }

  /**
   * mark an adspot as played
   * 
   * @param ad
   *          the adspot to be marked
   */
  public void markAsPlayed(T ad) {
    if (ad == null) {
      DebugMode.assertFail(TAG, "try to mark a null adspot");
      return;
    }

    _playedAds.add(ad);
  }

  /**
   * get the adspot list size
   * 
   * @return size
   */
  public int size() {
    return _ads.size();
  }

  /**
   * set the time alignment
   * 
   * @param alignment
   *          in millisecond
   */
  public void setAlignment(int alignment) {
    _timeAlignment = alignment;
  }

  /**
   * get the time alignment
   * 
   * @return the alignment in millisecond
   */
  public int getAlignment() {
    return _timeAlignment;
  }

  public Set<Integer> getCuePointsInMilliSeconds() {
    Set<Integer> cuePoints = new HashSet<Integer>();
    for (T ad : _ads) {
      if (ad.getTime() <= 0) {
        continue;
      }

      if (_playedAds.contains(ad)) {
        continue;
      }

      cuePoints.add(alignedAdTime(ad.getTime()));
    }
    return cuePoints;
  }

  private int alignedAdTime(int adTime) {
    if (_timeAlignment > 0) {
      return ((adTime + _timeAlignment / 2) / _timeAlignment)
          * _timeAlignment;
    } else {
      return adTime;
    }
  }
}
