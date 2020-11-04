package com.bkav.musicapplication.object;

import android.content.ContentUris;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import java.text.SimpleDateFormat;

/**
 * Create by: Tuantqd
 */
public class Song {

    /*Create by: Tuantqd*/
    public static final int FAVORITE_SONG = 100;    //constant to set song is favorite
    public static final int NOT_FAVORITE_SONG = -1; //constant to set song is not favorite

    /*Properties's position when get by cursor*/
    private static final int PATH = 0;
    private static final int TRACK = 1;
    private static final int YEAR = 2;
    private static final int DURATION = 3;
    private static final int TITLE = 4;
    private static final int ALBUM_NAME = 5;
    private static final int ARTIST_ID = 6;
    private static final int ARTIST_NAME = 7;
    private static final int ALBUM_ID = 8;
    private static final int ID = 9;


    static final Song EMPTY_SONG = new Song("", -1, -1, -1,
            null, "", -1, "", "", -1);

    /*Tuantqd
    * Song's properties*/
    private final String mTitle;
    private final int mTrackNumber;
    private final int mDuration;
    private final String mPath;
    private final String mAlbumName;
    private final int mArtistId;
    private final String mArtistName;
    private final int mYear;
    private final String mAlbumID;
    private final int mID;



    /*Create by: Tuantqd*/
    private int mCount;         //Count so lan click choi bai hat
    private int mIsFavorite;    //int value to check "Is favorite song?"

    public Song(String title, int trackNumber, int mYear, int mDuration, String mPath,
                String mAlbumName, int mArtistId, String mArtistName, String mAlbumID, int mID) {
        this.mTitle = title;
        this.mTrackNumber = trackNumber;
        this.mYear = mYear;
        this.mDuration = mDuration;
        this.mPath = mPath;
        this.mAlbumName = mAlbumName;
        this.mArtistId = mArtistId;
        this.mArtistName = mArtistName;
        this.mAlbumID = mAlbumID;
        this.mID = mID;
        this.mCount = 0;
    }

    /**
     * Increase play count number
      */
    public void countIncrease(){
        if(mCount >=0 && mCount < 2) {
            mCount++;
        } else {
            mIsFavorite = FAVORITE_SONG;
        }
    }

    /**
     * Check Song is Favorite
     * @return
     */
    public boolean isFavoriteSong(){
        if(mIsFavorite == FAVORITE_SONG){
            return true;
        } else {
            return false;
        }
    }

    /**
     * Set song to Unlike
     */
    public void setNotFavoriteSong(){
        mCount = -1;    //Dont increase mCount
        mIsFavorite = NOT_FAVORITE_SONG;
    }

    public void setFavoriteToDefault(){
        mCount = 0;
        mIsFavorite = NOT_FAVORITE_SONG;
    }

    /**
     * Convert Duration to String fommat hh:mm:ss
     */
    public String getmDurationString(){
        int totalTime = mDuration;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
        simpleDateFormat.format(totalTime);

        return simpleDateFormat.format(totalTime);
    }

    /**
     * Getter & Setter
     */
    public String getmTitle() {
        return mTitle;
    }

    public int getmTrackNumber() {
        return mTrackNumber;
    }

    public int getmDuration() {
        return mDuration;
    }

    public String getmPath() {
        return mPath;
    }

    public String getmAlbumName() {
        return mAlbumName;
    }

    public int getmArtistId() {
        return mArtistId;
    }

    public String getmArtistName() {
        return mArtistName;
    }

    public int getmYear() {
        return mYear;
    }

    public String getmAlbumID() {
        return mAlbumID;
    }

    public int getmID() {
        return mID;
    }

    public int getmCount() {
        return mCount;
    }

    public void setmCount(int mCount) {
        this.mCount = mCount;
    }


    /**
     * Tuantqd
     * Function to get Image Uri by AlbumID
     * @param imgUri
     * @return
     */
    public static Uri queryAlbumUri(String imgUri) {   //dung album de load anh
        final Uri artworkUri = Uri.parse("content://media/external/audio/albumart");
        return ContentUris.withAppendedId(artworkUri, Long.parseLong(imgUri));  //noi them imgUri vao artworkUri
    }


//    public static byte[] getAlbumArt(String uri) {  // dung file de load anh
//        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
//        mediaMetadataRetriever.setDataSource(uri);
//        byte[] albumArt = mediaMetadataRetriever.getEmbeddedPicture();  // chuyển đổi đường dẫn file media thành đường dẫn file Ảnh
//        mediaMetadataRetriever.release();
//        return albumArt;
//    }

    /**
     * Tuantqd
     * Function to get a song from database
     * @param cursor
     * @return
     */
    public static Song getSong(Cursor cursor) {

        String title = cursor.getString(TITLE);
        int trackNumber = cursor.getInt(TRACK);
        int year = cursor.getInt(YEAR);
        int duration = cursor.getInt(DURATION);
        String uri = cursor.getString(PATH);
        String albumName = cursor.getString(ALBUM_NAME);
        int artistId = cursor.getInt(ARTIST_ID);
        String artistName = cursor.getString(ARTIST_NAME);
        String albumID = cursor.getString(ALBUM_ID);
        int id = cursor.getInt(ID);

        return new Song(title, trackNumber, year, duration, uri, albumName,
                artistId, artistName, albumID, id);
    }
}
