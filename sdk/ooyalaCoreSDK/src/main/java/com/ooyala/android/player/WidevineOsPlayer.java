package com.ooyala.android.player;

import android.annotation.TargetApi;
import android.drm.DrmErrorEvent;
import android.drm.DrmEvent;
import android.drm.DrmInfoEvent;
import android.drm.DrmInfoRequest;
import android.drm.DrmManagerClient;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings.Secure;

import com.ooyala.android.Environment;
import com.ooyala.android.OoyalaException;
import com.ooyala.android.OoyalaException.OoyalaErrorCode;
import com.ooyala.android.OoyalaNotification;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayer.SeekStyle;
import com.ooyala.android.OoyalaPlayer.State;
import com.ooyala.android.item.Stream;
import com.ooyala.android.util.DebugMode;

import java.net.URL;
import java.util.HashSet;
import java.util.Observable;
import java.util.Set;

//the widevine player using the built in libraries, for honeycomb+
@TargetApi(11)
public class WidevineOsPlayer extends MoviePlayer implements DrmManagerClient.OnErrorListener,
    DrmManagerClient.OnEventListener, DrmManagerClient.OnInfoListener, WidevineStuckMonitor.Listener {

  private static final String TAG = "WidevineOsPlayer";
  private static DrmManagerClient _drmClient;
  private boolean _live = false;
  private WidevineStuckMonitor _stuckMonitor; // prevent GCing it.
  private boolean isSeeking = false;

  @Override
  public void init(OoyalaPlayer parent, Set<Stream> streams) {

    // Get the correct (presumably only) stream to play widevine
    Stream stream = null;
    if (Stream.streamSetContainsDeliveryType(streams, Stream.DELIVERY_TYPE_WV_WVM)) {
       stream = Stream.getStreamWithDeliveryType(streams, Stream.DELIVERY_TYPE_WV_WVM);
    } else if (Stream.streamSetContainsDeliveryType(streams, Stream.DELIVERY_TYPE_WV_HLS)) {
       stream = Stream.getStreamWithDeliveryType(streams, Stream.DELIVERY_TYPE_WV_HLS);
    }
    if (stream == null) {
      DebugMode.logE(TAG, "No available streams for the Widevine Lib Player, Cannot continue. " + streams.toString());
      this._error = new OoyalaException(OoyalaErrorCode.ERROR_PLAYBACK_FAILED, "Invalid Stream");
      setState(State.ERROR);
      return;
    }

    //Setup DRM Client
    if (_drmClient == null) {
      _drmClient = new DrmManagerClient(parent.getLayout().getContext());
      _drmClient.setOnErrorListener(this);
      _drmClient.setOnEventListener(this);
      _drmClient.setOnInfoListener(this);
    }

    // replace scheme of widevine assets
    // need to be widevine:// vs http://
    URL streamURL = stream.decodedURL();
    if (streamURL == null) {
      DebugMode.logE(TAG, "Invalid stream, Malformed URL, Cannot continue. URL: " + stream.getUrl());
      this._error = new OoyalaException(OoyalaErrorCode.ERROR_PLAYBACK_FAILED, "Invalid Stream");
      setState(State.ERROR);
      return;
    }

    Uri uri = Uri.parse(streamURL.toString());
    // live check
    if (uri.getLastPathSegment().endsWith(".m3u8")) {
      _live = true;
    }
    stream.setUrl(uri.buildUpon().scheme("widevine").build().toString());
    stream.setUrlFormat(Stream.STREAM_URL_FORMAT_TEXT);

    DrmInfoRequest request = new DrmInfoRequest(DrmInfoRequest.TYPE_RIGHTS_ACQUISITION_INFO, "video/wvm");
    // this should point to SAS once we get the proxy up
    String serverPath = Environment.DRM_HOST
        + String.format(DRM_TENENT_PATH, parent.getOoyalaAPIClient().getPcode(),
                        parent.getEmbedCode(), "widevine", "ooyala");

    //  If SAS included a widevine server path, use that instead
    if(stream.getWidevineServerPath() != null) {
      serverPath = stream.getWidevineServerPath();
    } else {
      // If auth token is available, append it to the license path
      String authToken = parent.getAuthToken();
      if (authToken != null && !authToken.equals("")) {
    	  serverPath += "?auth_token=" + authToken;
      }
    }
    request.put("WVDRMServerKey", serverPath);
    request.put("WVAssetURIKey", stream.getUrl());
    request.put("WVPortalKey", "ooyala"); // override in SAS
    request.put("WVDeviceIDKey",
        Secure.getString(parent.getLayout().getContext().getContentResolver(), Secure.ANDROID_ID));
    request.put("WVLicenseTypeKey", "3");

    _drmClient.acquireRights(request);

    // Update the stream to have the WV authorized stream URL, then super to MoviePlayer to play
    Set<Stream> newStreams = new HashSet<Stream>();
    newStreams.add(stream);
    super.init(parent, newStreams);

    _stuckMonitor = new WidevineStuckMonitor( parent, this, this );
  }

  @Override
  public void update(Observable arg0, Object arg1) {
    final String name = OoyalaNotification.getNameOrUnknown(arg1);
    if(name == OoyalaPlayer.SEEK_COMPLETED_NOTIFICATION_NAME) {
      isSeeking = false;
      DebugMode.logD(TAG, "Seek completed. Re-enabling seeking");
    }
    super.update(arg0, arg1);
  }

  @Override
  public void destroy() {
    if(_stuckMonitor != null) {
      _stuckMonitor.destroy();
    }
    super.destroy();
  }

  @Override
  public void onFrozen() {
    DebugMode.logV( TAG, "onFrozen(): posting the runnable" );
    new Handler(Looper.getMainLooper()).post(new Runnable() {
      @Override
      public void run() {
        DebugMode.logV( TAG, "onFrozen(): running the runnable" );
        // shouldn't compete with an error state.
        if( getState() != State.ERROR ) {
          // per PB-373 not State.ERROR.
          setState( State.COMPLETED );
          _stuckMonitor.reset();
        }
      }
    });
  }

  @Override
  public void onError(DrmManagerClient client, DrmErrorEvent event) {
    DebugMode.logD(TAG, "WidevineError: " + eventToString(event));

    _error = new OoyalaException(OoyalaErrorCode.ERROR_PLAYBACK_FAILED, Integer.toString(event.getType()));

    new Handler(Looper.getMainLooper()).post(new Runnable() {
      @Override
      public void run() {
        setState(State.ERROR);
      }
    });
  }

  @Override
  public void onEvent(DrmManagerClient client, DrmEvent event) {
    DebugMode.logD(TAG, "WidevineEvent: " + eventToString(event));
  }

  @Override
  public void onInfo(DrmManagerClient client, DrmInfoEvent event) {
    DebugMode.logD(TAG, "WidevineInfoEvent: " + eventToString(event));
  }

  @Override
  // Disable seeking in Live mode
  public void seekToTime(int timeInMillis) {
    if (_live) return;

    //Widevine has a nasty asynchronous seek that queues up very fast seeks.  We have to make sure Widevine doesn't get overwhelmed by
    //too many seek calls.
    if (!isSeeking) {
      DebugMode.logD(TAG, "Seek started. Disabling seeking");
      super.seekToTime(timeInMillis);
      isSeeking = true;
    } else {
      DebugMode.logI(TAG, "Trying to seek while already seeking, dropping the incoming seek");
    }
  }

  private static String eventToString(DrmEvent e) {
    switch (e.getType()) {
      case DrmEvent.TYPE_ALL_RIGHTS_REMOVED:
        return "All Rights Removed";
      case DrmEvent.TYPE_DRM_INFO_PROCESSED:
        return "DRM Info Processed";
      case DrmInfoEvent.TYPE_ACCOUNT_ALREADY_REGISTERED:
        return "Account Already Registered";
      case DrmInfoEvent.TYPE_ALREADY_REGISTERED_BY_ANOTHER_ACCOUNT:
        return "Already Registered by Another Account";
      case DrmInfoEvent.TYPE_REMOVE_RIGHTS:
        return "Remove Rights";
      case DrmInfoEvent.TYPE_RIGHTS_INSTALLED:
        return "Rights Installed";
      case DrmInfoEvent.TYPE_RIGHTS_REMOVED:
        return "Rights Removed";
      case DrmInfoEvent.TYPE_WAIT_FOR_RIGHTS:
        return "Wait for Rights";
      case DrmErrorEvent.TYPE_ACQUIRE_DRM_INFO_FAILED:
        return "Acquire DRM Info Failed";
      case DrmErrorEvent.TYPE_NO_INTERNET_CONNECTION:
        return "No Internet Connection";
      case DrmErrorEvent.TYPE_NOT_SUPPORTED:
        return "Type Not Supported";
      case DrmErrorEvent.TYPE_OUT_OF_MEMORY:
        return "Out of Memory";
      case DrmErrorEvent.TYPE_PROCESS_DRM_INFO_FAILED:
        return "DRM Info Request Failed";
      case DrmErrorEvent.TYPE_REMOVE_ALL_RIGHTS_FAILED:
        return "Remove All Rights Failed";
      case DrmErrorEvent.TYPE_RIGHTS_NOT_INSTALLED:
        return "Rights Not Installed";
      case DrmErrorEvent.TYPE_RIGHTS_RENEWAL_NOT_ALLOWED:
        return "Rights Renewal Not Allowed";
      default:
        return "";
    }
  }

  @Override
  public SeekStyle getSeekStyle() {
    return SeekStyle.BASIC;
  }
}