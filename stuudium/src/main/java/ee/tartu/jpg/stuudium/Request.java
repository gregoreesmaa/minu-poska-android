package ee.tartu.jpg.stuudium;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;

public class Request {

    private static final String TAG = "Request";

    private HashMap<String, String> params = new HashMap<>();
    private JSONResponseHandler handler;
    private ResponseHandler<Exception> onFailHandler;
    private String requestString;
    private String postBody;
    private int timeout;
    private long time;
    private int retryCount = -1;

    public void setRetry() {
        retryCount++;
    }

    public int getRetryCount() {
        return Math.max(0, retryCount);
    }

    public Request(String request) {
        this.time = System.currentTimeMillis();
        this.requestString = request;
    }

    private String formatRequestUri(String request) {
        StuudiumSettings s = Stuudium.getSettings();
        return String.format(s.getRequestPattern(), s.getProtocol(), s.getAPIHost(), s.getAPIVersion(), request);
    }

    public URI getUri() {
        StringBuilder uriString = new StringBuilder(this.requestString);
        if (!params.isEmpty()) {
            StringBuilder paramsString = new StringBuilder();
            for (String key : params.keySet())
                paramsString.append(paramsString.length() == 0 ? "?" : "&")
                        .append(Uri.encode(key))
                        .append("=")
                        .append(Uri.encode(params.get(key)));
            uriString.append(paramsString);
        }
        return URI.create(formatRequestUri(uriString.toString()));
    }

    public void send(final boolean ignoreErrors) {
        if (!Stuudium.isLoggedIn()) return;
        for (StuudiumEventListener sel : Stuudium.getEventListeners()) sel.onLoadingStarted();
        new AsyncTask<String, Integer, Void>() {

            @Override
            protected Void doInBackground(String... arg0) {
                sendAsynced(ignoreErrors);
                return null;
            }

        }.execute();
    }

    private boolean sendAsynced(boolean ignoreErrors) {
        if (!Stuudium.isLoggedIn())
            return false;
        if (retryCount >= 0)
            retryCount++;
        // Create a new HttpClient and Post Header
        boolean succeeded = false;
        try {
            URL url = getUri().toURL();
            Log.d(TAG, "Requesting from URL: " + url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            /*int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                Log.w(TAG, "Bad response code: " + responseCode + " (" + connection.getResponseMessage() + ")");
                return false;
            }*/
            if (timeout > 0) {
                connection.setConnectTimeout(timeout);
                connection.setReadTimeout(timeout);
            }
            connection.setRequestMethod(postBody == null ? "GET" : "POST");
            connection.setRequestProperty("Authorization", String.format("Bearer %s", Stuudium.getLoginData().getAccessToken()));
            connection.setRequestProperty("User-Agent", Stuudium.getSettings().getUserAgent());
            connection.setUseCaches(false);
            connection.setDoOutput(postBody != null);
            connection.setDoInput(true);

            connection.connect();

            if (postBody != null) {
                Log.d(TAG, "Adding post body: " + postBody);
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"));
                bw.write(postBody);
                bw.flush();
                bw.close();
            }
            // DEBUG
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String output = "";
            String str;
            while ((str = br.readLine()) != null) output += str;
            br.close();
            connection.disconnect();

            // Converting arrays to objects
            if (output.startsWith("[") && output.endsWith("]"))
                output = "{\"array\":" + output + "}";
            Log.v(TAG, "Request response: " + output);
            JSONObject responseobj;
            try {
                responseobj = new JSONObject(output);
                if (!ignoreErrors && responseobj.has("error") && responseobj.getBoolean("error")) {
                    String errormsg = responseobj.getString("message");
                    Log.w(TAG, "Error: " + responseobj.toString());
                    for (StuudiumEventListener sel : Stuudium.getEventListeners())
                        sel.onError(errormsg);
                    if (onFailHandler != null)
                        onFailHandler.handle(new StuudiumException(errormsg));
                } else {
                    if (handler != null)
                        handler.handle(responseobj);
                    succeeded = true;
                }
            } catch (JSONException e) {
                Log.e(TAG, "Failed to parse JSON", e);
                if (onFailHandler != null) onFailHandler.handle(e);
            }
        } catch (SocketTimeoutException ste) {
            Log.e(TAG, "Connection timed out", ste);
            for (StuudiumEventListener sel : Stuudium.getEventListeners())
                sel.onError("Request timed out");
            if (onFailHandler != null) onFailHandler.handle(ste);
        } catch (IOException e) {
            Log.e(TAG, "Failed with the request", e);
            if (onFailHandler != null) onFailHandler.handle(e);
        }
        for (StuudiumEventListener sel : Stuudium.getEventListeners())
            sel.onLoadingFinished();
        return succeeded;
    }

    public void setParameter(String key, String value) {
        params.put(key, value);
    }

    public void setPostBody(String str) {
        postBody = str;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void setHandler(JSONResponseHandler handler) {
        this.handler = handler;
    }

    public void setOnFailHandler(ResponseHandler<Exception> fhandler) {
        this.onFailHandler = fhandler;
    }

    public long getTime() {
        return time;
    }

    public JSONResponseHandler getResponseHandler() {
        return handler;
    }
}
