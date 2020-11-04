package com.bkav.musicapplication.adapter;

import android.content.ContentValues;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bkav.musicapplication.R;
import com.bkav.musicapplication.activity.MainActivity;
import com.bkav.musicapplication.constant.Constant;
import com.bkav.musicapplication.contentprovider.FavoriteSongProvider;
import com.bkav.musicapplication.favoritesongdatabase.DataBase;
import com.bkav.musicapplication.object.Song;

import java.util.ArrayList;

/**
 * Tuantqd
 * Create adapter for FavoriteSongFragment
 */
public class FavoriteSongAdapter extends RecyclerView.Adapter<FavoriteSongAdapter.FavoriteSongViewHolder> {
    private ArrayList<Song> mListFavoriteSongAdapter; //Create List of Song
    private LayoutInflater mInflater;
    private MainActivity mainActivity;

    private int mLastItemPositionInt = Constant.MEDIA_DEFAULT_POSITION;     //Vi tri cua phan tu khi clicked
    private String mCurretSongPath = Constant.MEDIA_DEFAULT_TITLE;           //Path cua phan tu khi clicked

    public FavoriteSongAdapter(ArrayList<Song> mListSongAdapter, MainActivity context) {
        this.mainActivity = context;
        this.mListFavoriteSongAdapter = mListSongAdapter;
        this.mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public FavoriteSongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.song_list_item, parent, false);
        return new FavoriteSongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteSongViewHolder holder, int position) {
        if(mainActivity.getmMediaService() != null){
            mCurretSongPath = mainActivity.getmMediaService().getmCurrentMediaTitle();
            //Set Name song
            holder.mSongNameItemTextView.setText(mListFavoriteSongAdapter.get(position).getmTitle());
            holder.mTotalTimeSongItemTextView.setText(mListFavoriteSongAdapter.get(position).getmDurationString());

            //Set font
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (mListFavoriteSongAdapter.get(position).getmTitle().equals(mCurretSongPath)) {
                    holder.mSongNameItemTextView.setTextAppearance(R.style.SongTheme_NameSongClickOverLay);
                } else {
                    holder.mSongNameItemTextView.setTextAppearance(R.style.SongTheme_NameSongOverLay);
                }
            }

            //Set Serial
            if (mListFavoriteSongAdapter.get(position).getmTitle().equals(mCurretSongPath)) {
                holder.mSerialSongNumberTextView.setVisibility(View.INVISIBLE);
                holder.mPlayingSongImageLinearLayout.setVisibility(View.VISIBLE);
            } else {
                holder.mSerialSongNumberTextView.setText((position + 1) + "");
                holder.mSerialSongNumberTextView.setVisibility(View.VISIBLE);
                holder.mPlayingSongImageLinearLayout.setVisibility(View.GONE);
            }
        } else {
            //Set Name song
            holder.mSongNameItemTextView.setText(mListFavoriteSongAdapter.get(position).getmTitle());
            holder.mTotalTimeSongItemTextView.setText(mListFavoriteSongAdapter.get(position).getmDurationString());

            //Set font
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (mListFavoriteSongAdapter.get(position).getmTitle().equals(mCurretSongPath)) {
                    holder.mSongNameItemTextView.setTextAppearance(R.style.SongTheme_NameSongClickOverLay);
                } else {
                    holder.mSongNameItemTextView.setTextAppearance(R.style.SongTheme_NameSongOverLay);
                }
            }

            //Set Serial
            if (mListFavoriteSongAdapter.get(position).getmTitle().equals(mCurretSongPath)) {
                holder.mSerialSongNumberTextView.setVisibility(View.INVISIBLE);
                holder.mPlayingSongImageLinearLayout.setVisibility(View.VISIBLE);
            } else {
                holder.mSerialSongNumberTextView.setText((position + 1) + "");
                holder.mSerialSongNumberTextView.setVisibility(View.VISIBLE);
                holder.mPlayingSongImageLinearLayout.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mListFavoriteSongAdapter.size();
    }

    public class FavoriteSongViewHolder extends RecyclerView.ViewHolder
            implements RecyclerView.OnClickListener {

        private TextView mSerialSongNumberTextView;
        private TextView mSongNameItemTextView;
        private TextView mTotalTimeSongItemTextView;
        private ImageButton mSongDetailItemImageButton;
        private LinearLayout mPlayingSongImageLinearLayout;

        /**
         * Tuantqd
         * Constructor of Song View Holder
         *
         * @param itemView
         */
        public FavoriteSongViewHolder(@NonNull View itemView) {
            super(itemView);
            mSerialSongNumberTextView = itemView.findViewById(R.id.serial_item_textview);
            mSongNameItemTextView = itemView.findViewById(R.id.song_name_item_textview);
            mTotalTimeSongItemTextView = itemView.findViewById(R.id.total_time_song_item_textview);
            mSongDetailItemImageButton = itemView.findViewById(R.id.song_detail_item);
            mSongDetailItemImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    PopupMenu popupMenu = new PopupMenu(mainActivity.getApplicationContext(), v);
                    popupMenu.inflate(R.menu.menu_favorite_song_item);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            if(item.getItemId() == R.id.remove_favorite_song_item){
                                deleteSongFromDataBase(position);
                                notifyItemRemoved(position);
                                notifyDataSetChanged();
                                return true;
                            }
                            return false;
                        }
                    });
                    popupMenu.show();
                }
            });
            mPlayingSongImageLinearLayout = itemView.findViewById(R.id.playing_icon_layout);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            //Get position of item
            mLastItemPositionInt = getAdapterPosition();

            //Theo chieu doc => Show small playing area
            if (v.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
//                //Get position of item
//                mLastItemPositionInt = getAdapterPosition();

                mainActivity.getmMediaService().setListSongService(mListFavoriteSongAdapter);

                //play Media
                mainActivity.getmMediaService().playMedia(mListFavoriteSongAdapter.get(mLastItemPositionInt));

                //Add Current Song to Database
//                addSongToDataBase(mainActivity.getmMediaService().getmMediaPosition());

                //Delete Current Song from Database
//                deleteSongFromDataBase(mLastItemPositionInt);

                //UpDate data on View
                notifyDataSetChanged();
                //Show small playing area
                mainActivity.getmFavoriteSongFragment().showSmallPlayingArea();
                //Update UI in AllSongFragment
                mainActivity.getmFavoriteSongFragment().upDateSmallPlayingRelativeLayout();
            } else {    //Theo chieu ngang => khong hien thi small playing area
                mLastItemPositionInt = getAdapterPosition();
                mainActivity.getmMediaService().playMedia(mListFavoriteSongAdapter.get(mLastItemPositionInt));
                notifyDataSetChanged();
            }
        }

