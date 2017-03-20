package ee.tartu.jpg.stuudium.data;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import ee.tartu.jpg.stuudium.JSONResponseHandler;
import ee.tartu.jpg.stuudium.Request;
import ee.tartu.jpg.stuudium.Requests;
import ee.tartu.jpg.stuudium.ResponseHandler;
import ee.tartu.jpg.stuudium.Stuudium;

/**
 * Created by gregor on 9/17/2015.
 */
public class TokenizedUrl {

    private static final String TAG = "TokenizedUrl";

    private final static String URL = "url";
    private final static String EXPIRES_IN = "expires_in";

    private final String page;

    private String url;
    private long expiresIn;
    private long loadedAt;

    private ResponseHandler<Object> responseHandler;

    private boolean requesting = false;

    public TokenizedUrl(String page, ResponseHandler<Object> responseHandler) {
        this.page = page;
        this.responseHandler = responseHandler;
        requestNewTokenizedUrl();
    }

    @SuppressWarnings("WeakerAccess")
    public void requestNewTokenizedUrl() {
        if (!requesting && Stuudium.isLoggedIn()) {
            requesting = true;
            Request tokenizedUrlRequest = Stuudium.getRequest(Requests.URL, page);
            tokenizedUrlRequest.setHandler(new JSONResponseHandler() {
                @Override
                public void handle(JSONObject obj) throws JSONException {
                    loadedAt = System.currentTimeMillis();
                    url = obj.getString(URL);
                    expiresIn = obj.getLong(EXPIRES_IN) * 1000;
                    requesting = false;
                    responseHandler.handle(obj);
                    Log.d(TAG, "Received new tokenized URL: " + url + ", expiring in " + expiresIn + "ms (" + obj.toString(4) + ")");
                }
            });
            tokenizedUrlRequest.setOnFailHandler(new ResponseHandler<Exception>() {
                @Override
                public void handle(Exception obj) {
                    requesting = false;
                    responseHandler.handle(null);
                }
            });
            tokenizedUrlRequest.send(false);
        }
    }

    public String getURL() {
        if (url != null && !isExpired()) {
            return url;
        } else {
            requestNewTokenizedUrl();
            return null;
        }
    }

    @SuppressWarnings("WeakerAccess")
    public boolean isExpired() {
        return System.currentTimeMillis() - loadedAt > expiresIn;
    }

    public void setResponseHandler(ResponseHandler<Object> responseHandler) {
        this.responseHandler = responseHandler;
    }
}
