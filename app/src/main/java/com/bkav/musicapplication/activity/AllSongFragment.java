package com.bkav.musicapplication.activity;

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
public class AllSongFragment extends Fragment {

    public static final String SMALL_PLAYING_VISIBLE = "small playing visible";
    private MainActivity mMainActivity;
    private RelativeLayout mSmallPlayingAreaRelativeLayout;
    private RelativeLayout mSmallPlayRelativeLayout;   //Relative to display Playing area
    private ImageView mSongImageView;
    private TextView mCurrentSongNameTextView;
    private TextView mCurrentArtistNameTextView;
    private ImageButton mPlayMediaImageButton;
    private ArrayList<Song> mListSongAdapter;  //song List object
    private SongAdapter mSongAdapter;   //song Adapter object
    private RecyclerView mRecyclerView; //Recycleview object

    /*Create by: Tuantqd*/
    //Variables to check when have to update UI
    private int mSongPosition = -1;
    boolean mIsPlay = false;

    private Handler mHandler = new Handler() {   //Handle object as a Thread

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (mSongPosition != mMainActivity.getmMediaService().getmMediaPosition()
                    || mIsPlay != mMainActivity.getmMediaService().getmMediaPlayer().isPlaying()) {
                upDateSmallPlayingRelativeLayout();
                mSongAdapter.notifyDataSetChanged();
            }

            mSongPosition = mMainActivity.getmMediaService().getmMediaPosition();
            mIsPlay = mMainActivity.getmMediaService().getmMediaPlayer().isPlaying();
            Message message = new Message();
            sendMessageDelayed(message, 500);
        }
    };

    /**
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
        setOnClick();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mMainActivity.getmMediaService() != null) {
            upDateSmallPlayingRelativeLayout();
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

    /**
     * Initial view byViewFindId
     * *@param view
     */
    private void initView(View view) {
        mSmallPlayRelativeLayout = view.findViewById(R.id.small_playing_area);
        mSongImageView = view.findViewById(R.id.small_song_imageview);
        mCurrentSongNameTextView = view.findViewById(R.id.small_name_current_song);
        mCurrentArtistNameTextView = view.findViewById(R.id.small_singer_name_current_song);
        mPlayMediaImageButton = view.findViewById(R.id.small_play_imagebutton);
        mSmallPlayingAreaRelativeLayout = view.findViewById(R.id.small_playing_area);
    }

    /**Bkav Thanhnch:  todo sao lai can synchronized
     * Set onlick for all element of all song fragment
     */
    public synchronized void setOnClick() {
        //Set onClick for SmallPlayRelativeLayout
        mSmallPlayRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Dung 2 Activity
                Intent intent = new Intent(getActivity().getApplicationContext(), MediaPlaybackActivity.class);
                startActivity(intent);
            }
        });

        //Set onClick for Play/Pause ImageButton
        mPlayMediaImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Bkav Thanhnch: todo giong han ben
                // com/bkav/musicapplication/activity/FavoriteSongFragment.java:178
                //gop ham de dung chung
                if (mMainActivity.getmMediaService().getmMediaPlayer().isPlaying()) {
                    mMainActivity.getmMediaService().pauseMedia();
                    mPlayMediaImageButton.setImageResource(R.drawable.ic_media_play_light);
                } else {
                    mMainActivity.getmMediaService().resumeMedia();
                    mPlayMediaImageButton.setImageResource(R.drawable.ic_media_pause_light);
                }

            }
        });
    }

    /**
     * Show Small Playing Area
     */
    public void showSmallPlayingArea() {
        if (mSmallPlayingAreaRelativeLayout.getVisibility() == View.GONE) {
            mSmallPlayingAreaRelativeLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Update UI for SmallPlayingRelativeLayout on AllSongFragment
     */
    public void upDateSmallPlayingRelativeLayout() {
        if (mMainActivity.getmMediaService().getmMediaPlayer() != null) {
            Message message = new Message();
            mHandler.sendMessage(message);

            int position = mMainActivity.getmMediaService().getmMediaPosition();
            //Update Song Name, Artist Name, Play Button
            mCurrentSongNameTextView.setText(mListSongAdapter.get(position).getmTitle());
            mCurrentArtistNameTextView.setText(mListSongAdapter.get(position).getmArtistName());
            if (mMainActivity.getmMediaService().getmMediaPlayer().isPlaying()) {
                mPlayMediaImageButton.setImageResource(R.drawable.ic_media_pause_light);
            } else {
                mPlayMediaImageButton.setImageResource(R.drawable.ic_media_play_light);
            }

            //Update AlbumArt
            mSongImageView.setImageURI(mListSongAdapter.get(position)
                    .queryAlbumUri(mListSongAdapter.get(position).getmAlbumID()));
            if (mSongImageView.getDrawable() == null) {
                mSongImageView.setImageResource(R.drawable.ic_reason_album);
            }
            //Update UI for Adapter
            mSongAdapter.notifyDataSetChanged();
            //Scroll to playing position
            mRecyclerView.smoothScrollToPosition(position);
        }
    }


    private static final String[] BASE_PROJECTION = new String[]{
            DataBase.COLUMN_TITLE,
            DataBase.COLUMN_TRACK,
            DataBase.COLUMN_YEAR,
            DataBase.COLUMN_DURATION,
            DataBase.COLUMN_PATH,
            DataBase.COLUMN_ALBUM,
            DataBase.COLUMN_ARTIST_ID,
            DataBase.COLUMN_ARTIST,
            DataBase.COLUMN_ALBUM_ID,
            DataBase.COLUMN_ID,
    };
    //Bkav Thanhnch: Khong dung?
    private ArrayList<Song> getFavoriteSong() {
        ArrayList<Song> songArrayList = new ArrayList<>();
        Cursor cursor = getContext().getContentResolver().query(FavoriteSongProvider.CONTENT_URI, BASE_PROJECTION,
                null, null, null);
        if(cursor != null){
            cursor.moveToFirst();
            while(cursor.moveToNext()){
                songArrayList.add(getSong(cursor));
            }
        }
        return songArrayList;
    }

    private Song getSong(Cursor cursor){
        //Bkav Thanhnch: Khong nen dung gia tri 1,2,3-> thu dung phuong an khac
        final String title = cursor.getString(4);
        final int trackNumber = cursor.getInt(1);
        final int year = cursor.getInt(2);
        final int duration = cursor.getInt(3);
        final String uri = cursor.getString(0);
        final String albumName = cursor.getString(5);
        final int artistId = cursor.getInt(6);
        final String artistName = cursor.getString(7);
        final String albumID = cursor.getString(8);
        //Bkav Thanhnch: sai convention
        final int _id = cursor.getInt(9);

        return new Song(title, trackNumber, year, duration, uri, albumName,
                artistId, artistName, albumID, _id);
    }

    public ArrayList<Song> getmListSongAdapter() {
        return mListSongAdapter;
    }

    public SongAdapter getmSongAdapter(){
        return mSongAdapter;
    }
}
