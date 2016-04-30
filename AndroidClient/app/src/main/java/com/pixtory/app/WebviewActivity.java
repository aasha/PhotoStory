package com.pixtory.app;

/**
 * Created by aasha.medhi on 12/1/15.
 */


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.pixtory.app.app.App;

public class WebviewActivity extends AppCompatActivity {
    private Context mCtx = null;
    private String mWebLink = null;
    Tracker mTracker;
    @Override
    protected void onResume() {
        super.onResume();
        mTracker.setScreenName("Webview");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_web_view);

        mCtx = this;
        mTracker = App.getmInstance().getDefaultTracker();
        WebView myWebView = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        final Activity activity = this;

        myWebView.setWebViewClient(new WebClientClass());
        // myWebView.setWebChromeClient(new AppWebViewClients());

        Bundle b = getIntent().getExtras();
        if (b != null) {
            mWebLink = b.getString("WEB_LINK", "http://www.inmobi.com");
        }

        myWebView.loadUrl(mWebLink);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_web_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }


    public class WebClientClass extends WebViewClient {
        ProgressDialog pd = null;

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            if (pd == null) {
                pd = new ProgressDialog(mCtx);
                //pd.setCanceledOnTouchOutside(false);
                pd.setMessage("Loading..");
                if (!pd.isShowing())
                    pd.show();
            }
        }


        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (pd != null && pd.isShowing())
                pd.dismiss();
        }
    }
}
