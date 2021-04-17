package com.my.notes.notesforlater.notes;

import android.os.Parcel;
import android.os.Parcelable;

import com.my.notes.notesforlater.courses.CourseInfo;

public class NoteInfo implements Parcelable
{
	public final static Parcelable.Creator<NoteInfo> CREATOR =
			new Parcelable.Creator<NoteInfo>()
			{

				@Override
				public NoteInfo createFromParcel(Parcel source)
				{
					return new NoteInfo(source);
				}

				@Override
				public NoteInfo[] newArray(int size)
				{
					return new NoteInfo[size];
				}
			};
	private int mId;
	private CourseInfo mCourse;
	private String mTitle;
	private String mText;

	public NoteInfo(int id, CourseInfo course, String title, String text)
	{
		mCourse = course;
		mTitle = title;
		mText = text;
		mId = id;
	}

	private NoteInfo(Parcel source)
	{
		mCourse = source.readParcelable(CourseInfo.class.getClassLoader());
		mTitle = source.readString();
		mText = source.readString();
	}

	public NoteInfo(CourseInfo course, String title, String text)
	{
		mCourse = course;
		mTitle = title;
		mText = text;
	}


	public CourseInfo getCourse()
	{
		return mCourse;
	}

	public void setCourse(CourseInfo course)
	{
		mCourse = course;
	}

	public String getTitle()
	{
		return mTitle;
	}

	public void setTitle(String title)
	{
		mTitle = title;
	}

	public String getText()
	{
		return mText;
	}

	public void setText(String text)
	{
		mText = text;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		NoteInfo that = (NoteInfo) o;

		return getCompareKey().equals(that.getCompareKey());
	}

	private String getCompareKey()
	{
		return mCourse.getCourseId() + "|" + mTitle + "|" + mText;
	}

	@Override
	public int hashCode()
	{
		return getCompareKey().hashCode();
	}

	@Override
	public String toString()
	{
		return getCompareKey();
	}

	@Override
	public int describeContents()
	{
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeParcelable(mCourse, 0);
		dest.writeString(mTitle);
		dest.writeString(mText);
	}

	public int getId()
	{
		return mId;
	}
}
