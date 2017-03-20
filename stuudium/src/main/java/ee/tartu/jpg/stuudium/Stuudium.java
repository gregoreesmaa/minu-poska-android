package ee.tartu.jpg.stuudium;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.webkit.WebView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import ee.tartu.jpg.stuudium.data.Assignment;
import ee.tartu.jpg.stuudium.data.DataMap;
import ee.tartu.jpg.stuudium.data.DataSet;
import ee.tartu.jpg.stuudium.data.Event;
import ee.tartu.jpg.stuudium.data.Journal;
import ee.tartu.jpg.stuudium.data.LoginData;
import ee.tartu.jpg.stuudium.data.Person;
import ee.tartu.jpg.stuudium.data.TokenizedUrl;
import ee.tartu.jpg.stuudium.data.User;
import ee.tartu.jpg.stuudium.utils.StuudiumDatabaseHelper;

@SuppressWarnings("WeakerAccess")
public class Stuudium {

    private static final String TAG = "Stuudium";

    private static LoginData loginData;

    private static User user;
    private static DataMap<String, Person> people;
    private static DataMap<String, DataSet<Event>> events;
    private static DataMap<String, DataSet<Journal>> journals;
    private static DataMap<String, DataSet<Assignment>> assignments;

    private static Set<StuudiumEventListener> eventListeners = new HashSet<>();
    private static StuudiumSettings settings;

    private static DataMap<String, Request> failedRequests = new DataMap<String, Request>();

    public static StuudiumDatabaseHelper db;

    /**
     * @return the StuudiumEventListener instance used by the API
     */
    public static Set<StuudiumEventListener> getEventListeners() {
        return eventListeners;
    }

    /**
     * @return the StuudiumSettings instance used by the API
     */
    public static StuudiumSettings getSettings() {
        return settings;
    }

    /**
     * Attaches StuudiumEventListener instance to the API
     *
     * @param sel - the event listener
     */
    public static void attach(StuudiumEventListener sel) {
        Log.d(TAG, "Attached Stuudium event listener: " + sel);
        eventListeners.add(sel);
    }

    /**
     * Detaches StuudiumEventListener from the API
     *
     * @param sel - the event listener
     */
    public static void detach(StuudiumEventListener sel) {
        Log.d(TAG, "Detached Stuudium event listener: " + sel);
        eventListeners.remove(sel);
    }

    /**
     * Attaches StuudiumSettings instance to the API
     *
     * @param set - the settings
     */
    public static void setSettings(StuudiumSettings set) {
        Log.d(TAG, "Set Stuudium settings");
        settings = set;
    }

	/* DATA */

    /**
     * @return the User of Stuudium, null if user has not authorized or has
     * logged off
     */
    public static User getUser() {
        return user;
    }

    /**
     * @return the identity of the Stuudium user, null if user has not authorized or has
     * logged off
     */
    public static Person getUserIdentity() {
        if (hasUser())
            return getUser().getIdentity();
        else
            return null;
    }

    /**
     * @return the assignments of the stuudium user, null if user has not authorized or has
     * logged off
     */
    public static DataSet<Assignment> getUserAssignments() {
        Person p = getUserIdentity();
        if (p != null) {
            return p.getAssignments();
        } else {
            return null;
        }
    }

    /**
     * @return the assignments of the stuudium user, null if user has not authorized or has
     * logged off
     */
    public static DataSet<Journal> getUserJournals() {
        Person p = getUserIdentity();
        if (p != null) {
            return p.getJournals();
        } else {
            return null;
        }
    }

    /**
     * @return the assignments of the stuudium user, null if user has not authorized or has
     * logged off
     */
    public static DataSet<Event> getUserEvents() {
        Person p = getUserIdentity();
        if (p != null) {
            return p.getEvents();
        } else {
            return null;
        }
    }

    /**
     * @return true if Stuudium User exists, false otherwise
     */
    public static boolean hasUser() {
        return user != null;
    }

