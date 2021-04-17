package com.my.notes.notesforlater.courses;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.google.android.material.snackbar.Snackbar;
import com.my.notes.notesforlater.R;

import java.util.List;

public class CourseRecyclerAdapter extends RecyclerView.Adapter<CourseRecyclerAdapter.CourseRecyclerViewHolder>
{
	private final Context mContext;
	private final List<CourseInfo> mCourses;
	private final LayoutInflater mLayoutInflater;
	private Cursor mCursor;

	public CourseRecyclerAdapter(Context context, List<CourseInfo> courses)
	{
		mContext = context;
		mCourses = courses;
		mLayoutInflater = LayoutInflater.from(mContext);
	}

	@NonNull
	@Override
	public CourseRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
	{
		View itemView = mLayoutInflater.inflate(R.layout.item_course_list, parent, false);
		return new CourseRecyclerViewHolder(itemView);
	}

	@Override
	public void onBindViewHolder(@NonNull CourseRecyclerViewHolder holder, int position)
	{
		CourseInfo course = mCourses.get(position);
		holder.mTextCourse.setText(course.getTitle());
		holder.mCurrentPosition = position;
	}

	@Override
	public int getItemCount()
	{
		return mCourses.size();
	}


	public class CourseRecyclerViewHolder extends ViewHolder
	{
		public final TextView mTextCourse;
		public int mCurrentPosition;

		public CourseRecyclerViewHolder(@NonNull View itemView)
		{
			super(itemView);
			mTextCourse = itemView.findViewById(R.id.text_course);

			itemView.setOnClickListener(v ->
			{
				Snackbar.make(v, mCourses.get(mCurrentPosition).getTitle(), Snackbar.LENGTH_LONG);
			});
		}
	}
}
