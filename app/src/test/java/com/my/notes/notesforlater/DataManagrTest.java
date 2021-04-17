package com.my.notes.notesforlater;

import com.my.notes.notesforlater.courses.CourseInfo;
import com.my.notes.notesforlater.data.DataManager;
import com.my.notes.notesforlater.notes.NoteInfo;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DataManagrTest
{
	public static DataManager sDataManager;

	@BeforeClass
	public static void classSetup()
	{
		sDataManager = DataManager.getInstance();
	}

	@Before
	public void setup()
	{
		sDataManager.getNotes().clear();
		sDataManager.initializeExampleNotes();
	}

	@Test
	public void createNewNote()
	{
		//set up
		final CourseInfo course = sDataManager.getCourse("android_async");
		final String noteTitle = "Test note title";
		final String noteText = "This is the body text of my test note";

		//action
		int noteIndex = sDataManager.createNewNote();
		NoteInfo newNote = sDataManager.getNotes().get(noteIndex);
		newNote.setCourse(course);
		newNote.setTitle(noteTitle);
		newNote.setText(noteText);

		//assert
		NoteInfo compareNote = sDataManager.getNotes().get(noteIndex);
		assertEquals(course, compareNote.getCourse());
		assertEquals(noteTitle, compareNote.getTitle());
		assertEquals(noteText, compareNote.getText());
	}


	@Test
	public void findSimilarNotes()
	{
		//set up
		final CourseInfo course = sDataManager.getCourse("android_async");
		final String noteTitle = "Test note title";
		final String noteText1 = "This is the body text of my test note";
		final String noteText2 = "This is the body of my second test note";

		//perform action
		int noteIndex1 = sDataManager.createNewNote();
		NoteInfo newNote1 = sDataManager.getNotes().get(noteIndex1);
		newNote1.setCourse(course);
		newNote1.setTitle(noteTitle);
		newNote1.setText(noteText1);

		//assert
		int foundIndex1 = sDataManager.findNote(newNote1);
		assertEquals(noteIndex1, foundIndex1);
	}


	@Test
	public void createNewNoteOneStepCreation()
	{
		//set up
		final CourseInfo course = sDataManager.getCourse("android_async");
		final String noteTitle = "Test note title";
		final String noteText1 = "This is the body text of my test note";
		final String noteText2 = "This is the body of my second test note";

		int noteIndex1 = sDataManager.createNewNote(course, noteTitle, noteText1);

		NoteInfo compareNote = sDataManager.getNotes().get(noteIndex1);
		assertEquals(noteTitle, compareNote.getTitle());
		assertEquals(noteText1, compareNote.getText());
		assertEquals(course, compareNote.getCourse());
	}
}
