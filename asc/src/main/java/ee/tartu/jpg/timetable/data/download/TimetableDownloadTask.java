package ee.tartu.jpg.timetable.data.download;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ee.tartu.jpg.timetable.Timetable;
import ee.tartu.jpg.timetable.TimetableUtils;
import ee.tartu.jpg.timetable.Timetables;

public class TimetableDownloadTask extends AsyncTask<String, Integer, Map<String, Timetable>> {

    private static final String TAG = "TimetableDownloadTask";

    private final String requestURL;
    private static final String charset = "windows-1257";
    private Timetables timetables;
    private Runnable onFinish;
    private Set<String> existedBefore = new HashSet<>();

    public TimetableDownloadTask(String requestURL, Timetables timetables, Runnable onFinish) {
        this.requestURL = requestURL;
        this.timetables = timetables;
        this.onFinish = onFinish;
    }

    @Override
    protected Map<String, Timetable> doInBackground(String... params) {
        Map<String, Timetable> map = new HashMap<String, Timetable>();

        try {
            URL url = new URL(requestURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            if (conn.getResponseCode() > 202) {
                return map;
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String str = in.readLine();
            long lastcheck = Long.parseLong(str);

            while ((str = in.readLine()) != null) {
                if (str.trim().length() == 0)
                    continue;
                String[] data = str.split(" ", 2);
                long lastchanged = Long.parseLong(data[0]);
                String filename = data[1];
                boolean has = timetables.hasTimetable(filename);
                if (has) {
                    existedBefore.add(filename);
                }
                if (has && lastchanged == timetables.getPeriod(filename).modified) {
                    // Timetable update not required as we already have the timetable and the modified dates match; Add to the map to show it still exists.
                    map.put(filename, null);
                } else {
                    map.put(filename, new Timetable(filename, lastchanged));
                }
            }
            timetables.setLastCheck(lastcheck);
            in.close();
        } catch (IOException e) {
            Log.e(TAG, "Error in downloading: " + e.toString(), e);
            return null;
        }
        timetables.db.getWritableDatabase().beginTransaction();
        for (String filename : map.keySet()) {
            Timetable t = map.get(filename);

            // Skip updated timetables
            if (t == null) continue;

            Log.d(TAG, "Updating timetable: " + filename);

            try {
                URL url = new URL(requestURL + filename);
                URLConnection conn = url.openConnection();
                StringBuilder source = new StringBuilder();
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), charset));
                String line;
                while ((line = br.readLine()) != null) {
                    // too much Log.v(TAG, "Line: " + line);
                    source.append(line);
                }
                timetables.createOrUpdateTimetable(t, source.toString());
                br.close();
            } catch (Exception e) {
                Log.e("Get URL", "Error in downloading: " + e.toString(), e);
                return null;
            }
        }
        timetables.db.getWritableDatabase().setTransactionSuccessful();
        timetables.db.getWritableDatabase().endTransaction();
        return map;
    }

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected void onPostExecute(final Map<String, Timetable> map) {
        super.onPostExecute(map);
        if (map != null) {
            List<String> newTimetables = new ArrayList<>();
            if (!map.isEmpty()) {
                for (String filename : map.keySet()) {
                    newTimetables.add(filename);

                    Timetable timetable = map.get(filename);
                    if (timetable != null) {
                        boolean existed = existedBefore.contains(filename);
                        // timetable added to map in createOrUpdateTimetable
                        for (TimetableChangeListener tcl : TimetableUtils.timetableListeners)
                            if (existed) tcl.onTimetableUpdated(timetable, true);
                            else tcl.onTimetableAdded(timetable, true);
                    }
                }
                // Base.refreshTimetables(true);
            }
            timetables.db.getWritableDatabase().beginTransaction();
            List<String> removeTimetables = new ArrayList<>();
            for (String filename : timetables.timetables.keySet()) {
                if (!newTimetables.contains(filename)) {
                    removeTimetables.add(filename);
                }
            }
            for (String filename : removeTimetables) {
                Timetable timetable = timetables.getPeriod(filename);
                for (TimetableChangeListener tcl : TimetableUtils.timetableListeners)
                    tcl.onTimetableRemoved(timetable, true);
                Log.d(TAG, "Timetable removed: " + filename);
                timetables.removeTimetable(filename);
            }

            timetables.db.getWritableDatabase().setTransactionSuccessful();
            timetables.db.getWritableDatabase().endTransaction();
        }
        onFinish.run();
    }

}