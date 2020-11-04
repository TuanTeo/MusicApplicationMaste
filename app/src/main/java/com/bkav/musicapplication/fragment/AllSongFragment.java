package com.bkav.musicapplication.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bkav.musicapplication.R;
import com.bkav.musicapplication.activity.MainActivity;
import com.bkav.musicapplication.activity.MediaPlaybackActivity;
import com.bkav.musicapplication.favoritesongdatabase.DataBase;
import com.bkav.musicapplication.object.Song;
import com.bkav.musicapplication.adapter.SongAdapter;
import com.bkav.musicapplication.contentprovider.FavoriteSongProvider;
import com.bkav.musicapplication.contentprovider.SongProvider;

import java.util.ArrayList;


/**
 * Create by: Tuantqd
 * Display Songs List
 */
public class AllSongFragment extends BaseFragment {

    public static final String SMALL_PLAYING_VISIBLE = "small playing visible";
    private MainActivity mMainActivity;
//    private RelativeLayout mSmallPlayingAreaRelativeLayout;
//    private RelativeLayout mSmallPlayRelativeLayout;   //Relative to display Playing area
//    private ImageView mSongImageView;
//    private TextView mCurrentSongNameTextView;
//    private TextView mCurrentArtistNameTextView;
//    private ImageButton mPlayMediaImageButton;
    private ArrayList<Song> mListSongAdapter;  //song List object
    private SongAdapter mSongAdapter;   //song Adapter object
    private RecyclerView mRecyclerView; //Recycleview object

    //Variables to check when have to update UI
    private int mSongPosition = -1;
    boolean mIsPlay = false;

    private Handler mHandler = new Handler() {   //Handle object as a Thread

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (mSongPosition != mMainActivity.getmMediaService().getmMediaPosition()
                    || mIsPlay != mMainActivity.getmMediaService().getmMediaPlayer().isPlaying()) {
                upDateSmallPlayingRelativeLayout(mListSongAdapter, mSongAdapter, mRecyclerView);
                mSongAdapter.notifyDataSetChanged();
            }

            mSongPosition = mMainActivity.getmMediaService().getmMediaPosition();
            mIsPlay = mMainActivity.getmMediaService().getmMediaPlayer().isPlaying();
            Message message = new Message();
            sendMessageDelayed(message, 500);
        }
    };

    /**
     * Tuantqd
     * Create all Items RecycleView
     *
     * @param view
     */
    public void createRecycleView(View view) {
        //Read all Song with SongProvider
        mListSongAdapter = SongProvider.getInstance(getActivity().getApplicationContext()).getmListSong();

        //Get Favorite Song with FavoriteProvider
//        mListSongAdapter = getFavoriteSong();

        mSongAdapter = new SongAdapter(mListSongAdapter, mMainActivity);
        mRecyclerView = view.findViewById(R.id.list_song_recycleview);
        mRecyclerView.setAdapter(mSongAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mMainActivity.getApplicationContext()));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.all_list_song_fragment, container, false);
        mMainActivity = (MainActivity) getActivity();
        createRecycleView(view);
        initView(view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mMainActivity.getmMediaService() != null) {
            showSmallPlayingArea();
            upDateSmallPlayingRelativeLayout(mListSongAdapter, mSongAdapter, mRecyclerView);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
       //Pause handler
//        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

//    /**
//     * Tuantqd
//     * Initial view byViewFindId
//     * *@param view
//     */
//    private void initView(View view) {
//        mSmallPlayRelativeLayout = view.findViewById(R.id.small_playing_area);
//        //Set onClick for SmallPlayRelativeLayout
//        mSmallPlayRelativeLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //Dung 2 Activity
//                Intent intent = new Intent(getActivity().getApplicationContext(), MediaPlaybackActivity.class);
//                startActivity(intent);
//            }
//        });
//        mSongImageView = view.findViewById(R.id.small_song_imageview);
//        mCurrentSongNameTextView = view.findViewById(R.id.small_name_current_song);
//        mCurrentArtistNameTextView = view.findViewById(R.id.small_singer_name_current_song);
//        mPlayMediaImageButton = view.findViewById(R.id.small_play_imagebutton);
//        //Set onClick for Play/Pause ImageButton
//        mPlayMediaImageButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //Bkav Thanhnch: todo giong ham ben
//                // com/bkav/musicapplication/activity/FavoriteSongFragment.java:178
//                //gop ham de dung chung
//                if (mMainActivity.getmMediaService().getmMediaPlayer().isPlaying()) {
//                    mMainActivity.getmMediaService().pauseMedia();
//                    mPlayMediaImageButton.setImageResource(R.drawable.ic_media_play_light);
//                } else {
//                    mMainActivity.getmMediaService().resumeMedia();
//                    mPlayMediaImageButton.setImageResource(R.drawable.ic_media_pause_light);
//                }
//
//            }
//        });
//        mSmallPlayingAreaRelativeLayout = view.findViewById(R.id.small_playing_area);
//    }

//    /**
//     * Tuantqd
//     * Show Small Playing Area
//     */
//    public void showSmallPlayingArea() {
//        if (mSmallPlayingAreaRelativeLayout.getVisibility() == View.GONE) {
//            mSmallPlayingAreaRelativeLayout.setVisibility(View.VISIBLE);
//        }
//    }

    /**
     * Tuantqd
     * Update UI for SmallPlayingRelativeLayout on AllSongFragment
     */
    public void upDateSmallPlayingRelativeLayout() {
        if (mMainActivity.getmMediaService().getmMediaPlayer() != null) {
            Message message = new Message();
            mHandler.sendMessage(message);

            super.upDateSmallPlayingRelativeLayout(mListSongAdapter, mSongAdapter, mRecyclerView);
        }
    }

    /**
     * Tuantqd
     * Function to get List all song
     * @return
     */
    public ArrayList<Song> getmListSongAdapter() {
        return mListSongAdapter;
    }

    /**
     * Tuantqd
     * Fuction to get SongAdapter object
     * @return
     */
    public SongAdapter getmSongAdapter(){
        return mSongAdapter;
    }
}
