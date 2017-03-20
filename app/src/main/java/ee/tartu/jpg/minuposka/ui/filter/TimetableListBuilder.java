package ee.tartu.jpg.minuposka.ui.filter;

import android.app.AlertDialog.Builder;
import android.content.DialogInterface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import ee.tartu.jpg.minuposka.PoskaApplication;
import ee.tartu.jpg.minuposka.R;
import ee.tartu.jpg.minuposka.ui.base.TimeTableBaseActivity;
import ee.tartu.jpg.minuposka.ui.util.TextUtils;
import ee.tartu.jpg.timetable.Timetable;

/**
 * Builds a list of timetables to choose.
 */
public class TimetableListBuilder extends Builder {

    public TimetableListBuilder(final TimeTableBaseActivity context) {
        super(context);
        setTitle(context.getString(R.string.select_timetable_title));

        final ArrayList<Timetable> timetables = new ArrayList<Timetable>();
        for (String key : PoskaApplication.timetables.timetables.keySet()) {
            timetables.add(PoskaApplication.timetables.timetables.get(key));
        }
        // e.g. Periood 5; Arvestus 4; Periood 4; Arvestus 3...
        Collections.sort(timetables, new Comparator<Timetable>() {

            @Override
            public int compare(Timetable lhs, Timetable rhs) {
                String s1 = lhs.getName();
                String timetableType1 = "";
                int timetableNumber1 = 0;
                {
                    boolean started = false;
                    boolean stopped = false;
                    char[] cs = s1.toCharArray();
                    for (char c : cs) {
                        if (Character.isDigit(c) && !stopped) {
                            started = true;
                            timetableNumber1 *= 10;
                            timetableNumber1 += Integer.parseInt(Character.toString(c));
                        } else {
                            if (started) {
                                stopped = true;
                            }
                            timetableType1 += c;
                        }
                    }
                }
                String s2 = rhs.getName();
                String timetableType2 = "";
                int timetableNumber2 = 0;
                {
                    boolean started = false;
                    boolean stopped = false;
                    char[] cs = s2.toCharArray();
                    for (char c : cs) {
                        if (Character.isDigit(c) && !stopped) {
                            started = true;
                            timetableNumber2 *= 10;
                            timetableNumber2 += Integer.parseInt(Character.toString(c));
                        } else {
                            if (started) {
                                stopped = true;
                            }
                            timetableType2 += c;
                        }
                    }
                }
                if (timetableNumber1 < timetableNumber2) {
                    return 1;
                } else if (timetableNumber1 > timetableNumber2) {
                    return -1;
                }
                return timetableType1.compareTo(timetableType2);
            }

        });
        final CharSequence[] titles = new CharSequence[timetables.size()];
        for (int i = 0; i < titles.length; i++) {
            titles[i] = TextUtils.translateFromEstonian(context, timetables.get(i).getName());
        }
        setItems(titles, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                context.setTimetable(timetables.get(i), true, false);
            }
        });
    }

}
