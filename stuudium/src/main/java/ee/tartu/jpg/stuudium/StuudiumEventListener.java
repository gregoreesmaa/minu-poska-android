package ee.tartu.jpg.stuudium;

import android.graphics.Bitmap;

import java.util.Map;

import ee.tartu.jpg.stuudium.data.Assignment;
import ee.tartu.jpg.stuudium.data.DataSet;
import ee.tartu.jpg.stuudium.data.Event;
import ee.tartu.jpg.stuudium.data.Journal;
import ee.tartu.jpg.stuudium.data.Person;
import ee.tartu.jpg.stuudium.data.User;

public interface StuudiumEventListener {
	void onStuudiumLogin();

	void onStuudiumLogout();

	void onAvatarLoaded(Person p, Bitmap bitmap, int size);

	void onJournalsLoaded(Person p, DataSet<Journal> journals);

	void onEventsLoaded(Person p, DataSet<Event> events);

	void onNewEvent(Person p, Event event, boolean manualUpdate);

	void onAssignmentsLoaded(Person p, DataSet<Assignment> assignments, boolean manualUpdate);

	void onAssignmentPushCompleted(Person p, Assignment assignment);

	void onUserDataLoaded(User u, boolean manualUpdate);

	void onPeopleChanged(Map<String, Person> people);

	void onLoadingStarted();

	void onLoadingFinished();

	void onError(final String str);

}