    /**
     * Sets the Stuudium User, alerting the EventListener set by attach
     *
     * @param u - the user to set
     */
    public static void setUser(User u, boolean manualUpdate) {
        Log.d(TAG, "Set user");
        user = u;
        for (StuudiumEventListener sel : getEventListeners()) sel.onUserDataLoaded(u, manualUpdate);
    }

    /**
     * Sets the Stuudium User
     *
     * @param u - the user o set
     */
    public static void setUserSilently(User u) {
        Log.d(TAG, "Set user silently");
        user = u;
    }

    /**
     * Sets the map of stored people, alerting the EventListener set by attach
     *
     * @param p - map of people to set
     */
    public static void setPeople(DataMap<String, Person> p) {
        Log.d(TAG, "Set people");
        if (p == null) people = new DataMap<>();
        else people = p;
        for (StuudiumEventListener sel : getEventListeners()) sel.onPeopleChanged(getPeople());
    }

    /**
     * @return the map of people, new instance will be created if null
     */
    public static DataMap<String, Person> getPeople() {
        if (people == null) people = new DataMap<>();
        return people;
    }

    /**
     * @param obj - the JSON object containing data for a new Person instance
     * @return a String containing the ID for the new person
     * @throws JSONException
     */
    public static String addPerson(JSONObject obj) throws JSONException {
        String id = obj.getString(Person.ID);
        Log.v(TAG, "Adding person with ID: " + id);
        Person p = getPeople().get(id);
        if (p == null) {
            p = new Person(obj);
            getPeople().put(id, p);
            db.insertStuudiumPerson(id, obj.toString());
        }
        return id;
    }

    /**
     * @param identity_id - the ID of the person to return
     * @return a Person instance with the corresponding ID
     */
    public static Person getPerson(String identity_id) {
        return getPeople().get(identity_id);
    }

    /**
     * @return the map of events, new instance will be created if null
     */
    public static DataMap<String, DataSet<Event>> getAllEvents() {
        if (events == null) events = new DataMap<>();
        return events;
    }

    /**
     * @return the map of journals, new instance will be created if null
     */
    public static DataMap<String, DataSet<Journal>> getAllJournals() {
        if (journals == null) journals = new DataMap<>();
        return journals;
    }

    /**
     * Adds specified Assignments to specified user ID
     *
     * @param person       - the ID of the person
     * @param assignments2 - the Assignments to add
     */
    public static void addAssignments(String person, DataSet<Assignment> assignments2) {
        Log.d(TAG, "Adding all assignments");
        DataMap<String, DataSet<Assignment>> assignments = getAllAssignments();
        if (!assignments.containsKey(person)) {
            assignments.put(person, new DataSet<Assignment>());
        }
        assignments.get(person).addAll(assignments2);
    }

    /**
     * @return the map of assignments, new instance will be created if null
     */
    public static DataMap<String, DataSet<Assignment>> getAllAssignments() {
        if (assignments == null) assignments = new DataMap<>();
        return assignments;
    }

	/* LOG IN */

    /**
     * @return a LoginData containing login information
     */
    public static LoginData getLoginData() {
        return loginData;
    }

    /**
     * Sets login information to specified value (LoginData). Also calls login
     * event (or logout if specified LoginData is null) on event listener
     *
     * @param ld - the login data to set
     */
    public static void setLoginData(LoginData ld) {
        Log.d(TAG, "Setting login data");
        boolean wasLoggedIn = isLoggedIn();
        loginData = ld;
        if (ld != null) {
            for (StuudiumEventListener sel : getEventListeners()) sel.onStuudiumLogin();
            db.setLoginData(ld.getAccessToken(), ld.getExpiresIn(), ld.getRequestTime());
        } else if (wasLoggedIn) {
            for (StuudiumEventListener sel : getEventListeners()) sel.onStuudiumLogout();
            db.removeLoginData();
        }
    }

    /**
     * Checks user is logged in by:
     * <ul>
     * <li>Checking if LoginData is set.</li>
     * <li>Checking if session hasn't expired.</li>
     * </ul>
     *
     * @return true if user is logged in, false otherwise.
     */
    public static boolean isLoggedIn() {
        return loginData != null && !isLoginExpired();
    }

