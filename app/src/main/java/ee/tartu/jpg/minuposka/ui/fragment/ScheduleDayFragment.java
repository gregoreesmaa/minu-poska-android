package ee.tartu.jpg.minuposka.ui.fragment;

import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import ee.tartu.jpg.minuposka.R;
import ee.tartu.jpg.minuposka.ui.MyScheduleActivity;
import ee.tartu.jpg.minuposka.ui.animation.ViewWeightAnimationWrapper;
import ee.tartu.jpg.minuposka.ui.base.TimeTableBaseActivity;
import ee.tartu.jpg.minuposka.ui.util.Calculations;
import ee.tartu.jpg.minuposka.ui.util.DataUtils;
import ee.tartu.jpg.minuposka.ui.util.TextUtils;
import ee.tartu.jpg.timetable.Timetable;
import ee.tartu.jpg.timetable.TimetableUtils;
import ee.tartu.jpg.timetable.data.Day;
import ee.tartu.jpg.timetable.data.LessonPeriod;
import ee.tartu.jpg.timetable.data.TimeTableSchedule;
import ee.tartu.jpg.timetable.utils.Filter;

/**
 * Creates a view with all schedules for one day
 */
public class ScheduleDayFragment extends Fragment {

    private static final String TAG = "ScheduleDayFragment";

    private static final String ARG_DAY = "ARG_DAY";

    private TimeTableBaseActivity activity;
    private LinearLayout expandedScheduleLayout;
    private Day day;

