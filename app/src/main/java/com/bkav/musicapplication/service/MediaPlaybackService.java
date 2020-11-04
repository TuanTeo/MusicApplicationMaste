package com.bkav.musicapplication.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import static androidx.core.app.NotificationCompat.DEFAULT_ALL;

import com.bkav.musicapplication.constant.Constant;
import com.bkav.musicapplication.enumdefine.MediaStatus;
import com.bkav.musicapplication.Playable;
import com.bkav.musicapplication.R;
import com.bkav.musicapplication.activity.MainActivity;
import com.bkav.musicapplication.broadcast.NotificationActionService;
import com.bkav.musicapplication.contentprovider.SongProvider;
import com.bkav.musicapplication.object.Song;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;


/**
 * Tuantqd
 * Service de quan ly cac logic choi nhac, cac bai hat dang choi,
 * object MediaPlayer de choi nhac, thong bao dieu khien nhac
 */
public class MediaPlaybackService extends Service implements Playable {
    private static final String TAG = "MediaPlaybackService";

    private static final String NOTIFI_CHANNEL_ID = "notification_channel";
    private static final int MEDIA_NOTIFICATION_ID = 0;
    private static final String ACTION_PREVIOUS = "action_previous";
    private static final String ACTION_PLAY = "action_play";
    private static final String ACTION_NEXT = "action_next";

