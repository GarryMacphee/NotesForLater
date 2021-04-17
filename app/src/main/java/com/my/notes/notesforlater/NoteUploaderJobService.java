package com.my.notes.notesforlater;

import android.annotation.SuppressLint;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.net.Uri;
import android.os.AsyncTask;

public class NoteUploaderJobService extends JobService
{
	public static String EXTRA_DATA_URI = "data_uri";
	private NotesUploader mNotesUploader;

	public NoteUploaderJobService()
	{
	}

	@SuppressLint("StaticFieldLeak")
	@Override
	public boolean onStartJob(JobParameters params)
	{
		AsyncTask<JobParameters, Void, Void> task = new AsyncTask<JobParameters, Void, Void>()
		{
			@Override
			protected Void doInBackground(JobParameters... jobParameters)
			{
				JobParameters jobParams = jobParameters[0];
				String stringDataUri = jobParams.getExtras().getString(EXTRA_DATA_URI);
				Uri dataUri = Uri.parse(stringDataUri);
				mNotesUploader.doUpload(dataUri);

				if (!mNotesUploader.isCanceled())
				{
					jobFinished(jobParams, false);
				}

				return null;
			}
		};

		mNotesUploader = new NotesUploader(this);

		task.execute(params);


		return true;
	}


	@Override
	public boolean onStopJob(JobParameters params)
	{
		mNotesUploader.cancel();
		return true;
	}


}