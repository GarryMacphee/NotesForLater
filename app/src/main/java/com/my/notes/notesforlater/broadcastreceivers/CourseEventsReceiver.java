package com.my.notes.notesforlater.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.my.notes.notesforlater.CourseEventDisplayCallback;

public class CourseEventsReceiver extends BroadcastReceiver
{
	public static final String ACTION_COURSE_EVENT = "com.my.notes.notesforlater.action.COURSE_EVENT";

	public static final String EXTRA_COURSE_ID = "com.my.notes.notesforlater.action.COURSE_ID";
	public static final String EXTRA_COURSE_MESSAGE = "com.my.notes.notesforlater.action.COURSE_MESSAGE";

	private CourseEventDisplayCallback mCourseEventDisplayCallback;

	@Override
	public void onReceive(Context context, Intent intent)
	{
		if (ACTION_COURSE_EVENT.equals(intent.getAction()))
		{
			String courseId = intent.getStringExtra(EXTRA_COURSE_ID);
			String courseMessage = intent.getStringExtra(EXTRA_COURSE_MESSAGE);

			if (mCourseEventDisplayCallback != null)
			{
				mCourseEventDisplayCallback.onEventReceived(courseId, courseMessage);
			}

		}
	}

	public void setCourseEventDisplayCallback(com.my.notes.notesforlater.CourseEventDisplayCallback courseEventDisplayCallback)
	{
		mCourseEventDisplayCallback = courseEventDisplayCallback;
	}

}