package com.bkav.musicapplication.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bkav.musicapplication.activity.MediaPlaybackActivity;
import com.bkav.musicapplication.enumdefine.MediaStatus;
import com.bkav.musicapplication.R;
import com.bkav.musicapplication.object.Song;
import com.bkav.musicapplication.contentprovider.SongProvider;
import com.bkav.musicapplication.service.MediaPlaybackService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Show playing music content
 */
public class MediaPlaybackFragment extends Fragment
        implements SeekBar.OnSeekBarChangeListener {

    public static final int MEDIA_DEFAULT_POSITION = -1;
    public static final int REPEAT_DEFAULT_VALUE = -1;
    public static final int NO_REPEAT = 0;
    public static final int REPEAT_ALL = 1;
    public static final int REPEAT_ONE = 2;
    public static final boolean SHUFFLE = true;
    public static final boolean NO_SHUFFLE = false;
    public static final String REPEAT_STATUS = "Repeat_status";     //Use when SharedPreferences
    public static final String SHUFFLE_STATUS = "Shuffle status";   //Use when SharedPreferences
    public static final String REPEAT_AND_SHUFFLE_SAVE = "Repeat_and_Shuffle_status";

    //Conponents of Fragment
    private RelativeLayout mMediaFragmentRelativeLayout;
    private ImageView mSongAlbumImageView;
    private TextView mSongNameTextView;
    private TextView mArtistNameTextView;
    private TextView mCurrentTimeTextView;
    private TextView mTotalTimeTextView;
    private ImageButton mShuffleImageButton;
    private ImageButton mRepeatImageButton;
    private ImageButton mBackToAllSongImageButton;
    //Se bo sung chuc nang an like (dislike)
    // de them (xoa) bai hat trong Favorite Song List
    private ImageButton mUnlikeImageButton;
    private ImageButton mLikeImageButton;
    private ImageButton mPreviousImageButton;
    private ImageButton mNextImageButton;
    private ImageButton mPlayImageButton;
    private SeekBar mSongSeekBar;

    //Get MediaPlayBack activity
    private MediaPlaybackActivity mMediaPlaybackActivity;

    //Get MediaService
    MediaPlaybackService mMediaPlaybackService;

    //Get List all song
    private ArrayList<Song> mListAllSong;

    //Check to repeat
    private int mIsRepeat;

    //Check to shuffle
    private boolean mIsShuffle;

    //Media Status
    MediaStatus mMediaStatus = MediaStatus.NONE;

    //SimpleDateFormat to format song time
    SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("mm:ss");

    //Variables to check when have to update UI
    private int mSongPosition = MEDIA_DEFAULT_POSITION;
    boolean mIsPlay = false;

    //TODO:Handler object to do update current play time - is a thread, send message each delay time
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            //Update Current time to TextView
            mCurrentTimeTextView.setText(
                    mSimpleDateFormat.format(
                            mMediaPlaybackService.getmMediaPlayer().getCurrentPosition()));
            //Update Current Progress to SeekBar
            mSongSeekBar.setProgress(
                    mMediaPlaybackService.getmMediaPlayer().getCurrentPosition());
            //Update View when next Song
            if (mSongPosition != mMediaPlaybackService.getmMediaPosition()
                    || mIsPlay != mMediaPlaybackActivity.getmMediaService().getmMediaPlayer().isPlaying()) {
                upDateInfoView();
                mSongPosition = mMediaPlaybackService.getmMediaPosition();
                mIsPlay = mMediaPlaybackActivity.getmMediaService().getmMediaPlayer().isPlaying();
            }
            Message message = new Message();
//                  message.arg1 = position;
            sendMessageDelayed(message, 500);
//                }
//            }

        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.media_playback_fragment, container, false);

        //Get MediaPlaybackActivity object
        mMediaPlaybackActivity = ((MediaPlaybackActivity) getActivity());

        //Create Service object
        mMediaPlaybackService = mMediaPlaybackActivity.getmMediaService();

