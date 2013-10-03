package com.ooyala.android;

import java.util.HashSet;
import java.util.Set;

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
import android.util.Log;

import com.ooyala.android.OoyalaPlayer.SeekStyle;
import com.ooyala.android.OoyalaPlayer.State;

//the widevine player using the built in libraries, for honeycomb+
@TargetApi(11)
class WidevineOsPlayer extends MoviePlayer implements DrmManagerClient.OnErrorListener,
    DrmManagerClient.OnEventListener, DrmManagerClient.OnInfoListener {

  private static DrmManagerClient _drmClient;

  private boolean _live = false;

  @Override
  public void init(OoyalaPlayer parent, Set<Stream> streams) {

    // Get the correct (presumably only) stream to play widevine
    Stream stream = null;
    if (Stream.streamSetContainsDeliveryType(streams, Constants.DELIVERY_TYPE_WV_WVM)) {
       stream = Stream.getStreamWithDeliveryType(streams, Constants.DELIVERY_TYPE_WV_WVM);
    } else if (Stream.streamSetContainsDeliveryType(streams, Constants.DELIVERY_TYPE_WV_HLS)) {
       stream = Stream.getStreamWithDeliveryType(streams, Constants.DELIVERY_TYPE_WV_HLS);
    }
    if (stream == null) {
      Log.e("Widevine", "No available streams for the Widevine Lib Player, Cannot continue. " + streams.toString());
      this._error = "Invalid Stream";
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
    Uri uri = Uri.parse(stream.decodedURL().toString());
    // live check
    if (uri.getLastPathSegment().endsWith(".m3u8")) {
      _live = true;
    }
    stream.setUrl(uri.buildUpon().scheme("widevine").build().toString());

    DrmInfoRequest request = new DrmInfoRequest(DrmInfoRequest.TYPE_RIGHTS_ACQUISITION_INFO, "video/wvm");
    // this should point to SAS once we get the proxy up
    String serverPath = Constants.DRM_HOST
        + String.format(Constants.DRM_TENENT_PATH, parent.getPlayerAPIClient().getPcode(),
                        parent.getEmbedCode(), "widevine", "ooyala");

    //  If SAS included a widevine server path, use that instead
    if(stream.getWidevineServerPath() != null) {
      serverPath = stream.getWidevineServerPath();
    } else {
      // If auth token is available, append it to the license path
      String authToken = parent.getPlayerAPIClient().getAuthToken();
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
    stream.setUrlFormat(Constants.STREAM_URL_FORMAT_TEXT);
    newStreams.add(stream);
    super.init(parent, newStreams);
  }

  @Override
  public void onError(DrmManagerClient client, DrmErrorEvent event) {
    Log.d("Widevine", "WidevineError: " + eventToString(event));

    _error = Integer.toString(event.getType());

    new Handler(Looper.getMainLooper()).post(new Runnable() {
      @Override
      public void run() {
        setState(State.ERROR);
      }
    });
  }

  @Override
  public void onEvent(DrmManagerClient client, DrmEvent event) {
    Log.d("Widevine", "WidevineEvent: " + eventToString(event));
  }

  @Override
  public void onInfo(DrmManagerClient client, DrmInfoEvent event) {
    Log.d("Widevine", "WidevineInfoEvent: " + eventToString(event));
  }

  @Override
  // Disable seeking in Live mode
  public void seekToTime(int timeInMillis) {
    if (_live) return;
    super.seekToTime(timeInMillis);
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

  public SeekStyle getSeekStyle() {
    return SeekStyle.BASIC;
  }

}