    public static ScheduleDayFragment newInstance(Day day) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_DAY, day);
        ScheduleDayFragment fragment = new ScheduleDayFragment();
        fragment.day = day;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (TimeTableBaseActivity) getActivity();
        day = (Day) getArguments().getSerializable(ARG_DAY);
    }

    //private static Toast currToast;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedules, container, false);
        LinearLayout vLayout = (LinearLayout) view.findViewById(R.id.container);
        Timetable timetable = activity.getTimetable();
        if (timetable != null) {
            List<TimeTableSchedule> timeTableSchedules;
            if (activity.getTimetableFilter() == null) {
                timeTableSchedules = new ArrayList<>();
            } else {
                Filter<TimeTableSchedule> filter = activity.getTimetableFilter().getFilter(day);
                Comparator<? super TimeTableSchedule> comparator = TimetableUtils.classroomComparator;
                if (filter == null || comparator == null || !activity.hasTimetable()) {
                    timeTableSchedules = new ArrayList<>();
                } else {
                    timeTableSchedules = timetable.getAll(TimeTableSchedule.class, filter, comparator);
                }
            }
            int rows = timetable.getCount(LessonPeriod.class);
            List<List<TimeTableSchedule>> schedules = new ArrayList<>(rows);
            for (int i = 0; i < rows; i++) {
                schedules.add(new ArrayList<TimeTableSchedule>());
            }
            for (TimeTableSchedule timeTableSchedule : timeTableSchedules) {
                int row = activity.getListRow(timeTableSchedule.getPeriod().period);
                List<TimeTableSchedule> parallelClasses = schedules.get(row);
                parallelClasses.add(timeTableSchedule);
            }
            if (activity instanceof MyScheduleActivity || DataUtils.isTablet(getActivity())) {
                View headerView = inflater.inflate(R.layout.item_schedule_day, null);
                TextView dayView = (TextView) headerView.findViewById(R.id.scheduleDay);
                dayView.setText(dayNameToRes(day.getName(), false));
                vLayout.addView(headerView);
            }
            for (int i = 0; i < rows; i++) {
                List<TimeTableSchedule> parallelClasses = schedules.get(i);
                View rowView = inflater.inflate(R.layout.fragment_schedules_row, null);
                LinearLayout dataContainer = (LinearLayout) rowView.findViewById(R.id.data_container);

                for (TimeTableSchedule timeTableSchedule : parallelClasses) {
                    final LinearLayout scheduleLayout = (LinearLayout) inflater.inflate(R.layout.item_schedule, dataContainer, false);

                    TextView subjectView = (TextView) scheduleLayout.findViewById(R.id.scheduleSubject);
                    TextView classroomView = (TextView) scheduleLayout.findViewById(R.id.scheduleClassroom);
                    TextView teacherView = (TextView) scheduleLayout.findViewById(R.id.scheduleTeacher);

                    final String subject = TextUtils.translateFromEstonian(activity, timeTableSchedule.getSubjectName());
                    final String classroomNumber = TextUtils.translateFromEstonian(activity, timeTableSchedule.getClassroomNumber());
                    final String teacherName = timeTableSchedule.getTeacherName();
                    final String cn = timeTableSchedule.getIncludedClasses(true);

                    String classStr = "";
                    if (timeTableSchedule.getClazzCount() > 1 || !(activity.getTimetableFilter().type == 0 || activity.getTimetableFilter().type == 1)) {
                        classStr = " (" + cn + ")";
                    }

                    subjectView.setText(subject + classStr);
                    classroomView.setText(classroomNumber);
                    teacherView.setText(teacherName);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        int normalColor = timeTableSchedule.getTeacherColor();
                        ColorStateList colorStateList = ColorStateList.valueOf(Color.WHITE);
                        RippleDrawable rippleDrawable = new RippleDrawable(colorStateList, new ColorDrawable(normalColor), null);
                        scheduleLayout.setBackground(rippleDrawable);
                    } else {
                        scheduleLayout.setBackgroundColor(timeTableSchedule.getTeacherColor());
                    }
                    if (Calculations.isDarkColor(timeTableSchedule.getTeacherColor())) {
                        subjectView.setTextColor(ContextCompat.getColor(activity, R.color.primary_text_light));
                        classroomView.setTextColor(ContextCompat.getColor(activity, R.color.primary_text_light));
                        teacherView.setTextColor(ContextCompat.getColor(activity, R.color.primary_text_light));
                        classroomView.setAlpha(0.70F);
                        teacherView.setAlpha(0.70F);
                    } else {
                        subjectView.setTextColor(ContextCompat.getColor(activity, R.color.primary_text));
                        classroomView.setTextColor(ContextCompat.getColor(activity, R.color.primary_text));
                        teacherView.setTextColor(ContextCompat.getColor(activity, R.color.primary_text));
                        classroomView.setAlpha(0.54F);
                        teacherView.setAlpha(0.54F);
                    }
                    dataContainer.addView(scheduleLayout);
                    /*
                    String row1 = subject;
                    String row2 = cn;
                    String row3_1 = teacherName;
                    String row3_2 = classroomNumber;
                    String row3 = "";
                    if (!row3_1.isEmpty()) {
                        row3 += row3_1;
                    }
                    if (!row3_2.isEmpty()) {
                        if (!row3.isEmpty()) {
                            row3 += ", ";
                        }
                        row3 += "klassis " + row3_2;
                    }
                    String msg = "";
                    if (!row1.isEmpty()) {
                        msg += row1;
                    }
                    if (!row2.isEmpty()) {
                        if (!msg.isEmpty()) {
                            msg += "\n";
                        }
                        msg += row2;
                    }
                    if (!row3.isEmpty()) {
                        if (!msg.isEmpty()) {
                            msg += "\n";
                        }
                        msg += row3;
                    }
                    final String finalMsg = msg;*/
                    scheduleLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            /*if (currToast != null) {
                                currToast.cancel();
                            }
                            currToast = Toast.makeText(v.getContext(), finalMsg, Toast.LENGTH_LONG);
                            currToast.show();*/
                            if (expandedScheduleLayout == scheduleLayout) {
                                ViewWeightAnimationWrapper collapseAnimationWrapper = new ViewWeightAnimationWrapper(scheduleLayout);
                                ObjectAnimator collapseAnim = ObjectAnimator.ofFloat(
                                        collapseAnimationWrapper,
                                        "weight",
                                        collapseAnimationWrapper.getWeight(),
                                        1);
                                collapseAnim.setDuration(250);
                                collapseAnim.start();
                                expandedScheduleLayout = null;
                            } else {
                                ViewWeightAnimationWrapper expandAnimationWrapper = new ViewWeightAnimationWrapper(scheduleLayout);
                                ObjectAnimator expandAnim = ObjectAnimator.ofFloat(
                                        expandAnimationWrapper,
                                        "weight",
                                        expandAnimationWrapper.getWeight(),
                                        30);
                                expandAnim.setDuration(250);
                                expandAnim.start();
                                if (expandedScheduleLayout != null) {
                                    ViewWeightAnimationWrapper collapseAnimationWrapper = new ViewWeightAnimationWrapper(expandedScheduleLayout);
                                    ObjectAnimator collapseAnim = ObjectAnimator.ofFloat(
                                            collapseAnimationWrapper,
                                            "weight",
                                            collapseAnimationWrapper.getWeight(),
                                            1);
                                    collapseAnim.setDuration(250);
                                    collapseAnim.start();
                                }
                                expandedScheduleLayout = scheduleLayout;
                            }
                        }
                    });
                }
                vLayout.addView(rowView);
            }
        }
        return view;
    }

    public CharSequence getDayTitle(Context context) {
        return context.getString(dayNameToRes(day.getName(), true));
    }

    private static int dayNameToRes(String name, boolean shrt) {
        name = name.toLowerCase().trim();
        if (name.startsWith("esm") || name.startsWith("mon"))
            return shrt ? R.string.monday_short : R.string.monday;
        else if (name.startsWith("tei") || name.startsWith("tue"))
            return shrt ? R.string.tuesday_short : R.string.tuesday;
        else if (name.startsWith("kol") || name.startsWith("wed"))
            return shrt ? R.string.wednesday_short : R.string.wednesday;
        else if (name.startsWith("nel") || name.startsWith("thu"))
            return shrt ? R.string.thursday_short : R.string.thursday;
        else if (name.startsWith("ree") || name.startsWith("fri"))
            return shrt ? R.string.friday_short : R.string.friday;
        else if (name.startsWith("lau") || name.startsWith("sat"))
            return shrt ? R.string.saturday_short : R.string.saturday;
        else if (name.startsWith("püh") || name.startsWith("sun"))
            return shrt ? R.string.sunday_short : R.string.sunday;
        return 0;
    }
}