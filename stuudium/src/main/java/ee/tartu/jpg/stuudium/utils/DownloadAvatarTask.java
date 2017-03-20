package ee.tartu.jpg.stuudium.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;

import java.net.URL;

import ee.tartu.jpg.stuudium.ResponseHandler;

public class DownloadAvatarTask extends AsyncTask<String, Void, Bitmap> {

    private static final String TAG = "DownloadAvatarTask";

    private ResponseHandler<Bitmap> handler;

    public DownloadAvatarTask(ResponseHandler<Bitmap> responseHandler) {
        this.handler = responseHandler;
    }

    @Override
    protected Bitmap doInBackground(String... urls) {
        String url = urls[0];
        Bitmap bm = null;
        try {
            bm = BitmapFactory.decodeStream(new URL(url).openStream());
        } catch (Exception e) {
            Log.e(TAG, "Failed to download avatar", e);
        }
        return bm;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        handler.handle(result);
    }
}