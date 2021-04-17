package com.my.notes.notesforlater.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.my.notes.notesforlater.notifications.NoteReminderNotification;

public class NoteReminderReceiver extends BroadcastReceiver
{
	public static final String EXTRA_NOTE_ID = "com.my.notes.notesforlater.action.EXTRA_NOTE_ID";
	public static final String EXTRA_NOTE_TITLE = "com.my.notes.notesforlater.action.EXTRA_NOTE_TITLE";
	public static final String EXTRA_NOTE_TEXT = "com.my.notes.notesforlater.action.EXTRA_NOTE_TEXT";

	@Override
	public void onReceive(Context context, Intent intent)
	{
		String noteTitle = intent.getStringExtra(EXTRA_NOTE_TITLE);
		String noteText = intent.getStringExtra(EXTRA_NOTE_TEXT);
		int noteId = intent.getIntExtra(EXTRA_NOTE_ID, 0);

		NoteReminderNotification.notify(context, noteTitle, noteText, noteId);
	}
}