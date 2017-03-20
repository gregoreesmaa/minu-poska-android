package ee.tartu.jpg.minuposka.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import ee.tartu.jpg.minuposka.R;
import ee.tartu.jpg.minuposka.ui.adapter.AssignmentsAdapter;
import ee.tartu.jpg.minuposka.ui.base.BaseActivity;
import ee.tartu.jpg.minuposka.ui.util.DataUtils;
import ee.tartu.jpg.stuudium.JSONResponseHandler;
import ee.tartu.jpg.stuudium.ResponseHandler;
import ee.tartu.jpg.stuudium.Stuudium;
import ee.tartu.jpg.stuudium.data.Assignment;
import ee.tartu.jpg.stuudium.data.DataSet;
import ee.tartu.jpg.stuudium.data.Person;
import ee.tartu.jpg.stuudium.data.User;
import ee.tartu.jpg.stuudium.utils.DateUtils;

/**
 * Created by gregor on 9/14/2015.
 */
public class AssignmentsFragment extends Fragment implements AssignmentsAdapter.LoadEarlierListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "AssignmentsFragment";

    public static long lastUpdateMs;

    private RecyclerView vRecyclerView;
    public SwipeRefreshLayout vSwipeRefreshLayout;

    private AssignmentsAdapter adapter;

    private Date since;
    private Date until;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_assignments, container, false);
        vSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        vSwipeRefreshLayout.setOnRefreshListener(this);
        vSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.primary));

        vRecyclerView = (RecyclerView) view.findViewById(R.id.cardList);
        vRecyclerView.setHasFixedSize(false);
        final LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        vRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // So the user didn't accidentally refresh on pulling up
                vSwipeRefreshLayout.setEnabled(llm.findFirstCompletelyVisibleItemPosition() == 0);
            }
        });
        vRecyclerView.setLayoutManager(llm);
        return view;
    }

    public void clearAssignments() {
        new Handler(getActivity().getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                clearAssignmentsOnThread();
            }
        });
    }

    private void clearAssignmentsOnThread() {
        vRecyclerView.setAdapter(null);
    }

    public void setAssignments(final Set<Assignment> assignments) {
        Log.d(TAG, "Set assignments");
        new Handler(getActivity().getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                setAssignmentsOnThread(assignments);
            }
        });
    }

    public void setAssignmentsOnThread(Set<Assignment> assignments) {
        Log.d(TAG, "Setting assignments on thread");
        if (adapter == null) {
            adapter = new AssignmentsAdapter(getActivity(), this, assignments, getSince());
            vRecyclerView.setAdapter(adapter);
        } else {
            adapter.setAssignments(assignments, getSince());
        }
        if (!DataUtils.isTablet(getActivity())) {
            ((LinearLayoutManager) vRecyclerView.getLayoutManager()).scrollToPositionWithOffset(1, 0);
        }
    }

    public void addAssignments(final Set<Assignment> assignments) {
        Log.d(TAG, "Adding assignments");
        new Handler(getActivity().getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                addAssignmentsOnThread(assignments);
            }
        });
    }

    private void addAssignmentsOnThread(Set<Assignment> assignments) {
        Log.d(TAG, "Adding assignments on thread");
        if (assignments != null) {
            adapter.addAssignments(assignments, since);
            adapter.notifyDataSetChanged();
        }
    }

    public Date getSince() {
        if (since == null) {
            Calendar c = Calendar.getInstance();
            if (c.get(Calendar.HOUR_OF_DAY) > 15) since = DateUtils.getDateToday(); // excluding
            else since = DateUtils.getDateYesterday(); // excluding
        }
        return since;
    }

    private void setSince(Date d) {
        if (d != null) since = d;
    }

    private Date getUntil() {
        if (until == null) until = DateUtils.getDateInDays(60);
        return until;
    }

    private void setUntil(Date d) {
        if (d != null) until = d;
    }

    private void scrollAdapter(final int ps) {
        Log.d(TAG, "Scrolling adapter: " + ps);
        new Handler(getActivity().getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                scrollAdapterOnThread(ps);
            }
        });
    }

    private void scrollAdapterOnThread(int ps) {
        if (adapter == null) return;
        Log.d(TAG, "Scrolling adapter on thread: " + ps);
        final int offsetIdx = adapter.getItemCount() - ps;
        adapter.notifyDataSetChanged();
        if (DataUtils.isTablet(getActivity())) {
            if (ps != -1 && offsetIdx != 0) {
                ((LinearLayoutManager) vRecyclerView.getLayoutManager()).scrollToPositionWithOffset(offsetIdx + 1, 0);
                vRecyclerView.smoothScrollToPosition(offsetIdx);
            }
        } else if (ps != -1 && offsetIdx > 1) {
            ((LinearLayoutManager) vRecyclerView.getLayoutManager()).scrollToPositionWithOffset(offsetIdx + 1, 0);
            vRecyclerView.smoothScrollToPosition(offsetIdx);
        }
    }

    @Override
    public void onEarlierRequested() {
        if (!Stuudium.hasUser()) return;
        Log.i(TAG, "Requested earlier assignments");
        final Person p = Stuudium.getUser().getIdentity();
        if (p == null) return;

        // Set new request range
        setUntil(DateUtils.addToDate(getSince(), -1));
        setSince(DateUtils.addToDate(getSince(), -14));
        ((BaseActivity) getActivity()).refreshTitle();
        final int ps = adapter.getItemCount();
        vSwipeRefreshLayout.setRefreshing(true);
        // Custom assignment requesting so it could be scrolled
        p.requestAssignments(getSince(), getUntil(), new JSONResponseHandler() {

            @Override
            public void handle(JSONObject obj) throws JSONException {
                if (obj.has("array")) {
                    JSONArray arr = obj.getJSONArray("array");
                    final DataSet<Assignment> newAssignments = new DataSet<>();
                    for (int i = 0; i < arr.length(); i++) {
                        // We don't store old assignments
                        newAssignments.add(new Assignment(p.getId(), arr.getJSONObject(i)));
                    }
                    adapter.addAssignments(newAssignments, since);
                    scrollAdapter(ps);
                    new Handler(getActivity().getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            vSwipeRefreshLayout.setRefreshing(false);
                        }
                    });
                }
            }

        }, false, 20000, new ResponseHandler<Exception>() {
            @Override
            public void handle(Exception obj) {
                new Handler(getActivity().getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        vSwipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });
    }

    @Override
    public void onRefresh() {
        Log.i(TAG, "Assignments manual refresh");
        if (!Stuudium.hasUser()) return;
        if (Stuudium.isLoginExpired()) Stuudium.setLoginData(null);
        else {
            User u = Stuudium.getUser();
            if (u != null) for (Person p : u.getStudents()) {
                if (p == null) continue;
                p.requestAssignments(getSince(), DateUtils.addToDate(getSince(), 60), new ResponseHandler<Exception>() {
                    @Override
                    public void handle(Exception obj) {
                        new Handler(getActivity().getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Log.i(TAG, "Refreshing completed");
                                vSwipeRefreshLayout.setRefreshing(false);
                            }
                        });
                    }
                }, true);
            }
        }
    }

}
