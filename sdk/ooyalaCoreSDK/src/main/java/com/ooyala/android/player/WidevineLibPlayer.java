package com.ooyala.android.player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import android.os.Handler;
import android.os.Message;

import com.ooyala.android.util.DebugMode;
import com.ooyala.android.Environment;
import com.ooyala.android.OoyalaException;
import com.ooyala.android.OoyalaException.OoyalaErrorCode;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayer.SeekStyle;
import com.ooyala.android.OoyalaPlayer.State;
import com.ooyala.android.item.Stream;
import com.widevine.drmapi.android.WVEvent;
import com.widevine.drmapi.android.WVEventListener;
import com.widevine.drmapi.android.WVPlayback;
import com.widevine.drmapi.android.WVStatus;

//Use the 2.x API for pre-honeycomb devices.
public class WidevineLibPlayer extends MoviePlayer implements WVEventListener, Handler.Callback {
  // messages
  private static final int INIT = 0;
  private static final int ERROR = -1;

  private WVPlayback _wvplayback = new WVPlayback();
  private Handler _handler = new Handler(this);
  private Stream _stream = null;

  @Override
  public void init(OoyalaPlayer parent, Set<Stream> streams) {
    _stream = null;
    if (Stream.streamSetContainsDeliveryType(streams, Stream.DELIVERY_TYPE_WV_MP4)) {
      _stream = Stream.getStreamWithDeliveryType(streams, Stream.DELIVERY_TYPE_WV_MP4);
    }
    if (_stream == null) {
      DebugMode.logE("Widevine", "No available streams for the WidevineLib Player, Cannot continue." + streams.toString());
      this._error = new OoyalaException(OoyalaErrorCode.ERROR_PLAYBACK_FAILED, "Invalid Stream");
      setState(State.ERROR);
      return;
    }

    _parent = parent;

    initializeWidevine();
  }

  @Override
  public WVStatus onEvent(WVEvent event, HashMap<String, Object> attributes) {
    DebugMode.logD("Widevine", event.toString() + ": " + attributes.toString());
    switch (event) {
      case InitializeFailed:
        if (this._error == null) this._error = new OoyalaException(OoyalaErrorCode.ERROR_PLAYBACK_FAILED, "Widevine Initialization Failed");
      case LicenseRequestFailed:
        if (this._error == null) this._error = new OoyalaException(OoyalaErrorCode.ERROR_PLAYBACK_FAILED, "Widevine License Request Failed");
      case PlayFailed:
        if (this._error == null) this._error = new OoyalaException(OoyalaErrorCode.ERROR_PLAYBACK_FAILED, "Widevine Playback Failed");
        _handler.sendEmptyMessage(ERROR);
        if (attributes.containsKey("WVStatusKey")) return (WVStatus) attributes.get("WVStatusKey");
        else return WVStatus.OK;
      case Initialized:
        //_handler.sendEmptyMessage(INIT);

        // Update the stream to have the WV authorized stream URL, then super to MoviePlayer to play
        _wvplayback.registerAsset(_stream.decodedURL().toString());
        _wvplayback.requestLicense(_stream.decodedURL().toString());
        _stream.setUrl(_wvplayback.play(_stream.decodedURL().toString()));
        _stream.setUrlFormat(Stream.STREAM_URL_FORMAT_TEXT);
        Set<Stream> newStreams = new HashSet<Stream>();
        newStreams.add(_stream);
        super.init(_parent, newStreams);
      case NullEvent:
      case Playing:
      case Stopped:
      case EndOfList:
      case Terminated:
      case SecureStore:
      case LicenseReceived:
      case LicenseRemoved:
      case Registered:
      case Unregistered:
      case QueryStatus:
        return WVStatus.OK;
      default:
        return WVStatus.OK;
    }
  }

  @Override
  public boolean handleMessage(Message msg) {
    switch (msg.what) {
      case INIT:
        // This was originally used when Widevine initialization was asynchronous.  Saving in case we do
        // asynch again.
        break;
      case ERROR:
        setState(State.ERROR);
        break;
      default:
    }
    return true;
  }

  private void initializeWidevine() {
    HashMap<String, Object> options = new HashMap<String, Object>();
    // this should point to SAS once we get the proxy up
    String path = Environment.DRM_HOST
        + String.format(DRM_TENENT_PATH, _parent.getOoyalaAPIClient().getPcode(),
            _parent.getEmbedCode(), "widevine", "ooyala");

    //  If SAS included a widevine server path, use that instead
    if(_stream.getWidevineServerPath() != null) {
      path = _stream.getWidevineServerPath();
    }
    options.put("WVPortalKey", "ooyala"); // add this value in SAS
    options.put("WVDRMServer", path);
    options.put("WVLicenseTypeKey", 3);

    WVStatus initStatus = _wvplayback.initializeSynchronous(_parent.getLayout().getContext(), options, this);

    // If we notice we're already initialized, we have to reset the WV object.
    if (initStatus == WVStatus.AlreadyInitialized) {
      _wvplayback.terminateSynchronous();
      _wvplayback.initializeSynchronous(_parent.getLayout().getContext(), options, this);
    }
    _handler.sendEmptyMessage(INIT);
  }

  @Override
  public void suspend(int millisToResume, State stateToResume) {
    _wvplayback.terminateSynchronous();
    super.suspend(millisToResume, stateToResume);
  }

  @Override
  public void suspend() {
    super.suspend();
  }


  @Override
  public void resume() {
    super.resume();
  }

  @Override
  public void resume(int millisToResume, State stateToResume) {
    initializeWidevine();
    super.resume(millisToResume, stateToResume);
  }
  @Override
  public void destroy() {
    _wvplayback.terminate();
    super.destroy();
  }

  @Override
  public SeekStyle getSeekStyle() {
    return SeekStyle.BASIC;
  }

}
