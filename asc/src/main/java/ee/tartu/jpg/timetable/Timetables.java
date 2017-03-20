package ee.tartu.jpg.timetable;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import ee.tartu.jpg.timetable.utils.TimetableDatabaseHelper;

/**
 * Created by gregor on 14-Dec-15.
 */
public class Timetables {

    private static final String TAG = "Timetables";

    private long lastcheck = 0;
    public long lastcheckSystem = 0;

    public TimetableDatabaseHelper db;
    public HashMap<String, Timetable> timetables = new HashMap<>();

    public Timetables(Context context, long lastcheckSystem) {
        db = new TimetableDatabaseHelper(context);
        this.lastcheckSystem = lastcheckSystem;
    }

    public void createOrUpdateTimetable(Timetable timetable, String data_source) {
        try {
            db.insertTimetableMeta(timetable.getId(), timetable.modified);
            // Clear schedules, because there is no way to uniquenize them properly.
            db.clearSchedules(timetable.getId());

            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(data_source));

            Document doc = documentBuilder.parse(is);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getDocumentElement().getChildNodes();
            for (int i = 0; i < nList.getLength(); i++) {
                Node nNode = nList.item(i);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    NodeList subList = eElement.getChildNodes();
                    if (subList.getLength() != 0) {
                        for (int j = 0; j < subList.getLength(); j++) {
                            Node subNode = subList.item(j);
                            if (subNode.getNodeType() == Node.ELEMENT_NODE) {
                                String name = subNode.getNodeName();
                                Element subElement = (Element) subNode;
                                loadData(name, subElement, timetable.getId());
                            }
                        }
                    }
                }
            }
            timetables.put(timetable.getId(), timetable);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            Log.e(TAG, "Failed to create or update timetable", e);
        }
    }

    public void removeTimetable(String id) {
        db.removeTimetable(id);
        timetables.remove(id);
    }

    public void loadTimetablesFromBase() {
        List<Timetable> timetableList = db.requestMetas();
        for (Timetable tt : timetableList) {
            timetables.put(tt.getId(), tt);
        }
    }

    private void loadData(String type, Element e, String timetableID) {
        try {
            switch (type) {
                case "day":
                    db.insertTimetableDay(toIntSafe(e.getAttribute("day")), e.getAttribute("name"), e.getAttribute("short"), timetableID);
                    break;
                case "period":
                    db.insertTimetablePeriod(toIntSafe(e.getAttribute("period")), e.getAttribute("starttime"), e.getAttribute("endtime"), timetableID);
                    break;
                case "subject":
                    db.insertTimetableSubject(toIntSafe(e.getAttribute("id").substring(1)), e.getAttribute("name"), e.getAttribute("short"), timetableID);
                    break;
                case "teacher":
                    db.insertTimetableTeacher(toIntSafe(e.getAttribute("id").substring(1)), e.getAttribute("name"), e.getAttribute("short"), Color.parseColor(e.getAttribute("color")), timetableID);
                    break;
                case "classroom":
                    db.insertTimetableClassroom(toIntSafe(e.getAttribute("id").substring(1)), e.getAttribute("name"), e.getAttribute("short"), timetableID);
                    break;
                case "class":
                    db.insertTimetableForm(toIntSafe(e.getAttribute("id").substring(1)), e.getAttribute("name"), e.getAttribute("short"), toIntSafe(e.getAttribute("teacherid"), 1), timetableID);
                    break;
                case "TimeTableSchedule":
                    String formIDs = e.getAttribute("ClassID").replace("*", "");
                    db.insertTimetableSchedule(toIntSafe(e.getAttribute("DayID")), toIntSafe(e.getAttribute("Period")), toIntSafe(e.getAttribute("SchoolRoomID"), 1), toIntSafe(e.getAttribute("TeacherID"), 1), toIntSafe(e.getAttribute("SubjectGradeID"), 1), formIDs, timetableID);
                    break;
            }
        } catch (NumberFormatException nfe) {
            Log.e(TAG, "Failed to load data", nfe);
        }
    }

    private int toIntSafe(String str, int startIdx) {
        if (str.length() > startIdx) {
            try {
                return Integer.parseInt(str.substring(startIdx));
            } catch (NumberFormatException nfe) {
                // we don't want these.
            }
        }
        return -1;
    }

    private int toIntSafe(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException nfe) {
            // we don't want these.
        }
        return -1;
    }

    public Timetable getPeriod(String id) {
        return timetables.get(id);
    }

    public boolean hasTimetable(String timetableID) {
        return timetables.containsKey(timetableID);
    }

    public long durationFromLastCheck() {
        return System.currentTimeMillis() - lastcheckSystem;
    }

    public void setLastCheck(long lastcheck) {
        this.lastcheck = lastcheck;
        this.lastcheckSystem = System.currentTimeMillis();
    }
}
