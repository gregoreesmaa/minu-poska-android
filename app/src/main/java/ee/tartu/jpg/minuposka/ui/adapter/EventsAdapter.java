package ee.tartu.jpg.minuposka.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import ee.tartu.jpg.minuposka.R;
import ee.tartu.jpg.minuposka.ui.util.TextUtils;
import ee.tartu.jpg.stuudium.data.DataSet;
import ee.tartu.jpg.stuudium.data.Event;
import ee.tartu.jpg.stuudium.data.Person;

/**
 * An adapter for Stuudium Events.
 */
public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventDayViewHolder> {

    private static final Comparator<Date> dateComparator = new Comparator<Date>() {
        @Override
        public int compare(Date lhs, Date rhs) {
            return rhs.compareTo(lhs);
        }
    };

    private final Context mContext;

    private TreeMap<Date, DataSet<Event>> eventMap;

    public EventsAdapter(Context context, Set<Event> eventSet) {
        mContext = context;
        setEvents(eventSet);
    }

    public void setEvents(Set<Event> eventSet) {
        if (eventMap != null) {
            eventMap.clear();
        } else {
            eventMap = new TreeMap<>(dateComparator);
        }
        addEvents(eventSet);
        notifyDataSetChanged();
    }

    private void addEvents(Set<Event> eventSet) {
        if (eventSet == null)
            return;
        for (Event event : eventSet) {
            Date date = event.getCreatedAtDate();
            DataSet<Event> events = eventMap.get(date);
            if (events == null) {
                events = new DataSet<>();
                eventMap.put(date, events);
            }
            events.add(event);
        }
    }

    @Override
    public EventDayViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_event_day, parent, false);
        return new EventDayViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(EventDayViewHolder holder, int position) {
        Map.Entry<Date, DataSet<Event>> entry = ((Map.Entry<Date, DataSet<Event>>) eventMap.entrySet().toArray()[position]);
        DataSet<Event> events = entry.getValue();

        holder.vDay.setText(TextUtils.getDayStringResource(mContext, entry.getKey()));
        holder.vDate.setText(Event.dateOnlyFormat.format(entry.getKey()));

        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
        lp.setMargins(lp.leftMargin, position == 0 ? mContext.getResources().getDimensionPixelSize(R.dimen.spacing_large) : mContext.getResources().getDimensionPixelSize(R.dimen.spacing_medium), lp.rightMargin, position == getItemCount() - 1 ? mContext.getResources().getDimensionPixelSize(R.dimen.spacing_large) : mContext.getResources().getDimensionPixelSize(R.dimen.spacing_medium));
        holder.vContainer.removeAllViews();
        for (Event event : events) {
            Event.Content content = event.getContent();
            if (content == null)
                continue;

            LinearLayout eventView = (LinearLayout) layoutInflater.inflate(R.layout.card_event_day_data, null);
            TextView gradeView = (TextView) eventView.findViewById(R.id.grade_view);
            TextView contentView = (TextView) eventView.findViewById(R.id.content_view);
            TextView descriptionView = (TextView) eventView.findViewById(R.id.description_view);

            String gradesStr = "";
            String contentStr = "";
            String descriptionStr = "";

            if (event.getType().equals("grade")) {
                // Set the grade and the extra labels
                String gradeSeparator = mContext.getString(R.string.grade_separator);
                Event.Grade grade = content.getGrade();
                if (grade != null) {
                    String gradeStr = grade.getValue().getCurrent();
                    gradeStr = TextUtils.colorize(mContext, gradeStr, gradeStr.equals("F") ? R.color.negative_grade : R.color.neutral_grade);
                    gradesStr = TextUtils.addAfter(gradesStr, gradeStr, gradeSeparator);
                }
                Event.ExtraLabels els = content.getExtraLabels();
                if (els != null) {
                    for (Event.ExtraLabel el : els.getLabels()) {
                        String labelStr = el.getLabelShort();
                        labelStr = TextUtils.colorize(mContext, labelStr, TextUtils.getExtraLabelColor(el));
                        gradesStr = TextUtils.addAfter(gradesStr, labelStr, gradeSeparator);
                    }
                }

                // Set the content
                contentStr = content.getComment();

                // Set the description
                Event.Lesson lesson = content.getLesson();
                String description1 = "";
                if (lesson != null) {
                    Date lessonDate = lesson.getTime();
                    if (lessonDate != null) {
                        String date = lessonDate == null ? "nulldate" : Event.dateOnlyFormat.format(lessonDate);
                        description1 = String.format("<strong>%s, %s</strong>", TextUtils.translateFromEstonian(mContext, lesson.getSubject()), date);
                    } else {
                        description1 = String.format("<strong>%s</strong>", TextUtils.translateFromEstonian(mContext, lesson.getSubject()));
                    }
                }
                String description2 = "";
                String label = content.getLabel();
                if (label != null) {
                    description2 = label;
                }
                String lessondescription = lesson.getDescription();
                if (lessondescription != null) {
                    if (!description2.isEmpty()) {
                        description2 += "; ";
                    }
                    description2 += lessondescription;
                }

                String secondrowFormat = "%s";
                if (!description2.isEmpty()) {
                    description2 = description2.substring(0, 1).toUpperCase() + description2.substring(1);
                    secondrowFormat = "%s<strong>:</strong> %s";
                }

                descriptionStr = String.format(secondrowFormat, description1, description2);
            } else {
                // Set the content
                contentStr = String.format("%s<strong>:</strong> %s", content.getLabel(), content.getSummary());

                // Set the description
                Person creator = event.getCreator();
                if (creator != null) {
                    descriptionStr = creator.getFullName();
                }
            }

            // Show the content
            if (gradesStr != null && !gradesStr.isEmpty()) {
                gradeView.setVisibility(View.VISIBLE);
                gradeView.setText(TextUtils.html(gradesStr), TextView.BufferType.SPANNABLE);
                gradeView.setMovementMethod(LinkMovementMethod.getInstance());
            } else {
                gradeView.setVisibility(View.GONE);
            }

            if (contentStr != null && !contentStr.isEmpty()) {
                contentView.setVisibility(View.VISIBLE);
                contentView.setText(TextUtils.linkify(TextUtils.html(TextUtils.capitalize(contentStr))), TextView.BufferType.SPANNABLE);
                contentView.setMovementMethod(LinkMovementMethod.getInstance());
            } else {
                contentView.setVisibility(View.GONE);
            }

            if (descriptionStr != null && !descriptionStr.isEmpty()) {
                descriptionView.setVisibility(View.VISIBLE);
                descriptionView.setText(TextUtils.linkify(TextUtils.html(TextUtils.capitalize(descriptionStr))), TextView.BufferType.SPANNABLE);
                descriptionView.setMovementMethod(LinkMovementMethod.getInstance());
            } else {
                descriptionView.setVisibility(View.GONE);
            }


            holder.vContainer.addView(eventView);
        }
    }

    @Override
    public int getItemCount() {
        return eventMap.size();
    }

    public class EventDayViewHolder extends RecyclerView.ViewHolder {
        TextView vDay;
        TextView vDate;
        LinearLayout vContainer;

        public EventDayViewHolder(View v) {
            super(v);
            vDay = (TextView) v.findViewById(R.id.label_left);
            vDate = (TextView) v.findViewById(R.id.label_right);
            vContainer = (LinearLayout) v.findViewById(R.id.data_container);
        }
    }

}