//        //Get list song
//        mListAllSong = mMediaPlaybackService.getListSongService();

        //initial components view
        initialView(view);
        //Set info to view
        upDateInfoView();
        //Return Fragment view
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences =
                mMediaPlaybackActivity
                        .getSharedPreferences(REPEAT_AND_SHUFFLE_SAVE, Context.MODE_PRIVATE);

        //Get status for repeat and shuffle button
        mIsRepeat = sharedPreferences.getInt(REPEAT_STATUS, REPEAT_DEFAULT_VALUE);
        mIsShuffle = sharedPreferences.getBoolean(SHUFFLE_STATUS, false);

        if (mIsRepeat == REPEAT_ALL) {
            mRepeatImageButton.setImageResource(R.drawable.ic_repeat_dark_selected);
        } else if (mIsRepeat == REPEAT_ONE) {
            mRepeatImageButton.setImageResource(R.drawable.ic_repeat_one_song_dark);
        } else {
            mRepeatImageButton.setImageResource(R.drawable.ic_repeat_white);
        }

        if (mIsShuffle == SHUFFLE) {
            mShuffleImageButton.setImageResource(R.drawable.ic_play_shuffle_orange_noshadow);
        } else {
            mShuffleImageButton.setImageResource(R.drawable.ic_shuffle_white);
        }

        //Update media status
        mMediaPlaybackService.setmMediaStatus(getMediaStatus());
    }

    /**
     * Tuantqd
     * Update View by SongPosition
     */
    private void upDateInfoView() {
        //upDateTimeSong();

        if (mMediaPlaybackActivity.getmMediaService().getmMediaPlayer() != null) {
            Message message = new Message();
//          message.arg1 = mMediaPlaybackService.getmMediaPlayer().getCurrentPosition();
            mHandler.sendMessage(message);

            if (mMediaPlaybackService.getmMediaPlayer().isPlaying()) {
                mPlayImageButton.setImageResource(R.mipmap.ic_media_pause_light);
            } else {
                mPlayImageButton.setImageResource(R.mipmap.ic_media_play_light);
            }

            //Get song position
            int songPositon = mMediaPlaybackService.getmMediaPosition();
            //Set song name view
            mSongNameTextView.setText(mMediaPlaybackService.getCurrentSong().getmTitle());
            //Set artist name view
            mArtistNameTextView.setText(mMediaPlaybackService.getCurrentSong().getmArtistName());
            //Set Album Art view
            mSongAlbumImageView.setImageURI(Song.queryAlbumUri(
                    mMediaPlaybackService.getCurrentSong().getmAlbumID()));
            if (mSongAlbumImageView.getDrawable() == null) {
                mSongAlbumImageView.setImageResource(R.drawable.ic_reason_album);
                mMediaFragmentRelativeLayout.setBackgroundResource(R.drawable.ic_reason_album);
            } else {
                mMediaFragmentRelativeLayout.setBackground(mSongAlbumImageView.getDrawable());
            }

            //Set total time view
            mTotalTimeTextView.setText(mMediaPlaybackService.getCurrentSong().getmDurationString());
            mSongSeekBar.setMax(mMediaPlaybackService.getCurrentSong().getmDuration());

        }
    }

    /**
     * Tuantqd
     * Initial FindViewbyID
     *
     * @param view
     */
    private void initialView(View view) {
        //Work with SeekBar
        mSongSeekBar = view.findViewById(R.id.seek_bar_play_song);
        //Lang nghe su kien cho SeekBar
        mSongSeekBar.setOnSeekBarChangeListener(this);

        //Find view by id
        mMediaFragmentRelativeLayout = view.findViewById(R.id.seek_bar_media_playback_area);
        mSongAlbumImageView = view.findViewById(R.id.media_album_small_image);
        mArtistNameTextView = view.findViewById(R.id.media_name_singer_current);
        mSongNameTextView = view.findViewById(R.id.media_name_song_current);
        mBackToAllSongImageButton = view.findViewById(R.id.media_allsong_button);
        /*Set onClick for backToAllSongImageButton*/
        mBackToAllSongImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMediaPlaybackActivity.finish();
            }
        });

        mCurrentTimeTextView = view.findViewById(R.id.current_time_playing);
        mTotalTimeTextView = view.findViewById(R.id.total_time_playing);
        mShuffleImageButton = view.findViewById(R.id.media_shuffle_button);
        /*Set onClick for mShuffleImageButton*/
        mShuffleImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsShuffle == NO_SHUFFLE) {
                    mIsShuffle = SHUFFLE;
                    mShuffleImageButton.setImageResource(R.drawable.ic_play_shuffle_orange_noshadow);
                } else {
                    mIsShuffle = NO_SHUFFLE;
                    mShuffleImageButton.setImageResource(R.drawable.ic_shuffle_white);
                }

                //Update media status
                mMediaPlaybackService.setmMediaStatus(getMediaStatus());
            }
        });

        mRepeatImageButton = view.findViewById(R.id.media_repeat_button);
        /*Set onClick for Repeat Button*/
        mRepeatImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsRepeat == NO_REPEAT) {
                    mIsRepeat = REPEAT_ALL;
                    mRepeatImageButton.setImageResource(R.drawable.ic_repeat_dark_selected);
                } else if (mIsRepeat == REPEAT_ALL) {
                    mIsRepeat = REPEAT_ONE;
                    mRepeatImageButton.setImageResource(R.drawable.ic_repeat_one_song_dark);
                } else {
                    mIsRepeat = NO_REPEAT;
                    mRepeatImageButton.setImageResource(R.drawable.ic_repeat_white);
                }
                //Update media play status
                mMediaPlaybackService.setmMediaStatus(getMediaStatus());
            }
        });

        mUnlikeImageButton = view.findViewById(R.id.media_dislike_button);
        mLikeImageButton = view.findViewById(R.id.media_like_button);
        mPreviousImageButton = view.findViewById(R.id.media_prev_button);
        /*Set onClick for Previous Button*/
        mPreviousImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMediaPlaybackService.previousMedia();
                mPlayImageButton.setImageResource(R.mipmap.ic_media_pause_light);
                upDateInfoView();
            }
        });

        mNextImageButton = view.findViewById(R.id.media_next_button);
        /*Set onClick for NextButton*/
        mNextImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMediaPlaybackService.nextMedia();
                mPlayImageButton.setImageResource(R.mipmap.ic_media_pause_light);
                upDateInfoView();

            }
        });

        mPlayImageButton = view.findViewById(R.id.media_play_button);
        /*Set onClick for PlayImageButton*/
        mPlayImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMediaPlaybackService.getmMediaPlayer().isPlaying()) {
                    mMediaPlaybackService.pauseMedia();
                    mPlayImageButton.setImageResource(R.mipmap.ic_media_play_light);
                } else {
                    mMediaPlaybackService.resumeMedia();
                    mPlayImageButton.setImageResource(R.mipmap.ic_media_pause_light);
                }
            }
        });
    }

    /**
     * Tuantqd
     * SeekBar Progress
     *
     * @param seekBar
     * @param progress
     * @param fromUser
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    /**
     * Tuantqd
     * Start Change progress of SeekBar
     *
     * @param seekBar
     */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    /**
     * Tuantqd
     * Stop change Progress of SeekBar
     *
     * @param seekBar
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mMediaPlaybackService.getmMediaPlayer().seekTo(mSongSeekBar.getProgress());
    }

    /**
     * Tuantqd
     * Update CurrentTime for SeekBar
     * (Copy: KhoaPham) - Dont use
     */
    private void upDateTimeSong() {
//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
//                //Update Current time to TextView
//                mCurrentTimeTextView.setText(
//                        simpleDateFormat.format(
//                                mMediaPlaybackService.getmMediaPlayer().getCurrentPosition()));
//
//                //Auto next Song
//                if(mMediaPlaybackService.getmMediaPlayer().getCurrentPosition() >=
//                        (mMediaPlaybackService.getmMediaPlayer().getDuration() - 200)){
//                    mMediaPlaybackService.nextMedia();
//                    upDateInfoView();
//                }
//                //Update Current Progress to SeekBar
//                mSongSeekBar.setProgress(
//                        mMediaPlaybackService.getmMediaPlayer().getCurrentPosition());
//                //CallBack
//                mHandler.postDelayed(this, 200);
//            }
//        }, 100);
    }


    /**
     * Tuantqd
     * Get media status (shuffle, repeat)
     *
     * @return
     */
    private MediaStatus getMediaStatus() {
        if (mIsShuffle == NO_SHUFFLE && mIsRepeat == NO_REPEAT) {
            mMediaStatus = MediaStatus.NONE;
        } else if (mIsShuffle == NO_SHUFFLE && mIsRepeat == REPEAT_ALL) {
            mMediaStatus = MediaStatus.REPEAT_ALL;
        } else if (mIsShuffle == NO_SHUFFLE && mIsRepeat == REPEAT_ONE) {
            mMediaStatus = MediaStatus.REPEAT_ONE;
        } else if (mIsShuffle == SHUFFLE && mIsRepeat == NO_REPEAT) {
            mMediaStatus = MediaStatus.SHUFFLE;
        } else if (mIsShuffle == SHUFFLE && mIsRepeat == REPEAT_ALL) {
            mMediaStatus = MediaStatus.REPEAT_AND_SHUFFLE;
        } else if (mIsShuffle == SHUFFLE && mIsRepeat == REPEAT_ONE) {
            mMediaStatus = MediaStatus.REPEAT_ONE_AND_SHUFFLE;
        }
        return mMediaStatus;
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences sharedPreferences =
                mMediaPlaybackActivity.getSharedPreferences(
                        REPEAT_AND_SHUFFLE_SAVE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SHUFFLE_STATUS, mIsShuffle);
        editor.putInt(REPEAT_STATUS, mIsRepeat);
        editor.commit();

    }

}
