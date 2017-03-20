package ee.tartu.jpg.minuposka.ui.base;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import ee.tartu.jpg.minuposka.R;
import ee.tartu.jpg.minuposka.ui.util.TextUtils;

/**
 * Provides Web interface with Stuudium and other useful methods, also a drawer.
 */
public abstract class WebviewActivity extends StuudiumBaseActivity {

    protected WebView vWebView;
    private SwipeRefreshLayout vSwipeRefreshLayout;

    protected abstract boolean shouldOverrideUrlLoading(String url);

    protected abstract String getCssFile(String url);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Loading indicator support
        vSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.mainContent);
        vSwipeRefreshLayout.setEnabled(false);

        // Initialise webview
        vWebView = (WebView) findViewById(R.id.webView);
        if (Build.VERSION.SDK_INT <= 18) {
            vWebView.getSettings().setSavePassword(false);
        }
        vWebView.getSettings().setJavaScriptEnabled(true);
        vWebView.getSettings().setSupportZoom(true);
        vWebView.getSettings().setBuiltInZoomControls(true);
        vWebView.getSettings().setDisplayZoomControls(false);
        vWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                boolean b = WebviewActivity.this.shouldOverrideUrlLoading(url);
                if (b)
                    return true;
                else if (!url.startsWith("javascript"))
                    view.animate().alpha(0).setDuration(120);
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                vSwipeRefreshLayout.setRefreshing(true);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                vSwipeRefreshLayout.setRefreshing(false);
                if (!url.startsWith("javascript")) {
                    TextUtils.injectCSS(WebviewActivity.this, WebviewActivity.this.getCssFile(url), view);
                    view.animate().alpha(1).setDuration(120);
                }
                super.onPageFinished(view, url);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                vSwipeRefreshLayout.setRefreshing(false);
                view.loadData(view.getContext().getString(ee.tartu.jpg.stuudium.R.string.html_error_page, errorCode, failingUrl), "text/html", "utf-8");
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (vWebView.canGoBack()) {
                        vWebView.goBack();
                    } else {
                        finish();
                    }
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
