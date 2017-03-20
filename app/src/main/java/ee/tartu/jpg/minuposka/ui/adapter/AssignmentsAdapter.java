package ee.tartu.jpg.minuposka.ui.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import ee.tartu.jpg.minuposka.R;
import ee.tartu.jpg.minuposka.ui.util.TextUtils;
import ee.tartu.jpg.stuudium.data.Assignment;
import ee.tartu.jpg.stuudium.data.DataSet;
import ee.tartu.jpg.stuudium.data.Event;

/**
 * Adapter for Stuudium assignments.
 */
public class AssignmentsAdapter extends RecyclerView.Adapter<AssignmentsAdapter.AssignmentDayViewHolder> {

    private static final int TODO_TOGGLE_DURATION = 120;
    private static final float TODO_DISABLED_OPACITY = 0.38F;

    private static final Comparator<Date> dateComparator = new Comparator<Date>() {
        @Override
        public int compare(Date lhs, Date rhs) {
            return lhs.compareTo(rhs);
        }
    };

    private final Context mContext;
    private LoadEarlierListener mLoadEarlierListener;

    private TreeMap<Date, DataSet<Assignment>> assignmentsMap;


    public AssignmentsAdapter(Context context, LoadEarlierListener loadEarlierListener, Set<Assignment> assignmentSet, Date since) {
        mContext = context;
        mLoadEarlierListener = loadEarlierListener;
        setAssignments(assignmentSet, since);
    }

    public void setAssignments(Set<Assignment> assignmentSet, Date since) {
        if (assignmentsMap != null) assignmentsMap.clear();
        else assignmentsMap = new TreeMap<>(dateComparator);
        addAssignments(assignmentSet, since);
        notifyDataSetChanged();
    }

    public void addAssignments(Set<Assignment> assignmentSet, Date since) {
        if (assignmentSet == null || since == null)
            return;
        for (Assignment assignment : assignmentSet) {
            Date date = assignment.getContent().getDeadline();
            if (date.before(since))
                continue;
            DataSet<Assignment> events = assignmentsMap.get(date);
            if (events == null) {
                events = new DataSet<>();
                assignmentsMap.put(date, events);
            }
            events.add(assignment);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? 1 : 0;
    }

    @Override
    public AssignmentDayViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 1) {
            return new ShowEarlierViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_assignment_load_earlier, parent, false));
        }
        return new AssignmentDayViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_assignment_day, parent, false));
    }

    @Override
    public void onBindViewHolder(AssignmentDayViewHolder holder, int position) {
        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
        lp.setMargins(lp.leftMargin, position == 0 ? mContext.getResources().getDimensionPixelSize(R.dimen.spacing_large) : mContext.getResources().getDimensionPixelSize(R.dimen.spacing_medium), lp.rightMargin, position == getItemCount() - 1 ? mContext.getResources().getDimensionPixelSize(R.dimen.spacing_large) : mContext.getResources().getDimensionPixelSize(R.dimen.spacing_medium));

        if (holder instanceof ShowEarlierViewHolder)
            return;
        position--;

        Map.Entry<Date, DataSet<Assignment>> entry = ((Map.Entry<Date, DataSet<Assignment>>) assignmentsMap.entrySet().toArray()[position]);
        DataSet<Assignment> assignments = entry.getValue();

        holder.vDay.setText(TextUtils.getDayStringResource(mContext, entry.getKey()));
        holder.vDate.setText(Event.dateOnlyFormat.format(entry.getKey()));
        holder.vContainer.removeAllViews();
        for (Assignment assignment : assignments) {
            final Assignment.Content content = assignment.getContent();
            if (content == null)
                continue;

            LinearLayout eventView = (LinearLayout) layoutInflater.inflate(R.layout.card_assignment_day_data, null);
            final CheckBox completedCheckbox = (CheckBox) eventView.findViewById(R.id.completed_checkbox);
            final TextView subjectView = (TextView) eventView.findViewById(R.id.subject_view);
            final TextView descriptionView = (TextView) eventView.findViewById(R.id.description_view);

            String subjectStr = content.getSubject();
            String descriptionStr = content.getDescription();

            if (content.getType().equals(Assignment.TYPE_TEST)) {
                subjectStr = TextUtils.addAfter(subjectStr, mContext.getString(R.string.test), " - ");
            }

            if (subjectStr != null && !subjectStr.isEmpty()) {
                subjectView.setVisibility(View.VISIBLE);
                subjectView.setText(TextUtils.linkify(TextUtils.html(TextUtils.translateFromEstonian(mContext, TextUtils.capitalize(subjectStr)))), TextView.BufferType.SPANNABLE);
                subjectView.setMovementMethod(LinkMovementMethod.getInstance());
            } else {
                subjectView.setVisibility(View.GONE);
            }

            if (descriptionStr != null && !descriptionStr.isEmpty()) {
                descriptionView.setVisibility(View.VISIBLE);
                descriptionView.setText(TextUtils.linkify(TextUtils.html(TextUtils.capitalize(descriptionStr))), TextView.BufferType.SPANNABLE);
                descriptionView.setMovementMethod(LinkMovementMethod.getInstance());
            } else {
                descriptionView.setVisibility(View.GONE);
            }

            completedCheckbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    content.pushCompleted(completedCheckbox.isChecked());
                }
            });
            completedCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(final CompoundButton row, boolean isChecked) {
                    if (isChecked) {
                        if (Build.VERSION.SDK_INT >= 16) {
                            subjectView.animate().alpha(TODO_DISABLED_OPACITY).setDuration(TODO_TOGGLE_DURATION).withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    subjectView.setPaintFlags(row.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                                }
                            }).start();
                            descriptionView.animate().alpha(TODO_DISABLED_OPACITY).setDuration(TODO_TOGGLE_DURATION).withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    descriptionView.setPaintFlags(row.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                                }
                            }).start();
                        } else {
                            subjectView.setPaintFlags(row.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                            descriptionView.setPaintFlags(row.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                            subjectView.animate().alpha(TODO_DISABLED_OPACITY).setDuration(TODO_TOGGLE_DURATION).start();
                            descriptionView.animate().alpha(TODO_DISABLED_OPACITY).setDuration(TODO_TOGGLE_DURATION).start();
                        }
                    } else {
                        subjectView.setPaintFlags(row.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                        descriptionView.setPaintFlags(row.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                        subjectView.animate().alpha(1).setDuration(TODO_TOGGLE_DURATION).start();
                        descriptionView.animate().alpha(1).setDuration(TODO_TOGGLE_DURATION).start();
                    }
                }

            });
            completedCheckbox.setChecked(content.isCompletedUnpushed());

            holder.vContainer.addView(eventView);
        }
    }

    @Override
    public int getItemCount() {
        return assignmentsMap.size() + 1;
    }

    public class AssignmentDayViewHolder extends RecyclerView.ViewHolder {
        TextView vDay;
        TextView vDate;
        LinearLayout vContainer;

        public AssignmentDayViewHolder(View v) {
            super(v);
            vDay = (TextView) v.findViewById(R.id.label_left);
            vDate = (TextView) v.findViewById(R.id.label_right);
            vContainer = (LinearLayout) v.findViewById(R.id.data_container);
        }
    }

    public class ShowEarlierViewHolder extends AssignmentDayViewHolder {
        Button vEarlierButton;

        public ShowEarlierViewHolder(View v) {
            super(v);
            vEarlierButton = (Button) v.findViewById(R.id.button_load_earlier);
            vEarlierButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mLoadEarlierListener.onEarlierRequested();
                }
            });
        }
    }

    public interface LoadEarlierListener {
        void onEarlierRequested();
    }

}
