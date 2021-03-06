/*******************************************************************************
 * Copyright
 *  This code is strictly confidential and the receiver is obliged to use it
 *  exclusively for his or her own purposes. No part of Viaccess Orca code may be
 *  reproduced or transmitted in any form or by any means, electronic or
 *  mechanical, including photocopying, recording, or by any information storage
 *  and retrieval system, without permission in writing from Viaccess Orca.
 *  The information in this code is subject to change without notice. Viaccess Orca
 *  does not warrant that this code is error free. If you find any problems
 *  with this code or wish to make comments, please report them to Viaccess Orca.
 *  
 *  Trademarks
 *  Viaccess Orca is a registered trademark of Viaccess S.A in France and/or other
 *  countries. All other product and company names mentioned herein are the
 *  trademarks of their respective owners.
 *  Viaccess S.A may hold patents, patent applications, trademarks, copyrights
 *  or other intellectual property rights over the code hereafter. Unless
 *  expressly specified otherwise in a Viaccess Orca written license agreement, the
 *  delivery of this code does not imply the concession of any license over
 *  these patents, trademarks, copyrights or other intellectual property.
 *******************************************************************************/

package com.example.csp.apis;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.discretix.vodx.VODXPlayer;
import com.example.csp.CspConstants;
import com.example.csp.R;
import com.visualon.OSMPPlayer.VOCommonPlayerListener;
import com.visualon.OSMPPlayer.VOOSMPOpenParam;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_RETURN_CODE;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_SRC_FLAG;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_SRC_FORMAT;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Represents the control elements of the player. It receives Events from both sides, the VOPlayer
 * and the user (via {@link SeekBar} and coordinate the media playback.
 */
public class PlaybackControl extends LinearLayout
        implements SeekBar.OnSeekBarChangeListener, VOCommonPlayerListener {

    private Context mContext = null;
    /* Messages for managing the user interface */
    private static final int MSG_SHOW_CONTROLLER = 1;
    private static final int MSG_HIDE_CONTROLLER = 2;
    private static final int MSG_UPDATE_UI = 3;
    private static final int MSG_PLAYCOMPLETE = 5;

    /* Media controls and User interface */
    private ImageButton mBtnPause;
    private SeekBar mSeekBar;
    private LinearLayout mLayoutTime;
    private LinearLayout mLayoutControls;
    private TextView mPositionText;
    private TextView mDurationText;
    private float mAudioSpeed = 1.0f;
    private ImageButton mBtnHighSpeedAudio = null;
    private ImageButton mBtnLowSpeed = null;
    private TextView mAudioSpeedTxt = null;
    private Button mBtnZapping = null;

    /** Wait icon for buffered or stopped video. */
    private ProgressBar mWaitIcon = null;
    /** Last update of media controls. */
    private Date mLastUpdateTime;
    /** Timer for display of media controls. */
    private Timer mTimer = null;
    /** Timer for display of media controls. */
    private TimerTask tmTask = null;
    /** {@link VODXPlayer} media player. */
    private VODXPlayer mPlayer = null;

    /** Seekbar traking flag */
    private boolean mTrackProgressing = false;
    /** Pause flag */
    private boolean mIsPaused = false;
    /** Stop flag */
    private boolean mIsStop = false;

    /** Total content duration */
    private long mDuration;
    /** Current position */
    public long mPos = 0;

    private Handler handler = new Handler() {
        /* Handler to manage user interface during playback */
        public void handleMessage(Message msg) {

            if (mPlayer == null)
                return;

            if (msg.what == MSG_SHOW_CONTROLLER) {
                /* Show media controls */
                showMediaControllerImpl();
            } else if (msg.what == MSG_HIDE_CONTROLLER) {
                /* Hide media controls */
                hideControllerImpl();
            } else if (msg.what == MSG_UPDATE_UI) {
                // Update UI
                // If the controls are not visible do not update
                if (mLayoutControls.getVisibility() == View.GONE)
                    return;

                /* If the player is stopped do not update */
                if (mIsStop)
                    return;

                /* If the user is dragging the progress bar do not update */
                if (mTrackProgressing)
                    return;

                Date timeNow = new Date(System.currentTimeMillis());
                long timePeriod = (timeNow.getTime() - mLastUpdateTime.getTime()) / 1000;
                if (timePeriod >= 5 && mIsPaused == false) {
                    /*
                     * If the media is being played back, hide media controls after 5 seconds if
                     * unused
                     */
                    hideController();
                } else {
                    /*
                     * Update the Seek-bar and Time display with current position
                     */
                    mPos = mPlayer.getPosition();
                    String str;
                    if (mPlayer.isLiveStreaming()) {
                        long totlaBuff = Math.abs(mPlayer.getMinPosition());
                        if (totlaBuff == 0) {
                            mSeekBar.setProgress(0);
                        } else {
                            // the mPos is a negative value here
                            int seekBarTo = (int) ((totlaBuff + mPos) / (totlaBuff / 100));
                            if (seekBarTo < 0) { // The position of the player
                                                 // can be a few seconds
                                                 // before the DVR window
                                                 // (But the seek bar can't).
                                seekBarTo = 0;
                            }
                            mSeekBar.setProgress(seekBarTo);
                        }
                        str = DateUtils.formatElapsedTime(totlaBuff / 1000);
                        mPositionText.setText("-" + str);
                    } else if (mDuration > 0) {
                        mSeekBar.setProgress((int) (mPos / (mDuration / 100)));
                        str = DateUtils.formatElapsedTime(mPos / 1000);
                        mPositionText.setText(str);
                    }
                }
            } else if (msg.what == MSG_PLAYCOMPLETE) {
                /* Playback in complete, stop player */
                if (mPlayer != null) {
                    mPlayer.pause();
                }
            }
        }
    };

    /** Basic object constructor */
    public PlaybackControl(Context context) {
        super(context);
        mContext = context;
        init();
    }

    /** Constructor with attributes */
    public PlaybackControl(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    /** Called when the activity is first created. */
    public void init() {
        Log.i(CspConstants.TAG, "CspVoExamplePlayerView onCreate");

        LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
        this.setLayoutParams(layoutParams);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.player, null, false);
        this.addView(v, layoutParams);

        // Control bar UI components
        mBtnPause = (ImageButton) findViewById(R.id.pause);
        mSeekBar = (SeekBar) findViewById(R.id.SeekBar01);
        mLayoutControls = (LinearLayout) findViewById(R.id.layout1);
        mLayoutTime = (LinearLayout) findViewById(R.id.layout2);
        mPositionText = (TextView) findViewById(R.id.CurrentPosition);
        mDurationText = (TextView) findViewById(R.id.Duration);
        mWaitIcon = (ProgressBar) findViewById(R.id.pbBuffer);

        mSeekBar.setOnSeekBarChangeListener(this);

        mBtnZapping = (Button) findViewById(R.id.zapButton);
        mBtnZapping.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // This flow should be use when the objective is to change the
                // stream source during playback.
                // *In this example we use the same stream for demonstration
                // purposes only.
                if (mPlayer != null) {
                    // Set icon to Pause
                    mBtnPause.setImageResource(R.drawable.pause);
                    mIsPaused = false;

                    VO_OSMP_RETURN_CODE nRet = VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE;
                    nRet = mPlayer.stop();
                    if (nRet != VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE) {
                        Log.e(CspConstants.TAG, "Zap sequence - STOP failed " + nRet);
                    }
                    nRet = mPlayer.close();
                    if (nRet != VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE) {
                        Log.e(CspConstants.TAG, "Zap sequence - CLOSE failed " + nRet);
                    }
                    VOOSMPOpenParam openParam = new VOOSMPOpenParam();
                    String mPlaybackPath = CspConstants.getActiveContent().getPlayBackPath();
                    nRet = mPlayer.open(mPlaybackPath, VO_OSMP_SRC_FLAG.VO_OSMP_FLAG_SRC_OPEN_ASYNC,
                            VO_OSMP_SRC_FORMAT.VO_OSMP_SRC_AUTO_DETECT, openParam);
                    if (nRet != VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE) {
                        Log.e(CspConstants.TAG, "Zap sequence - OPEN failed " + nRet);
                    }
                    // The Start function will execute on the
                    // VO_OSMP_SRC_CB_OPEN_FINISHED event.
                }

            }
        });
        mBtnHighSpeedAudio = (ImageButton) findViewById(R.id.ibHighSpeed);
        mBtnLowSpeed = (ImageButton) findViewById(R.id.ibLowSpeed);
        mAudioSpeedTxt = (TextView) findViewById(R.id.tvAudioSpeed);
        mAudioSpeedTxt.setText("1.0");
        mBtnHighSpeedAudio.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                mAudioSpeed += 0.1f;
                if (mAudioSpeed > 4.0f)
                    mAudioSpeed = 4.0f;
                updateAudioSpeed();
            }
        });
        mBtnLowSpeed.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                mAudioSpeed -= 0.1f;
                if (mAudioSpeed < 0.5f)
                    mAudioSpeed = 0.5f;
                updateAudioSpeed();

            }
        });

        mLayoutControls.setVisibility(View.GONE);
        mLayoutTime.setVisibility(View.GONE);
        mWaitIcon.setVisibility(View.VISIBLE);

        // Activate listener for Play/Pause button
        mBtnPause.setOnClickListener(new ImageButton.OnClickListener() {
            public void onClick(View view) {
                playerPauseRestart();
            }
        });
    }

    /**
     * Updates audio speed to the one set by user.
     */
    private void updateAudioSpeed() {

        mAudioSpeedTxt.setText(String.format("%.1f", mAudioSpeed));
        if (mPlayer != null) {
            VO_OSMP_RETURN_CODE ret = mPlayer.setAudioPlaybackSpeed(mAudioSpeed);

            if (ret != VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE) {
                Log.i(CspConstants.TAG, "It is not possible to set audio speed to " + mAudioSpeed);
                mAudioSpeedTxt.setText("1.0");
                mAudioSpeed = 1.0f;
            }
        }
    }

    /**
     * Sets media player and register as a listener.
     */
    void setPlayer(VODXPlayer player) {
        mPlayer = player;
        if (mPlayer != null)
            mPlayer.setOnEventListener(this);
    }

    /**
     * Shows media controller. Implemented by showMediaControllerImpl(), called by handler.
     */
    public void showMediaController() {
        if (mPlayer == null)
            return;

        handler.sendEmptyMessage(MSG_SHOW_CONTROLLER);
    }

    /**
     * Hides media controller. Implemented by showMediaControllerImpl(), called by handler.
     */
    public void hideController() {
        handler.sendEmptyMessage(MSG_HIDE_CONTROLLER);
    }

    /**
     * Hides media controller implementation.
     */
    private void hideControllerImpl() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
            tmTask = null;
        }

        mLayoutControls.setVisibility(View.GONE);
        mLayoutTime.setVisibility(View.GONE);
    }

    /**
     * Show media controller implementation
     */
    private void showMediaControllerImpl() {
        Log.i(CspConstants.TAG, "Touch screen, layout status is " + mLayoutControls.getVisibility());
        mLastUpdateTime = new Date(System.currentTimeMillis()); // Get current system time

        if (mLayoutControls.getVisibility() == View.GONE) {
            Log.i(CspConstants.TAG, "mIsStop is " + mIsStop);

            if ((mIsStop == false)) {
                // Get duration in milliseconds and set duration text in
                // HH:MM:SS
                mDuration = mPlayer.getDuration();
                mPos = 0;
                String str = DateUtils.formatElapsedTime(mDuration / 1000);
                if (mPlayer.isLiveStreaming()) {
                    mDurationText.setText("LIVE");
                } else {
                    mDurationText.setText(str);
                }
                Log.i(CspConstants.TAG, String.format("Duration is %s.", str));
            }

            // Schedule next UI update in 200 milliseconds
            if (tmTask != null) {
                tmTask = null;
            }
            tmTask = new TimerTask() {
                public void run() {
                    handler.sendEmptyMessage(MSG_UPDATE_UI);
                }
            };
            if (mTimer == null) {
                mTimer = new Timer();
            }
            mTimer.schedule(tmTask, 0, 200);

            // Show controls
            mLayoutControls.setVisibility(View.VISIBLE);
            mLayoutTime.setVisibility(View.VISIBLE);
            mLayoutTime.bringToFront();

            Log.i(CspConstants.TAG, "mLayoutTime show " + mLayoutTime.getVisibility());
        }
    }

    /**
     * Shows media controls on activity touch event.
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        showMediaController();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        showMediaController();
        return super.onTouchEvent(event);
    }

    /**
     * Flags when Seekbar is being dragged.
     */
    @Override
    public void onStartTrackingTouch(SeekBar arg0) {
        mTrackProgressing = true;
    }

    /**
     * Seeks to new position when Seekbar drag is complete.
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (mTrackProgressing && !mPlayer.isLiveStreaming()) {
            int currentPos = (int) (progress * mDuration / mSeekBar.getMax());
            String str = DateUtils.formatElapsedTime(currentPos / 1000);
            mPositionText.setText(str);
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar arg0) {
        // Calculate new position as percenCspConstants.TAGe of total duration
        int iCurrent = arg0.getProgress();
        int iMax = arg0.getMax();
        int iNewPosition;

        double SeekBarPos = (double) iCurrent / iMax;

        if (mPlayer.isLiveStreaming()) {
            iNewPosition = (int) ((1 - SeekBarPos) * mPlayer.getMinPosition());
            mTrackProgressing = false; // Disable Seekbar tracking flag
        } else {
            iNewPosition = (int) (SeekBarPos * mDuration);
            mTrackProgressing = false; // Disable Seekbar tracking flag
        }
        if (mPlayer != null) {
            Log.i(CspConstants.TAG, "Seek To " + iNewPosition);
            long maxPos = mPlayer.getMaxPosition();
            long minPos = mPlayer.getMinPosition();
            if (iNewPosition > maxPos || iNewPosition < minPos) {
                Log.e(CspConstants.TAG, "Seek out of bounds [" + minPos + ", " + maxPos + "]");
            } else {
                mPlayer.setPosition(iNewPosition); // Set new position
            }
        }

        // Reset time to hide media controls.
        mLastUpdateTime = new Date(System.currentTimeMillis());
    }

    /**
     * Toggle Play/Pause button display and functionality
     */
    private void playerPauseRestart() {
        if (mPlayer != null) {
            if (mIsPaused == false) {
                // If playing, pause media pipeline, show media controls, and
                // change button to the <Play> icon
                mPlayer.pause();
                showMediaController();
                mBtnPause.setImageResource(R.drawable.play_button);
                mIsPaused = true;
            } else if (mIsPaused == true) {
                // Else, play media pipeline and change button to the <Pause>
                // icon
                mPlayer.start();
                mBtnPause.setImageResource(R.drawable.pause);
                mIsPaused = false;
            }
            updateAudioSpeed();
        }
    }

    @Override
    public VO_OSMP_RETURN_CODE onVOEvent(VO_OSMP_CB_EVENT_ID nID, int nParam1, int nParam2,
            Object obj) {
        switch (nID) {
            case VO_OSMP_SRC_CB_ADAPTIVE_STREAMING_INFO: {
                VO_OSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT event = VO_OSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT
                        .valueOf(nParam1);
                switch (event) {
                    case VO_OSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_BITRATE_CHANGE: {
                        Log.v(CspConstants.TAG,
                                "OnEvent VOOSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_BITRATE_CHANGE, param2 is "
                                        + nParam2);
                        break;
                    }
                    case VO_OSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_MEDIATYPE_CHANGE: {
                        Log.v(CspConstants.TAG,
                                "OnEvent VOOSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_MEDIATYPE_CHANGE, param2 is "
                                        + nParam2);
                        VO_OSMP_AVAILABLE_TRACK_TYPE type = VO_OSMP_AVAILABLE_TRACK_TYPE
                                .valueOf(nParam2);
                        switch (type) {
                            case VO_OSMP_AVAILABLE_PUREAUDIO: {
                                Log.v(CspConstants.TAG,
                                        "OnEvent VOOSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_MEDIATYPE_CHANGE, VOOSMP_AVAILABLE_PUREAUDIO");
                                break;
                            }
                            case VO_OSMP_AVAILABLE_PUREVIDEO: {
                                Log.v(CspConstants.TAG,
                                        "OnEvent VOOSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_MEDIATYPE_CHANGE, VOOSMP_AVAILABLE_PUREVIDEO");
                                break;
                            }
                            case VO_OSMP_AVAILABLE_AUDIOVIDEO: {
                                Log.v(CspConstants.TAG,
                                        "OnEvent VOOSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_MEDIATYPE_CHANGE, VOOSMP_AVAILABLE_AUDIOVIDEO");
                                break;
                            }
                            default:
                                break;
                        }
                        break;
                    }
                    default:
                        break;
                }
                break;
            }
            case VO_OSMP_CB_PLAY_COMPLETE: {
                handler.sendEmptyMessage(MSG_PLAYCOMPLETE);
                break;
            }
            case VO_OSMP_CB_SEEK_COMPLETE: // Seek (SetPos) complete
            {
                break;
            }
            case VO_OSMP_CB_AUDIO_RENDER_START: // Audio render start
            {
                mWaitIcon.setVisibility(View.GONE); // Hide wait icon
                break;
            }
            case VO_OSMP_CB_VIDEO_RENDER_START: // Video render start
            {
                mWaitIcon.setVisibility(View.GONE); // Hide wait icon
                break;
            }
            case VO_OSMP_CB_VIDEO_STOP_BUFFER: // Video buffering stopped
            {
                mWaitIcon.setVisibility(View.GONE); // Hide wait icon
                break;
            }
            case VO_OSMP_CB_VIDEO_START_BUFFER: // Video buffering started
            {
                mWaitIcon.setVisibility(View.VISIBLE); // Show wait icon
                break;
            }
            case VO_OSMP_CB_AUDIO_STOP_BUFFER: // Audio buffering stopped
            {
                mWaitIcon.setVisibility(View.GONE); // Hide wait icon
                break;
            }
            case VO_OSMP_CB_AUDIO_START_BUFFER: // Audio buffering started
            {
                mWaitIcon.setVisibility(View.VISIBLE); // Show wait icon
                break;
            }
            case VO_OSMP_SRC_CB_BA_HAPPENED: // Unimplemented
            {
                Log.v(CspConstants.TAG, "OnEvent VOOSMP_SRC_CB_BA_Happened, param is " + nParam1);
                break;
            }
            case VO_OSMP_SRC_CB_DOWNLOAD_FAIL_WAITING_RECOVER: {
                Log.v(CspConstants.TAG,
                        "OnEvent VOOSMP_SRC_CB_Download_Fail_Waiting_Recover, param is " + nParam1);
                break;
            }
            case VO_OSMP_SRC_CB_DOWNLOAD_FAIL_RECOVER_SUCCESS: {
                Log.v(CspConstants.TAG,
                        "OnEvent VOOSMP_SRC_CB_Download_Fail_Recover_Success, param is " + nParam1);
                break;
            }
            case VO_OSMP_SRC_CB_OPEN_FINISHED: {
                Log.v(CspConstants.TAG, "OnEvent VOOSMP_SRC_CB_Open_Finished, param is " + nParam1);
                Log.i(CspConstants.TAG, "Starting playback");
                mPlayer.start();
                break;
            }
            default:
                break;
        }
        return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE;

    }

    @Override
    public VO_OSMP_RETURN_CODE onVOSyncEvent(VO_OSMP_CB_SYNC_EVENT_ID arg0, int arg1, int arg2,
            Object arg3) {

        return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE;
    }
}