    /**
     * Checks if login session has expired
     *
     * @return true if session is expired, false otherwise.
     */
    public static boolean isLoginExpired() {
        long currtime = System.currentTimeMillis() / 1000;
        return currtime >= loginData.getRequestTime() + loginData.getExpiresIn();
    }

    /**
     * Loads auth URL in webview
     */
    public static void authorizeIn(WebView webview) {
        Log.d(TAG, "Authorizing in WebView");
        // Generate authorization request
        Request authRequest = getRequest(Requests.AUTH);
        authRequest.setParameter("type", "user_agent");
        authRequest.setParameter("client_id", settings.getClientId());
        authRequest.setParameter("subdomain", settings.getSubdomain());
        authRequest.setParameter("redirect_uri", settings.getAuthRedirectUri());
        webview.setAlpha(0);
        webview.loadUrl(authRequest.getUri().toString());
    }

    private static TokenizedUrl getTokenizedUrl(String page, ResponseHandler<Object> responseHandler) {
        return new TokenizedUrl(page, responseHandler);
    }

    /**
     * Reacts to response after authorization from server.
     *
     * @param url - the response to authorization
     */
    public static void onAuthorizationResponse(String url) {
        Log.d(TAG, "Authorization response: " + url);
        url = url.replaceFirst("#", "?");
        Uri uri = Uri.parse(url);
        String error = uri.getQueryParameter("error");
        if (error != null) {
            for (StuudiumEventListener sel : getEventListeners()) sel.onError(error);
        } else {
            String access_token = uri.getQueryParameter("access_token");
            StringTokenizer st = new StringTokenizer(access_token, ".");
            String apiversion = st.nextToken();
            if (!apiversion.equals("v1")) {
                for (StuudiumEventListener sel : getEventListeners())
                    sel.onError("Vale Stuudiumi API versioon");
                return;
            }
            String expires_in = uri.getQueryParameter("expires_in");
            setLoginData(new LoginData(access_token, Long.parseLong(expires_in)));
        }
    }

    /**
     * Logs user off Stuudium and clears all data after.
     */
    public static void unauthorize() {
        unauthorize(new JSONResponseHandler() {

            @Override
            public void handle(JSONObject obj) throws JSONException {
                clearData();
            }

        });
    }

    /**
     * Requests unauthorization from server
     *
     * @param handler - Handler for actions after request
     */
    public static void unauthorize(JSONResponseHandler handler) {
        Log.d(TAG, "Unauthorizing");
        request(Requests.UNAUTH, true, handler);
    }

    /**
     * Clears all user data:
     * <ul>
     * <li>login data</li>
     * <li>people's data</li>
     * <li>events' data</li>
     * <li>journals' data</li>
     * <li>assignments' data</li>
     * </ul>
     */
    public static void clearData() {
        Log.i(TAG, "Clearing Stuudium data");
        db.removeAllData();
        setUserSilently(null);
        setLoginData(null);
        people = null;
        events = null;
        journals = null;
        assignments = null;
    }

	/* REQUEST RELATED */

    /**
     * Parses request
     *
     * @param types - a list of Strings representing request path
     * @return a Request
     */
    public static Request getRequest(String... types) {
        String request = "";
        for (String type : types) request += "/" + type;
        request = request.replace("//", "/");
        if (request.startsWith("/")) request = request.substring(1);
        Log.d(TAG, "Building request: " + request);
        return new Request(request);
    }

    /**
     * Requests server with a specified request
     *
     * @param request      - the request
     * @param ignoreErrors - whether to ignore errors
     * @param handler      - handler for actions after the request
     */
    public static void request(String request, boolean ignoreErrors, JSONResponseHandler handler) {
        Log.d(TAG, "Requesting: " + request);
        Request r = getRequest(request);
        r.setHandler(handler);
        r.send(ignoreErrors);
    }

