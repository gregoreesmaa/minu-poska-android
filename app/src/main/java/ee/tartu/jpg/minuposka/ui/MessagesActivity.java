package ee.tartu.jpg.minuposka.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import org.json.JSONException;

import ee.tartu.jpg.minuposka.R;
import ee.tartu.jpg.minuposka.ui.base.WebviewActivity;
import ee.tartu.jpg.stuudium.ResponseHandler;
import ee.tartu.jpg.stuudium.data.TokenizedUrl;

/**
 * Created by gregor on 9/14/2015.
 */
public class MessagesActivity extends WebviewActivity implements ResponseHandler<Object> {

    private static TokenizedUrl messagesUrl;

    @Override
    protected int getLayout() {
        return R.layout.activity_messages;
    }

    @Override
    protected int getMenuItem() {
        return R.id.drawer_messages;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (messagesUrl == null) messagesUrl = new TokenizedUrl("inbox", this);
        messagesUrl.setResponseHandler(this);
        String url = messagesUrl.getURL();
        if (url != null) {
            setUrlOnThread(url);
        }
    }

    @Override
    public void handle(Object obj) {
        if (obj != null)
            setUrl(messagesUrl.getURL());
    }

    @Override
    public boolean shouldOverrideUrlLoading(String url) {
        if (!url.contains(".ope.ee")) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
            return true;
        }
        return false;
    }

    @Override
    public String getCssFile(String url) {
        return "stuudium_messages_page.css";
    }

    private void setUrl(final String url) {
        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                setUrlOnThread(url);
            }
        });
    }

    private void setUrlOnThread(String url) {
        if (url == null)
            return;
        vWebView.setAlpha(0);
        vWebView.loadUrl(url);
    }

}
