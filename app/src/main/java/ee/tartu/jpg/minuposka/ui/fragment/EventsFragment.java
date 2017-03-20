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

import java.util.Set;

import ee.tartu.jpg.minuposka.R;
import ee.tartu.jpg.minuposka.service.DataUpdateService;
import ee.tartu.jpg.minuposka.ui.adapter.EventsAdapter;
import ee.tartu.jpg.stuudium.data.Event;

/**
 * Created by gregor on 9/14/2015.
 */
public class EventsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "EventsFragment";

    public static long lastUpdateMs;

    private RecyclerView vRecyclerView;
    public SwipeRefreshLayout vSwipeRefreshLayout;

    private EventsAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events, container, false);
        vSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        vSwipeRefreshLayout.setOnRefreshListener(this);
        vSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.primary));
        vSwipeRefreshLayout.setEnabled(true);

        vRecyclerView = (RecyclerView) view.findViewById(R.id.cardList);
        vRecyclerView.setHasFixedSize(false);
        final LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        vRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // So the user didn't accidentally refresh on pulling up
                vSwipeRefreshLayout.setEnabled(llm.findFirstCompletelyVisibleItemPosition() == 0 || (llm.findFirstVisibleItemPosition() == 0 && llm.findFirstCompletelyVisibleItemPosition() == -1));
            }
        });
        vRecyclerView.setLayoutManager(llm);
        return view;
    }


    public void clearEvents() {
        Log.d(TAG, "Clearing events");
        new Handler(getActivity().getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                clearEventsOnThread();
            }
        });
    }

    private void clearEventsOnThread() {
        Log.d(TAG, "Clearing events on thread");
        vRecyclerView.setAdapter(null);
    }


    public void setEvents(final Set<Event> events) {
        Log.d(TAG, "Setting events");
        new Handler(getActivity().getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                setEventsOnThread(events);
            }
        });
    }

    public void setEventsOnThread(Set<Event> events) {
        Log.d(TAG, "Setting events on thread");
        if (adapter == null) {
            adapter = new EventsAdapter(getActivity(), events);
            vRecyclerView.setAdapter(adapter);
        } else {
            adapter.setEvents(events);
        }
    }

    @Override
    public void onRefresh() {
        Log.i(TAG, "Events manual refresh");
        DataUpdateService.startStuudiumEventsUpdate(getActivity(), true);
    }

}
