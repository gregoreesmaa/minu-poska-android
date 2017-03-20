package ee.tartu.jpg.stuudium;

import org.json.JSONException;

public interface ResponseHandler<T> {
	void handle(T obj);
}