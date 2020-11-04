package com.bkav.musicapplication.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bkav.musicapplication.R;
import com.bkav.musicapplication.activity.MainActivity;
import com.bkav.musicapplication.activity.MediaPlaybackActivity;
import com.bkav.musicapplication.object.Song;

import java.util.ArrayList;

public class BaseFragment extends Fragment {

    private MainActivity mMainActivity;

    private RelativeLayout mSmallPlayingAreaRelativeLayout;
    private RelativeLayout mSmallPlayRelativeLayout;   //Relative to display Playing area
    private ImageView mSongImageView;
    private TextView mCurrentSongNameTextView;
    private TextView mCurrentArtistNameTextView;
    private ImageButton mPlayMediaImageButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMainActivity = (MainActivity) getActivity();
    }

    /**
     * Tuantqd
     * Initial view byViewFindId
     * *@param view
     */
    public void initView(View view) {
        mSmallPlayRelativeLayout = view.findViewById(R.id.small_playing_area);
        //Set onClick for SmallPlayRelativeLayout
        mSmallPlayRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Dung 2 Activity
                Intent intent = new Intent(getActivity().getApplicationContext(), MediaPlaybackActivity.class);
                startActivity(intent);
            }
        });
        mSongImageView = view.findViewById(R.id.small_song_imageview);
        mCurrentSongNameTextView = view.findViewById(R.id.small_name_current_song);
        mCurrentArtistNameTextView = view.findViewById(R.id.small_singer_name_current_song);
        mPlayMediaImageButton = view.findViewById(R.id.small_play_imagebutton);
        //Set onClick for Play/Pause ImageButton
        mPlayMediaImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMainActivity.getmMediaService().getmMediaPlayer().isPlaying()) {
                    mMainActivity.getmMediaService().pauseMedia();
                    mPlayMediaImageButton.setImageResource(R.drawable.ic_media_play_light);
                } else {
                    mMainActivity.getmMediaService().resumeMedia();
                    mPlayMediaImageButton.setImageResource(R.drawable.ic_media_pause_light);
                }
            }
        });
        mSmallPlayingAreaRelativeLayout = view.findViewById(R.id.small_playing_area);
    }

    /**
     * Tuantqd
     * Show Small Playing Area
     */
    public void showSmallPlayingArea() {
        if (mSmallPlayingAreaRelativeLayout.getVisibility() == View.GONE) {
            mSmallPlayingAreaRelativeLayout.setVisibility(View.VISIBLE);
        }

    }

    /**
     * Tuantqd
     * Update UI for SmallPlayingRelativeLayout on AllSongFragment
     */
    public void upDateSmallPlayingRelativeLayout(ArrayList<Song> mListSongAdapter,
                                                 RecyclerView.Adapter mSongAdapter,
                                                 RecyclerView mRecyclerView) {
        if (mMainActivity.getmMediaService().getmMediaPlayer() != null) {
            Song song =  mMainActivity.getmMediaService().getCurrentSong();
            //Update Song Name, Artist Name, Play Button
            mCurrentSongNameTextView.setText(song.getmTitle());
            mCurrentArtistNameTextView.setText(song.getmArtistName());
            if (mMainActivity.getmMediaService().getmMediaPlayer().isPlaying()) {
                mPlayMediaImageButton.setImageResource(R.drawable.ic_media_pause_light);
            } else {
                mPlayMediaImageButton.setImageResource(R.drawable.ic_media_play_light);
            }

            //Update AlbumArt
            mSongImageView.setImageURI(Song.queryAlbumUri(
                    song.getmAlbumID()));
            if (mSongImageView.getDrawable() == null) {
                mSongImageView.setImageResource(R.drawable.ic_reason_album);
            }
            //Update UI for Adapter
            mSongAdapter.notifyDataSetChanged();
            //Scroll to playing position
            mRecyclerView.smoothScrollToPosition(findSongPosition(mListSongAdapter, song));
        }
    }

    /**
     * Tuantqd
     * Function to find Song's position in list
     * @param mListSongAdapter
     * @param song
     * @return
     */
    private int findSongPosition(ArrayList<Song> mListSongAdapter, Song song){
        for(int i = 0; i < mListSongAdapter.size(); i++){
            if(song.getmTitle().equals(mListSongAdapter.get(i).getmTitle())){
                return i;
            }
        }
        return 0;
    }
}