    /**
     * Requests the server with specified request containing the user id
     *
     * @param request      - the request
     * @param userid       - the user ID associated with the request
     * @param ignoreErrors - whether to ignore errors
     * @param handler      - handler for actions after the request
     */
    public static void request(String request, String userid, boolean ignoreErrors, JSONResponseHandler handler) {
        Log.d(TAG, "Requesting: " + request + "; with userid: " + userid);
        Request r = getRequest(request, userid);
        r.setHandler(handler);
        r.send(ignoreErrors);
    }

    /**
     * Requests user data associated with the request
     *
     * @param manualUpdate - true, if the update is requested directly by the user, false otherwise
     */
    public static void requestUserData(boolean manualUpdate, boolean onLogin) {
        requestUserData(null, manualUpdate, onLogin);
    }

    /**
     * Requests user data associated with the request
     *
     * @param userDataResponseHandler - the response handler
     * @param manualUpdate            - true, if the update is requested directly by the user, false otherwise
     */
    public static void requestUserData(final ResponseHandler<User> userDataResponseHandler, final boolean manualUpdate, final boolean onLogin) {
        Log.d(TAG, "Requesting user data");
        request(Requests.USERS, Requests.ME, false, new JSONResponseHandler() {

            @Override
            public void handle(JSONObject obj) throws JSONException {
                User u = new User(obj);
                if (onLogin) setUser(u, manualUpdate);
                else setUserSilently(u);
                db.setUser(obj.toString());
                if (userDataResponseHandler != null)
                    userDataResponseHandler.handle(u);
            }

        });
    }

    /**
     * Puts request to failed requests list for possibly trying again later
     *
     * @param topic - name for that type of request to avoid duplicates
     * @param r     - the request that failed
     */
    public static void requestFailed(String topic, Request r) {
        Log.d(TAG, "Request failed: " + topic);
        if (failedRequests.containsKey(topic)) {
            Request r2 = failedRequests.get(topic);
            if (r2.getTime() > r.getTime())
                return;
        }
        failedRequests.put(topic, r);
    }

    /**
     * Removes any existing requests with same topic from failed requests list
     *
     * @param topic - name for that type of request to remove it
     * @param r     - the request that succeeded
     */
    public static void requestSucceeded(String topic, Request r) {
        Log.d(TAG, "Request succeeded: " + topic);
        if (!failedRequests.containsKey(topic))
            return;
        Request r2 = failedRequests.get(topic);
        if (r2.getTime() <= r.getTime()) {
            failedRequests.remove(topic);
        }
    }

    /**
     * @return an array of all failed requests
     */
    public static Request[] getFailedRequests() {
        Request[] rs = new Request[failedRequests.size()];
        int i = 0;
        for (final String key : failedRequests.keySet()) {
            final Request r = failedRequests.get(key);
            if (r.getRetryCount() == 0) {
                final JSONResponseHandler oldHandler = r.getResponseHandler();
                r.setHandler(new JSONResponseHandler() {

                    @Override
                    public void handle(JSONObject obj) throws JSONException {
                        Stuudium.requestSucceeded(key, r);
                        oldHandler.handle(obj);
                    }

                });
            }
            r.setRetry();
            rs[i] = r;
            i++;
        }
        Arrays.sort(rs, new Comparator<Request>() {

            @Override
            public int compare(Request lhs, Request rhs) {
                return lhs.getTime() > rhs.getTime() ? 1 : (lhs.getTime() < rhs.getTime() ? -1 : 0);
            }

        });
        return rs;
    }

    public static void init(Context context) {
        Log.i(TAG, "Initializing");
        db = new StuudiumDatabaseHelper(context);
        loginData = db.requestLoginData();
        user = db.requestUser();
        people = db.requestPeople();
        if (hasUser()) {
            events = new DataMap<>();
            assignments = new DataMap<>();
            journals = new DataMap<>();
            for (Person p : getUser().getStudents()) {
                String personId = p.getId();
                events.put(personId, db.requestEvents(personId));
                assignments.put(personId, db.requestAssignments(personId));
                journals.put(personId, db.requestJournals(personId));
            }
        }
    }
}
