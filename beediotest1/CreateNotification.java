package com.example.beediotest1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.media.session.MediaButtonReceiver;


import android.app.Notification;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.example.beediotest1.Services.NotificationActionService;

public class CreateNotification {
    public static final String CHANNEL_ID = "channel";
    public static final String ACTION_PREVIOUS = "actionprevious";
    public static final String ACTION_PLAY = "actionplay";
    public static final String ACTION_NEXT = "actionnext";
    private static final int NOTIFICATION_ID = 101 ;


    public static void createNotification(Context context, MusicListAdapter musicListAdapter,
                                          int pos, int size) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
            MediaSessionCompat mediaSessionCompat = new MediaSessionCompat(context, "tag");
            Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_music_logo_round);

            Intent intentPlay = new Intent(context, MainActivity.class);
            intentPlay.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
                    |Intent.FLAG_ACTIVITY_REORDER_TO_FRONT| Intent.FLAG_ACTIVITY_SINGLE_TOP);

            PendingIntent pendingIntent = PendingIntent.getActivity(context,NOTIFICATION_ID,
                    intentPlay, PendingIntent.FLAG_MUTABLE);


            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);
            builder .setSmallIcon(R.drawable.ic_baseline_music_note_24);
            builder.setContentTitle("Tap to go back BEEDIO");
            builder.setContentText("Beedio is now active");
            builder.setOnlyAlertOnce(true);
            builder.setShowWhen(false);
            builder.setAutoCancel(true);
            builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
            builder.setContentIntent(pendingIntent);
            notificationManagerCompat.notify(101 , builder.build());

        }

    }

}