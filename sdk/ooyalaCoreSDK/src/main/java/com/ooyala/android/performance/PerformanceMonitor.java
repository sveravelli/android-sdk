package com.ooyala.android.performance;

import com.ooyala.android.OoyalaNotification;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.performance.counting.PerformanceEventWatchCounting;
import com.ooyala.android.performance.matcher.PerformanceNotificationNameMatcher;
import com.ooyala.android.performance.matcher.PerformanceNotificationNameStateMatcher;
import com.ooyala.android.performance.startend.PerformanceEventWatchStartEnd;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

/**
 * Collect performance statistics, based on OoyalaNotifications.
 * Does not support watches that start and end on the exact same event occurrence, and thus have zero time.
 * Does support watches that start and end on a repeating event, with time in between.
 */
final public class PerformanceMonitor
  implements Observer {

  private final Set<PerformanceEventWatchInterface> eventWatches;
  private final Map<String,Set<PerformanceEventWatchInterface>> watchesByName;
  private final Observable observable;

  /**
   * @return OOPerformanceMonitor configured with standard monitoring.
   */
  public static PerformanceMonitor getStandardMonitor( final Observable observable ) {
    final PerformanceMonitorBuilder b = new PerformanceMonitorBuilder( observable );
    b.addEventWatch(
        new PerformanceEventWatchStartEnd(
            new PerformanceNotificationNameMatcher( OoyalaPlayer.EMBED_CODE_SET_NOTIFICATION_NAME ),
            new PerformanceNotificationNameStateMatcher( OoyalaPlayer.STATE_CHANGED_NOTIFICATION_NAME, OoyalaPlayer.State.READY )
        )
    );
    b.addEventWatch(
        new PerformanceEventWatchCounting(
            new PerformanceNotificationNameMatcher( OoyalaPlayer.EMBED_CODE_SET_NOTIFICATION_NAME )
        )
    );
    return b.build();
  }

  public PerformanceMonitor( final Set<PerformanceEventWatchInterface> eventWatches, final Observable observable ) {
    this.eventWatches = new HashSet<PerformanceEventWatchInterface>( eventWatches );
    this.watchesByName = createWatchesByName( eventWatches );
    this.observable = observable;
    this.observable.addObserver(this);
  }

  public void destroy() {
    this.observable.deleteObserver( this );
  }

  private Map<String,Set<PerformanceEventWatchInterface>> createWatchesByName( final Set<PerformanceEventWatchInterface> eventWatches ) {
    final Map<String,Set<PerformanceEventWatchInterface>> d = new HashMap<String,Set<PerformanceEventWatchInterface>>();
    for( final PerformanceEventWatchInterface w : this.eventWatches ) {
      for( final String n : w.getWantedNotificationNames() ) {
        if( d.containsKey(n) ) {
          d.get( n ).add( w );
        }
        else {
          Set<PerformanceEventWatchInterface> s = new HashSet<PerformanceEventWatchInterface>();
          s.add( w );
          d.put( n, s );
        }
      }
    }
    return d;
  }

  @Override
  public void update( final Observable observable, final Object data ) {
    if( data instanceof OoyalaNotification ) {
      final String name = OoyalaNotification.getNameOrUnknown( data );
      final Set<PerformanceEventWatchInterface> s = watchesByName.get( name );
      if( s != null ) {
        for( final PerformanceEventWatchInterface w : s ) {
          w.onNotification( (OoyalaNotification)data );
        }
      }
    }
  }

  public PerformanceStatisticsSnapshot buildStatisticsSnapshot() {
    final PerformanceStatisticsSnapshotBuilder b = new PerformanceStatisticsSnapshotBuilder();
    for( final Set<PerformanceEventWatchInterface> s : watchesByName.values() ) {
      for( final PerformanceEventWatchInterface w : s ) {
        w.addStatisticsToBuilder( b );
      }
    }
    return b.build();
  }

}