    private BroadcastReceiver mNotificationBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getExtras().getString(NotificationActionService.NOTIFICATION_ACTION_NAME);
            //Bkav Thanhnch: can check null.
            if(action != null) {
                switch (action) {
                    case ACTION_PREVIOUS:
                        onMediaPrevious();
                        break;
                    case ACTION_NEXT:
                        onMediaNext();
                        break;
                    case ACTION_PLAY:
                        if (mMediaPlayer.isPlaying()) {
                            onMediaPause();
                        } else {
                            onMediaPlay();
                        }
                        break;
                    default:    //Neu null hoac gia tri khac => log.d(action)
                        Log.d(TAG, "NotificationBroadcast onReceive: " + action);
                        break;
                }
            }
        }
    };

    private NotificationManager mNotificationManager;
    private int mMediaPosition = Constant.MEDIA_DEFAULT_POSITION;
    private String mCurrentMediaTitle = Constant.MEDIA_DEFAULT_TITLE;
    private Song mCurrentSong;
    private MediaStatus mMediaStatus = MediaStatus.NONE;
    private MediaPlayer mMediaPlayer;
    private ArrayList<Song> mListAllSong = SongProvider.getInstanceNotCreate().getmListSong();

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        registerReceiver(mNotificationBroadcast, new IntentFilter(NotificationActionService.BROADCAST_ACTION));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new BoundService();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mNotificationManager.cancel(MEDIA_NOTIFICATION_ID);
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY; //khong tao lai Service khi app bi tat ngang
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }
        mNotificationManager.cancelAll();
        stopSelf();
    }

    @Override
    public void onMediaPrevious() {
        previousMedia();
    }

    @Override
    public void onMediaPlay() {
        resumeMedia();
    }

    @Override
    public void onMediaPause() {
        pauseMedia();
    }

    @Override
    public void onMediaNext() {
        nextMedia();
    }

    /**
     *
     * @return
     */
    public Song getCurrentSong() {
        return mCurrentSong;
    }

    /**
     * Tuantqd
     * Tra ve 1 doi tuong MediaPlaybackService
     */
    public class BoundService extends Binder {
        public MediaPlaybackService getService() {
            return MediaPlaybackService.this;
        }
    }

    /**
     * Tuantqd
     * Function to get current list song ( mListSong )
     *
     * @return
     */
    public ArrayList<Song> getListSongService() {
        return SongProvider.getInstanceNotCreate().getmListSong();
    }

    /**
     * Tuantqd
     * Function to set list song ( mListSong ) for Service
     *
     * @param listSong
     */
    public void setListSongService(ArrayList<Song> listSong) {
        this.mListAllSong = listSong;
    }

    /**
     * Tuantqd
     * Call when media completion ( end song )
     */
    public void autoNextMedia() {
        if (mMediaStatus == MediaStatus.SHUFFLE
                || mMediaStatus == MediaStatus.REPEAT_AND_SHUFFLE) {
            nextByShuffleWithButton();
        } else if (mMediaStatus == MediaStatus.NONE) {
            if (mMediaPosition == mListAllSong.size() - 1) {
                /*Don't continue play*/
                mMediaPlayer.pause();
            } else {
                playMedia(mListAllSong.get(mMediaPosition + 1));
            }
        } else if (mMediaStatus == MediaStatus.REPEAT_ONE
                || mMediaStatus == MediaStatus.REPEAT_ONE_AND_SHUFFLE) {
            repeatMedia();
        } else {
            nextWithButton();
        }
    }

    /**
     * Tuantqd
     * Next media with repeat and shuffle status
     */
    public void nextMedia() {
        if (mMediaStatus == MediaStatus.SHUFFLE
                || mMediaStatus == MediaStatus.REPEAT_AND_SHUFFLE
                || mMediaStatus == MediaStatus.REPEAT_ONE_AND_SHUFFLE) {
            nextByShuffleWithButton();
        } else {
            nextWithButton();
        }
    }


    /**
     * Create by: Tuantqd
     * Previous media with repeat and shuffle status
     */
    public void previousMedia() {
        if (mMediaStatus == MediaStatus.NONE
                || mMediaStatus == MediaStatus.REPEAT_ALL
                || mMediaStatus == MediaStatus.REPEAT_ONE) {     //Previous with no shuffle status
            if (mMediaPlayer.getCurrentPosition() >= 3000) {     //Thoi gian dang phat lon hon 3s => phat lai
                repeatMedia();
            } else if (mMediaPosition == 0) {   //Dau danh sach => phat cuoi danh sach
                playMedia(mListAllSong.get(mListAllSong.size() - 1));
            } else {                            //Vi tri bat ki => position - 1
                playMedia(mListAllSong.get(mMediaPosition - 1));
            }
        } else {
            if (mMediaPlayer.getCurrentPosition() >= 3000) {     //Thoi gian dang phat lon hon 3s => phat lai
                repeatMedia();
            } else {    //Previous with shuffle status
                nextByShuffleWithButton();
            }
        }
    }

    /**
     * Tuantqd
     * <p>
     *     Function to Play media and also save current Song's ID and Song's positon in list song
     * </p>
     * @param song
     */
    public void playMedia(Song song) {
        //Save current song's id
        setmCurrentMediaTitle(song.getmTitle());

        this.mCurrentSong = song;

        //Save current song's positon
        setmMediaPosition(song);

        try {
            stopMedia();
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(song.getmPath());
            mMediaPlayer.prepare();
            mMediaPlayer.start();

            //Send notification
            sendNotification();

            //Set event when media completion
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    autoNextMedia();
                }
            });
        } catch (IOException e) {
            /*IOException co the xay ra khi thuc hien
            * cau lenh setDataSource(path) do duong dan khong dung
            * Khi bat duoc IOException thi in ra loi trong Logcat.
            */
            e.printStackTrace();
        }
    }

    /**
     * Tuantqd
     * Function to pause media
     */
    public void pauseMedia() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
        sendNotification();
    }

    /**
     * Tuantqd
     * Function to resume media
     */
    public void resumeMedia() {
        if (!mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
        }
        sendNotification();
    }

    /**
     * Tuantqd
     * Fuction to stop media
     */
    public void stopMedia() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    /**
     * Tuantqd
     * Fuction to repeat media
     */
    public void repeatMedia() {
        playMedia(mListAllSong.get(getmMediaPosition()));
    }

    /**
     * Tuantqd
     * Fuction to next media with shuffle status
     */
    public void nextByShuffleWithButton() {
        int position = randomPosition();
        if (position == mMediaPosition) {
            position = randomPosition();
        }
        playMedia(mListAllSong.get(position));
    }

    /**
     * Tuantqd
     * Fuction to next media wih no shuffle status
     */
    private void nextWithButton() {
        if (getmMediaPosition() == (mListAllSong.size() - 1)) {
            playMedia(mListAllSong.get(0));
        } else {
            playMedia(mListAllSong.get(getmMediaPosition() + 1));
        }
    }

    /**
     * Tuantqd
     * Fuction to create a random int value about (0, mListAllSong.size())
     * @return
     */
    private int randomPosition() {
        Random random = new Random();
        return random.nextInt(mListAllSong.size());
    }

    /**
     * Tuantqd
     * Function to check media player is playing
     * @return
     */
    public boolean isPlaying(){
        if(mMediaPlayer != null && mMediaPlayer.isPlaying()){
            return true;
        }
        return false;
    }

    /**
     * Tuantqd
     * Return current mediaPlayer
     * @return
     */
    public MediaPlayer getmMediaPlayer() {
        return mMediaPlayer;
    }

    /**
     * Tuantqd
     * Return media play status (shuffle, repeat)
     * @return
     */
    public MediaStatus getmMediaStatus() {
        return mMediaStatus;
    }

    /**
     * Tuantqd
     * Fuction to set media play status (shuffle, repeat)
     */
    public void setmMediaStatus(MediaStatus mMediaStatus) {
        this.mMediaStatus = mMediaStatus;
    }

    /**
     * Tuantqd
     * Set mMediaPotion by Song's position in list current song
     * @param song
     */
    public void setmMediaPosition(Song song) {
        for (int i = 0; i < mListAllSong.size(); i++) {
            if (mListAllSong.get(i) == song) {
                mMediaPosition = i;
            }
        }
    }

    /**
     * Tuantqd
     * Function to get current song's position
     * @return
     */
    public int getmMediaPosition() {
        return mMediaPosition;
    }

    /**
     * Function to get current song's id
     * @return
     */
    public String getmCurrentMediaTitle() {
        return mCurrentMediaTitle;
    }

    /**
     * Function to set current song's id
     * @param mCurrentMediaTitle
     */
    public void setmCurrentMediaTitle(String mCurrentMediaTitle) {
        this.mCurrentMediaTitle = mCurrentMediaTitle;
    }

    /**
     * Tuantqd
     * Function to create notification channel
     */
    private void createNotificationChannel() {
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel =
                    new NotificationChannel(NOTIFI_CHANNEL_ID,
                            "Media_Service",
                            NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.enableLights(false); //Set mau den thong bao (true)
            notificationChannel.setLightColor(Color.RED);   //Mau do
            notificationChannel.enableVibration(false);  //Rung khi thong bao

            mNotificationManager.createNotificationChannel(notificationChannel);    //Tao kenh thong bao
        }
    }

    /**
     * Tuantqd
     * To manager, build and show notification
     * @return
     */
    private NotificationCompat.Builder getNotificationBuilder() {
        //Tao event clicked in notification => back to MainActivity
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(this,
                MEDIA_NOTIFICATION_ID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        /*Add Media control button and set action*/
        //Previous button
        Intent intentPrev = new Intent(getApplicationContext(), NotificationActionService.class)
                .setAction(ACTION_PREVIOUS);
        PendingIntent pendingIntentPrev = PendingIntent.getBroadcast(getApplicationContext(),
                MEDIA_NOTIFICATION_ID, intentPrev, PendingIntent.FLAG_UPDATE_CURRENT);
        int prevImage = R.drawable.ic_rew_dark;

        //Next button
        Intent intentNext = new Intent(getApplicationContext(), NotificationActionService.class)
                .setAction(ACTION_NEXT);
        PendingIntent pendingIntentNext = PendingIntent.getBroadcast(getApplicationContext(),
                MEDIA_NOTIFICATION_ID, intentNext, PendingIntent.FLAG_UPDATE_CURRENT);
        int nextImage = R.drawable.ic_fwd_dark;

        //Play button
        int playImage;
        Intent intentPlay = new Intent(getApplicationContext(), NotificationActionService.class)
                .setAction(ACTION_PLAY);
        PendingIntent pendingIntentPlay = PendingIntent.getBroadcast(getApplicationContext(),
                MEDIA_NOTIFICATION_ID, intentPlay, PendingIntent.FLAG_UPDATE_CURRENT);
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            playImage = R.mipmap.ic_media_pause_light;
        } else {
            playImage = R.mipmap.ic_media_play_light;
        }


        //Bitmap to set to Drawer Notification
        Bitmap albumArt = BitmapFactory.decodeResource(getResources(), R.drawable.ic_reason_album);

        NotificationCompat.Builder notifyBuilder =
                new NotificationCompat.Builder(this, NOTIFI_CHANNEL_ID)
                        .setContentTitle(mListAllSong.get(mMediaPosition).getmTitle())  //Set title
                        .setContentText(mListAllSong.get(mMediaPosition).getmArtistName())  //Set text detail
                        .setSmallIcon(R.mipmap.ic_launcher) //Set smallIcon (bat buoc)
                        .setLargeIcon(albumArt)
                        .setDefaults(DEFAULT_ALL)
                        .setOnlyAlertOnce(true)     //Show notification for only first time
                        .setContentIntent(notificationPendingIntent)
                        .setAutoCancel(false)       //Can't remove notification
                        .addAction(prevImage, "Previous", pendingIntentPrev)
                        .addAction(playImage, "Play", pendingIntentPlay)
                        .addAction(nextImage, "Next", pendingIntentNext)
                        .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                                .setShowActionsInCompactView(0, 1, 2))
                        .setOngoing(true);          //Need to don't remove notification

        return notifyBuilder;
    }

    /**
     * Tuantqd
     * Function to send and show notification to UI
     */
    private void sendNotification() {
        NotificationCompat.Builder notifyBuilder = getNotificationBuilder();
        mNotificationManager.notify(MEDIA_NOTIFICATION_ID, notifyBuilder.build());
    }
}
