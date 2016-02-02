package com.ooyala.android.player.exoplayer;

import android.content.Context;
import android.media.MediaCodec;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.widget.FrameLayout;

import com.google.android.exoplayer.CodecCounters;
import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecTrackRenderer;
import com.google.android.exoplayer.MediaCodecTrackRenderer.DecoderInitializationException;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.TrackRenderer;
import com.google.android.exoplayer.audio.AudioTrack;
import com.google.android.exoplayer.chunk.Format;
import com.google.android.exoplayer.text.Cue;
import com.google.android.exoplayer.upstream.BandwidthMeter;
import com.google.android.exoplayer.util.Util;
import com.ooyala.android.OoyalaException;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.item.Stream;
import com.ooyala.android.player.BaseStreamPlayer;
import com.ooyala.android.player.MovieView;
import com.ooyala.android.player.StreamPlayer;
import com.ooyala.android.util.DebugMode;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by zchen on 1/28/16.
 */
public class ExoStreamPlayer extends StreamPlayer implements
    RendererBuilderListener, SurfaceHolder.Callback, ExoPlayer.Listener {
  private static final String TAG = BaseStreamPlayer.class.getName();
  private ExoPlayer exoplayer;
  private Stream stream;
  private String streamUrl;
  private Handler mainHander;
  private SurfaceHolder holder;
  private int timeBeforeSuspend;
  private OoyalaPlayer.State stateBeforeSuspend;

  private enum RendererBuildingState {
    Idle,
    Building,
    Built
  };

  private RendererBuildingState rendererBuildingState;
  private boolean surfaceCreated;

  private RendererBuilder rendererBuilder;
  private TrackRenderer videoRenderer;
  private CodecCounters codecCounters;
  private BandwidthMeter bandwidthMeter;

  public static final int RENDERER_COUNT = 4;
  public static final int TYPE_VIDEO = 0;
  public static final int TYPE_AUDIO = 1;
  public static final int TYPE_TEXT = 2;
  public static final int TYPE_METADATA = 3;

  @Override
  public void init(OoyalaPlayer parent, Set<Stream> streams) {
    WifiManager wifiManager = (WifiManager)parent.getLayout().getContext().getSystemService(Context.WIFI_SERVICE);
    boolean isWifiEnabled = wifiManager.isWifiEnabled();
    mainHander = new Handler();
    stream =  Stream.bestStream(streams, isWifiEnabled);
    surfaceCreated = false;
    timeBeforeSuspend = -1;
    stateBeforeSuspend = OoyalaPlayer.State.INIT;

    if (stream == null) {
      DebugMode.logE(TAG, "ERROR: Invalid Stream (no valid stream available)");
      this._error = new OoyalaException(OoyalaException.OoyalaErrorCode.ERROR_PLAYBACK_FAILED, "Invalid Stream");
      setState(OoyalaPlayer.State.ERROR);
      return;
    }

    if (parent == null) {
      this._error = new OoyalaException(OoyalaException.OoyalaErrorCode.ERROR_PLAYBACK_FAILED, "Invalid Parent");
      setState(OoyalaPlayer.State.ERROR);
      return;
    }
    setState(OoyalaPlayer.State.LOADING);
    setParent(parent);
    streamUrl = stream.getUrlFormat().equals(Stream.STREAM_URL_FORMAT_B64) ? stream.decodedURL().toString().trim() : stream.getUrl().trim();

    // initialize exoplayer
    String userAgent = Util.getUserAgent(parent.getLayout().getContext(), "OoyalaSDK");
    rendererBuildingState = RendererBuildingState.Building;
    rendererBuilder = new HlsRendererBuilder(parent.getLayout().getContext(), userAgent, streamUrl);
    exoplayer = ExoPlayer.Factory.newInstance(RENDERER_COUNT);
    if (exoplayer != null) {
      exoplayer.addListener(this);
      setupSurfaceView();
      rendererBuilder.buildRenderers(this);
    }
  }

  private void setupSurfaceView() {
    _view = new MovieView(_parent.getOptions().getPreventVideoViewSharing(), _parent.getLayout().getContext());
    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
    _parent.addVideoView(_view);
    if (stream.getWidth() > 0 && stream.getHeight() > 0) {
      setVideoSize(stream.getWidth(), stream.getHeight());
    } else {
      setVideoSize(16,9);
    }
    holder = _view.getHolder();
    holder.addCallback(this);
  }

  private void removeSurfaceView() {
    _parent.removeVideoView();
    if (holder != null) {
      holder.removeCallback(this);
    }
    _view = null;
    holder = null;
    surfaceCreated = false;
  }

  private void setVideoSize(int width, int height) {
    ((MovieView) _view).setAspectRatio(((float) width) / height);
  }

  // this is called when both surface and renderers are ready
  private void setSurface() {
    if (!surfaceCreated || rendererBuildingState != RendererBuildingState.Built) {
      return;
    }
    DebugMode.logD(TAG, "surface is" + holder.getSurface().toString() + "frame is" + holder.getSurfaceFrame().toString());
    if (exoplayer != null) {
      exoplayer.sendMessage(videoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, holder.getSurface());
    }
  }

  // surfaceholder callback
  public void surfaceCreated(SurfaceHolder holder) {
    surfaceCreated = true;
    setSurface();
  }

  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

  }

  public void surfaceDestroyed(SurfaceHolder holder) {
    surfaceCreated = false;
    if (exoplayer != null) {
      exoplayer.blockingSendMessage(videoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, null);
    }
  }

  // renderer builder interface
  @Override
  public void onRenderers(TrackRenderer[] renderers, BandwidthMeter bandwidthMeter) {
    videoRenderer = renderers[TYPE_VIDEO];
    codecCounters = (videoRenderer instanceof MediaCodecTrackRenderer)
        ? ((MediaCodecTrackRenderer) videoRenderer).codecCounters
        : renderers[TYPE_AUDIO] instanceof MediaCodecTrackRenderer
        ? ((MediaCodecTrackRenderer) renderers[TYPE_AUDIO]).codecCounters : null;
    this.bandwidthMeter = bandwidthMeter;
    if (exoplayer != null) {
      exoplayer.prepare(renderers);
      rendererBuildingState = RendererBuildingState.Built;
    }
    setSurface();
  }

  public void onRenderersError(Exception e) {
    DebugMode.logE(TAG, "renderer error" + e.getMessage(), e);
  }

  public Handler getMainHandler() {
    return mainHander;
  }

  // SampleSourceEvent Listeners
  @Override
  public void onLoadStarted(int sourceId, long length, int type, int trigger, Format format,
                     long mediaStartTimeMs, long mediaEndTimeMs) {

  }

  @Override
  public void onLoadCompleted(int sourceId, long bytesLoaded, int type, int trigger, Format format,
                       long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs) {

  }

  @Override
  public void onLoadCanceled(int sourceId, long bytesLoaded) {

  }

  @Override
  public void onLoadError(int sourceId, IOException e) {

  }

  @Override
  public void onUpstreamDiscarded(int sourceId, long mediaStartTimeMs, long mediaEndTimeMs) {

  }

  @Override
  public void onDownstreamFormatChanged(int sourceId, Format format, int trigger, long mediaTimeMs) {

  }

  // video track listener
  @Override
  public void onDroppedFrames(int count, long elapsed) {
    DebugMode.logV(TAG, "dropped " + count + " frames " + elapsed + " elapsed");

  }

  @Override
  public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees,
                          float pixelWidthHeightRatio) {
    DebugMode.logV(TAG, "video size changed, width " + width + " height " + height + " aspectRatio " + pixelWidthHeightRatio);

  }

  @Override
  public void onDrawnToSurface(Surface surface) {
    DebugMode.logV(TAG, "onDrawToSurface, surface is" + surface.toString());
  }

  // Audio track listener
  @Override
  public void onAudioTrackInitializationError(AudioTrack.InitializationException e) {
    DebugMode.logE(TAG, "audio track init error:" + e.getMessage(), e);
  }

  @Override
  public void onAudioTrackWriteError(AudioTrack.WriteException e) {

  }

  @Override
  public void onAudioTrackUnderrun(int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs) {

  }

  // codec track listener
  @Override
  public void onDecoderInitializationError(DecoderInitializationException e) {

  }

  @Override
  public void onCryptoError(MediaCodec.CryptoException e) {
    DebugMode.logE(TAG, "audio track init error:" + e.getMessage(), e);
  }

  @Override
  public void onDecoderInitialized(String decoderName, long elapsedRealtimeMs,
                            long initializationDurationMs) {

  }



  // MetadataRenderer interface
  @Override
  public void onMetadata(Map<String, Object> metadata) {
    // handle ID3 metadata here.
  }

  // TextRenderer interface
  @Override
  public void onCues(List<Cue> cues) {
    // handle CC here
  }

  // Exoplayer listener
  @Override
  public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
    DebugMode.logD(TAG, "Exoplayer.OnPlayerStateChanged, playWhenReady" + playWhenReady + "state" + playbackState);
    switch (playbackState) {
      case ExoPlayer.STATE_BUFFERING:
        setState(OoyalaPlayer.State.LOADING);
        break;
      case ExoPlayer.STATE_ENDED:
        setState(OoyalaPlayer.State.COMPLETED);
        break;
      case ExoPlayer.STATE_IDLE:
        break;
      case ExoPlayer.STATE_PREPARING:
        setState(OoyalaPlayer.State.LOADING);
        break;
      case ExoPlayer.STATE_READY:
        setState(playWhenReady ? OoyalaPlayer.State.PLAYING : OoyalaPlayer.State.PAUSED);
        break;
      default:
        break;
    }
  }

  @Override
  public void onPlayWhenReadyCommitted() {
    if (exoplayer == null) {
      return;
    }
    boolean isPlaying = exoplayer.getPlayWhenReady();
    if (isPlaying) {
      setState(OoyalaPlayer.State.PLAYING);
      startPlayheadTimer();
    } else {
      setState(OoyalaPlayer.State.PAUSED);
      stopPlayheadTimer();
    }
  }

  @Override
  public void onPlayerError(ExoPlaybackException error) {
    DebugMode.logE(TAG, "exoplayer error:" + error.getMessage(), error);
    setState(OoyalaPlayer.State.ERROR);
  }

  // player interface
  @Override
  public void play() {
    if (exoplayer != null) {
      exoplayer.setPlayWhenReady(true);
    }
  }

  @Override
  public void pause() {
    if (exoplayer != null) {
      exoplayer.setPlayWhenReady(false);
    }
  }

  @Override
  public void seekToTime(int timeMillis) {
    if (exoplayer == null) {
      return;
    }
    long seekPosition = exoplayer.getDuration() == ExoPlayer.UNKNOWN_TIME ? 0
        : Math.min(Math.max(0, timeMillis), duration());
    exoplayer.seekTo(seekPosition);
  }

  @Override
  public int duration() {
    return (exoplayer == null || exoplayer.getDuration() == ExoPlayer.UNKNOWN_TIME) ? 0
        : (int) exoplayer.getDuration();
  }

  @Override
  public int currentTime() {
    return  (exoplayer == null || exoplayer.getDuration() == ExoPlayer.UNKNOWN_TIME ) ? 0
        : (int) exoplayer.getCurrentPosition();
  }

  @Override
  public void stop() {
    pause();
    seekToTime(0);
  }

  @Override
  public void destroy() {

    if (exoplayer != null) {
      exoplayer.setPlayWhenReady(false);
      exoplayer.removeListener(this);
      exoplayer.release();
      exoplayer = null;
    }
    rendererBuildingState = RendererBuildingState.Idle;
    removeSurfaceView();
  }

  @Override
  public int buffer() {
    return exoplayer == null ? 0 : exoplayer.getBufferedPercentage();
  }

  @Override
  public boolean seekable() {
    return exoplayer != null;
  }

  @Override
  public boolean isPlaying() {
    return exoplayer != null && exoplayer.getPlayWhenReady();
  }

  @Override
  public void suspend() {
    int millisToResume = -1;
    if (exoplayer != null) {
      millisToResume = (int)exoplayer.getCurrentPosition();
    }
    suspend(millisToResume, getState());
  }

  private void suspend(int millisToResume, OoyalaPlayer.State stateToResume) {
    DebugMode.logD(TAG, "suspend with time " + millisToResume + "state" + stateToResume.toString());
    if (getState() == OoyalaPlayer.State.SUSPENDED) {
      DebugMode.logD(TAG, "Suspending an already suspended player");
      return;
    }
    if (exoplayer == null) {
      DebugMode.logD(TAG, "Suspending with a null player");
      return;
    }

    if (millisToResume >= 0) {
      timeBeforeSuspend = millisToResume;
    }
    stateBeforeSuspend = stateToResume;
    destroy();
    setState(OoyalaPlayer.State.SUSPENDED);
  }

  @Override
  public void resume() {
    resume(timeBeforeSuspend, stateBeforeSuspend);
  }

  @Override
  public void resume(int millisToResume, OoyalaPlayer.State stateToResume) {
    DebugMode.logD(TAG, "Resuming. time to resume: " + millisToResume + ", state to resume: " + stateToResume);
    if (exoplayer == null) {
      DebugMode.logE(TAG, "exoplayer is null, cannot resume");
      return;
    }
    setupSurfaceView();
    if (millisToResume  >= 0) {
      exoplayer.seekTo(millisToResume);
    }
    if (stateToResume == OoyalaPlayer.State.PLAYING) {
      exoplayer.setPlayWhenReady(true);
    } else {
      setState(stateToResume);
    }
  }
}
