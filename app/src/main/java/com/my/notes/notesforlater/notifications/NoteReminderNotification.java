package com.my.notes.notesforlater.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import androidx.core.app.NotificationCompat;

import com.my.notes.notesforlater.MainActivity;
import com.my.notes.notesforlater.R;
import com.my.notes.notesforlater.backup.NoteBackup;
import com.my.notes.notesforlater.backup.NotesBackupService;
import com.my.notes.notesforlater.notes.NotesActivity;

public class NoteReminderNotification
{
	public static String GROUP_NOTIFICATIONS_ID = "group_notify";
	private static final String NOTIFICATION_TAG = "NoteReminder";
	public static final String CHANNEL_ID = "1001";
	static NotificationManager manager;


	public static void setupNotificationChannel(Context context)
	{
		CharSequence channelName = "channel1";
		CharSequence groupName = "Notes";
		NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);

		notificationChannel.enableLights(true);
		notificationChannel.enableVibration(false);
		notificationChannel.setLightColor(Color.BLUE);
		notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

		manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.createNotificationChannel(notificationChannel);

		manager.createNotificationChannelGroup(new NotificationChannelGroup(GROUP_NOTIFICATIONS_ID, groupName));
	}


	public static void notify(final Context context, final String noteTitle, final String noteText, int noteId)
	{

		setupNotificationChannel(context);

		final Resources res = context.getResources();

		final Bitmap picture = BitmapFactory
				.decodeResource(res, R.mipmap.ic_sypho_launcher_foreground);

		Intent noteActivityIntent = new Intent(context, NotesActivity.class);
		noteActivityIntent.putExtra(NotesActivity.NOTE_ID, noteId);


		Intent backupServiceIntent = new Intent(context, NotesBackupService.class);
		backupServiceIntent.putExtra(NotesBackupService.EXTRA_COURSE_ID, NoteBackup.ALL_COURSES);

		final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)

				.setGroup(GROUP_NOTIFICATIONS_ID)
				.setDefaults(Notification.DEFAULT_ALL)
				.setSmallIcon(R.mipmap.ic_sypho_launcher_foreground)
				.setContentTitle("Review note")
				.setContentText(noteText)
				.setPriority(NotificationCompat.PRIORITY_DEFAULT)
				.setLargeIcon(picture)
				.setTicker("Review note")

				.setStyle(new NotificationCompat.BigTextStyle()
						.bigText(noteText)
						.setBigContentTitle(noteTitle)
						.setSummaryText("Review note"))


				.setContentIntent(PendingIntent.getActivity(
						context, 0, noteActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT))

				.addAction(0, "View all notes", PendingIntent.getActivity(
						context, 0, new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT))

				.addAction(0, "Backup notes", PendingIntent.getService(
						context, 0, backupServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT))

				.addAction(0, "Backup notes", PendingIntent.getService(
						context, 0, backupServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT))

				.setAutoCancel(true);

		notify(context, builder.build());
	}

	private static void notify(final Context context, final Notification notification)
	{
		final NotificationManager notificationManager =
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(NOTIFICATION_TAG, 0, notification);
	}


	public static void cancel(final Context context)
	{
		final NotificationManager notificationManager =
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(NOTIFICATION_TAG, 0);
	}
}
