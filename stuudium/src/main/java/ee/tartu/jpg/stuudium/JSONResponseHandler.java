package ee.tartu.jpg.stuudium;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Gregor on 20/03/2017.
 */

public interface JSONResponseHandler {
    void handle(JSONObject obj) throws JSONException;
}