//        /**
//         * Tuantqd
//         * Get Song data put to ContentValues
//         * @param position
//         * @return
//         */
//        private ContentValues getSongData(int position){
//            ContentValues contentValues = new ContentValues();
//
//            contentValues.put(DataBase.COLUMN_PATH, mListFavoriteSongAdapter.get(position).getmPath());
//            contentValues.put(DataBase.COLUMN_TITLE, mListFavoriteSongAdapter.get(position).getmTitle());
//            contentValues.put(DataBase.COLUMN_TRACK, mListFavoriteSongAdapter.get(position).getmTrackNumber());
//            contentValues.put(DataBase.COLUMN_YEAR, mListFavoriteSongAdapter.get(position).getmYear());
//            contentValues.put(DataBase.COLUMN_ALBUM, mListFavoriteSongAdapter.get(position).getmAlbumName());
//            contentValues.put(DataBase.COLUMN_ALBUM_ID, mListFavoriteSongAdapter.get(position).getmAlbumID());
//            contentValues.put(DataBase.COLUMN_ARTIST, mListFavoriteSongAdapter.get(position).getmArtistName());
//            contentValues.put(DataBase.COLUMN_ARTIST_ID, mListFavoriteSongAdapter.get(position).getmArtistId());
//            contentValues.put(DataBase.COLUMN_DURATION, mListFavoriteSongAdapter.get(position).getmDuration());
//
//            return contentValues;
//        }

//        /**
//         * Tuantqd
//         * Add Song To DataBase
//         * @param position
//         */
//        private void addSongToDataBase(int position){
//            ContentValues values = getSongData(position);
//            Uri uri = mainActivity.getContentResolver().insert(
//                    FavoriteSongProvider.CONTENT_URI, values);
//            Toast.makeText(mainActivity.getBaseContext(),
//                    uri.toString(), Toast.LENGTH_LONG).show();
//        }

        /**
         * Tuantqd
         * Delete Song from database
         * @param position
         */
        private void deleteSongFromDataBase(int position){
            mainActivity.getContentResolver()
                    .delete(FavoriteSongProvider.CONTENT_URI
                            , "Path=?", new String[] {mListFavoriteSongAdapter.get(position).getmPath()});
            Toast.makeText(mainActivity, "Deleted!", Toast.LENGTH_SHORT).show();
        }
    }
}
